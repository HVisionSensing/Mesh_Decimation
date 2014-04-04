
public class LiMesh {

	
	public Vector3D[] vertices = new Vector3D[3];
	/**
	 * faces:
	 * 1-dimension: each face's vertices index
	 * 2-dimension: face index in mesh
	 */
	public int[][] faces = new int[3][3];
	public Vector3D center;
	public float size;
	
	public LiMesh(){
		vertices[0] = new Vector3D();
		vertices[1] = new Vector3D();
		vertices[2] = new Vector3D();
		
		faces[0][0] = 0; faces[0][1] = 0; faces[0][2] = 0;
		faces[1][0] = 0; faces[1][1] = 0; faces[1][2] = 0;
		faces[2][0] = 0; faces[2][1] = 0; faces[2][2] = 0;
		
		center = new Vector3D();
		size = 0.0f;
	}
	
}
