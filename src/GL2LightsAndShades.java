package src;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.media.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.gl2.GLUT;

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

public class GL2LightsAndShades extends JFrame implements GLEventListener {
	private GLCanvas canvas;
	GLUT glut=new GLUT();
	GLU glu=new GLU();
	/* initial tetrahedron */
	private static float[][] v = {
		{0.0f, 0.0f, 1.0f}, 
		{0.0f, 0.942809f, -0.33333f},
		{-0.816497f, -0.471405f, -0.333333f},
		{0.816497f, -0.471405f, -0.333333f}
	};
	private static float[] theta = {0.0f, 0.0f, 0.0f};
	private static int n;
	public GL2LightsAndShades(){
		super("GLSkeleton");
		//Need a canvas to draw
		canvas = new GLCanvas();
		//Actual window size (monitor)
		canvas.setSize(640,480);
		//Callbacks
		canvas.addGLEventListener(this);
		//Add canvas to the Frame
		getContentPane().add(canvas);

	}

	@Override
	/**
	 * Called by the drawable to initiate OpenGL rendering by the client. After all GLEventListeners have been 
	 * notified of a display event, the drawable will swap its buffers if setAutoSwapBufferMode is enabled. 
	 */
	public void display(GLAutoDrawable arg0) {
		System.out.println("Entering display();");
		GL2 gl = arg0.getGL().getGL2();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		// mode 0
		tetrahedron(gl, n, 0);
		// mode 1
		gl.glTranslatef(-2.0f, 0.0f, 0.0f);
		tetrahedron(gl, n, 1);
		// mode 2
		gl.glTranslatef(4.0f, 0.0f, 0.0f);
		tetrahedron(gl, n, 2);

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
		float[] mat_specular   = {1.0f, 1.0f, 1.0f, 1.0f};
		float[] mat_diffuse    = {1.0f, 0.0f, 0.0f, 1.0f};
		float[] mat_ambient    = {1.0f, 0.0f, 0.0f, 1.0f};
		float   mat_shininess  = 64.0f;
		float[] light_ambient  = {0.0f, 0.0f, 0.0f, 1.0f};
		float[] light_diffuse  = {1.0f, 1.0f, 1.0f, 1.0f};
		float[] light_specular = {1.0f, 1.0f, 1.0f, 1.0f};
		float[] light_position = {0.0f, 2.0f, 2.0f, 0.0f};
		/* set up ambient, diffuse, and specular components for light 0 */
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, FloatBuffer.wrap(light_ambient));
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, FloatBuffer.wrap(light_diffuse));
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, FloatBuffer.wrap(light_specular));
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, FloatBuffer.wrap(light_position));
		/** define material properties for front face of all polygons
		 * Default values are:
		 * Ambient: (0.2,0.2,.0.2,1.0)
		 * Diffuse: (0.8,0.8,0.8,1.0)
		 * Specular: (0,0,0,1)
		 * Shininess: 0 [max is 128 - it is really controlling rate of decay]
		 */
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, FloatBuffer.wrap(mat_specular));
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, FloatBuffer.wrap(mat_ambient));
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, FloatBuffer.wrap(mat_diffuse));
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, mat_shininess);
		/** Color tracking
		 * 
		 */
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);


		gl.glShadeModel(GL2.GL_SMOOTH); /* enable smooth shading, alternative is GL_FLAT */
		gl.glEnable(GL2.GL_LIGHTING); /* enable lighting */
		gl.glEnable(GL2.GL_LIGHT0);  /* enable light 0 */
		gl.glEnable(GL2.GL_DEPTH_TEST); /* enable z buffer */
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glColor3f(0.0f, 1.0f, 0.0f);

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
		GL2 gl = arg0.getGL().getGL2();

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		if (width <= height)
			gl.glOrtho(-4.0, 4.0, -4.0 * (double) height / (double) width, 4.0 * (double) height / (double) width, -10.0, 10.0);
		else
			gl.glOrtho(-2.5 * (double) width / (double) height, 2.5 * (double) width / (double) height, -2.5, 2.5, -10.0, 10.0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
	}


	public static void createAndShowGUI(){
		GL2LightsAndShades s = new GL2LightsAndShades();
		s.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		s.pack();
		s.setVisible(true);
	}

	public static void main(String[] args) {
		n = Integer.parseInt(args[0]);

		javax.swing.SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				createAndShowGUI();
			}
		});
	}
	/* apply triangle subdivision to faces of tetrahedron */
	private void tetrahedron(GL2 gl, int n, int mode)
	{
		divide_triangle(gl, v[0], v[1], v[2], n, mode);
		divide_triangle(gl, v[3], v[2], v[1], n, mode);
		divide_triangle(gl, v[0], v[3], v[1], n, mode);
		divide_triangle(gl, v[0], v[2], v[3], n, mode);
	}
	/* Triangle subdivision using vertex numbers. Right-hand rule
	applied to create outward-pointing faces. */
	private void divide_triangle(GL2 gl, float[] a, float[] b, float[] c, int m, int mode)
	{
		float[] v1 = new float[3];
		float[] v2 = new float[3];
		float[] v3 = new float[3];

		if (m > 0)
		{
			for(int j = 0; j < 3; j++) v1[j] = a[j] + b[j];
			normal(v1);
			for(int j = 0; j < 3; j++) v2[j] = a[j] + c[j];
			normal(v2);
			for(int j = 0; j < 3; j++) v3[j] = b[j] + c[j];
			normal(v3);
			divide_triangle(gl, a, v1, v2, m-1, mode);
			divide_triangle(gl, c, v2, v3, m-1, mode);
			divide_triangle(gl, b, v3, v1, m-1, mode);
			divide_triangle(gl, v1, v3, v2, m-1, mode);
		}
		else triangle(gl, a, b, c, mode); /* draw triangle at end of recursion */
	}
	/* normalize a vector */
	private void normal(float[] p)
	{
		float d = 0.0f;

		for (int i = 0; i < 3; i++) d += p[i] * p[i];
		d = (float) Math.sqrt(d);
		if(d > 0.0f) for (int i = 0; i < 3; i++) p[i] /= d;
	}
	/* display one triangle using a line loop for wire frame, a single
	normal for constant shading, or three normals for interpolative
	shading */
	private void triangle(GL2 gl, float[] a, float[] b, float[] c, int mode)
	{
		if (mode == 0)
			gl.glBegin(GL.GL_LINE_LOOP);
		else
			gl.glBegin(GL2.GL_POLYGON);
		if (mode == 1) gl.glNormal3fv(FloatBuffer.wrap(a));
		if (mode == 2) gl.glNormal3fv(FloatBuffer.wrap(a));
		gl.glVertex3fv(FloatBuffer.wrap(a));
		if (mode == 2) gl.glNormal3fv(FloatBuffer.wrap(b));
		gl.glVertex3fv(FloatBuffer.wrap(b));
		if (mode == 2) gl.glNormal3fv(FloatBuffer.wrap(c));
		gl.glVertex3fv(FloatBuffer.wrap(c));
		gl.glEnd();
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {}

}
