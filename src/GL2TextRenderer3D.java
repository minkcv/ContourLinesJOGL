package src;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.awt.TextRenderer;

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A minimal program that draws with JOGL in an AWT Frame.
 *
 * @author Mauricio Papa
 */
public class GL2TextRenderer3D extends Frame implements GLEventListener{

    static {
        // setting this true causes window events not to get sent on Linux if you run from inside Eclipse
        GLProfile.initSingleton(  );
    }
    //Instance aux vars
    GLProfile glprofile=null;  //Profile
    GLCapabilities glcapabilities=null;  //Capabilities
    GLCanvas glcanvas=null; //Canvas
    //Aux var
    TextRenderer renderer;
 
    //Constructor
    public GL2TextRenderer3D() {
    	super("GL2TextRenderer3D");
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
    	new GL2TextRenderer3D();
    }
 
    //Implementing GLEventListener methods
     
    @Override
    public void init( GLAutoDrawable glautodrawable ) {
		System.out.println("Entering init();");
		GL2 gl = glautodrawable.getGL().getGL2();
		gl.glClearColor(1f, 1f, 1f, 1f); //set to non-transparent black
		gl.glEnable(GL2.GL_DEPTH_TEST);
		//Text renderer
		renderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 36));
		// optionally set the color
	    renderer.setColor(0.0f, 0.0f, 1.0f, 0.4f);
	    renderer.setSmoothing(true);
    }
    
    @Override
    public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
    	System.out.println("Entering reshape()");
    	//Get the context
    	GL2 gl=glautodrawable.getGL().getGL2();
    	//Set up projection
        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity();

        // coordinate system origin at lower left with width and height same as the window
		gl.glOrtho(0,800,0,600,-200,200);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

    }
    
    @Override
    public void display( GLAutoDrawable glautodrawable ) {
    	System.out.println("Entering display");
    	//Get context
    	GL2 gl=glautodrawable.getGL().getGL2();
        gl.glClear( GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        //gl.glRotatef(45,0,1,0);
		//Add text 3D
		renderer.begin3DRendering();
	    renderer.draw3D("Mauricio Papa 3D", 300, 250, 52, 1);
		renderer.end3DRendering();
		//Rectangle 1 (redish)
		gl.glColor3f(0.4f,0.4f,0.0f);
		//Define a primitive -  A polygon in this case
		gl.glBegin(GL2.GL_POLYGON);
			gl.glVertex3d( 130, 40,50);
			gl.glVertex3d( 130,440,50);
			gl.glVertex3d(540,440,50);
			gl.glVertex3d(540, 40,50);
		gl.glEnd();
		//Add text 2D
		//renderer.beginRendering(glautodrawable.getWidth(), glautodrawable.getHeight());
		//renderer.draw("Mauricio Papa 2D", 100, 100);
		//renderer.draw("Mauricio Papa 2D", 150, 150);
		//renderer.draw("Mauricio Papa 2D", 200, 200);
		//renderer.endRendering();

    }
    @Override
    public void dispose( GLAutoDrawable glautodrawable ) {
    }

}