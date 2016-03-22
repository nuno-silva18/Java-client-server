package aux_classes;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FilePartition {
	private ArrayList<Chunk> chunks = new ArrayList<>();
	
	public void splitFile(File f) throws IOException {
		
		int partCounter = 1; // Integer used to name the file (file name will end in "00" + partCounter)
        int fileSize = 64000 ; // 64 KB
        byte[] buffer = new byte[fileSize]; // buffer where chunks will be saved
        
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {
            int tmp = 0; // Integer used to save the chunk's byte size
            while ((tmp = bis.read(buffer)) > 0) {
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    out.write(buffer, 0, tmp); // Writes tmp number of bytes into byte array buffer
                    Chunk newChunk = new Chunk(partCounter, out.toByteArray()); // Create new Chunk object with chunk ID equal to partCounter and body equal to bytes written to ByteArrayOutputStream out  
                    this.chunks.add(newChunk); // Adds chunk written to ArrayList chunks
                    partCounter++; // Increments the partCounter which is used as the chunk ID for Chunk objects
                  
                }
            }
        } catch (FileNotFoundException e) { //Exception handling
            e.printStackTrace();
        } catch (IOException e) { // Exception handling
            e.printStackTrace();
        }
    }
	
	/*
	 * Class GET functions
	 */
	
	/*
	 * Gets the array list of Chunk objects that compose the partitioned file
	 * @return array list of Chunk objects that compose the partitioned file
	 */
	
	public ArrayList<Chunk> getChunks() {
		return this.chunks;
	}
	
	public static int writeChunks(ArrayList<Chunk> chunks, File f) {
		
		for(int i = 0; i < chunks.size(); i++) {
			
			File fchunk = new File(f.getParent(), f.getName() + "." + String.format("%03d", chunks.get(i).getChunkNumber()));
			try (FileOutputStream out = new FileOutputStream(fchunk)) {
                out.write(chunks.get(i).getBody(), 0, chunks.get(i).getBody().length);
		} catch (IOException e) {
			e.printStackTrace();
		} 
			}
		
		return 0;
	}
	
	public static void main(String args[]) throws IOException {
		
		File testf = new File("C:\\Users\\Nuno Silva\\workspace\\project1_sdis\\src\\aux_classes\\master.of.none.s01e01.720p.webrip.x264-2hd[ettv].mkv");
		
		FilePartition test = new FilePartition();
		try {
			test.splitFile(testf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(writeChunks(test.getChunks(), testf) == 0)
			System.out.println("It worked!");
		else
			System.out.println("wut");
	}
}
