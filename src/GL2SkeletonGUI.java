package src;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.opengl.util.Animator;

/**
 * 
 * Minimalistic JOGL skeleton with menus.
 * 
 * Default values:
 * OpenGL places a camera at the origin in object space pointing in the negative z direction.
 * The default viewing volume is a box centered at the origin with a side of  length 2
 * @author papama
 *
 */

public class GL2SkeletonGUI extends JFrame implements GLEventListener, ChangeListener, ActionListener, ItemListener {
	private GLJPanel canvas;
	//Rectangle initial color
	double RED =0.5;
	double GREEN=0.5;
	double BLUE=0.5;
	JSlider redSlider, greenSlider, blueSlider;
	
	public GL2SkeletonGUI(){
		super("GLSkeletonGUI");
		//Need a canvas to draw
		canvas = new GLJPanel();
		//Actual window size (monitor)
		canvas.setPreferredSize(new Dimension(400,400));
		//Callbacks
		canvas.addGLEventListener(this);
	    // The color control panel
	    JPanel colorPanel = new JPanel(new GridBagLayout());
	    Border border = BorderFactory.createTitledBorder(
	                                              BorderFactory.createLineBorder(new Color(0.0f,0.0f,1.0f)),
	                                              "Rectangle Color");
	    colorPanel.setBorder(border);        
	    // The RGB sliders
	    redSlider = new JSlider(0, 100, (int)(RED*100)) ;
	    redSlider.addChangeListener(this);
	    greenSlider = new JSlider(0, 100, (int)(GREEN*100)) ;
	    greenSlider.addChangeListener(this);
	    blueSlider = new JSlider(0, 100, (int)(BLUE*100)) ;
	    blueSlider.addChangeListener(this);
	    //Layout constraints for the color panel
	    GridBagConstraints constraints = new GridBagConstraints();
	    constraints.insets = new Insets(3,2,3,2);   
	    constraints.fill=constraints.HORIZONTAL;
	        
	    constraints.gridx = 0;
	    constraints.gridy = 0;
	    colorPanel.add(new JLabel("Red"), constraints);      
	    constraints.gridx = 1;
	    colorPanel.add(redSlider, constraints);
	        
	    constraints.gridx = 0;
	    constraints.gridy = 1;
	    colorPanel.add(new JLabel("Green"), constraints);
	    constraints.gridx = 1;
	    colorPanel.add(greenSlider, constraints);
	        
	    constraints.gridx = 0;
	    constraints.gridy = 2;
	    colorPanel.add(new JLabel("Blue"), constraints);
	    constraints.gridx = 1;
	    colorPanel.add(blueSlider, constraints);
	    // The panel containing the panels that contain all the other components

		getContentPane().add(colorPanel, BorderLayout.PAGE_END);
		getContentPane().add(canvas, BorderLayout.CENTER);
		// A menu with a submenu
		JMenuBar menuBar; 
		JMenu menu, submenu;
		JMenuItem menuItem;
		//Create the menu bar.
		menuBar = new JMenuBar();
		//Build the first menu.
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		//Add JMenuItems to Menu
		menuItem = new JMenuItem("Save", KeyEvent.VK_A);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuItem = new JMenuItem("Export", KeyEvent.VK_X);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		//Done with first menu. Add it the main menu bar
		menuBar.add(menu);
		//Build the second menu.
		menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);
		
		//a group of radio button menu items
		menu.addSeparator();
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem("A radio button menu item");
		rbMenuItem.setSelected(true);
		group.add(rbMenuItem);
		rbMenuItem.addItemListener(this);
		menu.add(rbMenuItem);
		rbMenuItem = new JRadioButtonMenuItem("Another radio button");
		group.add(rbMenuItem);
		rbMenuItem.addItemListener(this);
		menu.add(rbMenuItem);

		//a group of check box menu items
		menu.addSeparator();
		JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("A check box menu item");
		cbMenuItem.addItemListener(this);
		menu.add(cbMenuItem);
		cbMenuItem = new JCheckBoxMenuItem("Another checkbox");
		cbMenuItem.addItemListener(this);
		menu.add(cbMenuItem);
		menuBar.add(menu);

		setJMenuBar(menuBar);
	}
	
	@Override
	/**
	 * Called by the drawable to initiate OpenGL rendering by the client. After all GLEventListeners have been 
	 * notified of a display event, the drawable will swap its buffers if setAutoSwapBufferMode is enabled. 
	 */
	public void display(GLAutoDrawable arg0) {
		//We always need a context to draw
		GL2 gl = arg0.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
		//Set a color (redish - no other components)
		gl.glColor3d(RED,GREEN,BLUE);
		//Define a primitive -  A polygon in this case
		gl.glBegin(GL2.GL_POLYGON);
			gl.glVertex2i( -200, -200);
			gl.glVertex2i( -200, 200);
			gl.glVertex2i( 200, 200);
			gl.glVertex2i( 200, -200);
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
		GL2 gl = arg0.getGL().getGL2();
		gl.glClearColor(1f, 1f, 1f, 0f); //set to non-transparent black
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
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(-400,400,-400,400,0,10);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	
	public static void createAndShowGUI(){
		GL2SkeletonGUI s = new GL2SkeletonGUI();
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

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
	    if(e.getSource().equals(redSlider)) {
	        float value = (float)(redSlider.getValue());
	        value/=100;
	        RED = value;
	      }
	      if(e.getSource().equals(greenSlider)) {
	        float value = (float)(greenSlider.getValue());
	        value/=100;
	        GREEN = value;            
	      }
	      if(e.getSource().equals(blueSlider)) {
	        float value = (float)(blueSlider.getValue());
	        value/=100;
	        BLUE = value;            
	      }
	      if(e.getSource().equals(blueSlider)) {
		        float value = (float)(blueSlider.getValue());
		        value/=100;
		        BLUE = value;            
		      }

	      //Redraw canvas since someone changed the color
	      canvas.display();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Action event detected."
                   + "\n"
                   + "    Event source: " + source.getText();
        System.out.println(s);
		
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "State change event detected."
                   	+ "\n"
                   	+ "    Event source: " + source.getText()+"\n	"
        			+ ((e.getStateChange() == ItemEvent.SELECTED) ?
                            "selected":"unselected");
        System.out.println(s);
		
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}
}
