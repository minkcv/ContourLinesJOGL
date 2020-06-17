package src;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JOptionPane;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.DoubleBuffer;
import java.util.Random;

/**
 * A minimal program that draws a koch snowflake fractal in an AWT Frame.
 *
 * @author Will Smith with sample code from Mauricio Papa
 */
public class WillSmithProject1 extends Frame implements GLEventListener, KeyListener{
	
	GL2 gl2;
	//Triangle coordinates if no arguments given
	static double[] V1={1,2};
	static double[] V2={9,2};
	static double[] V3={5,10};
	//Recursion depth if no arguments given
	static int N=5;
	//for converting degrees to radians
	final double toRad = Math.PI / 180;
	//statistics
	static int totalSegments = 0;
	static double singleLength = 0;
	static double totalArea = 0;
	//red green blue colors
	static float[][] colors = {
		{ 1.0f, 0.0f, 0.0f},
		{ 0.0f, 1.0f, 0.0f},
		{ 0.0f, 0.0f, 1.0f},
	};
	//used to change colors easily
	static float[] currentColor; // use n to alternate colors
	
	//used to create an orthographic viewing window that shows the whole snowflake
	double orthoMinX = 0;
	double orthoMaxX = 10;
	double orthoMinY = 0;
	double orthoMaxY = 10;
	
	//A segment can have 1 of 6 orientations
	//these are used to determine the angle needed for finding the new point
	enum TSide{
		top,
		bottom,
		leftUp,
		leftDown,
		rightUp,
		rightDown
	}

	static {
		// takes no arguments on linux but takes a boolean on some other systems
		GLProfile.initSingleton();
	}
	//Instance aux vars
	GLProfile glprofile=null;
	GLCapabilities glcapabilities=null;
	GLCanvas glcanvas=null;

	//Constructor
	public WillSmithProject1() {
		super("GL2Snowflake");
		String input = JOptionPane.showInputDialog("Enter number of times to recurse between 0 and 11");
		N = Integer.parseInt(input);
		if(N > 11){
			N = 11;
		}
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
		setSize( 800, 700);
		setVisible( true );    	
		glcanvas.requestFocus();
		
		calculateOrtho();
	}

	public static void main( String [] args ) {
		if(args.length > 0){ //read command line args
			V1[0] = Double.parseDouble(args[0]);
			V1[1] = Double.parseDouble(args[1]);
			V2[0] = Double.parseDouble(args[2]);
			V2[1] = Double.parseDouble(args[3]);
			V3[0] = Double.parseDouble(args[4]);
			V3[1] = Double.parseDouble(args[5]);
			N = Integer.parseInt(args[6]);
		}
		new WillSmithProject1();
	}

	//Implementing GLEventListener methods

	@Override
	public void init( GLAutoDrawable glautodrawable ) {
		System.out.println("Entering init();");
		gl2 = glautodrawable.getGL().getGL2();
//		gl.glShadeModel(GL.GL_FLAT);
		gl2.glClearColor(.8f, .8f, .8f, 0f); //set to non-transparent black
		gl2.glPointSize(4); //If you want a fat point
	}

