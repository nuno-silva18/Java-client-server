package sdis.multicast;

import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Random;
import sdis.Peer;
import sdis.handler.ChunkID;

public class MultiCastDriverMDR extends Thread{
    private final int BUFF_SIZE = 256000;
        
    private Random random = new Random();   
    
    @Override
    public void run(){
        try {
            synchronized(this){
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
            }
        } catch (Exception ex) {
            System.out.println("Error in MDRThread: " + ex);
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
