package main;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class FilePartition {
	
	RandomAccessFile file;
	int num_chunks;
	int max_buffer_size = 64000;
	HashMap<Integer, byte[]> file_map;
	
	private void makeFileMap(RandomAccessFile file, int max_buffer_size, int chunk_num) throws IOException {
		
		int offset = chunk_num * max_buffer_size;
		byte[] buff = new byte[max_buffer_size];
		file.seek(offset);
		file.read(buff, 0, max_buffer_size);
		file_map.put(chunk_num, buff);
		
	}
	
	public FilePartition(String file_n) throws IOException
	{
		this.file = new RandomAccessFile(file_n, "r");
		this.num_chunks = (int) (this.file.length() / this.max_buffer_size);
		
		if((int) this.file.length() % this.max_buffer_size != 0)
			this.num_chunks++;
		
		if(this.num_chunks < 1)
			this.num_chunks = 1;
		
		for(int dest = 0; dest < this.num_chunks; dest++) {
			makeFileMap(this.file, this.max_buffer_size, dest);
		}
		
		file.close();
	}
	
	public void printFileMap() {
		System.out.print(file_map);
	}
	
	public int getNumChunks() {
		return num_chunks;
	}
	
	public void setNumChunks(int num_chunks) {
		this.num_chunks = num_chunks;
	}
	
	public HashMap<Integer, byte[]> getFileMap() {
		return file_map;
	}
	
	public void setFileMap(HashMap<Integer, byte[]> file_map) {
		this.file_map = file_map;
	}
}

	
