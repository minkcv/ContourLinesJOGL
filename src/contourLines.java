package src;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * A program that draws contour maps with USGS data
 * 
 * @author Will Smith with sample code from Mauricio Papa
 */
public class contourLines extends Frame implements GLEventListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener{

    static {
        GLProfile.initSingleton();
    }
    //Instance aux vars
    GLProfile glprofile=null;
    GLCapabilities glcapabilities=null;
    GLCanvas glcanvas=null;
    GL2 gl;
    double AR; //We'll use this for the aspect ratio
    
    //read in from arguments
    static String basename;
    static float low, high, step;
    static float lowR, lowG, lowB;
    static float highR, highG, highB;
    static boolean marker;
    static boolean auto;
    
    gridFloatReader gridReader; //where the height data is
    
    double scaleFactor = 2.0d; // >1 zooms in
    double xOffset = 0.0d; //might need to be really large if you have a large map area
    double yOffset = 0.0d;
    
    int mouseX, mouseY;
    double oldXOffset, oldYOffset;
    
    private class PointPair{ //used for storing two points and a color
    	public double x1, y1, x2, y2;
    	public float r, g, b;
    	public PointPair(){}
    	public void setPoints(double ax, double ay, double bx, double by){
    		x1 = ax; y1 = ay; x2 = bx; y2 = by;
    	}
    	public void setColor(float r, float g, float b){
    		this.r = r; this.g = g; this.b = b;
    	}
    }   
    ArrayList<PointPair> lines;
    
 
    public static void main( String [] args ) {
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
    	
    	new contourLines();
    }
    
