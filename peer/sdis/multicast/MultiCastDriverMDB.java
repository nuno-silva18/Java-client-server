package sdis.multicast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Random;
import sdis.Peer;
import sdis.handler.Chunk;
import sdis.handler.ChunkID;

public class MultiCastDriverMDB extends Thread{
    private final int BUFF_SIZE = 256000;
        
    private Random random = new Random();   
    
    @Override
    public void run(){
        try {
            synchronized(this){
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
            }
        } catch (Exception ex) {
            System.out.println("Error in MDBThread: " + ex);
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
               
                //Verify is this chunk is owned by me
                Chunk checked = Peer.tracked.get(id);
                
                if(checked==null){
                    Chunk c = new Chunk(id, new Integer(params[5]), body);
                    c.increase_rep_deg();
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
