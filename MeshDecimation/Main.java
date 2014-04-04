/**
 * @author Andong.Li(andong.li@outlook.com)
 * Please notify me before you take advantage of it
 */

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;

import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_B;
import static java.awt.event.KeyEvent.VK_F;
import static java.awt.event.KeyEvent.VK_L;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_PAGE_DOWN;
import static java.awt.event.KeyEvent.VK_PAGE_UP;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import com.jogamp.newt.Window;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureIO;

public class Main extends Applet implements GLEventListener, KeyListener, MouseListener, MouseMotionListener{

	// Applet configure
	private static final long serialVersionUID = 1L;

	private final int WIDTH = 640;
	private final int HEIGHT = 480;
	private final int EXPECTED_FPS = 60;
	
	private final String modelFileName = "model/test.off";
	private final String modelFileType = ".off";

	private GLAnimatorControl animator;

	public void init() {
		// set Applet window size
		setSize(WIDTH, HEIGHT);
		// set container layout
		setLayout(new BorderLayout());
		// create a GL canvas
		GLCanvas canvas = new GLCanvas();
		canvas.addGLEventListener(this);
		// register key listener
		canvas.addKeyListener(this);
		// register mouse listener
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.setFocusable(true);
		canvas.requestFocus();

		canvas.setSize(getSize());
		add(canvas, BorderLayout.CENTER);
		animator = new FPSAnimator(canvas, EXPECTED_FPS);
	}

	public void start() {
		animator.start();
	}

	public void stop() {
		animator.stop();
	}

	public void destroy() {
	}

	// ********************************************************

	// for the GL Utility
	private GLU glu = new GLU();
	private int prevMouseX, prevMouseY;
	private float view_rotx = 20.0f, view_roty = 30.0f;
	
	private float[][] VERTICES;
	private float[][] FACES;
	
	
	// for computing FPS
	public final long FPS_UPADTE_CAP = 100;
	private int myFrameCount;
	private long myLastFrameTime;
	private double myFPS;
	private boolean showFPS;
	private final Font fpsFont = new Font("Consolas", Font.PLAIN, 17);
	String fpsStr = "";
	TextRenderer textrenderer = new TextRenderer(fpsFont);

	boolean drawEdges = false;

	//enable light
	private boolean isLightOn = false;
	//enable texture blending
	private boolean blendingEnabled = true;

	@Override
	public void display(GLAutoDrawable glDrawable) {
		GL2 gl = glDrawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		// Rotate the entire assembly of gears based on how the user
	    // dragged the mouse around
	    gl.glPushMatrix();
	    gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);

		

