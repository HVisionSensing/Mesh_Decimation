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
import java.nio.ByteBuffer;
import java.util.LinkedList;

import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_E;
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

public class Main extends Applet implements GLEventListener, KeyListener, MouseListener, MouseMotionListener{

	// Applet configure
	private static final long serialVersionUID = 1L;

	private int WIDTH = 640; // default window width
	private int HEIGHT = 480; // default window height
	private final int EXPECTED_FPS = 60;
	
	private final String modelFileName = "model/test.off";
	//private final String modelFileType = ".off";

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
	private float view_rotx = 20.0f, view_roty = 30.0f, view_rotz = 0.0f;
	private float X_pos = 0.0f;
	private float Y_pos = 0.0f;
	private float Z_pos = -10.0f;
	private int mouseID =0;//0-left, 1-middle, 2-right
	
	private final float DOF = 10.0f;
	private final float MAXDOF = 10000.0f;
	
	private final float[] mat_front = { 1.0f, 0.95f, 0.9f, 1f };
	private final float[] mat_back = { 0.7f, 0.6f, 0.6f, 1f };
	private final float[] mat_specular = { 0.18f, 0.18f, 0.18f, 0.18f };
	private final float[] mat_shininess = { 64f };
	private final float[] global_ambient = { 0.02f, 0.02f, 0.05f, 0.05f };
	private final float[] light0_ambient = { 0f, 0f, 0f, 0f };
	private final float[] light0_diffuse = { 0.85f, 0.85f, 0.8f, 0.85f };
	private final float[] light0_specular = { 0.85f, 0.85f, 0.85f, 0.85f };
	
	private Vector3D[] VERTICES;
	private int[][] FACES;
	private Vector3D CENTER;
	private float SIZE = 0.0f;
	
	private LinkedList<HE_Edge> heEdgeList = new LinkedList<HE_Edge>();
	private LinkedList<HE_Vert> heVertList = new LinkedList<HE_Vert>();
	private LinkedList<HE_Face> heFaceList = new LinkedList<HE_Face>();
	
	//private HE_Edge[] heEdgeArray;
	//private HE_Vert[] heVertArray;
	//private HE_Face[] heFaceArray;
	
	private boolean drawEdges = false;
	
	
	// for computing FPS
	public final long FPS_UPADTE_CAP = 100;
	private int myFrameCount;
	private long myLastFrameTime;
	private double myFPS;
	private boolean showFPS;
	private final Font fpsFont = new Font("Consolas", Font.PLAIN, 17);
	String fpsStr = "";
	TextRenderer textrenderer = new TextRenderer(fpsFont);
	
