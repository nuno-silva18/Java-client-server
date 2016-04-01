package sdis.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Random;
import sdis.Peer;
import sdis.handler.Chunk;
import sdis.handler.ChunkID;

public class MultiCastDriverMC extends Thread{
    private final int BUFF_SIZE = 256000;
        
    private Random random = new Random();    
    
    @Override
    public void run(){
        try {
            synchronized(this){
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
            }
        } catch (Exception ex) {
            System.out.println("Error in MCThread: " + ex);
        }
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

                Chunk ct = Peer.tracked.get(id);
                if(ct!=null){
                    ct.decrease_rep_deg();
                    System.out.println("Updated (-1) Replication level of " + id.fileID);
                }
                
                Chunk cs = Peer.stored.get(id);
                if(cs!=null){
                    cs.decrease_rep_deg();
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
            //Handle Incoming DELETE Messages
            }else if(params[0].equals("DELETE")){
                String fileID = params[3];
                int tc = 0; // Tracked Count
                int sc = 0; // Stored Count
                
                System.out.println("Removing Chunks for FileID: " + fileID);
                

                //Remove Tracking line Chunks
                for (ChunkID it : Peer.tracked.keySet()) {
                    if(it.fileID.equals(fileID)){
                        Peer.tracked.remove(it);
                        tc++;
                    }
                }  
                System.out.printf("Removed %d Chunks from Tracking Line\n", tc);

                //Remove Stored line Chunks
                for (ChunkID it : Peer.stored.keySet()) {
                    if(it.fileID.equals(fileID)){
                        Peer.stored.remove(it);
                        sc++;
                    }
                }  
                System.out.printf("Removed %d Chunks from Storage Line\n", sc);

            }                   
        }
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
}
