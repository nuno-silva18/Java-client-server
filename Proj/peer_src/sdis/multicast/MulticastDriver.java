package sdis.multicast;

// Performs and handles operations related to the multicast groups

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Arrays;
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
    
    private void checkLoopback(String id, MulticastSocket socket) throws SocketException{
        //On windows the following line breaks the app, 
        //on UbuntuGuest/OSXHost it doesnt seem to make a difference, just leave it commented for now
        //socket.setLoopbackMode(true);
        if(socket.getLoopbackMode()){
            System.out.println("Loopback Mode is Disabled for " + id);
        }else{
            System.out.println("Loopback Mode is Enabled for " + id);
        }
    }
    
    // MC Control Thread
    private void startMC(){
        Thread t = new Thread(){
            @Override
            public void run(){
                try {
                    MulticastSocket mc_socket = new MulticastSocket(Peer.mc_port);
                                        
                    mc_socket.joinGroup(Peer.mc_addr);
                    checkLoopback("MC", mc_socket);
                    
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
                    checkLoopback("MDB", mc_socket);
                    
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
                    checkLoopback("MDR", mc_socket);
                    
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
    private void handleControl(DatagramPacket packet) throws InterruptedException, SocketException, IOException{
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
            //Handle Incoming GETCHUNK Messages
            }else if(params[0].equals("GETCHUNK")){
                ChunkID id = new ChunkID(params[3], new Integer(params[4]));
               
                Chunk c = Peer.stored.get(id);
                if(c!=null){
                    Thread.sleep(random.nextInt(400)); //Wait random between 400...
                    
                    String header = "CHUNK 1.0 " + Peer.id  + " " + id.fileID + " " + id.chunkNO + " " + Peer.createCRLF() + Peer.createCRLF();
                    byte[] body = c.data;
                    
                    byte[] msg = Peer.joinArray(header.getBytes(), body);
                    
                    DatagramSocket ds = new DatagramSocket();
                    DatagramPacket pack = new DatagramPacket(msg, msg.length, Peer.mdr_addr, Peer.mdr_port);
                    
                    ds.send(pack);
                    System.out.println("Sent a Requested Chunk");                    
                }
            }            
        }
    }
    
    //Handles a message received on the MDB
    private void handleBackup(DatagramPacket packet){
        byte[] message = packet.getData();
        int dcrlf_index = -1;
        
        //Fetch crlfcrlf index
        for(int i = 0; i < message.length - 3; i++){
            if(message[i]==0xD && message[i+1]==0xA && message[i+2]==0xD && message[i+3]==0xA){
                dcrlf_index = i;
            }
        }        
        
        String header = new String(message, 0,dcrlf_index);
        byte[] body = Arrays.copyOfRange(message, dcrlf_index+4, packet.getLength());
                
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
        byte[] message = packet.getData();
        int dcrlf_index = -1;
        
        //Fetch crlfcrlf index
        for(int i = 0; i < message.length - 3; i++){
            if(message[i]==0xD && message[i+1]==0xA && message[i+2]==0xD && message[i+3]==0xA){
                dcrlf_index = i;
            }
        }        
        
        String header = new String(message, 0,dcrlf_index);
        byte[] body = Arrays.copyOfRange(message, dcrlf_index+4, packet.getLength());
                
        String[] params = header.trim().split(" ");
        
        if(params[1].equals("1.0") && ! params[2].equals(String.valueOf(Peer.id))){
            System.out.println(header);
            
            //Handle Incoming CHUNK Messages            
            if(params[0].equals("CHUNK") && Peer.restore_wait){
                ChunkID id = new ChunkID(params[3], new Integer(params[4]));
                
                if(id.fileID.equals(Peer.restore_fileID) && id.chunkNO == new Integer(Peer.restore_chunkNO)){
                    Peer.restore_data = body;
                    
                    Peer.restore_wait = false;
                    System.out.println("Fetched data for Chunk: " + id.fileID + "(" + id.chunkNO + ")");
                }
            }
        }        
    }
}
