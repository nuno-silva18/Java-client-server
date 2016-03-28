package sdis;

import java.net.*;

// Parses the command line arguments and launches a Peer Server instance.
public class Peer {
    private static int id;
    private static InetAddress mc_address;
    private static int         mc_port;
    private static InetAddress mdb_address;
    private static int         mdb_port;
    private static InetAddress mdr_address;
    private static int         mdr_port;

    // Prints the CLI usage information
    public static void printUsage(){
        System.out.println("Usage:");
        System.out.println("$ java Peer <svid> <mcca> <mccp> <mdba> <mdbp> <mdra> <mdrp>");
        System.out.println("      - <svid>      : Server ID");
        System.out.println("      - <mcca>      : [MC]  Multicast Control Channel Address");
        System.out.println("      - <mccp>      : [MC]  Multicast Control Channel Port");
        System.out.println("      - <mdba>      : [MDB] Multicast Data Backup Channel Address");
        System.out.println("      - <mdbp>      : [MDB] Multicast Data Backup Channel Port");
        System.out.println("      - <mdra>      : [MDR] Multicast Data Restore Channel Address");
        System.out.println("      - <mdrp>      : [MDR] Multicast Data Restore Channel Port");
        System.out.println();
        System.out.println("Example:");
        System.out.println("      $ java Peer 4410 224.0.0.1 1441 224.0.0.2 1442 224.0.0.3 1443");
        System.out.println();
        System.out.println("Note:");
        System.out.println("      The server id is also used as the access point interface's port number.");
    }
    
    public static void main(String[] args) {
        if(args.length < 7){
            printUsage();
            System.exit(1);
        }
        
        // Parse Arguments
        try{
            id = new Integer(args[0]);        
            mc_address = InetAddress.getByName(args[1]);
            mc_port = new Integer(args[2]);
            mdb_address = InetAddress.getByName(args[3]);
            mdb_port = new Integer(args[4]);
            mdr_address = InetAddress.getByName(args[5]);
            mdr_port = new Integer(args[6]);
        }catch(Exception ex){
            System.out.println("Error: One or More of the Arguments is invalid or malformed: " + ex);
            System.exit(1);
        }
        
        // Start Peer
        System.out.println("--- Peer Initializing ---");
        System.out.printf("ID  - %d\n", id);
        System.out.printf("MC  - %s:%d\n", mc_address, mc_port);
        System.out.printf("MDB - %s:%d\n", mdb_address, mdb_port);
        System.out.printf("MDR - %s:%d\n", mdr_address, mdr_port);
        
        
        
        
        // Start Control Interface
        APInterface ci = new APInterface(id);
        ci.start();
    }
    
}
