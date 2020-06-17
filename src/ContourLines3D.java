package src;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A program that draws contour maps with USGS data
 * 
 * @author Will Smith with sample code from Mauricio Papa
 */
public class ContourLines3D extends JFrame implements GLEventListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, ActionListener, ItemListener{

	static {
		GLProfile.initSingleton();
	}
	//Instance aux vars
	GLProfile glprofile=null;
	GLCapabilities glcapabilities=null;
	GLJPanel gljpanel=null;
	GL2 gl;
	GLU glu;
	double AR; //We'll use this for the aspect ratio

	//read in from arguments
	static String basename;
	static float low, high, step;
	static float lowR, lowG, lowB;
	static float highR, highG, highB;
	static boolean marker;
	static boolean auto;

	gridFloatReader gridReader; //where the height data is
	double xTolerance;
	double yTolerance;

	double scaleFactor = 2.0d; // >1 zooms in
	double xOffset = 0.0d; //might need to be really large if you have a large map area
	double yOffset = 0.0d;
	double oldXOffset, oldYOffset;
	int mouseX, mouseY;
	float xRotation = 0f;
	float yRotation = 0f;
	float oldXRotation = 0f;
	float oldYRotation = 0f;
	
	boolean wireframe = true;
	boolean perspective = true;
	boolean drawContourLines = true;
	boolean drawTerrain = true;

	private class PointPair{ //used for storing two points and a color
		public double x1, y1, x2, y2, h;
		public float r, g, b;
		public PointPair(){}
		public void setPoints(double ax, double ay, double bx, double by, double h){
			x1 = ax; y1 = ay; x2 = bx; y2 = by; this.h = h;
		}
		public void setColor(float r, float g, float b){
			this.r = r; this.g = g; this.b = b;
		}
	} 

	ArrayList<ArrayList<PointPair>> lines; //separate contour lines

	public static void main( String [] args ) {
		if(args.length == 0){
			String[] defaultArgs = {"data/ned_86879038", "900", "4000", "-100", "0", "0", "1", "1", "0", "0", "true"};
			args = defaultArgs;
		}
		basename = args[0];
		if(args[1].equals("auto")){
			step = -10;
			lowR = Float.parseFloat(args[2]);
			lowG = Float.parseFloat(args[3]);
			lowB = Float.parseFloat(args[4]);
			highR = Float.parseFloat(args[5]);
			highG = Float.parseFloat(args[6]);
			highB = Float.parseFloat(args[7]);
			marker = Boolean.parseBoolean(args[8]);
			auto = true;
		}
		else{
			low = Float.parseFloat(args[1]);
			high = Float.parseFloat(args[2]);
			step = Float.parseFloat(args[3]);
			lowR = Float.parseFloat(args[4]);
			lowG = Float.parseFloat(args[5]);
			lowB = Float.parseFloat(args[6]);
			highR = Float.parseFloat(args[7]);
			highG = Float.parseFloat(args[8]);
			highB = Float.parseFloat(args[9]);
			marker = Boolean.parseBoolean(args[10]);
		}

		new ContourLines3D();
	}

