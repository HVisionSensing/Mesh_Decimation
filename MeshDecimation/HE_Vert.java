
public class HE_Vert {
	public Vector3D coordinate;
    public HE_Edge edge;
    public int index;
    
    public float[][] Q = new float[4][4];
    
    public HE_Vert(Vector3D point){
		coordinate = point.copy();
	}
    
    public HE_Vert(){
    	coordinate = new Vector3D();
    }
    
    /**
     * get number of edges around this vertex
     * @return
     */
    public int computeDegree(){
    	if (edge == null){
    		return -1;
    	}
    	
    	int degree = 0;
    	// make two references
    	HE_Edge a_edge = edge;
    	HE_Edge c_edge = edge;
    	do{
    		c_edge = c_edge.he_inv.he_next;
    		degree++;
    		if (degree > 1000)
    			break;
    	} while (c_edge != a_edge && c_edge != a_edge.he_inv);
    	
    	a_edge = null;
    	c_edge = null;
    	return degree;
    }
}
