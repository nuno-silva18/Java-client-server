package sdis.handler;

import java.io.IOException;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
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
        
    }
    
    /*
     * File Delete
     * Deletes a file, and informs the MC group,
     * of said deletion
     */    
    public void delete(String filepath){
        
    }
}