    public contourLines() {
    	super("contourLines");
    	gridReader = new gridFloatReader(basename);
    	if(auto){
    		low = (int)(gridReader.minHeight);
    		high = (int)(gridReader.maxHeight);
    	}
    	createContourLines();
    	
        glprofile = GLProfile.getDefault();
        glcapabilities = new GLCapabilities( glprofile );
        glcanvas = new GLCanvas( glcapabilities );

        glcanvas.addGLEventListener( this);
        
        add( glcanvas );
        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent windowevent ) {
                remove( glcanvas );
                dispose();
                System.exit( 0 );
            }
        });
        glcanvas.addKeyListener(this);
        glcanvas.addMouseMotionListener(this);
        glcanvas.addMouseListener(this);
        glcanvas.addMouseWheelListener(this);
        setSize(640, 800);
        setVisible( true );    	
        glcanvas.requestFocus();
    }

    private void createContourLines(){
    	System.out.println("creating contour line data");
    	lines = new ArrayList<PointPair>();
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
						pp.setPoints(x * dx, yContour, xContour, y * dy);
					}
					else if(llc < c &&
							lrc > c &&
							ulc < c &&
							urc < c){ //lower right is greater
						xContour = (c - llc) / (lrc - llc) * xLeftLower + x * dx;
						yContour = (c - urc) / (lrc - urc) * yBottomHigher + (y + 1) * dy;
						pp.setPoints((x + 1) * dx, yContour, xContour, y * dy);
					}
					else if(llc < c &&
							lrc < c &&
							ulc > c &&
							urc < c){ //upper left is greater
						xContour = (c - urc) / (ulc - urc) * xLeftHigher + (x + 1) * dx;
						yContour = (c - llc) / (ulc - llc) * yBottomLower + y * dy;
						pp.setPoints(x * dx, yContour, xContour, (y + 1) * dy);
					}
					else if(llc < c &&
							lrc < c &&
							ulc < c &&
							urc > c){ //upper right is greater
						xContour = (c - ulc) / (urc - ulc) * xLeftLower + x * dx;
						yContour = (c - lrc) / (urc - lrc) * yBottomLower + y * dy;
						pp.setPoints((x + 1) * dx, yContour, xContour, (y + 1) * dy);
					}
					//one corner is less than c
					else if(llc < c &&
							lrc > c &&
							ulc > c &&
							urc > c){ //lower left is less
						xContour = (c - llc) / (lrc - llc) * xLeftLower + x * dx;
						yContour = (c - llc) / (ulc - llc) * yBottomLower + y * dy;
						pp.setPoints(x * dx, yContour, xContour, y * dy);
					}
					else if(llc > c &&
							lrc < c &&
							ulc > c &&
							urc > c){ //lower right is less
						xContour = (c - lrc) / (llc - lrc) * xLeftHigher + (x + 1) * dx;
						yContour = (c - lrc) / (urc - lrc) * yBottomLower + y * dy;
						pp.setPoints((x + 1) * dx, yContour, xContour, y * dy);
					}
					else if(llc > c &&
							lrc > c &&
							ulc < c &&
							urc > c){ //upper left is less
						xContour = (c - ulc) / (urc - ulc) * xLeftLower + x * dx;
						yContour = (c - ulc) / (llc - ulc) * yBottomHigher + (y + 1) * dy;
						pp.setPoints(x * dx, yContour, xContour, (y + 1) * dy);
					}
					else if(llc > c &&
							lrc > c &&
							ulc > c &&
							urc < c){ //upper right is less
						xContour = (c - urc) / (ulc - urc) * xLeftHigher + (x + 1) * dx;
						yContour = (c - urc) / (lrc - urc) * yBottomHigher + (y + 1) * dy;
						pp.setPoints((x + 1) * dx, yContour, xContour, (y + 1) * dy);
					}
					//one side is less and one side is greater
					else if(llc < c &&
							lrc < c &&
							ulc > c &&
							urc > c){ //bottom side is less
						yContour = (c - llc) / (ulc - llc) * yBottomLower + y * dy;
						double yContour2 = (c - lrc) / (urc - lrc) * yBottomLower + y * dy;
						pp.setPoints(x * dx, yContour, (x + 1) * dx, yContour2);
					}
					else if(llc < c &&
							lrc > c &&
							ulc < c &&
							urc > c){ //left side is less
						xContour = (c - llc) / (lrc - llc) * xLeftLower + x * dx;
						double xContour2 = (c - ulc) / (urc - ulc) * xLeftLower + x * dx;
						pp.setPoints(xContour, y * dy, xContour2, (y + 1) * dy);
					}
					else if(llc > c &&
							lrc > c &&
							ulc < c &&
							urc < c){ //top side is less
						yContour = (c - ulc) / (llc - ulc) * yBottomHigher + (y + 1) * dy;
						double yContour2 = (c - urc) / (lrc - urc) * yBottomHigher + (y + 1) * dy;
						pp.setPoints(x * dx, yContour, (x + 1) * dx, yContour2);
					}
					else if(llc > c &&
							lrc < c &&
							ulc > c &&
							urc < c){ //right side is less
						xContour = (c - lrc) / (llc - lrc) * xLeftHigher + (x + 1) * dx;
						double xContour2 = (c - urc) / (ulc - urc) * xLeftHigher + (x + 1) * dx;
						pp.setPoints(xContour, y * dy, xContour2, (y + 1) * dy);
					}
					//ambiguous case
					else{
						xContour = (c - lrc) / (llc - lrc) * xLeftHigher + (x + 1) * dx;
						yContour = (c - ulc) / (llc - ulc) * yBottomHigher + (y + 1) * dy;
						pp.setPoints(x * dx, yContour, xContour, y * dy);
						xContour = (c - ulc) / (urc - ulc) * xLeftHigher + (x + 1) * dx;
						yContour = (c - lrc) / (urc - lrc) * yBottomHigher + (y + 1) * dy;
						pp2 = new PointPair();
						pp2.setPoints((x + 1) * dx, yContour, xContour, (y + 1) * dy);
					}
					
					if(pp != null){
						pp.setColor(r, g, b);
						lines.add(pp);
					}
					if(pp2 != null){
						pp2.setColor(r, g, b);
						lines.add(pp2);
					}
				}
			}
			c += dc; 
		}
    	System.out.println("finished createContourLines");
    }
    
    private void drawLine(PointPair pp){
    	gl.glColor3f(pp.r, pp.g, pp.b);
    	gl.glBegin(GL2.GL_LINES);
    	{
    		gl.glVertex2d((pp.x1 + xOffset) * scaleFactor, (pp.y1 + yOffset) * scaleFactor);
    		gl.glVertex2d((pp.x2 + xOffset) * scaleFactor, (pp.y2 + yOffset) * scaleFactor);
    	}
    	gl.glEnd();
    }
    
    private void markSummit(){
    	gl.glColor3f(1.0f - highR, 1.0f - highG, 1.0f - highB); //opposite color of high color will be very visible
    	gl.glPointSize((float)(gridReader.cellsizedx * scaleFactor) / 2.0f);
    	gl.glBegin(GL2.GL_POINTS);
    	{
    		gl.glVertex2d((xOffset + gridReader.maxHeightxi * gridReader.cellsizedx) * scaleFactor, (yOffset + gridReader.maxHeightyi * gridReader.cellsizedy) * scaleFactor);
    	}
    	gl.glEnd();
    	System.out.println("drew marker at x: " + gridReader.maxHeightxi * gridReader.cellsizedx * scaleFactor + " y: " + gridReader.maxHeightyi * gridReader.cellsizedy * scaleFactor);
    }
    
 
    //Implementing GLEventListener methods 
    @Override
    public void init( GLAutoDrawable glautodrawable ) {
		System.out.println("Entering init();");
		gl = glautodrawable.getGL().getGL2();
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0f); //set to non-transparent black
    }
    
    @Override
    public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
		System.out.println("Entering reshape(); x="+x+" y="+y+" width="+width+" height="+height);
    	GL2 gl=glautodrawable.getGL().getGL2();
    	//Set up projection
        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity();
		//this glOrtho call sets up a plane with a parallel projection. 
		gl.glOrtho(0, gridReader.ncols * gridReader.cellsizedx, gridReader.nrows * gridReader.cellsizedy, 0, 0, 10);

		AR = (gridReader.ncols * gridReader.cellsizedx)  / (gridReader.nrows * gridReader.cellsizedy);
		if (AR*height<width) gl.glViewport(x, y, (int) (AR*height), height);
		else gl.glViewport(x, y, width, (int) (width/AR));
//		gl.glViewport(x, y, (int) (AR*height), height);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
    }
    
    @Override
    public void display( GLAutoDrawable glautodrawable ) {
    	System.out.println("Entering display");
    	//Get context
    	gl=glautodrawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		//Set a color (red - no other components)
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		
		for(PointPair l : lines){
			drawLine(l);
		}
		
		if(marker)
			markSummit();
		
		System.out.println("finished display");
    }
    @Override
    public void dispose( GLAutoDrawable glautodrawable ) {
    }
    
   //implement KeyListener methods
  	@Override
  	public void keyPressed(KeyEvent e) {
  		//exit on escape key for convenience
  		if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
  			remove(glcanvas);
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
		xOffset = (arg0.getX() - mouseX) * (gridReader.cellsizedx / scaleFactor) + oldXOffset;
		yOffset = (arg0.getY() - mouseY) * (gridReader.cellsizedy / scaleFactor) + oldYOffset;
		glcanvas.display();
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
		oldXOffset = xOffset;
		oldYOffset = yOffset;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(scaleFactor - e.getWheelRotation() * 0.1f > 0){
			scaleFactor -= e.getWheelRotation() * 0.1f;
		}
		glcanvas.display();
	}

}