
public class HE_Edge {
	
    public HE_Vert v_begin;// vertex at the start of the half-edge
	public HE_Edge he_inv;// oppositely oriented adjacent half-edge
	public HE_Face f_left; // face the half-edge borders (face usually on the left)
	public HE_Edge he_next;// next half-edge around the face (counter-clockwise)
	
	public int index;

    public HE_Edge(){
    	
    }
    
    public HE_Edge(HE_Vert vBegin, HE_Edge heInv, HE_Face fLeft, HE_Edge heNext){
    	v_begin = vBegin;
    	he_inv = heInv;
    	f_left = fLeft;
    	he_next = heNext;
    }
    
    /***
     * make a copy of current HE_Edge
     * @return a new copy instead of reference
     */
    public HE_Edge copy(){
    	return new HE_Edge(v_begin, he_inv, f_left, he_next);
    }
}
