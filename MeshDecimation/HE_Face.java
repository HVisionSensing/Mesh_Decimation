
public class HE_Face {

	public HE_Edge edge;
	
	public int index;
	
	public HE_Face(){
	}
	
	/**
	 * Get this face's plane
	 * @return
	 */
	public float[] getPlane(){
		float[] plane = new float[4];
		
		//get this face's three vertices
		Vector3D p0 = edge.v_begin.coordinate.copy();
		Vector3D p1 = edge.he_next.v_begin.coordinate.copy();
		Vector3D p2 = edge.he_next.he_next.v_begin.coordinate.copy();
		
		// Find normal from cross product
		p1.subtract(p0);
		p2.subtract(p0);
		Vector3D normal = p1.crossProduct(p2);
		normal.normalize();
		
		// Find point from dot product
		normal.scale(-1);
		float d = normal.dotProduct(p0);
		
		plane[0] = normal.X;
		plane[1] = normal.Y;
		plane[2] = normal.Z;
		plane[3] = d;
		
		return plane;

	}
}
