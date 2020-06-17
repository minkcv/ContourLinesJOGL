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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.DoubleBuffer;
import java.util.Random;

/**
 * A minimal program that draws a fractal in an AWT Frame.
 *
 * @author Mauricio Papa
 */
public class GL2Sierpinski2DPolygon extends Frame implements GLEventListener{

    static {
        // setting this true causes window events not to get sent on Linux if you run from inside Eclipse
        GLProfile.initSingleton( );
    }
    //Instance aux vars
    GLProfile glprofile=null;  //Profile
    GLCapabilities glcapabilities=null;  //Capabilities
    GLCanvas glcanvas=null; //Canvas
 
    //Constructor
    public GL2Sierpinski2DPolygon() {
    	super("GL2Sierpinski");
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

        setSize( 640, 480 );
        setVisible( true );    	
    }
 
    public static void main( String [] args ) {
    	new GL2Sierpinski2DPolygon();
    }
 
    //Implementing GLEventListener methods
     
    @Override
    public void init( GLAutoDrawable glautodrawable ) {
		System.out.println("Entering init();");
		GL2 gl = glautodrawable.getGL().getGL2();
		//gl.glShadeModel(GL.GL_FLAT);
		gl.glClearColor(.8f, .8f, .8f, 0f); //set to non-transparent black
		//gl.glPointSize(4); //If you want a fat point
    }
    
    @Override
    public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
    	System.out.println("Entering reshape()");
    	//Get the context
    	gl2=glautodrawable.getGL().getGL2();
    	//Set up projection
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		gl2.glOrtho(0,10,0,10,0,10);
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();

    }
    
    @Override
    public void display( GLAutoDrawable glautodrawable ) {
		System.out.println("Entering display();");
		//We always need a context to draw
		gl2 = glautodrawable.getGL().getGL2();
		gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
		//Set a color (blue - no other components)
		gl2.glColor3f(0.0f,0.5f,0.5f);
		//Process triangles
		//gl.glBegin(GL.GL_TRIANGLES);
		processTriangle(V1,V2,V3,N);
		//gl.glEnd();
    }
    @Override
    public void dispose( GLAutoDrawable glautodrawable ) {
    }

    //Processing triangles
	private void processTriangle(double[] v1, double[] v2, double[] v3, int n) {
		if (n>0) //Recurse
		{
			//Coordinates for midle points
			double[] m1=new double[2];
			double[] m2=new double[2];
			double[] m3=new double[2];
			for (int i=0; i<2; i++)
			{
				m1[i]=(v1[i]+v2[i])/2;
				m2[i]=(v2[i]+v3[i])/2;
				m3[i]=(v1[i]+v3[i])/2;	
			}
			//Recurse
			processTriangle(m1,v2,m2,n-1);
			processTriangle(v1,m1,m3,n-1);
			processTriangle(m3,m2,v3,n-1);
		}
		else drawTriangle(v1, v2, v3); //Draw
		
	}

	private void drawTriangle(double[] v1, double[] v2, double[] v3) {
		//Draw triangle
		gl2.glBegin(GL.GL_TRIANGLES);
		gl2.glVertex2dv(DoubleBuffer.wrap(v1));
		gl2.glVertex2dv(DoubleBuffer.wrap(v2));
		gl2.glVertex2dv(DoubleBuffer.wrap(v3));
		gl2.glEnd();
	}
    
    
    
	//Reference to GL
	GL2 gl2;
	//Triangle coordinates
	double[] V1={1,1};
	double[] V2={9,1};
	double[] V3={5,9};
	//Recursion depth
	int N=3;

}