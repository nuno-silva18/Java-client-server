/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdis.handler;

import java.util.Objects;

/**
 *
 * @author Pmma
 */
public class ChunkID {
    public String fileID;
    public int chunkNO;
    
    public ChunkID(String fileID, int chunkNO){
        this.fileID = fileID;
        this.chunkNO = chunkNO;
    }
    
    //Required for hashmap working properly
    @Override
    public boolean equals(Object other){
        if(other instanceof ChunkID){
           if(((ChunkID) other).chunkNO == chunkNO && ((ChunkID) other).fileID.equals(fileID)){
               return true;
           }
        }
        
        return false;        
    }

 
    //Same reason as equals
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.fileID);
        hash = 71 * hash + this.chunkNO;
        return hash;
    }
    
    
}
