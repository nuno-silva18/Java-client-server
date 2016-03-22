package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MCServer {
	
	private InetAddress mc_addr;
	private InetAddress mdb_addr;
	private InetAddress mdr_addr;
	private int mc_port;
	private int mdb_port;
	private int mdr_port;
	
	private MulticastSocket s_control, s_db, s_dr;
	
	public MCServer(String mc_addr, int mc_port, String mdb_addr, int mdb_port, String mdr_addr, int mdr_port) throws IOException {
		
		// Save multicast control socket's IP address and port number
		this.mc_addr = InetAddress.getByName(mc_addr);
		this.mc_port = mc_port;
		
		// Save multicast data backup socket's IP address and port number
		this.mdb_addr = InetAddress.getByName(mdb_addr);
		this.mdb_port = mdb_port;
		
		// Save multicast data recovery socket's IP address and port number
		this.mdr_addr = InetAddress.getByName(mdr_addr);
		this.mdr_port = mdr_port;
		
		// Initialize sockets
		this.s_control = new MulticastSocket(this.mc_port);
		s_control.setTimeToLive(1);
		
		this.s_db = new MulticastSocket(this.mdb_port);
		s_db.setTimeToLive(1);
		
		this.s_dr = new MulticastSocket(this.mdr_port);
		s_dr.setTimeToLive(1);
		
	}
	
	public void start() throws IOException {
		
		// Join multicast group of each socket
		s_control.joinGroup(this.mc_addr);
		s_db.joinGroup(this.mdb_addr);
		s_dr.joinGroup(this.mdr_addr);
		
		byte m_buf[] = new byte[1024]; // Create temporary buffer to store multicast messages
		
		while(true) {
			DatagramPacket m_packet = new DatagramPacket(m_buf, m_buf.length);
			s_control.receive(m_packet);
			
			m_buf = m_packet.getData();
			String m_str = new String(m_buf, 0, m_packet.getLength());
			System.out.println(m_str);
		}
	}
	
	public static void main(String args[]) throws IOException {
		
		MCServer test = new MCServer("224.0.0.1", 9876, "224.0.0.2", 9875, "224.0.0.4", 9874);
		test.start();
	}
}
