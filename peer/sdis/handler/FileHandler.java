package sdis.handler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import sdis.Peer;

// Handles operations related to files, and applies respective actions to linked chunks
public class FileHandler {
    ChunkHandler ch;
    
    public FileHandler(ChunkHandler chunkhandler){
        ch = chunkhandler;
    }
    
    /*
     * File Backup
     * Generates file identification properties,
     * and partitions it in chunks, followed by sending them to the MDB
     */
    public void backup(String file_n, int rep_degree) throws IOException, NoSuchAlgorithmException, SocketException, InterruptedException{
        String crlf =  Peer.createCRLF();
        ConcurrentHashMap<Integer, byte[]> file_map;
        
        
        FilePartition file_p = new FilePartition(file_n);
        file_map = file_p.getFileMap();        
        
        String fileID = Peer.getFileID(file_n);

        for(int i = 0; i < file_map.size(); i++) {
            String i_string = String.valueOf(i);

            String header = "PUTCHUNK 1.0 " + Peer.id + " " + fileID + " " + i_string + " " + String.valueOf(rep_degree) + " " + crlf + crlf;
            byte[] body = file_map.get(i);
            
            ch.putChunk(fileID, i, header, body, rep_degree);
        }
    }
    
    /*
     * File Restore
     * Collects chunks and information required to restore a file,
     * followed by its re-generation
     */
    public void restore(String filepath){        
        try{
            LinkedList<byte[]> rfile = new LinkedList<>();
            
            String crlf =  Peer.createCRLF();
            int count = 0; // Find how many chunks the file has
            String fileID = Peer.getFileID(filepath);
            
            for(ChunkID it : Peer.tracked.keySet()){
                if(it.fileID.equals(fileID)){
                    count++;
                }
            }
            
            for(int i = 0; i < count; i++){
                System.out.printf("Retrieving Chunk %d/%d\n", i+1, count);
                
                String header = "GETCHUNK 1.0 " + Peer.id + " " + fileID + " " + i + " " + crlf + crlf;
                rfile.add(ch.getChunk(header, fileID, String.valueOf(i)));
            }
            
            if(count > 0){
                //Write file to disc
                System.out.println("Writing \"restored_" + fileID + "\" to disc...");

                FileOutputStream file = new FileOutputStream("restored_" + fileID);

                for(int i = 0; i < rfile.size(); i++) {
                    file.write(rfile.get(i));
                }
                file.close();
            }
        }catch(Exception ex){
            System.out.println("Error in Restore: " + ex);
        }        
    }
    
    /*
     * File Delete
     * Deletes a file, and informs the MC group,
     * of said deletion
     */    
    public void delete(String filepath){
        try{
            String fileID = Peer.getFileID(filepath);
            String crlf =  Peer.createCRLF();            
            String header = "DELETE 1.0 " + Peer.id + " " + fileID + crlf + crlf;
            byte[] packdata = header.getBytes();
            
            //Perform Local Delete Prior to Other Peers
            System.out.println("Removing Chunks for FileID: " + fileID);
                
            int tc = 0;
            int sc = 0;

            //Remove Tracking line Chunks
            for (ChunkID it : Peer.tracked.keySet()) {
                if(it.fileID.equals(fileID)){
                    Peer.tracked.remove(it);
                    tc++;
                }
            }  
            System.out.printf("Removed %d Chunks from Tracking Line\n", tc);

            //Remove Stored line Chunks
            for (ChunkID it : Peer.stored.keySet()) {
                if(it.fileID.equals(fileID)){
                    Peer.stored.remove(it);
                    sc++;
                }
            }  
            System.out.printf("Removed %d Chunks from Storage Line\n", sc);
            
            
            DatagramSocket dbs = new DatagramSocket();
            System.out.println("Sending DELETE Messages...");
            for(int i = 0; i < 5; i++){
                System.out.printf("[%d]", i);
                DatagramPacket pack = new DatagramPacket(packdata, packdata.length, Peer.mc_addr, Peer.mc_port);
                dbs.send(pack);
            } 
            System.out.println();
        }catch(Exception ex){
            System.out.println("Error requesting DELETE " + ex);
        }
    }
}
