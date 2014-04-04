import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

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
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureIO;

public class Main extends Applet implements GLEventListener, KeyListener, MouseListener{

	private static final long serialVersionUID = 1L;

	private final int WIDTH = 640;
	private final int HEIGHT = 480;
	private final int EXPECTED_FPS = 60;

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

	// for computing FPS
	public final long FPS_UPADTE_CAP = 100;
	private int myFrameCount;
	private long myLastFrameTime;
	private double myFPS;
	private boolean showFPS;
	private final Font fpsFont = new Font("Consolas", Font.PLAIN, 17);
	String fpsStr = "";
	TextRenderer textrenderer = new TextRenderer(fpsFont);


	// Textures with three different filters - Nearest, Linear & MIPMAP
	private Texture[] textures = new Texture[3];
	private int currentTextureFilter;
	private final String imageDir = "img/glass.png";
	private final String imageType = ".png";
	private float textureTop, textureBottom, textureLeft, textureRight;

	//enable light
	private boolean isLightOn = false;
	//enable texture blending
	private boolean blendingEnabled = true;

	@Override
	public void display(GLAutoDrawable glDrawable) {
		GL2 gl = glDrawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		gl.glTranslatef(0.0f, 0.0f, zpos);
		gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);

		textures[currentTextureFilter].enable(gl);
		textures[currentTextureFilter].bind(gl);

		//light control
		if (isLightOn) {
			gl.glEnable(GL2.GL_LIGHTING);
		} else {
			gl.glDisable(GL2.GL_LIGHTING);
		}
		
		// Blending control
		if (blendingEnabled) {
			gl.glEnable(GL2.GL_BLEND);
			gl.glDisable(GL2.GL_DEPTH_TEST);
		} else {
			gl.glDisable(GL2.GL_BLEND);
			gl.glEnable(GL2.GL_DEPTH_TEST);
		}

		gl.glBegin(GL2.GL_QUADS);

		// front
		gl.glNormal3f(0.0f, 0.0f, 1.0f);
		gl.glTexCoord2f(textureLeft, textureBottom);
		gl.glVertex3f(-1.0f, -1.0f, 1.0f);
		gl.glTexCoord2f(textureRight, textureBottom);
		gl.glVertex3f(1.0f, -1.0f, 1.0f);
		gl.glTexCoord2f(textureRight, textureTop);
		gl.glVertex3f(1.0f, 1.0f, 1.0f);
		gl.glTexCoord2f(textureLeft, textureTop);
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);

		// back
		gl.glNormal3f(0.0f, 0.0f, -1.0f);
		gl.glTexCoord2f(textureRight, textureBottom);
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glTexCoord2f(textureRight, textureTop);
		gl.glVertex3f(-1.0f, 1.0f, -1.0f);
		gl.glTexCoord2f(textureLeft, textureTop);
		gl.glVertex3f(1.0f, 1.0f, -1.0f);
		gl.glTexCoord2f(textureLeft, textureBottom);
		gl.glVertex3f(1.0f, -1.0f, -1.0f);

		// top
		gl.glNormal3f(0.0f, 1.0f, 0.0f);
		gl.glTexCoord2f(textureLeft, textureTop);
		gl.glVertex3f(-1.0f, 1.0f, -1.0f);
		gl.glTexCoord2f(textureLeft, textureBottom);
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);
		gl.glTexCoord2f(textureRight, textureBottom);
		gl.glVertex3f(1.0f, 1.0f, 1.0f);
		gl.glTexCoord2f(textureRight, textureTop);
		gl.glVertex3f(1.0f, 1.0f, -1.0f);

		// bottom
		gl.glNormal3f(0.0f, -1.0f, 0.0f);
		gl.glTexCoord2f(textureRight, textureTop);
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glTexCoord2f(textureLeft, textureTop);
		gl.glVertex3f(1.0f, -1.0f, -1.0f);
		gl.glTexCoord2f(textureLeft, textureBottom);
		gl.glVertex3f(1.0f, -1.0f, 1.0f);
		gl.glTexCoord2f(textureRight, textureBottom);
		gl.glVertex3f(-1.0f, -1.0f, 1.0f);

		// right
		gl.glNormal3f(1.0f, 0.0f, 0.0f);
		gl.glTexCoord2f(textureRight, textureBottom);
		gl.glVertex3f(1.0f, -1.0f, -1.0f);
		gl.glTexCoord2f(textureRight, textureTop);
		gl.glVertex3f(1.0f, 1.0f, -1.0f);
		gl.glTexCoord2f(textureLeft, textureTop);
		gl.glVertex3f(1.0f, 1.0f, 1.0f);
		gl.glTexCoord2f(textureLeft, textureBottom);
		gl.glVertex3f(1.0f, -1.0f, 1.0f);

		// left
		gl.glNormal3f(-1.0f, 0.0f, 0.0f);
		gl.glTexCoord2f(textureLeft, textureBottom);
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glTexCoord2f(textureRight, textureBottom);
		gl.glVertex3f(-1.0f, -1.0f, 1.0f);
		gl.glTexCoord2f(textureRight, textureTop);
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);
		gl.glTexCoord2f(textureLeft, textureTop);
		gl.glVertex3f(-1.0f, 1.0f, -1.0f);

		gl.glEnd();

		xrot += xspeed;
		yrot += yspeed;

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

		// load texture
		try {

			URL imageURL = getClass().getClassLoader().getResource(imageDir);
			// first image filter: linear
			textures[0] = TextureIO.newTexture(imageURL, false, imageType);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
					GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
					GL.GL_LINEAR);
			// second image filter: nearest
			textures[1] = TextureIO.newTexture(imageURL, false, imageType);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
					GL.GL_NEAREST);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
					GL.GL_NEAREST);
			// third image filter: mipmap
			textures[2] = TextureIO.newTexture(imageURL, true, imageType);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
					GL.GL_NEAREST);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
					GL.GL_LINEAR_MIPMAP_NEAREST);

			// record texture coords
			TextureCoords textureCoords = textures[0].getImageTexCoords();
			textureTop = textureCoords.top();
			textureBottom = textureCoords.bottom();
			textureLeft = textureCoords.left();
			textureRight = textureCoords.right();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// init light source
		float[] lightAmbientValue = { 0.5f, 0.5f, 0.5f, 1.0f };
		float[] lightDiffuseValue = { 1.0f, 1.0f, 1.0f, 1.0f };
		float lightDiffusePosition[] = { 0.0f, 0.0f, 2.0f, 1.0f };
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightAmbientValue, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightDiffuseValue, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightDiffusePosition, 0);
		gl.glEnable(GL2.GL_LIGHT1);
		gl.glDisable(GL2.GL_LIGHTING);

		//init blend
		gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		gl.glEnable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_DEPTH_TEST);

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
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

}
