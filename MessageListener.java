package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MessageListener extends Thread {

	private InetAddress addr;
	private int port, chunk_num;
	private byte[] buff;
	private String file;
	
	public MessageListener(InetAddress addr, int port, String file, int chunk_num) {
		
		this.addr = addr;
		this.port = port;
		this.file = file;
		this.chunk_num = chunk_num;
	}
	
	public void run() {
		
		MessageParser message_parsed;
		
		boolean state = true;
		boolean end = true;
		
		do {
			
			buff = new byte[256];
			
			try {
				
				MulticastSocket mc_socket = new MulticastSocket(port);
				mc_socket.joinGroup(addr);
				mc_socket.setLoopbackMode(true);
				
				DatagramPacket message_packet = new DatagramPacket(buff, buff.length);
				mc_socket.receive(message_packet);
				
				String message_string = new String(buff, 0, buff.length);
				
				message_parsed = new MessageParser(message_string);
				if(! (Peer.files_s.get("chunknumber").equals(String.valueOf(chunk_num)) && Peer.files_s.get("filename").equals(file)))
				{
					state = false;
					break;
				}
				
				end = message_parsed.confirmStored();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		while(!end);
		
		if(state)
		{
			int counter = Integer.parseInt(Peer.files_s.get("totalreceived"));
			counter++;
			Peer.files_s.replace("totalReceived", String.valueOf(counter));
		}	
	}	
}
