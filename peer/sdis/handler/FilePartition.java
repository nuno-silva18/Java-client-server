package sdis.handler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class FilePartition {
	
	RandomAccessFile file;
	int num_chunks;
	int max_buffer_size = 64000;
	ConcurrentHashMap<Integer, byte[]> file_map = new ConcurrentHashMap<>();
	
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
	
	public ConcurrentHashMap<Integer, byte[]> getFileMap() {
            return file_map;
	}
}

	