	public ContourLines3D() {
		super("contourLines3D");
		gridReader = new gridFloatReader(basename);
		xTolerance = gridReader.cellsizedx / 2;
		yTolerance = gridReader.cellsizedy / 2;
		if(auto){
			low = (int)(gridReader.minHeight);
			high = (int)(gridReader.maxHeight);
		}
		setupGUI();
		createContourLines();

		glprofile = GLProfile.getDefault();
		glcapabilities = new GLCapabilities( glprofile );
		gljpanel = new GLJPanel( glcapabilities );

		gljpanel.addGLEventListener(this);

		add( gljpanel );
		addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent windowevent ) {
				remove( gljpanel );
				dispose();
				System.exit( 0 );
			}
		});
		gljpanel.addKeyListener(this);
		gljpanel.addMouseMotionListener(this);
		gljpanel.addMouseListener(this);
		gljpanel.addMouseWheelListener(this);
		setSize(1280, 768);
		setVisible( true );
		gljpanel.requestFocus();
	}
	
	private void setupGUI(){
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		
		JMenuItem menuItem = new JMenuItem("Open Elevation Data", KeyEvent.VK_O);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Save Screenshot", KeyEvent.VK_S);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuBar.add(menu);
		
		menu = new JMenu("View");
		menu.setMnemonic(KeyEvent.VK_V);
		
		menuItem = new JMenuItem("Reset", KeyEvent.VK_R);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menu.addSeparator();
		JCheckBoxMenuItem contoursEnabled = new JCheckBoxMenuItem("Show Contour Lines");
		contoursEnabled.addItemListener(this);
		contoursEnabled.setMnemonic(KeyEvent.VK_C);
		contoursEnabled.setSelected(drawContourLines);
		menu.add(contoursEnabled);
		
		JCheckBoxMenuItem terrainEnabled = new JCheckBoxMenuItem("Show Terrain");
		terrainEnabled.addItemListener(this);
		terrainEnabled.setMnemonic(KeyEvent.VK_T);
		terrainEnabled.setSelected(drawTerrain);
		menu.add(terrainEnabled);
		
		JCheckBoxMenuItem markerEnabled = new JCheckBoxMenuItem("Mark Summit");
		markerEnabled.addItemListener(this);
		markerEnabled.setMnemonic(KeyEvent.VK_M);
		markerEnabled.setSelected(marker);
		menu.add(markerEnabled);
		
		menu.addSeparator();
		ButtonGroup drawModeGroup = new ButtonGroup();
		JRadioButtonMenuItem wireframeButton = new JRadioButtonMenuItem("Wire Frame");
		wireframeButton.setSelected(wireframe);
		wireframeButton.setMnemonic(KeyEvent.VK_W);
		wireframeButton.addItemListener(this);
		drawModeGroup.add(wireframeButton);
		menu.add(wireframeButton);
		wireframeButton = new JRadioButtonMenuItem("Triangle Face");
		wireframeButton.setSelected(!wireframe);
		wireframeButton.setMnemonic(KeyEvent.VK_F);
		wireframeButton.addItemListener(this);
		drawModeGroup.add(wireframeButton);
		menu.add(wireframeButton);
		
		menu.addSeparator();
		ButtonGroup viewModeGroup = new ButtonGroup();
		JRadioButtonMenuItem perspectiveButton = new JRadioButtonMenuItem("Perspective");
		perspectiveButton.setSelected(perspective);
		perspectiveButton.setMnemonic(KeyEvent.VK_P);
		perspectiveButton.addItemListener(this);
		viewModeGroup.add(perspectiveButton);
		menu.add(perspectiveButton);
		perspectiveButton = new JRadioButtonMenuItem("Orthographic");
		perspectiveButton.setSelected(!perspective);
		perspectiveButton.setMnemonic(KeyEvent.VK_O);
		perspectiveButton.addItemListener(this);
		viewModeGroup.add(perspectiveButton);
		menu.add(perspectiveButton);
		
		menuBar.add(menu);
		
		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menuItem = new JMenuItem("Instructions");
		menuItem.addActionListener(this);
		menuItem.setMnemonic(KeyEvent.VK_I);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("About");
		menuItem.addActionListener(this);
		menuItem.setMnemonic(KeyEvent.VK_A);
		menu.add(menuItem);
		
		menuBar.add(menu);
		
		setJMenuBar(menuBar);
	}

	private void createContourLines(){
		System.out.println("creating contour line data");
		lines = new ArrayList<ArrayList<PointPair>>();
		double dx = gridReader.cellsizedx;
		double dy = gridReader.cellsizedy;
		double heightRange = gridReader.maxHeight - gridReader.minHeight;
		float redRange = highR - lowR;
		float greenRange = highG - lowG;
		float blueRange = highB - lowB;

		int increment;
		int start;
		int end;
		float c;
		double dc;

		if(step < 0){ //draw "step" number of contour lines
			increment = 1;
			start = 0;
			end = (int)Math.abs(step);
			dc = (high - low) / Math.abs(step);
			c = (float)gridReader.minHeight;
		}
		else{ //increment by "step" from low to high
			increment = (int)step;
			start = (int)low;
			end = (int)high;
			dc = step;
			c = low;
		}

		for(int i = start; i < end; i += increment){
			ArrayList<PointPair> line = new ArrayList<PointPair>();
			for(int x = 0; x < gridReader.ncols - 1; x++){
				for(int y = 0; y < gridReader.nrows - 1; y++){
					float llc = gridReader.height[y][x];
					float ulc = gridReader.height[y + 1][x];
					float lrc = gridReader.height[y][x + 1];
					float urc = gridReader.height[y + 1][x + 1];

					double xContour = 0.0f;
					double yContour = 0.0f;

					double heightFromMin = llc - gridReader.minHeight;
					double heightRatio = heightFromMin / heightRange;

					//brought out of the cases below to simplify the math
					double xLeftHigher = (x * dx - ((x + 1) * dx));
					double xLeftLower = (((x + 1) * dx) - (x * dx));
					double yBottomHigher = (y * dy - ((y + 1) * dy));
					double yBottomLower = (((y + 1) * dy) - (y * dy));

					//colors for each line
					float r = (float)heightRatio * redRange + lowR;
					float g = (float)heightRatio * greenRange + lowG;
					float b = (float)heightRatio * blueRange + lowB;

					if(increment == 1 && step == 1 || increment == high - low){ //only one contour line
						r = lowR;
						g = lowG;
						b = lowB;
					}

					PointPair pp = new PointPair();
					PointPair pp2 = null;

					//test which case of marching squares, and interpolate x and y coordinates
					if(		llc < c &&
							lrc < c &&
							ulc < c &&
							urc < c){ //all corners are less than c
						//don't add a contour line
						pp = null;
					}
					else if(llc > c &&
							lrc > c &&
							ulc > c &&
							urc > c){ //all corners are greater than c
						//don't add a contour line
						pp = null;
					}
					//one corner is greater than c
					else if(llc > c &&
							lrc < c &&
							ulc < c &&
							urc < c){ //lower left is greater
						xContour = (c - lrc) / (llc - lrc) * xLeftHigher + (x + 1) * dx;
						yContour = (c - ulc) / (llc - ulc) * yBottomHigher + (y + 1) * dy;
						pp.setPoints(x * dx, yContour, xContour, y * dy, c);
					}
					else if(llc < c &&
							lrc > c &&
							ulc < c &&
							urc < c){ //lower right is greater
						xContour = (c - llc) / (lrc - llc) * xLeftLower + x * dx;
						yContour = (c - urc) / (lrc - urc) * yBottomHigher + (y + 1) * dy;
						pp.setPoints((x + 1) * dx, yContour, xContour, y * dy, c);
					}
					else if(llc < c &&
							lrc < c &&
							ulc > c &&
							urc < c){ //upper left is greater
						xContour = (c - urc) / (ulc - urc) * xLeftHigher + (x + 1) * dx;
						yContour = (c - llc) / (ulc - llc) * yBottomLower + y * dy;
						pp.setPoints(x * dx, yContour, xContour, (y + 1) * dy, c);
					}
					else if(llc < c &&
							lrc < c &&
							ulc < c &&
							urc > c){ //upper right is greater
						xContour = (c - ulc) / (urc - ulc) * xLeftLower + x * dx;
						yContour = (c - lrc) / (urc - lrc) * yBottomLower + y * dy;
						pp.setPoints((x + 1) * dx, yContour, xContour, (y + 1) * dy, c);
					}
					//one corner is less than c
					else if(llc < c &&
							lrc > c &&
							ulc > c &&
							urc > c){ //lower left is less
						xContour = (c - llc) / (lrc - llc) * xLeftLower + x * dx;
						yContour = (c - llc) / (ulc - llc) * yBottomLower + y * dy;
						pp.setPoints(x * dx, yContour, xContour, y * dy, c);
					}
					else if(llc > c &&
							lrc < c &&
							ulc > c &&
							urc > c){ //lower right is less
						xContour = (c - lrc) / (llc - lrc) * xLeftHigher + (x + 1) * dx;
						yContour = (c - lrc) / (urc - lrc) * yBottomLower + y * dy;
						pp.setPoints((x + 1) * dx, yContour, xContour, y * dy, c);
					}
					else if(llc > c &&
							lrc > c &&
							ulc < c &&
							urc > c){ //upper left is less
						xContour = (c - ulc) / (urc - ulc) * xLeftLower + x * dx;
						yContour = (c - ulc) / (llc - ulc) * yBottomHigher + (y + 1) * dy;
						pp.setPoints(x * dx, yContour, xContour, (y + 1) * dy, c);
					}
					else if(llc > c &&
							lrc > c &&
							ulc > c &&
							urc < c){ //upper right is less
						xContour = (c - urc) / (ulc - urc) * xLeftHigher + (x + 1) * dx;
						yContour = (c - urc) / (lrc - urc) * yBottomHigher + (y + 1) * dy;
						pp.setPoints((x + 1) * dx, yContour, xContour, (y + 1) * dy, c);
					}
					//one side is less and one side is greater
					else if(llc < c &&
							lrc < c &&
							ulc > c &&
							urc > c){ //bottom side is less
						yContour = (c - llc) / (ulc - llc) * yBottomLower + y * dy;
						double yContour2 = (c - lrc) / (urc - lrc) * yBottomLower + y * dy;
						pp.setPoints(x * dx, yContour, (x + 1) * dx, yContour2, c);
					}
					else if(llc < c &&
							lrc > c &&
							ulc < c &&
							urc > c){ //left side is less
						xContour = (c - llc) / (lrc - llc) * xLeftLower + x * dx;
						double xContour2 = (c - ulc) / (urc - ulc) * xLeftLower + x * dx;
						pp.setPoints(xContour, y * dy, xContour2, (y + 1) * dy, c);
					}
					else if(llc > c &&
							lrc > c &&
							ulc < c &&
							urc < c){ //top side is less
						yContour = (c - ulc) / (llc - ulc) * yBottomHigher + (y + 1) * dy;
						double yContour2 = (c - urc) / (lrc - urc) * yBottomHigher + (y + 1) * dy;
						pp.setPoints(x * dx, yContour, (x + 1) * dx, yContour2, c);
					}
					else if(llc > c &&
							lrc < c &&
							ulc > c &&
							urc < c){ //right side is less
						xContour = (c - lrc) / (llc - lrc) * xLeftHigher + (x + 1) * dx;
						double xContour2 = (c - urc) / (ulc - urc) * xLeftHigher + (x + 1) * dx;
						pp.setPoints(xContour, y * dy, xContour2, (y + 1) * dy, c);
					}
					//ambiguous case
					else{
						xContour = (c - lrc) / (llc - lrc) * xLeftHigher + (x + 1) * dx;
						yContour = (c - ulc) / (llc - ulc) * yBottomHigher + (y + 1) * dy;
						pp.setPoints(x * dx, yContour, xContour, y * dy, c);
						xContour = (c - ulc) / (urc - ulc) * xLeftHigher + (x + 1) * dx;
						yContour = (c - lrc) / (urc - lrc) * yBottomHigher + (y + 1) * dy;
						pp2 = new PointPair();
						pp2.setPoints((x + 1) * dx, yContour, xContour, (y + 1) * dy, c);
					}

					if(pp != null){
						pp.h = c;
						pp.setColor(r, g, b);
						line.add(pp);
					}
					if(pp2 != null){
						pp2.h = c;
						pp2.setColor(r, g, b);
						line.add(pp2);
					}
				}
			}
			c += dc; 
			lines.add(line);
		}
		System.out.println("finished createContourLines");
	}

	private void drawLine(PointPair pp){
//		gl.glColor3f(pp.r, pp.g, pp.b);
		gl.glColor3f(0f, 1f, 0f);
		gl.glBegin(GL2.GL_LINES);
		{
			gl.glVertex3d((pp.x1 + xOffset) * scaleFactor, (pp.y1 + yOffset) * scaleFactor, pp.h * scaleFactor);
			gl.glVertex3d((pp.x2 + xOffset) * scaleFactor, (pp.y2 + yOffset) * scaleFactor, pp.h * scaleFactor);
		}
		gl.glEnd();
	}

	private void drawSurface(){
		float dx = (float)gridReader.cellsizedx;
		float dy = (float)gridReader.cellsizedy;
		for(int x = 0; x < gridReader.ncols - 1; x++){
			if(wireframe){
				gl.glBegin(GL2.GL_LINE_STRIP);
			}
			else{
				gl.glBegin(GL2.GL_TRIANGLE_STRIP);
			}
			for(int y = 0; y < gridReader.nrows - 1; y++){
				float r = (float)((gridReader.height[y][x] - gridReader.minHeight) / (gridReader.maxHeight - gridReader.minHeight));
				float b = 1.0f - (float)((gridReader.height[y][x] - gridReader.minHeight) / (gridReader.maxHeight - gridReader.minHeight));
				gl.glColor3f(r, 0.0f, b);
				gl.glVertex3d((x * dx + xOffset) * scaleFactor, (y * dy + yOffset) * scaleFactor, gridReader.height[y][x] * scaleFactor);
				gl.glVertex3d(((x + 1) * dx + xOffset) * scaleFactor, (y * dy + yOffset) * scaleFactor, gridReader.height[y][x + 1] * scaleFactor);
				gl.glVertex3d((x * dx + xOffset) * scaleFactor, ((y + 1) * dy + yOffset) * scaleFactor, gridReader.height[y + 1][x] * scaleFactor);
				gl.glVertex3d(((x + 1) * dx + xOffset) * scaleFactor, ((y + 1) * dy + yOffset) * scaleFactor, gridReader.height[y + 1][x + 1] * scaleFactor);
			}
			gl.glEnd();
		}
	}

	private void markSummit(){
		gl.glColor3f(1.0f - highR, 1.0f - highG, 1.0f - highB); //opposite color of high color will be very visible
		gl.glPointSize((float)(gridReader.cellsizedx * scaleFactor) / 2.0f);
		gl.glBegin(GL2.GL_POINTS);
		{
			gl.glVertex3d((xOffset + gridReader.maxHeightxi * gridReader.cellsizedx) * scaleFactor, 
					(yOffset + gridReader.maxHeightyi * gridReader.cellsizedy) * scaleFactor, 
					gridReader.maxHeight * scaleFactor);
		}
		gl.glEnd();
	}


	//Implementing GLEventListener methods 
	@Override
	public void init( GLAutoDrawable glautodrawable ) {
		System.out.println("Entering init();");
		gl = glautodrawable.getGL().getGL2();
		glu = new GLU();
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0f); //set to non-transparent black
		gl.glEnable(GL2.GL_DEPTH_TEST);
	}

	@Override
	public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
