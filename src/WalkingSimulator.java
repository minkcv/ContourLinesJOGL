package src;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
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

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureIO;

import javax.vecmath.*; //apt-get install libvecmath-java


import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

/**
 * A program that draws contour maps with USGS data and simulates walking on them
 * 
 * @author Will Smith with sample code from Mauricio Papa
 */
public class WalkingSimulator extends JFrame implements GLEventListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, ActionListener, ItemListener{

	static {
		GLProfile.initSingleton();
	}
	//Instance aux vars
	GLProfile glprofile=null;
	GLCapabilities glcapabilities=null;
	GLJPanel gljpanel=null;
	GL2 gl;
	GLU glu;
	FPSAnimator animator;
	double AR; //We'll use this for the aspect ratio
	Robot robot;

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

	double scaleFactor = 1.0d; // >1 zooms in
	double xOffset = 0.0d; //might need to be really large if you have a large map area
	double yOffset = 0.0d;
	double oldXOffset, oldYOffset;

	float xRotation = 0f;
	float yRotation = 0f;

	boolean wireframe = true;
	boolean perspective = true;
	boolean textured = false;
	boolean drawContourLines = false;
	boolean drawTerrain = true;
	boolean[] painted;
	boolean picking;

	boolean touchingGround = true;
	double cameraFOV = 140;
	float cameraXPosition = 0;
	float cameraYPosition = 0;
	float cameraZPosition = -1300;
	float yVelocity = 0;
	final float fallSpeed = -9.81f;
	float cameraFeet = 0;
	boolean crouching;
	boolean grabMouse = true;
	float jumpSpeed = -10.81f;

	float moveSpeed = 1.34f;

	float[] light_position;
	
	Texture mshtexture;
	TextureCoords mshcoords;
	

	ArrayList<Integer> pressedKeys; // list of currently pressed keys, slower than a boolean for each, but easier

	private class PointPair{ //used for storing two points and a color
		public double x1, y1, x2, y2, h;
		public float r, g, b;
		public int pointId;
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

		new WalkingSimulator();
	}

