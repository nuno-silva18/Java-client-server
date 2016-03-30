package sdis.multicast;

// Performs and handles operations related to the multicast groups

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.util.Random;
import sdis.Peer;
import sdis.handler.Chunk;
import sdis.handler.ChunkID;

public class MulticastDriver{
    private final int BUFF_SIZE = 256000;
    
    private Random random = new Random();
    
    public MulticastDriver(){
        
        startMC();
        startMDB();
        startMDR();
    }
    
    // MC Control Thread
    private void startMC(){
        Thread t = new Thread(){
            @Override
            public void run(){
                try {
                    MulticastSocket mc_socket = new MulticastSocket(Peer.mc_port);
                                        
                    mc_socket.joinGroup(Peer.mc_addr);
                    mc_socket.setLoopbackMode(true);
                    
                    System.out.println("MC Thread Started");
                    
                    while(true){
                        byte[] buff = new byte[BUFF_SIZE];
                        
                        DatagramPacket message_packet = new DatagramPacket(buff, buff.length);
                        mc_socket.receive(message_packet);
                        
                        handleControl(message_packet);  
                    }
                } catch (Exception ex) {
                    System.out.println("Error in MCThread: " + ex);
                }
            }
        };
        
        t.start();
    }
    
    // MC Data Backup Thread
    private void startMDB(){
        Thread t = new Thread(){
            @Override
            public void run(){
                try {
                    MulticastSocket mc_socket = new MulticastSocket(Peer.mdb_port);
                                        
                    mc_socket.joinGroup(Peer.mdb_addr);
                    mc_socket.setLoopbackMode(true);
                    
                    System.out.println("MDB Thread Started");
                    
                    while(true){
                        byte[] buff = new byte[BUFF_SIZE];
                        
                        DatagramPacket message_packet = new DatagramPacket(buff, buff.length);
                        mc_socket.receive(message_packet);
                        
                        handleBackup(message_packet);  
                    }
                } catch (Exception ex) {
                    System.out.println("Error in MDBThread: " + ex);
                }
            }
        };
        
        t.start();
    }
    
    // MC Data Restore Thread
    private void startMDR(){
        Thread t = new Thread(){
            @Override
            public void run(){
                try {
                    MulticastSocket mc_socket = new MulticastSocket(Peer.mdr_port);
                                        
                    mc_socket.joinGroup(Peer.mdr_addr);
                    mc_socket.setLoopbackMode(true);
                    
                    System.out.println("MDR Thread Started");
                    
                    while(true){
                        byte[] buff = new byte[BUFF_SIZE];
                        
                        DatagramPacket message_packet = new DatagramPacket(buff, buff.length);
                        mc_socket.receive(message_packet);
                        
                        handleRestore(message_packet);  
                    }
                } catch (Exception ex) {
                    System.out.println("Error in MDRThread: " + ex);
                }
            }
        };
        
        t.start();
    }
    
    //Handles a message received on the MC
    private void handleControl(DatagramPacket packet){
        //Control Channel Messages don't contain data, so we just ignore the CRLF
        
        String message = new String(packet.getData()).trim();        
        String[] params = message.split(" ");
        
        if(params[1].equals("1.0") && ! params[2].equals(String.valueOf(Peer.id))){
            System.out.println("MC Received: " + message);
            
            //Handle Incoming STORED Messages            
            if(params[0].equals("STORED")){
               ChunkID id = new ChunkID(params[3], new Integer(params[4]));
               
               Chunk c = Peer.tracked.get(id);
               if(c!=null){
                   c.increase_rep_deg();
                   System.out.println("Updated (+1) Replication level of " + id.fileID);
               }
           //Handle Incoming REMOVED Messages
           }else if(params[0].equals("REMOVED")){
               ChunkID id = new ChunkID(params[3], new Integer(params[4]));
               
               Chunk c = Peer.tracked.get(id);
               if(c!=null){
                   c.decrease_rep_deg();
                   System.out.println("Updated (-1) Replication level of " + id.fileID);
               }
           }
        }
    }
    
    //Handles a message received on the MDB
    private void handleBackup(DatagramPacket packet){
        String message = new String(packet.getData());
        String dcrlf = Peer.createCRLF() + Peer.createCRLF();
        int dcrlf_index = message.indexOf(dcrlf);
        
        String header = message.substring(0, dcrlf_index);
        byte[] body = message.substring(dcrlf_index + dcrlf.length()).getBytes();
        
        String[] params = header.trim().split(" ");
        
        if(params[1].equals("1.0") && ! params[2].equals(String.valueOf(Peer.id))){
            //Handle Incoming PUTCHUNK Messages            
            if(params[0].equals("PUTCHUNK")){
                ChunkID id = new ChunkID(params[3], new Integer(params[4]));
               
                Chunk c = new Chunk(id, new Integer(params[5]), body);
                Peer.stored.put(id, c);
                System.out.println("Stored Chunk: " + id.fileID + " " + id.chunkNO);
                
                //Send Store Message
                try{
                    Thread.sleep(random.nextInt(400)); //Wait a random delay from 0-400
                    System.out.println("dicks");
                    DatagramSocket dg_socket = new DatagramSocket();

                    String stored_head = "STORED 1.0 " + Peer.id + " " + id.fileID + " " + id.chunkNO + " " + Peer.createCRLF() + Peer.createCRLF();
            
                    byte[] pack = stored_head.getBytes();
                    DatagramPacket msg = new DatagramPacket(pack, pack.length, Peer.mc_addr, Peer.mc_port);

                    dg_socket.send(msg);
                }catch(Exception ex){
                    System.out.println("Error sending stored message." + ex);
                }
           }
        }
        
    }
    
    //Handles a message received on the MDR
    private void handleRestore(DatagramPacket packet){
        String message = new String(packet.getData());
        String dcrlf = Peer.createCRLF() + Peer.createCRLF();
        int dcrlf_index = message.indexOf(dcrlf);
        
        String header = message.substring(0, dcrlf_index);
        byte[] body = message.substring(dcrlf_index + dcrlf.length()).getBytes();
        
        String[] params = header.trim().split(" ");
        
        
        
    }
}
