package sdis.handler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
        HashMap<Integer, byte[]> file_map;
        
        
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
            
            for(Iterator<ChunkID> it = Peer.tracked.keySet().iterator(); it.hasNext();){
                if(it.next().fileID.equals(fileID)){
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
        
    }
}