	@Override
	public void display(GLAutoDrawable glDrawable) {
		
		GL2 gl = glDrawable.getGL().getGL2();	
		
		gl.glClearColor(1, 1, 1, 0);
		gl.glClearDepth(1);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		

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
	    
	    gl.glDepthFunc(GL2.GL_LESS);
	    gl.glEnable(GL2.GL_DEPTH_TEST);
	    gl.glDisable(GL2.GL_CULL_FACE);
	    gl.glShadeModel(GL2.GL_FLAT);
	    gl.glEnable(GL2.GL_NORMALIZE);
	    
	    gl.glPushMatrix();
	    gl.glTranslatef(X_pos, Y_pos, Z_pos);

		gl.glPushMatrix();
		gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(view_rotz, 0.0f, 0.0f, 1.0f);
		// gl.glPopMatrix();
	   
		//gl.glPopMatrix();
	    
	    // Draw mesh - first pass
	    if (drawEdges) {
	      gl.glPolygonOffset(1, 1);
	      gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
	    }
	    gl.glBegin(GL2.GL_TRIANGLES);
	    int nf = FACES.length;
	    for (int i = 0; i < nf; i++) {
	      Vector3D v0 = VERTICES[FACES[i][0]].copy();
	      Vector3D v1 = VERTICES[FACES[i][1]].copy();
	      Vector3D v2 = VERTICES[FACES[i][2]].copy();
	      v1.subtract(v0);
	      v2.subtract(v0);
	      Vector3D norm = v1.crossProduct(v2);
	      
	      gl.glNormal3fv(norm.toArray(), 0);
	      gl.glVertex3fv(VERTICES[FACES[i][0]].toArray(), 0);
	      gl.glVertex3fv(VERTICES[FACES[i][1]].toArray(), 0);
	      gl.glVertex3fv(VERTICES[FACES[i][2]].toArray(), 0);
	    }
	    gl.glEnd();

	    // Draw mesh - second pass to draw the edges on top
	    if (drawEdges) {
	      gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
	      gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
	      gl.glLineWidth(1.0f);
	      float[] mat_diffuse= { 0.0f, 0.0f, 1.0f, 1.0f };
	      gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, mat_diffuse, 0);
	      gl.glBegin(GL2.GL_TRIANGLES);
	      for (int i = 0; i < nf; i++) {
	    	  Vector3D v0 = VERTICES[FACES[i][0]].copy();
		      Vector3D v1 = VERTICES[FACES[i][1]].copy();
		      Vector3D v2 = VERTICES[FACES[i][2]].copy();
		      v1.subtract(v0);
		      v2.subtract(v0);
		      Vector3D norm = v1.crossProduct(v2);
		      
		      gl.glNormal3fv(norm.toArray(), 0);
		      gl.glVertex3fv(VERTICES[FACES[i][0]].toArray(), 0);
		      gl.glVertex3fv(VERTICES[FACES[i][1]].toArray(), 0);
		      gl.glVertex3fv(VERTICES[FACES[i][2]].toArray(), 0);
	      }
	      gl.glEnd();
	      gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	    }

	    gl.glPopMatrix();
	    gl.glDisable(GL2.GL_DEPTH_TEST);
	    gl.glDisable(GL2.GL_LIGHTING);
	    

