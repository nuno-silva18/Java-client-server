package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MCClient {
	
	public static void main(String[] args) throws IOException {
		
		int ttl = 1;
		
		InetAddress mc_addr = InetAddress.getByName("224.0.0.1");
		int mc_port = 9876;
		
		MulticastSocket s_control = new MulticastSocket(mc_port);
		
		s_control.joinGroup(mc_addr);
		
		String m_test = "I'm sending, I'm sending!";
		
		byte[] m_buffer = new byte[1024];
		m_buffer = m_test.getBytes();
		
		DatagramPacket m_packet = new DatagramPacket(m_buffer, m_buffer.length, mc_addr, mc_port);
		
		s_control.setTimeToLive(1);
		s_control.send(m_packet);
		
		s_control.close();

	}

}
