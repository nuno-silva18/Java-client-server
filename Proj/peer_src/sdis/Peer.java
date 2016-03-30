package sdis;

import java.io.UnsupportedEncodingException;
import sdis.handler.AccessPointHandler;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import sdis.handler.Chunk;
import sdis.handler.ChunkHandler;
import sdis.handler.ChunkID;
import sdis.handler.FileHandler;
import sdis.multicast.MulticastDriver;

// Parses the command line arguments and launches a Peer Server instance.
public class Peer {
    public static int id;
    public static InetAddress mc_addr;
    public static int         mc_port;
    public static InetAddress mdb_addr;
    public static int         mdb_port;
    public static InetAddress mdr_addr;
    public static int         mdr_port;
    
    public static HashMap<ChunkID, Chunk> tracked = new HashMap<>();
    public static HashMap<ChunkID, Chunk> stored = new HashMap<>();

    
    
    //Joins two byte arrays
    public static byte[] joinArray(byte[] a, byte[] b){
        byte[] newArray = new byte[a.length + b.length];
        
        System.arraycopy(a, 0, newArray, 0, a.length);
        System.arraycopy(b, 0, newArray, a.length, b.length);
        
        return newArray;
    }
    
    /**
     * Function responsible for generating the CRLF token
     * @return CRLF token as a String object
     */
    public static String createCRLF(){
        byte[] crlf = new byte[2];
        crlf[0] = 0xD;
        crlf[1] = 0xA;

        String crlf_s = new String(crlf);
        return crlf_s;
    }
    
    /**
     * Generates the fileID based on the given path
     * @param path
     * @return The fileID byte array
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.UnsupportedEncodingException
     */
    public static String getFileID(String path) throws NoSuchAlgorithmException, UnsupportedEncodingException{
        MessageDigest md = MessageDigest.getInstance("SHA-256");        
        md.update(path.getBytes("UTF-8"));
        byte[] digest = md.digest();
        
        return String.format("%064x", new java.math.BigInteger(1, digest));
    }
    
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
            mc_addr = InetAddress.getByName(args[1]);
            mc_port = new Integer(args[2]);
            mdb_addr = InetAddress.getByName(args[3]);
            mdb_port = new Integer(args[4]);
            mdr_addr = InetAddress.getByName(args[5]);
            mdr_port = new Integer(args[6]);
        }catch(Exception ex){
            System.out.println("Error: One or More of the Arguments is invalid or malformed: " + ex);
            System.exit(1);
        }
        
        // Start Peer
        System.out.println("--- Peer Initializing ---");
        System.out.printf("ID  - %d\n", id);
        System.out.printf("MC  - %s:%d\n", mc_addr, mc_port);
        System.out.printf("MDB - %s:%d\n", mdb_addr, mdb_port);
        System.out.printf("MDR - %s:%d\n", mdr_addr, mdr_port);
        
        
        
        
        // Start Control Classes
        ChunkHandler chunkhandler = new ChunkHandler();
        FileHandler filehandler = new FileHandler(chunkhandler);
        
        MulticastDriver driver = new MulticastDriver();
        
        AccessPointHandler ci = new AccessPointHandler(id, filehandler, chunkhandler);
        ci.start();
    }
    
}