		if (showFPS) {
			computeFPS();
			textrenderer.beginRendering(glDrawable.getWidth(),
					glDrawable.getHeight());
			gl.glColor3f(0.1f, 0.1f, 0.1f);
			String temp = " Vertices: "+heVertList.size()+" Edges: "+(heEdgeList.size())/2 + " Faces: "+heFaceList.size();
			textrenderer.draw("FPS: " + fpsStr + temp, 10, 10);
			textrenderer.draw("Hold mouse left to drag, middle to move, right to zoom.", 10, HEIGHT-20);
			textrenderer.draw("Press E to show mesh edges, E to decimate meshes.", 10, HEIGHT-40);
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
		
		VERTICES = new Vector3D[nverts];
		FACES = new int[nfaces][3];
		
		int i = 0;
		
		for(i = 0; i< nverts;i++){
			line = br.readLine();
			temp = line.split(" ");
			VERTICES[i] = new Vector3D();
			VERTICES[i].setX(Float.parseFloat(temp[0]));
			VERTICES[i].setY(Float.parseFloat(temp[1]));
			VERTICES[i].setZ(Float.parseFloat(temp[2]));
		}
		
		for(i=0; i<nfaces;i++){
			line = br.readLine();
			temp = line.split(" ");
			if(temp[0].compareToIgnoreCase("3")!=0){
				System.out.println("Invalid off model file!");
				return;
			}
			FACES[i][0] = Integer.parseInt(temp[1]);
			FACES[i][1] = Integer.parseInt(temp[2]);
			FACES[i][2] = Integer.parseInt(temp[3]);
		}
		br.close();
		
		//compute center and size
		Vector3D minpt = VERTICES[0].copy();
		Vector3D maxpt = VERTICES[0].copy();
		
		for(i=0 ;i<VERTICES.length;i++){
			if(VERTICES[i].X < minpt.X ) minpt.setX(VERTICES[i].X);
			if(VERTICES[i].X > maxpt.X ) maxpt.setX(VERTICES[i].X);
			if(VERTICES[i].Y < minpt.Y ) minpt.setY(VERTICES[i].Y);
			if(VERTICES[i].Y > maxpt.Y ) maxpt.setY(VERTICES[i].Y);
			if(VERTICES[i].Z < minpt.Z ) minpt.setZ(VERTICES[i].Z);
			if(VERTICES[i].Z > maxpt.Z ) maxpt.setZ(VERTICES[i].Z);
		}
		
		CENTER = minpt.copy();
		CENTER.add(maxpt);
		CENTER.scale(0.5f);
		
		SIZE = 0.5f * minpt.computeDistance(maxpt);
		
		
		//Construct half edge data structure
		//Loop all faces
		int j=0;
		for(i=0;i<nfaces;i++){
			//Three vertices from a face
			HE_Vert v1 = null;
			HE_Vert v2 = null;
			HE_Vert v3 = null;
			HE_Edge e1 = null;
			HE_Edge e2 = null;
			HE_Edge e3 = null;
			HE_Face f = null;
			
			boolean v1_new = true;
			boolean v2_new = true;
			boolean v3_new = true;
			
			//Check if this vertex is an existing one
			for(j = 0;j<heVertList.size();j++){
				if(heVertList.get(j).coordinate.equals(VERTICES[FACES[i][0]])){
					v1_new = false;
					v1 = heVertList.get(j);
				}
				if(heVertList.get(j).coordinate.equals(VERTICES[FACES[i][1]])){
					v2_new = false;
					v2 = heVertList.get(j);
				}
				if(heVertList.get(j).coordinate.equals(VERTICES[FACES[i][2]])){
					v3_new = false;
					v3 = heVertList.get(j);
				}
			}
			
			if(v1_new){
				v1 = new HE_Vert( VERTICES[FACES[i][0]] );
				heVertList.add(v1);
			}
			if(v2_new){
				v2 = new HE_Vert( VERTICES[FACES[i][1]] );
				heVertList.add(v2);
			}
			if(v3_new){
				v3 = new HE_Vert( VERTICES[FACES[i][2]] );
				heVertList.add(v3);
			}
			
			//Create HE_edges
			e1 = new HE_Edge();
			heEdgeList.add(e1);
			e2 = new HE_Edge();
			heEdgeList.add(e2);
			e3 = new HE_Edge();
			heEdgeList.add(e3);
			
			//Create HE_Face
			f = new HE_Face();
			heFaceList.add(f);
			
			//make connection
			f.edge = e1; //here choose the first
			v1.edge = e1;
			v2.edge = e2;
			v3.edge = e3;
			
			e1.v_begin = v1;
			e1.he_inv = null;
			e1.f_left = f;
			e1.he_next = e2;
			
			e2.v_begin = v2;
			e2.he_inv = null;
			e2.f_left = f;
			e2.he_next = e3;
			
			e3.v_begin = v3;
			e3.he_inv = null;
			e3.f_left = f;
			e3.he_next = e1;
			
			// pair match, loop remain edges
			for (int e_i = heEdgeList.size()-4; e_i >= 0; e_i--){
				// CASE 1: Normal half edges pointing in opposite directions
				if (heEdgeList.get(e_i).v_begin == e1.he_next.v_begin
						&& heEdgeList.get(e_i).he_next.v_begin == e1.v_begin){
					heEdgeList.get(e_i).he_inv = e1;
					e1.he_inv = heEdgeList.get(e_i);
					continue; // Should be a unique half-edge matching
				}
				if (heEdgeList.get(e_i).v_begin == e2.he_next.v_begin
						&& heEdgeList.get(e_i).he_next.v_begin == e2.v_begin){
					heEdgeList.get(e_i).he_inv = e2;
					e2.he_inv = heEdgeList.get(e_i);
					continue;
				}
				if (heEdgeList.get(e_i).v_begin == e3.he_next.v_begin 
						&& heEdgeList.get(e_i).he_next.v_begin == e3.v_begin){
					heEdgeList.get(e_i).he_inv = e3;
					e3.he_inv = heEdgeList.get(e_i);
					continue;
				}
				
				// CASE 2: Normal half edges pointing in same direction
				if (heEdgeList.get(e_i).v_begin == e1.v_begin 
						&& heEdgeList.get(e_i).he_next.v_begin == e1.he_next.v_begin){
					heEdgeList.get(e_i).he_inv = e1;
					e1.he_inv = heEdgeList.get(e_i);
					continue;
				}
				if (heEdgeList.get(e_i).v_begin == e2.v_begin 
						&& heEdgeList.get(e_i).he_next.v_begin == e2.he_next.v_begin){
					heEdgeList.get(e_i).he_inv = e2;
					e2.he_inv = heEdgeList.get(e_i);
					continue;
				}
				if (heEdgeList.get(e_i).v_begin == e3.v_begin 
						&& heEdgeList.get(e_i).he_next.v_begin == e3.he_next.v_begin){
					heEdgeList.get(e_i).he_inv = e3;
					e3.he_inv = heEdgeList.get(e_i);
					continue;
				}
			}
			
			v1 = null;
			v2 = null;
			v3 = null;
			e1 = null;
			e2 = null;
			e3 = null;
			f = null;
		}
		

		
		// Now make the boundary consistent
		outer:
		for (i = 0; i < heVertList.size(); i++){
			
			// Find one boundary edge
			if (heVertList.get(i).edge.he_inv == null){
				HE_Edge edge = heVertList.get(i).edge;
				HE_Edge edgec = heVertList.get(i).edge;
				// Create the inverse edge
				HE_Edge inv = new HE_Edge();
				heEdgeList.add(inv);
				
				// Link the edges
				inv.he_inv = edge;
				inv.f_left = null;
				edge.he_inv = inv;
				
				//Link vertex
				inv.v_begin = edge.he_next.v_begin;
				
				do{
					HE_Edge next_inv = edge.he_next.he_next;
					if(next_inv == edgec){
						inv.he_next = edgec.he_inv;
						break;
					}
					while (next_inv.he_inv != null){
						if (next_inv == edgec){
							inv.he_next = edgec.he_inv;
							continue outer;
						}
						next_inv = next_inv.he_inv.he_next.he_next;
					}
					
					// Create the next inverse edge around boundary
					HE_Edge new_inv = new HE_Edge();
					
					// Link the previous edge and this new edge together
					inv.he_next = new_inv;
					heEdgeList.add(new_inv);
					next_inv.he_inv = new_inv;
					new_inv.he_inv = next_inv;
					new_inv.v_begin = inv.he_inv.v_begin;
					new_inv.f_left = null;
					
					inv = new_inv;
					edge = new_inv.he_inv;
					
				} while (edge != edgec);					
			}	
		}
	}
	
