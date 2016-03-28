package main;

import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import handlers.FileHandler;
import listeners.ThreadListener;

public class Peer {
	
	static public ConcurrentHashMap<String, String> files_s;

	/**
	 * Function responsible for generating the CRLF token
	 * @return CRLF token as a String object
	 */
	public static String createCRLF() {
		
		byte[] crlf = new byte[2];
		crlf[0] = 0xD;
		crlf[1] = 0xA;
		
		String crlf_s = new String(crlf);
		return crlf_s;
	}
	
	public static void main(String[] args) throws UnknownHostException {
		
		if(args.length != 6)
		{
			System.out.println("Usage: java Peer <Multicast Control Channel>" +
					   " <port> <Multicast Backup Channel> <port> <Multicast Restore Channel> <port>");
			System.exit(1);	
		}
		
		files_s = new ConcurrentHashMap<>();
		files_s.put("filename", " ");
		files_s.put("chunknumber" , "0");
		files_s.put("totalreceived", "0");
		
		String mc_addr = args[0];
		int mc_port = Integer.parseInt(args[1]);
		
		String mdb_addr = args[2];
		int mdb_port = Integer.parseInt(args[3]);
		
		String mdr_addr = args[4];
		int mdr_port = Integer.parseInt(args[5]);
		
		FileHandler fh = new FileHandler(mc_addr, mc_port, mdb_addr, mdb_port, mdr_addr, mdr_port);
		
		ThreadListener mcThread = new ThreadListener(mc_addr, mc_port, fh);
		ThreadListener mdbThread = new ThreadListener(mdb_addr, mdb_port, fh);
		
		mcThread.start();
		mdbThread.start();

	}

}
