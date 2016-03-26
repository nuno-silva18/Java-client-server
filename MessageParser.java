package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MessageParser {
	
	public enum headers {PUTCHUNK, GETCHUNK, STORED, DELETE, CHUNK, REMOVED, TRASH};
	
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
							if(message.startsWith("REMOVED"))
								prot = headers.valueOf("REMOVED");
							else
								prot = headers.valueOf("TRASH");
		return prot;
	}
	
	public String assignChunk() throws IOException {
		String msg = " ";
		
		switch(this.protocol) {
		case PUTCHUNK:
			System.out.println("Received PUTCHUNK!");
			if(saveChunk())
				msg = confirmBackup();
			break;
		case GETCHUNK:
			System.out.println("Received GETCHUNK!");
			msg = confirmRestore();
			break;
		case STORED:
			System.out.println("Received STORED!");
			// Handled by MessageListener class
			break;
		case DELETE:
			//TODO
			break;
		case CHUNK:
			System.out.println("Received chunk!");
			break;
		case REMOVED:
			//TODO
			break;
		case TRASH:
			System.out.println("Invalid message, please try again!");
			break;
		default:
			System.out.println("???... How did this happen...");
			break;
		}
		return msg;
	}
	
	public boolean validChunk(String file_n, int chunk_num) {
		
		String[] tokens = message.split(" ");
		
		if(tokens[1].equals(1.0) && file_n.equals(tokens[2]) && tokens[3].equals(String.valueOf(chunk_num)))
			return true;
		else
			return false;
	}
	
	public String readChunk() {
		String chunk = " ";
		String crlf = Peer.createCRLF();
		
		String[] data = message.split(crlf + crlf);
		
		for(int i = 0; i < data.length; i++) {
			chunk = data[i];
		}
		
		return chunk;
	}
	
	private boolean saveChunk() throws IOException {
		String[] file_n = message.split(" ");
		
		String crlf = Peer.createCRLF();
		String[] data = message.split(crlf + crlf);
		
		String file_id_chunk_num = file_n[2] + "." + file_n[3];
		
		File f_chunk = new File(file_id_chunk_num);
		if(f_chunk.exists()) {
			System.out.println("ERROR: This chunk has already been saved...!");
			return true;
		}
		
		FileOutputStream chunk_body = new FileOutputStream(file_id_chunk_num);
		
		for(int i = 1; i < data.length; i++) {
			if(i != 1)
			{
				chunk_body.write(crlf.getBytes());
				chunk_body.write(crlf.getBytes());	
			}
				chunk_body.write(data[i].getBytes());
		}
		
		chunk_body.close();
		return true;
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
	
	private String confirmBackup() {
		String stored = "STORED";
		String[] tokens = this.message.split(" ");
		
		if(tokens.length < 6)
		{
			System.out.print("???");
			return "";
		}
		
		stored += " " + tokens[1] + " " + tokens[2] + " " + tokens[3] + " " + tokens[5];
		System.out.println(stored);
		return stored;
	}
	
	private String confirmRestore() throws IOException {
		String stored = "CHUNK";
		String [] tokens = this.message.split(" ");
		if(tokens.length < 5)
		{
			System.out.println("???");
			return "";
		}
		stored += " " + tokens[1] + " " + tokens[2] + " " + tokens[3] + " " + tokens[4];
		
		byte[] buff = new byte[64000];
		RandomAccessFile chunk = new RandomAccessFile(tokens[2] + "." + tokens[3], "r");
		chunk.read(buff);
		
		stored += buff;
		return stored;
	}
	
}
