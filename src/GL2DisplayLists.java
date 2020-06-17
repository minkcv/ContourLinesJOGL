package src;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

/**
 * 
 * Minimalistic JOGL skeleton.
 * 
 * Default values:
 * OpenGL places a camera at the origin in object space pointing in the negative z direction.
 * The default viewing volume is a box centered at the origin with a side of  length 2
 * @author papama
 *
 */

public class GL2DisplayLists extends Frame implements GLEventListener {
    static {
        // setting this true causes window events not to get sent on Linux if you run from inside Eclipse
        GLProfile.initSingleton(  );
    }
    //Instance aux vars
    GLProfile glprofile=null;  //Profile
    GLCapabilities glcapabilities=null;  //Capabilities
    GLCanvas glcanvas=null; //Canvas
	
	public GL2DisplayLists(){
		super("DisplayLists");
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

        setSize( 800, 800 );
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
		//Draw
		long startTime, stopTime;
		startTime=System.nanoTime();
		for (int i=0; i<8; i++)
			for (int j=0; j<8; j++)
			{
				gl.glPushMatrix();
				gl.glTranslated(i*100, j*100, 0);
				//Draw octagon
				if (useDisplayList) gl.glCallList(octagonDisplayID);
				else
				{
					defineOctagon(gl);
				}
				gl.glPopMatrix();
				
			}
		stopTime=System.nanoTime();
		System.out.println("Display time: "+(stopTime-startTime)/1000+" us");
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
		gl.glClearColor(1f, 1f, 1f, 0f); //set to non-transparent white
		if (useDisplayList)
		{
			octagonDisplayID=gl.glGenLists(1); //We only need one display list
			gl.glNewList(octagonDisplayID, GL2.GL_COMPILE); //We are only creating one - this zero
			 defineOctagon(gl);
			gl.glEndList();
		}
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
		gl.glOrtho(0,800,0,800,0,10);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	
	
	public static void main(String[] args) {
		new GL2DisplayLists();}

	public void defineOctagon(GL2 gl)
	{
		gl.glBegin(GL2.GL_POLYGON);
			gl.glColor3d(0, 0, 0.125);
			gl.glVertex2d(30, 0);
			gl.glColor3d(0, 0, 2*0.125);
			gl.glVertex2d(60, 0);
			gl.glColor3d(0, 0, 3*0.125);
			gl.glVertex2d(90, 30);
			gl.glColor3d(0, 0, 4*0.125);
			gl.glVertex2d(90, 60);
			gl.glColor3d(0, 0, 5*0.125);
			gl.glVertex2d(60, 90);
			gl.glColor3d(0, 0, 6*0.125);
			gl.glVertex2d(30, 90);
			gl.glColor3d(0, 0, 7*0.125);
			gl.glVertex2d(0, 60);
			gl.glColor3d(0, 0, 8*0.125);
			gl.glVertex2d(0, 30);
		gl.glEnd();
	}
	
	boolean useDisplayList=false ;
	int octagonDisplayID;

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}
}
