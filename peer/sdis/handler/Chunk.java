package sdis.handler;

// Stores the data for a chunk

import java.io.IOException;
import java.net.SocketException;
import java.util.Objects;
import sdis.Peer;

public class Chunk {
    public ChunkID id;
    public int desired_rep_deg;
    public int current_rep_deg;
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
    }
    
    public void decrease_rep_deg() throws IOException, SocketException, InterruptedException{
        current_rep_deg--;
        
        if(desired_rep_deg > current_rep_deg && data!=null){
            System.out.println("Chunk is below desired rep_degree, broadcasting chunk now...");
            String crlf =  Peer.createCRLF();
           
            String header = "PUTCHUNK 1.0 " + Peer.id + " " + id.fileID + " " + id.chunkNO + " " + desired_rep_deg + " " + crlf + crlf;
       
            Peer.getCH().putChunk(id.fileID, id.chunkNO, header, data, desired_rep_deg);
        }
    }
    
    //Required for hashmap working properly
    @Override
    public boolean equals(Object other){
        if(other instanceof Chunk){
           if(((Chunk) other).id.equals(id)){
               return true;
           }
        }
        
        return false;        
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
        return hash;
    }
}