//		System.out.println("Entering reshape(); x="+x+" y="+y+" width="+width+" height="+height);
		GL2 gl=glautodrawable.getGL().getGL2();
		
		//do the projection setup in display so we can change perspective/ortho in the menu
		
		AR = 16f/9f;
		if (AR*height<width) gl.glViewport(x, y, (int) (AR*height), height);
		else gl.glViewport(x, y, width, (int) (width/AR));
	}

	@Override
	public void display( GLAutoDrawable glautodrawable ) {
		gl=glautodrawable.getGL().getGL2();
		
		gl.glMatrixMode( GL2.GL_PROJECTION );
		gl.glLoadIdentity();
		if(perspective){
			glu.gluPerspective(45, 16f/9f, 1, gridReader.nrows * gridReader.cellsizedy * 2);
		}
		else{
			gl.glOrtho(0, gridReader.ncols * gridReader.cellsizedx * scaleFactor, 
					gridReader.nrows * gridReader.cellsizedy * scaleFactor / 2, 0, 
					-gridReader.nrows * gridReader.cellsizedy, gridReader.nrows * gridReader.cellsizedy* scaleFactor);
		}
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		
		if(perspective){
			glu.gluLookAt( gridReader.ncols * gridReader.cellsizedx, 0, gridReader.maxHeight * 5,
					gridReader.ncols * gridReader.cellsizedx, gridReader.nrows * gridReader.cellsizedy, 0,
					0, 0, 1);
			gl.glTranslatef((float)(gridReader.ncols * gridReader.cellsizedx), (float)(gridReader.nrows * gridReader.cellsizedy),  (float)gridReader.maxHeight);
			gl.glRotatef(yRotation, 1, 0, 0);
			gl.glRotatef(xRotation, 0, 0, 1);
			gl.glTranslatef(-(float)(gridReader.ncols * gridReader.cellsizedx), -(float)(gridReader.nrows * gridReader.cellsizedy),  -(float)gridReader.maxHeight);
		}
		else{
			gl.glTranslatef((float)(gridReader.ncols * gridReader.cellsizedx), (float)(gridReader.nrows * gridReader.cellsizedy),  0);
			gl.glRotatef(-yRotation, 1, 0, 0);
			gl.glRotatef(-xRotation, 0, 0, 1);
			gl.glTranslatef(-(float)(gridReader.ncols * gridReader.cellsizedx), -(float)(gridReader.nrows * gridReader.cellsizedy),  0);
		}
		
		if(drawTerrain)
			drawSurface();
		
		if(marker)
			markSummit();
		
		if(drawContourLines){
			for(ArrayList<PointPair> line : lines){
				for(PointPair l : line){
					drawLine(l);
				}
			}
		}
	}
	@Override
	public void dispose( GLAutoDrawable glautodrawable ) {
	}

	//implement KeyListener methods
	@Override
	public void keyPressed(KeyEvent e) {
		//exit on escape key for convenience
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
			remove(gljpanel);
			dispose();
			System.exit(0);
		}	
	}
	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

	//implement mouse listener, mouse motion listener, and mouse wheel listener methods
	@Override
	public void mouseDragged(MouseEvent arg0) {
		xRotation = (arg0.getX() - mouseX) + oldXRotation;
		yRotation = (arg0.getY() - mouseY) + oldYRotation;
		gljpanel.display();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {}
	@Override
	public void mouseClicked(MouseEvent arg0) {}
	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {
		mouseX = arg0.getX();
		mouseY = arg0.getY();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		oldXRotation = xRotation;
		oldYRotation = yRotation;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		//disabled because it can cause weird issues in 3d and with different projections
//		if(scaleFactor - e.getWheelRotation() * 0.1f > 0){
//			scaleFactor -= e.getWheelRotation() * 0.1f;
//		}
//		gljpanel.display();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem)e.getSource();
		String sourceName = source.getText();
		if(sourceName.equals("Open Elevation Data")){
			String input = JOptionPane.showInputDialog("Enter base name for .NED, .PRJ, and .FLT files\nThese files must be in the program's data directory");
			if (input != null && !input.equals("")){
				JOptionPane.showMessageDialog(null, "The main window will close. The data may take minutes to load.");

				basename = "data/" + input;
				remove(gljpanel);
				dispose();
				new ContourLines3D();
			}
		}
		else if(sourceName.equals("Save Screenshot")){
			
			BufferedImage screencapture = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = screencapture.createGraphics();
			gljpanel.paint(graphics);
			try{
				File screenshot = new File("terrain_screenshot.png");
				ImageIO.write(screencapture, "png", screenshot);
				JOptionPane.showMessageDialog(null, "Saved screenshot in " + screenshot.getAbsolutePath());
			}
			catch(IOException err){
				err.printStackTrace();
			}
		}
		else if(sourceName.equals("Reset")){
			xRotation = 0;
			yRotation = 0;
			oldXRotation = 0;
			oldYRotation = 0;
			wireframe = true;
			drawContourLines = true;
			drawTerrain = true;
			gljpanel.display();
		}
		else if(sourceName.equals("Instructions")){
			JOptionPane.showMessageDialog(null, "Click and drag in the viewport to rotate the terrain.\n\n" +
												"If you experience performance issues try using the\n" +
												"wireframe render or turning off the terrain under View.\n\n" +
												"You can reset the view to the default by using View > Reset");
		}
		else if(sourceName.equals("About")){
			JOptionPane.showMessageDialog(null, "Made by Will Smith with code from Mauricio Papa.\nminkcv.github.io");
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		JMenuItem source = (JMenuItem)e.getSource();
		String sourceName = source.getText();
		if(sourceName.equals("Wire Frame") && e.getStateChange() == ItemEvent.SELECTED){
			wireframe = true;
		}
		else if (sourceName.equals("Triangle Face") && e.getStateChange() == ItemEvent.SELECTED){
			wireframe = false;
		}
		
		if(sourceName.equals("Perspective") && e.getStateChange() == ItemEvent.SELECTED){
			perspective = true;
			xRotation = 0;
			yRotation = 0;
			oldXRotation = 0;
			oldYRotation = 0;
		}
		else if (sourceName.equals("Orthographic") && e.getStateChange() == ItemEvent.SELECTED){
			perspective = false;
			xRotation = 0;
			yRotation = 0;
			oldXRotation = 0;
			oldYRotation = 0;
		}
		else if (sourceName.equals("Show Contour Lines")){
			drawContourLines = e.getStateChange() == ItemEvent.SELECTED;
		}
		else if(sourceName.equals("Mark Summit")){
			marker = e.getStateChange() == ItemEvent.SELECTED;
		}
		else if(sourceName.equals("Show Terrain")){
			drawTerrain = e.getStateChange() == ItemEvent.SELECTED;
		}
		
		if(gljpanel != null)
			gljpanel.display();
	}

}