	private void dumpMatrix(int[][] matrix){
		
		System.out.println("Matrix size is: "+ matrix.length +" X "+matrix[0].length);
		
		for(int i=0; i< matrix.length;i++){
			for(int j=0;j< matrix[0].length;j++){
				System.out.print(matrix[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	private void dumpMatrix(Vector3D[] vec){
		System.out.println("Matrix size is: "+ vec.length +" X 3");
		for(int i=0;i<vec.length;i++){
			System.out.println(vec[i].X +" "+vec[i].Y +" "+vec[i].Z);
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
			//dumpMatrix(VERTICES);
			//dumpMatrix(FACES);
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

		WIDTH = width;
		HEIGHT = height;
		
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
		case VK_E:
			drawEdges = !drawEdges;
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
        
        prevMouseX = x;
        prevMouseY = y;

        if(mouseID == 0){
        	view_rotx += thetaX;
        	view_roty += thetaY;
        }
        else if(mouseID == 1){
        	X_pos += thetaY/3;
        	Y_pos -= thetaX/3;
        }
        else if(mouseID == 2){
        	Z_pos += thetaX;
        }
      }
	
	@Override
	public void mousePressed(MouseEvent e) {
		prevMouseX = e.getX();
        prevMouseY = e.getY();
		if(e.getButton() == e.BUTTON1){
			mouseID = 0;
			//System.out.println("Mouse left button pressed!");
		}
		if(e.getButton() == e.BUTTON2){
			mouseID = 1;
			//System.out.println("Mouse center button pressed!");
		}
		if(e.getButton() == e.BUTTON3){
			mouseID = 2;
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
		
	}

}
