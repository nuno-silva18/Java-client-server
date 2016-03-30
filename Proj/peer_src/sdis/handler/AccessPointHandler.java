package sdis.handler;

import java.net.*;
import java.io.*;

// The class that enables Peer control via the interface
public class AccessPointHandler extends Thread{ 
    private final int port;
    
    //Handlers
    FileHandler fh;
    ChunkHandler ch;
    
    
    public AccessPointHandler(int id, FileHandler filehandler, ChunkHandler chunkhandler){
        port = id;
        fh = filehandler;
        ch = chunkhandler;
    }
    
    @Override
    public void run(){
        try {
            // Initialize Server Socket
            ServerSocket server = new ServerSocket(port);
            System.out.println("APInterface: Listening at port " + port);
          
            Socket client;
            PrintWriter out;
            BufferedReader in;
            
            while(true){
                // Accept Interface Connections and Handle it
                client = server.accept();
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                
                //Get Message sent by the Interface 
                String message;
                String result_message = "NO_RESULT";
                
                message = in.readLine(); //Only one, so no loop needed
                System.out.println("APInterface: " + message);

                //Handle the Message
                String[] arg = message.split("::");
                
                if(arg[0].equals("RUN")){
                    if(arg[1].equals("BACKUP")){
                        String filepath = arg[2];
                        int desired_replication = new Integer(arg[3]);
                        
                        //Launch File Backup on Peer
                        fh.backup(filepath, desired_replication);
                        
                        
                        result_message = String.format("Peer will now attempt to backup \"%s\" with a replication degree of %d", filepath, desired_replication);
                    }else if(arg[1].equals("RESTORE")){
                        String filepath = arg[2];
                        
                        //Launch File Restore on Peer
                        fh.restore(filepath);
                        
                        result_message = String.format("Peer will now attempt to restore \"%s\"", filepath);
                    }else if(arg[1].equals("DELETE")){
                        String filepath = arg[2];
                        
                        //Launch File Delete on Peer
                        fh.delete(filepath);
                        
                        result_message = String.format("Peer will now attempt to delete \"%s\"", filepath);
                    }else if(arg[1].equals("RECLAIM")){
                        int space = new Integer(arg[2]);
                        
                        //Launch Space Reclaim on Peer
                        ch.reclaimSpace(space);
                        
                        result_message = String.format("Peer will now reclaim %d SPACEUNITS", space);
                    }else{
                        result_message = "Unknown Sub Protocol Command";
                    }
                }else{
                    result_message = "Unknown Interface Command";
                }

                //Send Result Message                
                out.println(result_message);                
                client.close();
            }
        } catch (Exception ex) {
            System.out.println("Error in APInterface: " + ex);
            System.exit(1);
        }
    }
}
