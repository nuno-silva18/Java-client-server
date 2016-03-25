package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Random;

public class ThreadListener extends Thread {
	
	private InetAddress addr;
	private int port;
	private byte[] buff = new byte[256];
	private FileHandler confirm_socket;
	
	public ThreadListener(String addr, int port, FileHandler confirm_socket) throws UnknownHostException {
		
		this.addr = InetAddress.getByName(addr);
		this.port = port;
		this.confirm_socket = confirm_socket;
	}
	
	public void run() {
		
		boolean state = true;
		MessageParser msg_parsed;
		
		try {
			MulticastSocket mc_socket = new MulticastSocket(this.port);
			mc_socket.joinGroup(this.addr);
			mc_socket.setLoopbackMode(true);
			
			System.out.println(this.addr + " " + this.port);
			
			while(state) {
				
				DatagramPacket msg_packet = new DatagramPacket(buff, buff.length);
				mc_socket.receive(msg_packet);
				
				String msg_unparsed = new String(buff, 0, buff.length);
				msg_parsed = new MessageParser(msg_unparsed);
				String msg_confirm = msg_parsed.assignChunk();
				
				Random r_time = new Random();
				sleep(r_time.nextInt(400));
				
				ConfirmMessageHandler reply = new ConfirmMessageHandler(msg_confirm, confirm_socket);
				reply.start();
				
				buff = new byte[256];
			}	
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
