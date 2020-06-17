package src;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.DoubleBuffer;

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
 * Draw color cube
 * 
 * Default values:
 * OpenGL places a camera at the origin in object space pointing in the negative z direction.
 * The default viewing volume is a box centered at the origin with a side of  length 2
 * @author papama
 *
 */

public class GL2ColorCubePerspective extends JFrame implements GLEventListener, KeyListener {
	private GLCanvas canvas;
	public GL2ColorCubePerspective(){
		super("GLColorCube");
		//Need a canvas to draw
		canvas = new GLCanvas();
		//Actual window size (monitor)
		canvas.setPreferredSize(new Dimension(640,640));
		//Callbacks
		canvas.addGLEventListener(this);
		canvas.addKeyListener(this);
		//Add canvas to the Frame
		getContentPane().add(canvas);
	}
	
	@Override
	/**
	 * Called by the drawable to initiate OpenGL rendering by the client. After all GLEventListeners have been 
	 * notified of a display event, the drawable will swap its buffers if setAutoSwapBufferMode is enabled. 
	 */
	public void display(GLAutoDrawable arg0) {
		//We always need a context to draw
		GL2 gl = arg0.getGL().getGL2();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		//Set-up the camera
		System.out.println(targetPos[0]);
		System.out.println(targetPos[1]);
		System.out.println(targetPos[2]);
		glu.gluLookAt(eyePos[0], eyePos[1], eyePos[2], targetPos[0], targetPos[1], targetPos[2], upVector[0], upVector[1], upVector[2]);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glPushMatrix();
		//gl.glTranslated(0, 0, eyePos[2]);
		//Define vertices position and color
		double halfSide=0.5;
		double cubeVertex[][]={{halfSide, halfSide, halfSide},
							   {halfSide, halfSide, -halfSide},
							   {halfSide, -halfSide, halfSide},
							   {halfSide, -halfSide, -halfSide},
							   {-halfSide, halfSide, halfSide},
							   {-halfSide, halfSide, -halfSide},
							   {-halfSide, -halfSide, halfSide},
							   {-halfSide, -halfSide, -halfSide}};
		double vertexColor[][]={{1,1,1}, {1,1,0}, {1,0,1},{1,0,0},
								{0,1,1}, {0,1,0}, {0,0,1},{0,0,0}};
		//Rotate
		//gl.glTranslated(0, 0, -3.0);
		//gl.glRotated(90,0,0,1);
		//gl.glRotated(45, 0, 1, 0);
		//gl.glRotated(45, 1, 0, 0);
		//gl.glRotated(-90, 0, 1, 0);
		
		//Isometric
		//gl.glRotated(45, 1, 0, 0);
		//gl.glRotated(45, 0, 1, 0);
		//Bottom face
		gl.glBegin(GL2.GL_POLYGON);
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[6]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[6]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[7]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[7]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[3]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[3]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[2]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[2]));
		gl.glEnd();
		//Back face
		gl.glBegin(GL2.GL_POLYGON);
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[1]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[1]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[3]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[3]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[7]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[7]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[5]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[5]));
		gl.glEnd();
		//Top face
		gl.glBegin(GL2.GL_POLYGON);
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[0]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[0]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[1]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[1]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[5]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[5]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[4]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[4]));
		gl.glEnd();
		//Front face
		gl.glBegin(GL2.GL_POLYGON);
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[0]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[0]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[4]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[4]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[6]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[6]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[2]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[2]));
		gl.glEnd();
		//Left face
		gl.glBegin(GL2.GL_POLYGON);
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[4]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[4]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[5]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[5]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[7]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[7]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[6]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[6]));
		gl.glEnd();
		//Right face
		gl.glBegin(GL2.GL_POLYGON);
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[2]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[2]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[3]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[3]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[1]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[1]));
			gl.glColor3dv(DoubleBuffer.wrap(vertexColor[0]));
			gl.glVertex3dv(DoubleBuffer.wrap(cubeVertex[0]));
		gl.glEnd();
		//Restore matrix
		gl.glPopMatrix();
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
		glu=new GLU();
		gl.glClearColor(1f, 1f, 1f, 0f); //set to non-transparent black
		//Enable depth test
		gl.glEnable(GL2.GL_DEPTH_TEST);
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
		//Use perspective 
//		gl.glOrtho(-1.5,1.5,-1.5,1.5,-10,10);
		glu.gluPerspective(fov, aspectRatio, zNear, zFar); //Always done with in projection mode
		
	}

	
	public static void createAndShowGUI(){
		GL2ColorCubePerspective s = new GL2ColorCubePerspective();
		s.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		s.pack();
		s.setVisible(true);
	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				createAndShowGUI();
			}
		});
	}
	//Camera set-up
	GLU glu;
	double fov=25;
	double aspectRatio=1;
	double zNear=0.1;
	double zFar=100;
	double[] eyePos={3,3,3};
	//double[] eyePos={3,3,3};
	
	double[] targetPos={0,0,0};
	double[] upVector={0,1,0};
	double dIncrement=.2;
	double aIncrement=0.1;
	double eyeDist=Math.sqrt(eyePos[0]*eyePos[0]+eyePos[1]*eyePos[1]+eyePos[2]*eyePos[2]);
	double phi=0;
	double theta=90.0/180.0*Math.PI;
	@Override
	public void keyPressed(KeyEvent ke) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent ke) {
		// TODO Auto-generated method stub
		System.out.println(ke.getKeyChar());
		char key=ke.getKeyChar();
		boolean updateSpherical=false, updateCartesian=false;
		
		if (key=='x')      {eyePos[0]+=dIncrement; updateSpherical=true;}
		else if (key=='X') {eyePos[0]-=dIncrement; updateSpherical=true;}
		else if (key=='y') {eyePos[1]+=dIncrement; updateSpherical=true;}
		else if (key=='Y') {eyePos[1]-=dIncrement; updateSpherical=true;}
		else if (key=='z') {eyePos[2]+=dIncrement; updateSpherical=true;}
		else if (key=='Z') {eyePos[2]-=dIncrement; updateSpherical=true;}
		else if (key=='r') {eyeDist+=dIncrement; updateCartesian=true;}
		else if (key=='R') {eyeDist-=dIncrement; updateCartesian=true;} 
		else if (key=='t') {theta+=aIncrement; updateCartesian=true;}
		else if (key=='T') {theta-=aIncrement; updateCartesian=true;}
		else if (key=='p') {phi+=aIncrement; updateCartesian=true;}
		else if (key=='P') {phi-=aIncrement; updateCartesian=true;}
		//Update spherical
		if (updateSpherical)
		{
			eyeDist=Math.sqrt(eyePos[0]*eyePos[0]+eyePos[1]*eyePos[1]+eyePos[2]*eyePos[2]);
			theta=Math.atan2(eyePos[1], eyePos[0]);
			phi=Math.acos(eyePos[2]/eyeDist);
			updateSpherical=false;
		}
		if (updateCartesian)
		{
			eyePos[0]=eyeDist*Math.cos(theta)*Math.sin(phi);
			eyePos[1]=eyeDist*Math.sin(theta)*Math.sin(phi);
			eyePos[2]=eyeDist*Math.cos(phi);
			updateCartesian=false;
		}
		//Redisplay
		canvas.display();
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}
	

}
