package sdis;

import java.net.*;
import java.util.Arrays;

// Parses the command line arguments and launches a Test Client instance that connects to the Peer.
public class TestApp {
    private static final String[] PROTOCOLS = {"BACKUP", "RESTORE", "DELETE", "RECLAIM"};    
    private static final String DEFAULT_HOST = "localhost";
     
    private static InetAddress pHost;
    private static int pPort;
     
    private static String proto;
    private static String op1;
    private static String op2;

    // Prints the CLI usage information
    public static void printUsage(){
        System.out.println("Usage:");
        System.out.println("$ java sdis.TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
        System.out.println("      - <peer_ap>      : The peer's access point, identified by host:port, "
                + "defaults to localhost if host is not provided.");
        System.out.println("      - <sub_protocol> : The subprotocol to be executed by the peer");
        System.out.println("      - <opnd_1>       : File (BACKUP, RESTORE, DELETE) or Space Ammount (RECLAIM)");
        System.out.println("      - <opnd_2>       : Replication Degree (BACKUP ONLY)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("      $ java TestApp 127.0.0.1:4001 BACKUP image.jpg 3");
        System.out.println("      $ java TestApp :4002 RESTORE document.txt");
        System.out.println("      $ java TestApp 4003 DELETE database.db") ;    
        System.out.println();
        System.out.println("Note:");
        System.out.println("      Filepaths must be relative to location the Peer is running on,");
        System.out.println("      as its the Peer that handles the files and not this application");
        
    }
    
    
    
    public static void main(String[] args){
        if(args.length < 3){
            printUsage();
            System.exit(1);
        }
        
        // Parse Peer Access Point
        String[] splitter = args[0].split(":");
        
        try{
            if(splitter.length == 1){
                pHost = InetAddress.getByName(DEFAULT_HOST);
                pPort = new Integer(splitter[0]);
            }else{
                pHost = InetAddress.getByName((splitter[0].equals("") ? DEFAULT_HOST : splitter[0]));
                pPort = new Integer(splitter[1]);
            }
        }catch (Exception ex){
            System.out.println("Error in <peer_ap>: " + ex);
            System.exit(1);
        }
        
        // Parse Sub Protocol
        String userProto = args[1].toUpperCase();
        for(String p : PROTOCOLS){
            if(p.equals(userProto)){
                proto = p;
                break;
            }
        }
        
        if(proto == null){
            System.out.println("Error in <sub_protocol>: Protocol is not supported. Should be: " + Arrays.toString(PROTOCOLS));
            System.exit(1);
        }
        
        // Parse Operand 1
        op1 = args[2];
        
        // Parse Operand 2
        if(proto.equals("BACKUP") && args.length > 3){
            op2 = args[3];
        }else if(proto.equals("BACKUP")){
            System.out.println("Error: Replication Degree <opnd_2>, not specified");
            System.exit(1);
        }
        
        // Launch and Execute Connection to Peer
        TestAppMain client = new TestAppMain(pHost, pPort, proto, op1, op2);
        client.start();
    }
    
}
