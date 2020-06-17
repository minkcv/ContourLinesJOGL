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

/**
 * A minimal program that draws with JOGL in an AWT Frame.
 *
 * @author Mauricio Papa
 */
public class GL2SkeletonConstantAspectRatio extends Frame implements GLEventListener{

    static {
        // setting this true causes window events not to get sent on Linux if you run from inside Eclipse
        GLProfile.initSingleton( );
    }
    //Instance aux vars
    GLProfile glprofile=null;  //Profile
    GLCapabilities glcapabilities=null;  //Capabilities
    GLCanvas glcanvas=null; //Canvas
    double AR; //We'll use this for the aspect ratio
 
    //Constructor
    public GL2SkeletonConstantAspectRatio() {
    	super("GL2Skeleton");
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
    	new GL2SkeletonConstantAspectRatio();
    }
 
    //Implementing GLEventListener methods
     
    @Override
    public void init( GLAutoDrawable glautodrawable ) {
		System.out.println("Entering init();");
		GL2 gl = glautodrawable.getGL().getGL2();
		gl.glClearColor(.8f, .8f, .8f, 0f); //set to non-transparent black
    }
    
    @Override
    public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
		System.out.println("Entering reshape(); x="+x+" y="+y+" width="+width+" height="+height);
    	//Get the context
    	GL2 gl=glautodrawable.getGL().getGL2();
    	//Set up projection
        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity();
		//this glOrtho call sets up a 640x480 unit plane with a parallel projection. 
		gl.glOrtho(0,640,0,480,0,10);
		//Handle aspect ratio
		AR= 640.0/480.0;
		if (AR*height<width) gl.glViewport(x, y, (int) (AR*height), height);
		else gl.glViewport(x, y, width, (int) (width/AR));
		//gl.glViewport(x, y, (int) (AR*height), height);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
    }
    
    @Override
    public void display( GLAutoDrawable glautodrawable ) {
    	System.out.println("Entering display");
    	//Get context
    	GL2 gl=glautodrawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		//Set a color (redish - no other components)
		gl.glColor3f(0.3f,0.0f,0.0f);
		//Define a primitive -  A polygon in this case
		gl.glBegin(GL2.GL_POLYGON);
			gl.glVertex2i( 100, 20);
			gl.glVertex2i( 100,460);
			gl.glVertex2i(540,460);
			gl.glVertex2i(540, 20);
		gl.glEnd();
    }
    @Override
    public void dispose( GLAutoDrawable glautodrawable ) {
    }

}