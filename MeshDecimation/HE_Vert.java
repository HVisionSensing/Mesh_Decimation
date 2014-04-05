
public class HE_Vert {
	public Vector3D coordinate;
    public HE_Edge edge;
    public int index;
    
    public HE_Vert(Vector3D point){
		coordinate = point.copy();
	}
    
    public HE_Vert(){
    	coordinate = new Vector3D();
    }
}
