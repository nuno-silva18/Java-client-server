package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

public class FileHandler {
	
	private InetAddress mc_addr, mdb_addr, mdr_addr;
	private int mc_port, mdb_port, mdr_port;
	
	public FileHandler(String mc_addr, int mc_port, String mdb_addr, int mdb_port, String mdr_addr, int mdr_port) throws UnknownHostException {
		
		this.mc_addr = InetAddress.getByName(mc_addr);
		this.mc_port = mc_port;
			
		this.mdb_addr = InetAddress.getByName(mdb_addr);
		this.mdb_port = mdb_port;
			
		this.mdr_addr = InetAddress.getByName(mdr_addr);
		this.mdr_port = mdr_port;
			
	}
	
	private void backup(String file_n, int rep_degree) throws IOException, InterruptedException {
		
		String crlf =  Peer.createCRLF();
		HashMap<Integer, byte[]> file_map;
		String header;
		
		
		FilePartition file_p = new FilePartition(file_n);
		file_map = file_p.getFileMap();
		
		for(int i = 0; i < file_map.size(); i++) {
			
			String i_string = String.valueOf(i);
			
			header = "PUTCHUNK 1.0 " + file_n + " " + i_string + " " + String.valueOf(rep_degree) + " " + crlf + crlf + file_map.get(i);
			
			for(int j = 0; j < rep_degree; j++) {
				
				Peer.files_s.replace("filename", file_n);
				Peer.files_s.replace("chunknumber", i_string);
				
				DatagramSocket dg_socket = new DatagramSocket();
				DatagramPacket msg = new DatagramPacket(header.getBytes(), header.getBytes().length, mdb_addr, mdb_port);
				dg_socket.send(msg);
				
				System.out.println("Package sent with message: " + header);
				Thread.sleep(500);
				if(storedChunkConfirmation(rep_degree, file_n, i)) {
					System.out.println("FILEMANAGER: obtained the desired repetition degree!");
					Peer.files_s.replace("confirmationCount", "0");
					break;
				}	
			}
		}
	}
	
	public void sendConfirmation(String msg) throws IOException {
		if(msg.startsWith("STORED"))
			sendMC(msg);
	}
	
	public void sendMC(String msg) throws IOException {
		
		byte[] msg_bytes = msg.getBytes();
		
		DatagramSocket dg_socket = new DatagramSocket();
		DatagramPacket msg_packet = new DatagramPacket(msg_bytes, msg_bytes.length, this.mc_addr, this.mc_port);
		dg_socket.send(msg_packet);
		System.out.println("Peer sent packet with message: " + msg);
	}
		
	public boolean storedChunkConfirmation(int rep_degree, String file_n, int chunk_num) {
		int confirm_counter = 0;
			
		try {	
			MessageListener msg_listener = new MessageListener(mc_addr, mc_port, file_n, chunk_num);
			msg_listener.start();
			synchronized (msg_listener) {
				msg_listener.wait(500);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			confirm_counter = Integer.parseInt(Peer.files_s.get("totalReceived"));
			if(confirm_counter < rep_degree)
				return false;
			else
				return true;		
	}	
}