	@Override
	public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
		System.out.println("Entering reshape();");
		//Get the context
		gl2=glautodrawable.getGL().getGL2();
		//Set up projection
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		
//		gl2.glOrtho(0,10,0,10,0,10);
		gl2.glOrtho(orthoMinX, orthoMaxX,orthoMinY,orthoMaxY ,0,10);
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
	}

	@Override
	public void display( GLAutoDrawable glautodrawable ) {
		currentColor = colors[1];
		//reset statistics
		totalSegments = 0;
		singleLength = 0;
		totalArea = 0;
		
		System.out.println("Entering display();");
		//We always need a context to draw
		gl2 = glautodrawable.getGL().getGL2();
		gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
		gl2.glColor3f(currentColor[0], currentColor[1], currentColor[2]);

		drawTriangle(V1, V2, V3); //draw the center triangle
		double l = V2[0] - V1[0];
		totalArea += (Math.sqrt(3) / 4.0) * Math.pow(l, 2); //add the area of the center triangle
		
		snowflake(V1, V2, TSide.bottom,  N);
		snowflake(V2, V3, TSide.rightUp, N);
		snowflake(V1, V3, TSide.leftUp, N);
		
		printStatistics();
		
	}
	@Override
	public void dispose( GLAutoDrawable glautodrawable ) {
	}
	
	//recursively draw one side of a koch snowflake for n iterations
	private void snowflake(double[] v1, double[] v2, TSide side, int n){
		double[] t1 = new double[2]; //third of the way point along v1 and v2
		double[] t2 = new double[2]; //two thirds of the way point along v1 and v2
		double[] m = new double[2]; //midpoint between v1 and v2
		double[] newPoint = new double[2]; // tip of new triangle
		//length from v1 to v2
		double d = Math.sqrt(Math.pow(v1[0] - v2[0], 2) + Math.pow(v1[1] - v2[1], 2)); //distance formula
		if (n > 0){ //keep recursing
			double height = (d / 6.0f) * Math.sqrt(3); //height of new triangle
			//hypotenuse from v1 or v2 to the newPoint
			//this also happens to be the radius in polar coordinates
			double hypot = Math.sqrt(Math.pow(d / 2.0f, 2) + Math.pow(height, 2));
			double theta = 0;
			
			//determine angle for calculating polar coordinates based of triangle orientation
			switch (side){
				case top:
					theta = 30;
					break;
				case leftUp:
					theta = 90;
					break;
				case leftDown:
					theta = 150;
					break;
				case rightUp:
					theta = 90;
					break;
				case rightDown:
					theta = -90;
					break;
				case bottom:
					theta = -30;
					break;
			}
			
			theta *= toRad; //convert to radians
			
			newPoint = polarToCartesian(hypot, theta); //get back to x y coordinates

			for(int i = 0; i < 2; i++){ //for x and y
				//note that everything is offset from the origin so "+ v1[i]" on the end of everything
				t1[i] = (v2[i] - v1[i]) / 3.0f + v1[i];
				t2[i] = (2.0f * (v2[i] - v1[i]) / 3.0f) + v1[i];
				m[i] = (v2[i] - v1[i]) / 2.0f + v1[i];

				newPoint[i] += v1[i];
			}
			
			//start next recursive calls based on current side orientation
			switch(side){
				case top:
					snowflake(v1, t1, side, n - 1);
					snowflake(t2, v2, side, n - 1);
					snowflake(t1, newPoint, TSide.leftUp, n - 1);
					snowflake(t2, newPoint, TSide.rightUp, n - 1);
					break;
				case leftUp:
					snowflake(v1, t1, side, n - 1);
					snowflake(t2, v2, side, n - 1);
					snowflake(t1, newPoint, TSide.leftDown, n - 1);
					snowflake(newPoint, t2, TSide.top, n - 1);
					
					break;
				case leftDown:
					snowflake(v1, t1, side, n - 1);
					snowflake(t2, v2, side, n - 1);
					snowflake(newPoint, t2, TSide.leftUp, n - 1);
					snowflake(newPoint, t1, TSide.bottom, n - 1);
					break;
				case rightUp:
					snowflake(v1, t1, side, n - 1);
					snowflake(t2, v2, side, n - 1);
					snowflake(t2, newPoint, TSide.top, n - 1);
					snowflake(newPoint, t1, TSide.rightDown, n - 1);
					break;
				case rightDown:
					snowflake(v1, t1, side, n - 1);
					snowflake(t2, v2, side, n - 1);
					snowflake(newPoint, t1, TSide.rightUp, n - 1);
					snowflake(t2, newPoint, TSide.bottom, n - 1);
					break;
				case bottom:
					snowflake(v1, t1, side, n - 1);
					snowflake(t2, v2, side, n - 1);
					snowflake(newPoint, t1, TSide.leftDown, n - 1);
					snowflake(t2, newPoint, TSide.rightDown, n - 1);
					break;
			}
			currentColor = colors[n % 3]; // change color every iteration
			
		}else{ //end of recursion
			//this is one segment that does not recurse
			totalSegments += 1;
			singleLength = d;
		}
		//calculate area of created triangle
		double area = (Math.sqrt(3) / 4.0) * Math.pow(d / 3.0, 2);
		totalArea += area;
		drawTriangle(t1, t2, newPoint);
	}

	private void drawTriangle(double[] v1, double[] v2, double[] v3) {
		boolean gradient = true; //set to true for gradient rainbow triangles
		int c = 0;
		if(gradient){
			currentColor = colors[c];
		}
		gl2.glColor3f(currentColor[0], currentColor[1], currentColor[2]);
		gl2.glBegin(GL.GL_TRIANGLES);
		{
			gl2.glVertex2dv(DoubleBuffer.wrap(v1));
			if(gradient){
			c++;
			currentColor = colors[c];
			gl2.glColor3f(currentColor[0], currentColor[1], currentColor[2]);
			}
			
			gl2.glVertex2dv(DoubleBuffer.wrap(v2));
			if(gradient){
			c++;
			currentColor = colors[c];
			gl2.glColor3f(currentColor[0], currentColor[1], currentColor[2]);
			}
			gl2.glVertex2dv(DoubleBuffer.wrap(v3));
		}
		gl2.glEnd();
	}
	
	//convert to cartesian coordinates from polar
	//give angle t in radians
	public double[] polarToCartesian(double r, double t){
		double[] xy = new double[2];
		xy[0] = r * Math.cos(t);
		xy[1] = r * Math.sin(t);
		return xy;
	}

	//print calculated values
	private void printStatistics(){
		System.out.println(N);
		System.out.println(totalSegments);
		System.out.println(singleLength * totalSegments);
		System.out.println(totalArea);
	}
	
	//calculate values for creating viewing window so that the entire snowflake is visible	
	private void calculateOrtho(){
		double rangeX = (V2[0] - V1[0]) / 3.0;
		orthoMinX = V1[0] - rangeX;
		orthoMaxX = V2[0] + rangeX;
		
		double rangeY = (V3[1] - V1[1]) / 3.0;
		orthoMinY = V1[1] - rangeY;
		orthoMaxY = V3[1] + rangeY;
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

}