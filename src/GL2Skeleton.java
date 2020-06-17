package src;

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
public class GL2Skeleton extends Frame implements GLEventListener{

	static {
		// setting this true causes window events not to get sent on Linux if you run from inside Eclipse
		// this also seems unnecessary
		GLProfile.initSingleton();
	}
	//Instance aux vars
	GLProfile glprofile=null;  //Profile
	GLCapabilities glcapabilities=null;  //Capabilities
	GLCanvas glcanvas=null; //Canvas

	//Constructor
	public GL2Skeleton() {
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
		new GL2Skeleton();
	}

	//Implementing GLEventListener methods

	@Override
	public void init( GLAutoDrawable glautodrawable ) {
		System.out.println("Entering init();");
		GL2 gl = glautodrawable.getGL().getGL2();
		gl.glClearColor(1f, 1f, 1f, 0f); //set to non-transparent black
		gl.glPointSize(24); //If you want a fat point
		//gl.glLineWidth(4); //If you want a fat line
	}

	@Override
	public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
		System.out.println("Entering reshape()");
		//Get the context
		GL2 gl2=glautodrawable.getGL().getGL2();
		//Set up projection
		gl2.glMatrixMode( GL2.GL_PROJECTION );
		gl2.glLoadIdentity();

		// coordinate system origin at lower left with width and height same as the window
		GLU glu = new GLU(); //GL utilities
		glu.gluOrtho2D( 0.0f, width, 0.0f, height );
		//Set up view
		gl2.glMatrixMode( GL2.GL_MODELVIEW );
		gl2.glLoadIdentity();

	}

	@Override
	public void display( GLAutoDrawable glautodrawable ) {
		System.out.println("Entering display");
		//Get context
		GL2 gl2=glautodrawable.getGL().getGL2();
		//gl2.glClearColor(0f, 0f, 0f, 0f); // change the background color used when clearing
		gl2.glClear( GL2.GL_COLOR_BUFFER_BIT );
		//gl2.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);
		float width=glautodrawable.getSurfaceWidth();
		float height=glautodrawable.getSurfaceHeight();
		// draw a triangle filling the window
		gl2.glLoadIdentity();
		gl2.glBegin( GL2.GL_POLYGON );
		{
			gl2.glColor3f( 1, 0, 0 );
			gl2.glVertex2f( 10, 10 );
			gl2.glColor3f( 0, 1, 0 );
			gl2.glVertex2f( width-10, 10 );
			gl2.glColor3f( 0, 0, 1 );
			gl2.glVertex2f( width / 2, height-10 );
		}
		gl2.glEnd();

	}
	@Override
	public void dispose( GLAutoDrawable glautodrawable ) {
	}

}