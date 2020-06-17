package src;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
//These are needed for the events
import java.awt.Frame;
import java.awt.event.*;
/**
 * 
 * Minimalistic JOGL skeleton with Mouse and Keyboard Listeners.
 * 
 * Default values:
 * OpenGL places a camera at the origin in object space pointing in the negative z direction.
 * The default viewing volume is a box centered at the origin with a side of  length 2
 * @author papama
 *
 * 
 */

public class GL2SkeletonKeyMouse extends Frame implements GLEventListener, MouseListener, MouseWheelListener, MouseMotionListener, KeyListener {
    static {
        // setting this true causes window events not to get sent on Linux if you run from inside Eclipse
        GLProfile.initSingleton( );
    }
    //Instance aux vars
    GLProfile glprofile=null;  //Profile
    GLCapabilities glcapabilities=null;  //Capabilities
    GLCanvas glcanvas=null; //Canvas
	
	public GL2SkeletonKeyMouse(){
    	super("GL2Skeleton");
        glprofile = GLProfile.getDefault();
        glcapabilities = new GLCapabilities( glprofile );
        glcanvas = new GLCanvas( glcapabilities );

        glcanvas.addGLEventListener( this);
        glcanvas.addKeyListener(this);
		glcanvas.addMouseListener(this);
		glcanvas.addMouseMotionListener(this);
		glcanvas.addMouseWheelListener(this);
		
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
	
	@Override
	/**
	 * Called by the drawable to initiate OpenGL rendering by the client. After all GLEventListeners have been 
	 * notified of a display event, the drawable will swap its buffers if setAutoSwapBufferMode is enabled. 
	 */
	public void display(GLAutoDrawable arg0) {
		System.out.println("Entering display();");
		//We always need a context to draw
		GL2 gl = arg0.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
		//Set a color (redish - no other components)
		gl.glColor3f(0.3f,0.0f,0.0f);
		//Define a primitive -  A polygon in this case
		gl.glBegin(GL2.GL_POLYGON);
			gl.glVertex2i( 110, 10);
			gl.glVertex2i( 110,470);
			gl.glVertex2i(580,470);
			gl.glVertex2i(580, 10);
		gl.glEnd();
	}


	@Override
	/***
	 * Called by the drawable immediately after the OpenGL context is initialized. Can be used to 
	 * perform one-time OpenGL initialization such as setup of lights and display lists. Note that 
	 * this method may be called more than once if the underlying OpenGL context for the GLAutoDrawable 
	 * is destroyed and recreated, for example if a GLCanvas is removed from the widget hierarchy and later 
	 * added again.  
	 * 
	 * Grab the gl context from the GLAutoDrawable and set the clear color
	 */
	public void init(GLAutoDrawable arg0) {
		System.out.println("Entering init();");
		GL2 gl = arg0.getGL().getGL2();
		gl.glClearColor(0f, 0f, 0f, 0f); //set to non-transparent black
	}

	@Override
	/**
	 * Called by the drawable during the first repaint after the component has been resized. The client 
	 * can update the viewport and view volume of the window appropriately, for example by a call to 
	 * GL.glViewport(int, int, int, int); note that for convenience the component has already called 
	 * glViewport(x, y, width, height) when this method is called, so the client may not have to do 
	 * anything in this method. 
	 */
	public void reshape(GLAutoDrawable arg0, int x, int y, int width,
			int height) {
		System.out.println("Entering reshape();");
		GL2 gl = arg0.getGL().getGL2();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		//this glOrtho call sets up a 640x480 unit plane with a parallel projection. 
		gl.glOrtho(0,640,0,480,0,10);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	
	
	public static void main(String[] args) {
		new GL2SkeletonKeyMouse();	
		}
	/**
	 * MouseListener Events (callbacks)
	 * 
	 */
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		System.out.print("mouseClicked "+arg0.getClickCount()+" times");
		switch(arg0.getButton())
		{
		case MouseEvent.BUTTON1: System.out.println("BUTTON1"); break;
		case MouseEvent.BUTTON2: System.out.println("BUTTON2"); break;
		case MouseEvent.BUTTON3: System.out.println("BUTTON3"); break;
		default: System.out.println("but no button was pressed !!!");
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("mouseEntered event (pointer is now inside canvas)");
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("mouseExited event (pointer is now outside canvas)");
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		System.out.print("mousePressed event ");
		int bcode=arg0.getButton();
		System.out.println("BUTTON CODE IS "+bcode);
		switch(arg0.getButton())
		{
		case MouseEvent.BUTTON1: System.out.println("BUTTON1"); break;
		case MouseEvent.BUTTON2: System.out.println("BUTTON2"); break;
		case MouseEvent.BUTTON3: System.out.println("BUTTON3"); break;
		default: System.out.println("but no button was pressed !!!");
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		System.out.print("mouseReleased event ");
		switch(arg0.getButton())
		{
		case MouseEvent.BUTTON1: System.out.println("BUTTON1"); break;
		case MouseEvent.BUTTON2: System.out.println("BUTTON2"); break;
		case MouseEvent.BUTTON3: System.out.println("BUTTON3"); break;
		default: System.out.println("but no button was pressed !!!");
		}

	}

	/**
	 * MouseWheel Events (callbacks)
	 * 
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("mouseWheelMoved event "+arg0.getWheelRotation()+" clicks");
	}

	/**
	 * MouseMotion Events (callbacks)
	 * 
	 */
	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		int x,y; //Read mouse position relative to canvas
		x=arg0.getX();
		y=arg0.getY();
		System.out.println("mouseDragged event ("+x+","+y+")");

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		int x,y; //Read mouse position relative to canvas
		x=arg0.getX();
		y=arg0.getY();
		//Uncomment next line if needed (too noisy)
		//System.out.println("mouseMoved event ("+x+","+y+")");
	}

	/**
	 * Key Events (callbacks)
	 * 
	 */
	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("keyPressed event "+arg0.toString());
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("keyReleased event "+arg0.toString());
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("keyTyped event "+arg0.toString());
		if (arg0.getKeyChar()=='A') System.out.println("Got an A");
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}
}
