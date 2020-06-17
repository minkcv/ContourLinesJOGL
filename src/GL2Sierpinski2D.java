package src;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

/**
 * A minimal program that draws a fractal in an AWT Frame.
 *
 * @author Mauricio Papa
 */
public class GL2Sierpinski2D extends Frame implements GLEventListener{

    static {
        // setting this true causes window events not to get sent on Linux if you run from inside Eclipse
        GLProfile.initSingleton( );
    }
    //Instance aux vars
    GLProfile glprofile=null;  //Profile
    GLCapabilities glcapabilities=null;  //Capabilities
    GLCanvas glcanvas=null; //Canvas
 
    //Constructor
    public GL2Sierpinski2D() {
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
    	new GL2Sierpinski2D();
    }
 
    //Implementing GLEventListener methods
     
    @Override
    public void init( GLAutoDrawable glautodrawable ) {
		System.out.println("Entering init();");
		GL2 gl = glautodrawable.getGL().getGL2();
		gl.glClearColor(1f, 1f, 1f, 0f); //set to non-transparent black
    }
    
    @Override
    public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
    	System.out.println("Entering reshape()");
    	//Get the context
    	GL2 gl2=glautodrawable.getGL().getGL2();
    	//Set up projection
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		//this glOrtho call sets up a 24x24 unit plane with a parallel projection. 
		gl2.glOrtho(-12,12,-12,12,0,10);
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();

    }
    
    @Override
    public void display( GLAutoDrawable glautodrawable ) {
    	System.out.println("Entering display");
		//We always need a context to draw
		GL2 gl = glautodrawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
		//Set a color (redish - no other components)
		gl.glColor3f(0.3f,0.0f,0.0f);
		//We need a random number generator
		Random random=new Random();
		//Pick random point inside the triangle
		double[] p0={0,-3,0};
		double[] p1=new double[3];
		int randv;//Random vertex index
		//Main loop
		gl.glBegin(GL.GL_POINTS);
		for (int i=0; i< npoints; i++)
		{
			//Select random vertex
			randv=random.nextInt(3);
			//Calculate middle point to random vertex
			for (int j=0; j<3; j++) p1[j]=(p0[j]+vertices[randv][j])/2;
			//Draw middle point
			gl.glVertex3d( p1[0], p1[1], p1[2]);
			//p0=p1
			for (int j=0; j<3; j++) p0[j]=p1[j];
		}
		gl.glEnd();

    }
    @Override
    public void dispose( GLAutoDrawable glautodrawable ) {
    }

    
    //Triangle coordinates
	double[][] vertices={{-10,-10,0},{10,-10,0},{0,10,0}}; //Define triangle coordinates
	int npoints=500; //Number of points to calculate

}