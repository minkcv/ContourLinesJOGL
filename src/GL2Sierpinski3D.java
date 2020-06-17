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
import java.util.Random;

/**
 * A minimal program that draws with JOGL in an AWT Frame.
 *
 * @author Mauricio Papa
 */
public class GL2Sierpinski3D extends Frame implements GLEventListener{

    static {
        // setting this true causes window events not to get sent on Linux if you run from inside Eclipse
        GLProfile.initSingleton(  );
    }
    //Instance aux vars
    GLProfile glprofile=null;  //Profile
    GLCapabilities glcapabilities=null;  //Capabilities
    GLCanvas glcanvas=null; //Canvas
    //Aux vars
	//Arbitrary initial point (must be inside tetrahedron)
	private double[] p0={0,7,0};
	private double[][] vertices=new double[4][3];
	//Recursion depth
	double side_length=15;
	double circumradius;
	double inradius;
	int npoints=50000; //Number of points to calculate

    //Constructor
    public GL2Sierpinski3D() {
    	super("GL2Sierpinski3D");
        glprofile = GLProfile.getDefault();
        glcapabilities = new GLCapabilities( glprofile );
        glcanvas = new GLCanvas( glcapabilities );
        glcanvas.addGLEventListener( this);
        //Setup
		//Define tetrahedron
		circumradius=0.25*Math.sqrt(6)*side_length;
		inradius=Math.sqrt(6)*side_length/12;
		//Closes to viewer
		vertices[0][0]=0;
		vertices[0][1]=0;
		vertices[0][2]=circumradius;
		//Back triangle z=-inradius
		vertices[1][0]=0;
		vertices[1][1]=Math.sqrt(3)*side_length/3;
		vertices[1][2]=-inradius;
		vertices[2][0]=-side_length/2;
		vertices[2][1]=-Math.sqrt(3)*side_length/6;
		vertices[2][2]=-inradius;
		vertices[3][0]=side_length/2;
		vertices[3][1]=-Math.sqrt(3)*side_length/6;
		vertices[3][2]=-inradius;

		//Windowing
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
    	new GL2Sierpinski3D();
    }
 
    //Implementing GLEventListener methods
     
    @Override
    public void init( GLAutoDrawable glautodrawable ) {
		System.out.println("Entering init();");
		GL2 gl = glautodrawable.getGL().getGL2();
		gl.glClearColor(1f, 1f, 1f, 0f); //set to non-transparent black
		//gl.glPointSize(4); //If you want a fat point
		//gl.glLineWidth(4); //If you want a fat line
    }
    
    @Override
    public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
    	System.out.println("Entering reshape()");
    	//Get the context
    	GL2 gl=glautodrawable.getGL().getGL2();
    	//Set up projection
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		//this glOrtho call sets up a 24x24 unit plane with a parallel projection. 
		gl.glOrtho(-16,16,-16,16,-16,16);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

    }
    
    @Override
    public void display( GLAutoDrawable glautodrawable ) {
		System.out.print(".");
		//We always need a context to draw
		GL2 gl = glautodrawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
		//Set a color (redish - no other components)
		gl.glColor3f(0.3f,0.0f,0.0f);
		gl.glRotated(30, 1, 0, 0);
		//We need a random number generator
		Random random=new Random();
		//Pick random point inside the triangle
		double[] p0={0,-3,0};
		double[] p1=new double[3];
		int randv;//Random vertex index
		//Main loop
		gl.glBegin(GL2.GL_POINTS);
		for (int i=0; i< npoints; i++)
		{
			//Select random vertex
			randv=random.nextInt(4);
			//Calculate middle point to random vertex
			for (int j=0; j<3; j++) p0[j]=(p0[j]+vertices[randv][j])/2;
			//Pick a color based on point coordinates
			gl.glColor3d((p0[0]+10.0)/20.0, (p0[1]+10.0)/20.0, (p0[2]+10)/20.0);
			//Draw middle point
			gl.glVertex3d( p0[0], p0[1], p0[2]);
		}
		gl.glEnd();

    }
    @Override
    public void dispose( GLAutoDrawable glautodrawable ) {
    }

}