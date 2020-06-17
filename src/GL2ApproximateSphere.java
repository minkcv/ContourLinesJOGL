package src;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

public class GL2ApproximateSphere extends Frame implements GLEventListener {
    static {
        // setting this true causes window events not to get sent on Linux if you run from inside Eclipse
        GLProfile.initSingleton( );
    }
    //Instance aux vars
    GLProfile glprofile=null;  //Profile
    GLCapabilities glcapabilities=null;  //Capabilities
    GLCanvas glcanvas=null; //Canvas
 
    //Constructor
    public GL2ApproximateSphere() {
    	super("GL2ApproximateSphere");
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

        setSize( 640, 640);
        setVisible( true );    	
    }
 
    public static void main( String [] args ) {
    	new GL2ApproximateSphere();
    }
 
    //Implementing GLEventListener methods
     
    @Override
    public void init( GLAutoDrawable glautodrawable ) {
		System.out.println("Entering init();");
		GL2 gl = glautodrawable.getGL().getGL2();
		gl.glClearColor(1f, 1f, 1f, 0f); //set to non-transparent black
		gl.glPolygonMode( GL2.GL_FRONT_AND_BACK, GL2.GL_LINE );
		//gl.glPointSize(4); //If you want a fat point
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
        gl2.glOrtho( -2*R, 2*R, -2*R, 2*R,-2*R,2*R );
        //Set up view
        gl2.glMatrixMode( GL2.GL_MODELVIEW );
        gl2.glLoadIdentity();

    }
    
    @Override
    public void display( GLAutoDrawable glautodrawable ) {
    	System.out.println("Entering display");
    	//Get context
    	GL2 gl2=glautodrawable.getGL().getGL2();
		gl2.glColor3f(0.7f,0.0f,0.0f);
        gl2.glClear( GL2.GL_COLOR_BUFFER_BIT );
        float width=glautodrawable.getSurfaceWidth();
        float height=glautodrawable.getSurfaceHeight();
        //Draw middle part of sphere
        double phi, theta, phir, thetar,phirplusdelta,thetarplusdelta;
        double angledelta=20, c=3.141592654/180.0;
        double x,y,z,phi0;
        phi0=60;
        gl2.glLoadIdentity();
        //Translate/Rotate (move camera)
		//gl.glTranslated(0, 5, -40);
		gl2.glRotated(-30, 1, 0, 0);
		//Quad strips
        gl2.glBegin( GL2.GL_QUAD_STRIP);
        for (phi=-phi0; phi<phi0; phi+=angledelta) {
        	phir=c*phi; //Phi in radians
        	phirplusdelta=c*(phi+angledelta);
        	for (theta=-180.0; theta<=180; theta+=angledelta) {
        		thetar=c*theta;
        		System.out.print("ph="+phi+" th="+theta);
        		x=R*Math.sin(thetar)*Math.cos(phir);
        		y=R*Math.cos(thetar)*Math.cos(phir);
        		z=R*Math.sin(phir);
        		gl2.glVertex3d(x, y, z);
        		System.out.print(" x1="+x+" y1="+y+" z1="+z);
        		//Complete quad
        		x=R*Math.sin(thetar)*Math.cos(phirplusdelta);
        		y=R*Math.cos(thetar)*Math.cos(phirplusdelta);
        		z=R*Math.sin(phirplusdelta);
        		gl2.glVertex3d(x, y, z);        		        		
        		System.out.println(" x2="+x+" y2="+y+" z2="+z);
        	}
        }
        gl2.glEnd();
        //Sphere NORTH cap
        gl2.glColor3f(0.0f, 0.7f, 0.0f);
        gl2.glBegin(GL2.GL_TRIANGLE_FAN);
        gl2.glVertex3d(0,0,R); //Initial point
        z=R*Math.sin(c*phi0);
        for (theta=-180; theta<=180; theta+=angledelta){
        	thetar=c*theta;
    		x=R*Math.sin(thetar)*Math.cos(c*phi0);
    		y=R*Math.cos(thetar)*Math.cos(c*phi0);
    		gl2.glVertex3d(x,y,z);
        }
       gl2.glEnd(); 
       
       //Sphere SOUTH cap
       gl2.glColor3f(0.0f, 0.0f, 0.7f);
       gl2.glBegin(GL2.GL_TRIANGLE_FAN);
       gl2.glVertex3d(0,0,-R); //Initial point
       z=-R*Math.sin(c*phi0);
       for (theta=-180; theta<=180; theta+=angledelta){
       	thetar=c*theta;
   		x=R*Math.sin(thetar)*Math.cos(c*phi0);
   		y=R*Math.cos(thetar)*Math.cos(c*phi0);
   		gl2.glVertex3d(x,y,z);
       }
      gl2.glEnd(); 
       

    }
    @Override
    public void dispose( GLAutoDrawable glautodrawable ) {
    }
    double R=2;
}
