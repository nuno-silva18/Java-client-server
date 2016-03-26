package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ChunkRestoration {

	private InetAddress addr;
	private int port, chunk_num;
	private byte[] buff = new byte[64000];
	private String file_n;
	
	public ChunkRestoration(InetAddress addr, int port, String file_n, int chunk_num) {
		this.addr = addr;
		this.port = port;
		this.file_n = file_n;
		this.chunk_num = chunk_num;
	}
	
	public byte[] assignChunk() throws IOException {
		MessageParser msg_parsed;
		
		boolean state = true;
		
		do {
			
			buff = new byte[64000];
			MulticastSocket mc_socket = new MulticastSocket(this.port);
			mc_socket.joinGroup(this.addr);
			mc_socket.setLoopbackMode(true);
			DatagramPacket msg_packet = new DatagramPacket(buff, buff.length);
			mc_socket.receive(msg_packet);
			
			String msg_string = new String(buff);
			msg_parsed = new MessageParser(msg_string);
			
			if(msg_parsed.validChunk(file_n, chunk_num))
			{
				buff = msg_parsed.readChunk().getBytes();
				state = false;
			}
		}while(state);
		return buff;
	}
	
	
}
