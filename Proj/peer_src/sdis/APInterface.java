package sdis;

import java.net.*;
import java.io.*;

// The class that enables Peer control via the interface
public class APInterface extends Thread{
    private final int port;
    
    public APInterface(int id){
        port = id;
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
                String result_message;
                
                message = in.readLine(); //Only one, so no loop needed
                System.out.println("APInterface: " + message);

                //Handle the Message
                //WRITE TRIGGERS HERE

                //Generate Result Message
                result_message = "OK";
                out.println(result_message);

                
                client.close();
            }
        } catch (Exception ex) {
            System.out.println("Error in APInterface: " + ex);
            System.exit(1);
        }
    }
}
