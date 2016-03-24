package main;

public class MessageParser {
	
	public enum headers {PUTCHUNK, GETCHUNK, STORED, DELETE, CHUNK, REMOVED};
	
	private headers protocol;
	private String message;
	
	public MessageParser(String message) {
		this.message = message;
		System.out.println(message);
		
		protocol = getProtocol(message);
		
	}
	
	public headers getProtocol(String message) {
		headers prot;
		
		if(message.startsWith("PUTCHUNK"))
			prot = headers.valueOf("PUTCHUNK");
		else
			if(message.startsWith("GETCHUNK"))
				prot = headers.valueOf("GETCHUNK");
			else
				if(message.startsWith("STORED"))
					prot = headers.valueOf("STORED");
				else
					if(message.startsWith("DELETE"))
						prot = headers.valueOf("DELETE");
					else
						if(message.startsWith("CHUNK"))
							prot = headers.valueOf("CHUNK");
						else
							prot = headers.valueOf("REMOVED");
	
		return prot;
	}
	
	public boolean confirmStored() {
		String[] message_tokens = message.split(" ");
		
		byte crlf[] = {0xD, 0xA};
		
		byte[] crlf_token = message_tokens[4].getBytes();
		
		if(message_tokens[2].equals(Peer.files_s.get("filename"))
				&& message_tokens[3].equals(Peer.files_s.get("chunknumber"))
				&& (crlf_token[0] == crlf[0] && crlf_token[1] == crlf[1])) {
				return true;
		}
		else
			return false;
	} 
}