		if (showFPS) {
			computeFPS();
			textrenderer.beginRendering(glDrawable.getWidth(),
					glDrawable.getHeight());
			gl.glColor3f(0.8f, 0.8f, 0.8f);
			textrenderer.draw("FPS: " + fpsStr, 10, 10);
			textrenderer.endRendering();
		}

	}

	@Override
	public void dispose(GLAutoDrawable glDrawable) {

	}
	
	private void readOff(String filename) throws Exception {
		URL fileURL = getClass().getClassLoader().getResource(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(fileURL.openStream()));
		String line = br.readLine();
		if(line.compareToIgnoreCase("off")!=0){
			System.err.println("Invalid off model file!");
			return;
		}
		int nverts = 0;
		int nfaces = 0;
		line = br.readLine();
		String[] temp = line.split(" ");
		nverts = Integer.parseInt(temp[0]);
		nfaces = Integer.parseInt(temp[1]);
		
		System.out.println("Model has " +nverts +" vertices, and "+ nfaces+" faces.");
		
		VERTICES = new float[nverts][3];
		FACES = new float[nfaces][3];
		
		int i = 0;
		
		for(i = 0; i< nverts;i++){
			line = br.readLine();
			temp = line.split(" ");
			VERTICES[i][0] = Float.parseFloat(temp[0]);
			VERTICES[i][1] = Float.parseFloat(temp[1]);
			VERTICES[i][2] = Float.parseFloat(temp[2]);
		}
		
		for(i=0; i<nfaces;i++){
			line = br.readLine();
			temp = line.split(" ");
			if(temp[0].compareToIgnoreCase("3")!=0){
				System.out.println("Invalid off model file!");
				return;
			}
			FACES[i][0] = Float.parseFloat(temp[1]);
			FACES[i][1] = Float.parseFloat(temp[2]);
			FACES[i][2] = Float.parseFloat(temp[3]);
		}
		br.close();
	}
	
	private void dumpMatrix(float[][] matrix){
		
		System.out.println("Matrix size is: "+ matrix.length +" X "+matrix[0].length);
		
		for(int i=0; i< matrix.length;i++){
			for(int j=0;j< matrix[0].length;j++){
				System.out.print(matrix[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	@Override
	public void init(GLAutoDrawable glDrawable) {
		// get openGL 3.0 context
		GL2 gl = glDrawable.getGL().getGL2();
		// smooth shadow
		gl.glShadeModel(GL2.GL_SMOOTH);
		// clear background color
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
		// gl.glClearColor(0.17f, 0.65f, 0.92f, 0.0f);//sky color
		// clear depth
		gl.glClearDepth(1.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

		myFrameCount = 0;
		myLastFrameTime = System.currentTimeMillis();
		myFPS = 0;
		showFPS = true;

		

		// init light source
		float[] mat_front = { 1.0f, 0.95f, 0.9f, 1f };
		float[] mat_back = { 0.7f, 0.6f, 0.6f, 1f };
		float[] mat_specular = { 0.18f, 0.18f, 0.18f, 0.18f };
		float[] mat_shininess = { 64f };
		float[] global_ambient = { 0.02f, 0.02f, 0.05f, 0.05f };
		float[] light0_ambient = { 0f, 0f, 0f, 0f };
		float[] light0_diffuse = { 0.85f, 0.85f, 0.8f, 0.85f };
		float[] light0_specular = { 0.85f, 0.85f, 0.85f, 0.85f };
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, mat_front,0);
		gl.glMaterialfv(GL2.GL_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, mat_back,0);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, mat_specular,0);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, mat_shininess,0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, light0_ambient,0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light0_diffuse,0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, light0_specular,0);
		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, global_ambient,0);
		gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL2.GL_FALSE);
		gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_TRUE);
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);

		try{
			readOff(modelFileName);
			dumpMatrix(VERTICES);
			dumpMatrix(FACES);
		}catch(Exception ex){
			System.err.println("Invalid off model file! " +ex);
		}
		
	}

	@Override
	public void reshape(GLAutoDrawable glDrawable, int x, int y, int width,
			int height) {

		GL2 gl = glDrawable.getGL().getGL2();

		if (height == 0)
			height = 1; // prevent divide by zero
		float aspect = (float) width / height;

		// Set the view port (display area) to cover the entire window
		gl.glViewport(0, 0, width, height);

		// Setup perspective projection, with aspect ratio matches viewport
		gl.glMatrixMode(GL2.GL_PROJECTION); // choose projection matrix
		gl.glLoadIdentity(); // reset projection matrix

		// fovy, aspect, zNear, zFar
		glu.gluPerspective(45.0, aspect, 0.1, 100.0);

		// Enable the model-view transform
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity(); // reset

	}

	private double computeFPS() {
		myFrameCount++;
		long currentTime = System.currentTimeMillis();
		if (currentTime - myLastFrameTime > FPS_UPADTE_CAP) {
			myFPS = myFrameCount * 1000
					/ (double) (currentTime - myLastFrameTime);
			myLastFrameTime = currentTime;
			fpsStr = String.format("%3.2f\n", myFPS);
			myFrameCount = 0;
		}

		return myFPS;
	}

	@Override
	public void keyPressed(KeyEvent e) {

		int keyCode = e.getKeyCode();
		
		switch (keyCode) {
		case VK_B:
            break;
		case VK_L:
			break;
		case VK_F:
			break;
		case VK_PAGE_UP:
			break;
		case VK_PAGE_DOWN:
			break;
		case VK_UP:
			break;
		case VK_DOWN:
			break;
		case VK_LEFT:
			break;
		case VK_RIGHT:
			break;
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int width=0, height=0;
        Object source = e.getSource();
        if(source instanceof Window) {
            Window window = (Window) source;
            width=window.getWidth();
            height=window.getHeight();
        } else if (GLProfile.isAWTAvailable() && source instanceof java.awt.Component) {
            java.awt.Component comp = (java.awt.Component) source;
            width=comp.getWidth();
            height=comp.getHeight();
        } else {
            throw new RuntimeException("Event source neither Window nor Component: "+source);
        }
        float thetaY = 360.0f * ( (float)(x-prevMouseX)/(float)width);
        float thetaX = 360.0f * ( (float)(y-prevMouseY)/(float)height);
        
        System.out.println(thetaY + " "+ thetaX);
        
        //prevMouseX = x;
        //prevMouseY = y;

        //view_rotx += thetaX;
        //view_roty += thetaY;
      }
	
	@Override
	public void mousePressed(MouseEvent e) {
		prevMouseX = e.getX();
        prevMouseY = e.getY();
		if(e.getButton() == e.BUTTON1){
			//System.out.println("Mouse left button pressed!");
		}
		if(e.getButton() == e.BUTTON2){
			//System.out.println("Mouse center button pressed!");
		}
		if(e.getButton() == e.BUTTON3){
			//System.out.println("Mouse right button pressed!");
		}
		if(e.getClickCount() == 2){
			//System.out.println("Mouse double click!");
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
