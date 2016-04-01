package sdis;

import java.net.*;
import java.io.*;

// Connects to the Peer, and performs requested operations
public class TestAppMain extends Thread{
    private final InetAddress host;
    private final int port;
    private final String proto;
    private final String op1;
    private final String op2;
    
    
    // Constructor
    TestAppMain(InetAddress host, int port, String protocol, String operand1, String operand2) {
        this.host = host;
        this.port = port;
        this.proto = protocol;
        this.op1 = operand1;
        this.op2 = operand2;
        
        System.out.println("PAP Host            : " + this.host);
        System.out.println("PAP Port            : " + this.port);
        System.out.println("Protocol to Execute : " + this.proto);
        
        if(proto.equals("RECLAIM")){
            System.out.println("Space to free       : " + this.op1);
        }else{
            System.out.println("Target File         : " + this.op1);    
        }
        
        if(proto.equals("BACKUP")){
            System.out.println("Replication Degree  : " + this.op2);
        }
    }
    
    // The main procedure for the client
    @Override
    public void run(){
        System.out.println("---- Running ----");
        try{
            Socket server = new Socket(host, port);
            PrintWriter out = new PrintWriter(server.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            
            // Generate Execution Message and Send it
            String message;
            if(op2==null){
                message = String.format("RUN::%s::%s", proto, op1);
            }else{
                message = String.format("RUN::%s::%s::%s", proto, op1, op2);
            }
            
            
            System.out.println(message);
            out.println(message);
                       
            // Get Reply and print it
            String reply = in.readLine(); //Only 1 message, so no loop
            System.out.println(">> " + reply);
                        
            server.close();
        }catch(Exception ex){
            System.out.println("An error has occured: " + ex);
            System.exit(1);
        }
    }
}
