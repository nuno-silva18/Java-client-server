package sdis.handler;

// Handles Chunk related operations

import java.io.IOException;
import sdis.Peer;
import java.net.*;

public class ChunkHandler {    
    
    /*
     * Sends a chunk to the MDB
     */
    public void putChunk(String fileID, int chunkNO, String header, byte[] body, int desired_rep_deg) throws SocketException, IOException, InterruptedException{
        ChunkID localChunkID = new ChunkID(fileID, chunkNO);
        Chunk localChunk = new Chunk(localChunkID, desired_rep_deg, null); //We are only tracking the status, not storing, so null
        Peer.tracked.put(localChunkID, localChunk);
        
        DatagramSocket dg_socket = new DatagramSocket();
        for(int j = 0; j < desired_rep_deg; j++){            
            byte[] pack = Peer.joinArray(header.getBytes(), body);
            DatagramPacket msg = new DatagramPacket(pack, pack.length, Peer.mdb_addr, Peer.mdb_port);
            
            dg_socket.send(msg);
            System.out.println("Package sent with header: " + header);
            Thread.sleep(500);
            
            
            //Confirmation messages are handled by the driver, so the value updates automatically
            if(localChunk.current_rep_deg >= desired_rep_deg) {
                System.out.println("Obtained the desired replication degree!");
                break;
            }	
        }
    }
    
    /*
     * Fetches a chunk from storage,
     * announces it on the MC
     */
    public void get(){
        
    }
    
    /*
     * Removes a socket from storage,
     * announces it on the MC
     */
    public void remove(){
        
    }
    
    //Reclaims Space on the Storage
    public void reclaimSpace(int space){
        
    }
}
