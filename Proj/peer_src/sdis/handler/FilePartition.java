package sdis.handler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;

public class FilePartition {
	
	RandomAccessFile file;
	int num_chunks;
	int max_buffer_size = 64000;
	HashMap<Integer, byte[]> file_map = new HashMap<>();
	
	private void makeFileMap(RandomAccessFile file, int max_buffer_size, int chunk_num) throws IOException {
            for(int i = 0; i < chunk_num; i++){
                byte[] buff = new byte[max_buffer_size];
                int readed = file.read(buff);
                
                //Trim Chunk
                byte[] tbuff = Arrays.copyOf(buff, readed);
                
                file_map.put(i, tbuff);                
            }
	}
	
	public FilePartition(String file_n) throws IOException
	{
		this.file = new RandomAccessFile(file_n, "r");
                
		this.num_chunks = (int) (this.file.length() / this.max_buffer_size);
		
		if((int) this.file.length() % this.max_buffer_size != 0)
			this.num_chunks++;
		
		if(this.num_chunks < 1)
			this.num_chunks = 1;
		
		           
                makeFileMap(this.file, this.max_buffer_size, num_chunks);
		
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

	
