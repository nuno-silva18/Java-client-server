package aux_classes;

public class Chunk {
	
	private int chunkNumber;
	private byte[] body;
	
	/*
	 * Constructor of class Chunk. Each Chunk object will have a file ID (that identifies the parent file), 
	 * number (that identifies the Chunk object)  
	 * and an array of bytes associated to them (the actual content of the file stored in the Chunk object)
	 */
	public Chunk(int chunkNumber, byte[] body) {
		
		this.chunkNumber = chunkNumber;
		this.body = body;
	}
	
	/*
	 * Class GET functions
	 */
	
	/*
	 * Gets the Chunk object's ID to identify its place in the file's composition
	 * @return Chunk object's ID that identifies its place in the chunks that compose the file
	 */
	public int getChunkNumber() {
		return this.chunkNumber;
	}
	
	/*
	 * Gets the byte content of the Chunk object
	 * @return Byte content of Chunk object
	 */
	public byte[] getBody() {	
		return this.body;
	}
}
