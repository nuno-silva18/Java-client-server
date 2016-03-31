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
    public byte[] getChunk(String header, String fileID, String chunkNO) throws SocketException, IOException, InterruptedException{
        Peer.restore_fileID = fileID;
        Peer.restore_chunkNO = chunkNO;
        Peer.restore_wait = true;
        
        DatagramSocket dg_socket = new DatagramSocket();
        
        //Send GETCHUNK every 500ms, until we get packet
        do{
            byte[] pack = header.getBytes();

            DatagramPacket msg = new DatagramPacket(pack, pack.length, Peer.mc_addr, Peer.mc_port);            
            dg_socket.send(msg);

            //Check if we got the packed every 10ms
            int count = 0;
            do{
                System.out.print(".");
                count++;
                Thread.sleep(10);            
            }while(Peer.restore_wait && count < 50);
        }while(Peer.restore_wait);
        System.out.println();        
        
        return Peer.restore_data;
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