	public WalkingSimulator() {
		super("Terrain Walking Simulator");
		JOptionPane.showMessageDialog(null, "IMPORTANT: Hold Control to release the mouse cursor!");
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		pressedKeys = new ArrayList<Integer>();
		gridReader = new gridFloatReader(basename);
		painted = new boolean[gridReader.ncols * gridReader.nrows];
		xTolerance = gridReader.cellsizedx / 2;
		yTolerance = gridReader.cellsizedy / 2;
		if(auto){
			low = (int)(gridReader.minHeight);
			high = (int)(gridReader.maxHeight);
		}
		setupGUI();
		createContourLines();

		cameraXPosition = (float) (gridReader.maxHeightxi * gridReader.cellsizedx);
		cameraYPosition = (float) (gridReader.maxHeightyi * gridReader.cellsizedy);
		glprofile = GLProfile.getDefault();
		glcapabilities = new GLCapabilities( glprofile );
		gljpanel = new GLJPanel( glcapabilities );
		animator = new FPSAnimator(gljpanel, 60);

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
		centerCursor();
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
		wireframeButton = new JRadioButtonMenuItem("Textured");
		wireframeButton.setSelected(textured);
		wireframeButton.setMnemonic(KeyEvent.VK_E);
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
		
		float idx = 1f / gridReader.ncols;
		float idy = 1f / gridReader.nrows;
		
		int pointId = 0;
		if(textured){
			mshtexture.enable(gl);
			mshtexture.bind(gl);
			gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
			mshcoords = mshtexture.getImageTexCoords();
		}
		for(int x = 0; x < gridReader.ncols - 1; x++){
			if(!picking){
				if(wireframe){
					gl.glBegin(GL2.GL_LINE_STRIP);
				}
				else{
					gl.glBegin(GL2.GL_TRIANGLE_STRIP);
				}
			}
			for(int y = 0; y < gridReader.nrows - 1; y++){
				
				gl.glLoadName(pointId);
				if(picking){
					gl.glBegin(GL2.GL_TRIANGLE_STRIP);
				}
				float red = (float)((gridReader.height[y][x] - gridReader.minHeight) / (gridReader.maxHeight - gridReader.minHeight));
				float blue = 1.0f - (float)((gridReader.height[y][x] - gridReader.minHeight) / (gridReader.maxHeight - gridReader.minHeight));
				gl.glColor3f(red, 0.0f, blue);
				if(painted[pointId]){
					gl.glColor3f(0.2f, 1f, 0.2f);
				}
				
				Vector3f v1 = new Vector3f((x + 1) * dx, y * dy, gridReader.height[y][x + 1]);
				Vector3f v2 = new Vector3f(x * dx, (y + 1) * dy, gridReader.height[y + 1][x]);
				Vector3f n = new Vector3f();
				n.cross(v1, v2);
				n.normalize();
				gl.glNormal3f(n.x, n.y, n.z);
				
				if(textured)
					gl.glTexCoord2f(x * idx, y * idy);
				gl.glVertex3d((x * dx + xOffset) * scaleFactor, (y * dy + yOffset) * scaleFactor, gridReader.height[y][x] * scaleFactor);
				if(textured)
					gl.glTexCoord2f((x + 1) * idx, y * idy);
				gl.glVertex3d(((x + 1) * dx + xOffset) * scaleFactor, (y * dy + yOffset) * scaleFactor, gridReader.height[y][x + 1] * scaleFactor);
				if(textured)
					gl.glTexCoord2f(x * idx, (y + 1) * idy);
				gl.glVertex3d((x * dx + xOffset) * scaleFactor, ((y + 1) * dy + yOffset) * scaleFactor, gridReader.height[y + 1][x] * scaleFactor);
				if(textured)
					gl.glTexCoord2f((x + 1) * idx, (y + 1) * idy);
				gl.glVertex3d(((x + 1) * dx + xOffset) * scaleFactor, ((y + 1) * dy + yOffset) * scaleFactor, gridReader.height[y + 1][x + 1] * scaleFactor);
				pointId++;
				if(picking)
					gl.glEnd();
			}
			
			if(!picking){
				gl.glEnd();
			}
		}
		if(textured){
			mshtexture.disable(gl);
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
		
		//texture setup
		try {
			mshtexture = TextureIO.newTexture(new File("data/msh.jpg"), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//lighting setup
		float[] mat_specular   = {1.0f, 1.0f, 1.0f, 1.0f};
		float[] mat_diffuse    = {1.0f, 0.0f, 0.0f, 1.0f};
		float[] mat_ambient    = {1.0f, 0.0f, 0.0f, 1.0f};
		float   mat_shininess  = 64.0f;
		float[] light_ambient  = {0.0f, 0.0f, 0.0f, 1.0f};
		float[] light_diffuse  = {1.0f, 1.0f, 1.0f, 1.0f};
		float[] light_specular = {1.0f, 1.0f, 1.0f, 1.0f};
		float[] light_pos_temp = {0.0f, 0.0f, (float) gridReader.maxHeight * 2, 0.0f};

		light_position = light_pos_temp;
		/* set up ambient, diffuse, and specular components for light 0 */
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, FloatBuffer.wrap(light_ambient));
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, FloatBuffer.wrap(light_diffuse));
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, FloatBuffer.wrap(light_specular));
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, FloatBuffer.wrap(light_position));
		/** define material properties for front face of all polygons
		 * Default values are:
		 * Ambient: (0.2,0.2,.0.2,1.0)
		 * Diffuse: (0.8,0.8,0.8,1.0)
		 * Specular: (0,0,0,1)
		 * Shininess: 0 [max is 128 - it is really controlling rate of decay]
		 */
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, FloatBuffer.wrap(mat_specular));
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, FloatBuffer.wrap(mat_ambient));
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, FloatBuffer.wrap(mat_diffuse));
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, mat_shininess);


		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);


		gl.glShadeModel(GL2.GL_SMOOTH); /* enable smooth shading */
		gl.glEnable(GL2.GL_LIGHTING); /* enable lighting */
		gl.glEnable(GL2.GL_LIGHT0);  /* enable light 0 */


		gl.glClearColor(0.0f, 0.0f, 0.0f, 0f); //set to non-transparent black
		gl.glEnable(GL2.GL_DEPTH_TEST);

		animator.start();
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
		update();
		gl=glautodrawable.getGL().getGL2();

		if(picking){
			int buffersize = 256;
			int[] viewPort = new int[4];
			IntBuffer selectBuffer = GLBuffers.newDirectIntBuffer(buffersize);
			int hits = 0;
			gl.glGetIntegerv(GL.GL_VIEWPORT, viewPort, 0);
			gl.glSelectBuffer(buffersize, selectBuffer);
			gl.glRenderMode(GL2.GL_SELECT);
			gl.glInitNames();
			gl.glPushName(-1);
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glPushMatrix();
			gl.glLoadIdentity();
			glu.gluPickMatrix(getWidth() / 2,  getHeight() / 2 - 40, 10.0d, 10.0d, viewPort, 0);
			displayScene(gl);
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glPopMatrix();
			gl.glFlush();
			hits = gl.glRenderMode(GL2.GL_RENDER);
			processHits(hits, selectBuffer);
			picking = false;
		}
		else{ //normal display
			displayScene(gl);
		}

	}
	
	private void displayScene(GL2 gl){
		if(!picking){
			gl.glMatrixMode( GL2.GL_PROJECTION );
			gl.glLoadIdentity();
		}
		if(perspective){
			glu.gluPerspective(cameraFOV / 2, 16f/9f, 0.001, gridReader.nrows * gridReader.cellsizedy * 2);
		}
		else{
			gl.glOrtho(0, gridReader.ncols * gridReader.cellsizedx, 
					gridReader.nrows * gridReader.cellsizedy / 2, 0, 
					-gridReader.nrows * gridReader.cellsizedy, gridReader.nrows * gridReader.cellsizedy);
		}
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);


		if(perspective){
			gl.glRotatef(-90, 1, 0, 0);

			if(yRotation > 90) //limit view at bottom and top
				yRotation = 90;
			else if(yRotation < -90)
				yRotation = -90;
			gl.glRotatef(yRotation, 1, 0, 0);
			gl.glRotatef(xRotation, 0, 0, 1);

			gl.glTranslatef(cameraXPosition, cameraYPosition, cameraZPosition);
			gl.glRotatef(180, 0, 0, 1);
		}
		else{
			gl.glTranslatef((float)(gridReader.ncols * gridReader.cellsizedx / 2), (float)(gridReader.nrows * gridReader.cellsizedy / 2),  0);
			gl.glRotatef(-yRotation, 1, 0, 0);
			gl.glRotatef(-xRotation, 0, 0, 1);
			gl.glTranslatef(-(float)(gridReader.ncols * gridReader.cellsizedx / 2), -(float)(gridReader.nrows * gridReader.cellsizedy / 2),  0);
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
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, FloatBuffer.wrap(light_position));//position the light source
		gl.glBegin(GL.GL_POINTS);
		gl.glVertex3f(0.0f, 0.0f, (float) gridReader.maxHeight * 2); //draw a point at the light source
		gl.glEnd();

		//draw swing style on top of the gljpanel
		Graphics g = gljpanel.getGraphics();
		g.setColor(Color.YELLOW);
		g.drawRect(getWidth() / 2 - 5, getHeight() / 2 - 5, 10, 10);
	}
	@Override
	public void dispose( GLAutoDrawable glautodrawable ) {
	}

	private void update(){
		float originalMoveSpeed = moveSpeed;
		float originalJumpSpeed = jumpSpeed;
		
		if(pressedKeys.contains(KeyEvent.VK_SHIFT)){
			moveSpeed *= 6;
			jumpSpeed *= 3;
		}
		if(pressedKeys.contains(KeyEvent.VK_R)){//reset view
			cameraFOV = 140;
			yRotation = 0;
		}
		if(pressedKeys.contains((KeyEvent.VK_CONTROL))){
			grabMouse = false;
			unHideCursor();
		}
		else{
			grabMouse = true;
			hideCursor();
			centerCursor();
		}
		if(pressedKeys.contains(KeyEvent.VK_W)){
			double xRotationRadians = xRotation * Math.PI / 180d;
			cameraXPosition -= moveSpeed * Math.sin(xRotationRadians);
			cameraYPosition -= moveSpeed * Math.cos(xRotationRadians);
		}
		if(pressedKeys.contains(KeyEvent.VK_S)){
			double xRotationRadians = xRotation * Math.PI / 180d;
			cameraXPosition += moveSpeed * Math.sin(xRotationRadians);
			cameraYPosition += moveSpeed * Math.cos(xRotationRadians);
		}
		if(pressedKeys.contains(KeyEvent.VK_A)){
			double xRotationRadians = xRotation * Math.PI / 180d;
			cameraXPosition += moveSpeed * Math.cos(xRotationRadians);
			cameraYPosition -= moveSpeed * Math.sin(xRotationRadians);
		}
		if(pressedKeys.contains(KeyEvent.VK_D)){
			double xRotationRadians = xRotation * Math.PI / 180d;
			cameraXPosition -= moveSpeed * Math.cos(xRotationRadians);
			cameraYPosition += moveSpeed * Math.sin(xRotationRadians);
		}

		if(cameraXPosition < 0)
			cameraXPosition = 0;
		if(cameraYPosition < 0)
			cameraYPosition = 0;

		//cast to int is intentional here
		int gridx = (int)(cameraXPosition / gridReader.cellsizedx);
		int gridy = (int)(cameraYPosition / gridReader.cellsizedy);
		
		//distance to each corner has an influence on height (interpolate between 4 verticies)
		double distxy = Math.sqrt(Math.pow(cameraXPosition - gridx * gridReader.cellsizedx, 2) + Math.pow(cameraYPosition - gridy * gridReader.cellsizedy, 2));
		double distx1y = Math.sqrt(Math.pow(cameraXPosition - (gridx + 1) * gridReader.cellsizedx, 2) + Math.pow(cameraYPosition - gridy * gridReader.cellsizedy, 2));
		double distxy1 = Math.sqrt(Math.pow(cameraXPosition - gridx * gridReader.cellsizedx, 2) + Math.pow(cameraYPosition - (gridy + 1) * gridReader.cellsizedy, 2));
		double distx1y1 = Math.sqrt(Math.pow(cameraXPosition - (gridx + 1) * gridReader.cellsizedx, 2) + Math.pow(cameraYPosition - (gridy + 1) * gridReader.cellsizedy, 2));

		double distTotal = distxy + distx1y + distxy1 + distx1y1;
		float groundHeight = -(float)((gridReader.height[gridy][gridx] * (distx1y1 / distTotal)) + 
				(gridReader.height[gridy + 1][gridx] * (distx1y / distTotal)) + 
				(gridReader.height[gridy][gridx + 1] * (distxy1 / distTotal)) + 
				(gridReader.height[gridy + 1][gridx + 1] * (distxy / distTotal))); //an amount of each height relative to distance
		
//		groundHeight = (float)(gridReader.height[gridy][gridx] * (1f - cameraXPosition / gridReader.cellsizedx) * (1f - cameraYPosition / gridReader.cellsizedy) +
//				gridReader.height[gridy][gridx + 1] * cameraXPosition / gridReader.cellsizedx * (1f - cameraYPosition / gridReader.cellsizedy) + 
//				gridReader.height[gridy + 1][gridx] * (1f - cameraXPosition / gridReader.cellsizedx) * cameraYPosition / gridReader.cellsizedy + 
//				gridReader.height[gridy + 1][gridx + 1] * cameraXPosition / gridReader.cellsizedx * cameraYPosition / gridReader.cellsizedy);
		
		//jump physics
		if(pressedKeys.contains(KeyEvent.VK_SPACE)){
			if(touchingGround){
				yVelocity = jumpSpeed;
				touchingGround = false;
			}
		}

		if(cameraFeet > groundHeight){
			touchingGround = true;
			cameraFeet = groundHeight;
			yVelocity = 0;
		}
		else{
			yVelocity -= fallSpeed;
			touchingGround = false;
		}

		if(!crouching){
			cameraZPosition = cameraFeet - 10;
		}
		else{
			cameraZPosition = cameraFeet- 5;
		}
		//		System.out.println(gridx + " " + gridy + " " + gridReader.height[gridy][gridx]);
		moveSpeed = originalMoveSpeed; //restore movespeed
		jumpSpeed = originalJumpSpeed;
		cameraFeet += yVelocity;
	}
	
	public void processHits(int hits, IntBuffer buffer){
//		System.out.println("---------------------------------");
		System.out.println(" HITS: " + hits);
		int offset = 0;
		int names;
		long z1, z2;
		for (int i=0;i<hits;i++)
		{
//			System.out.println("- - - - - - - - - - - -");
//			System.out.println(" hit: " + (i + 1));
			names = buffer.get(offset); offset++;
			z1 = ((long) buffer.get(offset) & 0xffffffffl); 
			offset++;
			z2 = ((long) buffer.get(offset) & 0xffffffffl); 
			offset++;
//			System.out.println(" number of names: " + names);
//			System.out.println(" z1: " + z1);
//			System.out.println(" z2: " + z2);
//			System.out.println(" names: ");

			for (int j=0;j<names;j++){
//				System.out.print("       " + buffer.get(offset));
				if (j==(names-1)){
//					System.out.println("<-");
				}
				else{
//					System.out.println();
				}

				painted[buffer.get(offset)] = true;
				offset++;
			}
//			System.out.println("- - - - - - - - - - - -");
		}
//		System.out.println("---------------------------------");
		for (int i = 0; i < painted.length; i++) {
			if(painted[i]){
//				System.out.println("painted " + i);
			}
		}
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

		//used for all other keybings
		if(!pressedKeys.contains(new Integer(e.getKeyCode()))){
			pressedKeys.add(e.getKeyCode());
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		pressedKeys.remove(new Integer(e.getKeyCode()));
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	//implement mouse listener, mouse motion listener, and mouse wheel listener methods
	@Override
	public void mouseDragged(MouseEvent arg0) {
		mouseMoved(arg0);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		if(grabMouse){
			xRotation += (MouseInfo.getPointerInfo().getLocation().getX() - getWidth() / 2) / 3;
			yRotation += (MouseInfo.getPointerInfo().getLocation().getY() - getHeight() / 2) / 3;
			centerCursor();
		}
	}
	@Override
	public void mouseClicked(MouseEvent arg0) {}
	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {
		switch(arg0.getButton()){
		case MouseEvent.BUTTON1:
			picking = true;
			break;
		case MouseEvent.BUTTON3:
			crouching = true;
			break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		switch(arg0.getButton()){
		case MouseEvent.BUTTON1:
			break;
		case MouseEvent.BUTTON3:
			crouching = false;
			break;
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(e.getWheelRotation() > 0){
			if(cameraFOV < 360)
				cameraFOV += 10;
		}
		else if(e.getWheelRotation() < 0){
			if(cameraFOV > 10)
				cameraFOV -= 10;
		}
	}

	//listeners for gui menus

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
				new WalkingSimulator();
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
			wireframe = true;
			drawContourLines = true;
			drawTerrain = true;
			gljpanel.display();
		}
		else if(sourceName.equals("Instructions")){
			JOptionPane.showMessageDialog(null, "Hold Control to release the mouse cursor, press Escape to exit.\n" +
					"Use WASD to move and hold Shift to move faster\n\n" + 
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
			textured = false;
		}
		else if (sourceName.equals("Triangle Face") && e.getStateChange() == ItemEvent.SELECTED){
			wireframe = false;
			textured = false;
		}
		else if (sourceName.equals("Textured") && e.getStateChange() == ItemEvent.SELECTED){
			wireframe = false;
			textured = true;
		}

		if(sourceName.equals("Perspective") && e.getStateChange() == ItemEvent.SELECTED){
			perspective = true;
			xRotation = 0;
			yRotation = 0;
		}
		else if (sourceName.equals("Orthographic") && e.getStateChange() == ItemEvent.SELECTED){
			perspective = false;
			xRotation = 0;
			yRotation = 0;
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

	//used for capturing / releasing the mouse
	private void hideCursor() {
		setCursor(getToolkit().createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR),new Point(0,0),""));
	}
	private void unHideCursor(){
		setCursor(null);
	}

	private void centerCursor(){
		robot.mouseMove(getWidth() / 2, getHeight() / 2);
	}
}