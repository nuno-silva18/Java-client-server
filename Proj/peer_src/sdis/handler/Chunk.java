package sdis.handler;

// Stores the data for a chunk
public class Chunk {
    public ChunkID id;
    public int desired_rep_deg;
    public int current_rep_deg;
    public boolean isRemote;
    public byte[] data;
    
    //Instances a chunk, for tracking its status on other peers and/or storing it locally
    public Chunk(ChunkID id, int desired_rep_deg, byte[] data){
        this.id = id;
        this.desired_rep_deg = desired_rep_deg;
        this.current_rep_deg = 0;
        this.data = data;
    }
    
    public void increase_rep_deg(){
        current_rep_deg++;
        
        //TODO Implement deg < desired recovery
    }
    
    public void decrease_rep_deg(){
        current_rep_deg--;
        
        //TODO Implement deg < desired recovery
    }
}
