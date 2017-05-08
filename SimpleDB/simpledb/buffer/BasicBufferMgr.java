package simpledb.buffer;

import java.util.ArrayList;
import java.util.HashMap;
import simpledb.file.*;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
class BasicBufferMgr {
   private Buffer[] bufferpool;
   private int numAvailable;
   private HashMap<Block, Buffer> bufferPoolMap; 
   public int readcount;
   public int writecount;
   
   /**
    * Creates a buffer manager having the specified number 
    * of buffer slots.
    * This constructor depends on both the {@link FileMgr} and
    * {@link simpledb.log.LogMgr LogMgr} objects 
    * that it gets from the class
    * {@link simpledb.server.SimpleDB}.
    * Those objects are created during system initialization.
    * Thus this constructor cannot be called until 
    * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
    * is called first.
    * @param numbuffs the number of buffer slots to allocate
    */
   BasicBufferMgr(int numbuffs) {
      bufferpool = new Buffer[numbuffs];
      bufferPoolMap = new HashMap<Block,Buffer>();
      numAvailable = numbuffs;
      for (int i=0; i<numbuffs; i++)
         bufferpool[i] = new Buffer();
   }
   
   /**
    * Flushes the dirty buffers modified by the specified transaction.
    * @param txnum the transaction's id number
    */
   synchronized void flushAll(int txnum) {
      for (Buffer buff : bufferpool)
         if (buff.isModifiedBy(txnum))
         buff.flush();
  
   }
   
   /**
    * Pins a buffer to the specified block. 
    * If there is already a buffer assigned to that block
    * then that buffer is used;  
    * otherwise, an unpinned buffer from the pool is chosen.
    * Returns a null value if there are no available buffers.
    * @param blk a reference to a disk block
    * @return the pinned buffer
    */
   synchronized Buffer pin(Block blk) {
      Buffer buff = findExistingBuffer(blk);
      if (buff == null) {
         buff = chooseUnpinnedBuffer();
         //System.out.println(buff);
         if (buff == null)
            throw new BufferAbortException();
         if (buff.block()!=null) {
        	 buff.unpin(); //Because if the buff was already pinned to some other blk , we are changing the pin to new blk, numAvailable was reduced already.
         }
         buff.assignToBlock(blk);       
      }
      if (!buff.isPinned())
         numAvailable--;
      buff.pin();
      bufferPoolMap.put(blk, buff); 
      //System.out.println(bufferPoolMap.size());
      //System.out.println("\nPin-> Number of buffers available: " + numAvailable);
      //System.out.println(returnBufferStatistics());
      return buff;
   }
   
   /**
    * Allocates a new block in the specified file, and
    * pins a buffer to it. 
    * Returns null (without allocating the block) if 
    * there are no available buffers.
    * @param filename the name of the file
    * @param fmtr a pageformatter object, used to format the new block
    * @return the pinned buffer
    */
   synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
      Buffer buff = chooseUnpinnedBuffer();
      if (buff == null)
         throw new BufferAbortException();
      buff.assignToNew(filename, fmtr);
      numAvailable--;
      bufferPoolMap.put(buff.block(), buff);
      buff.pin();
      //System.out.println("\nPin-> Number of buffers available: " + numAvailable);	
      return buff;
   }
   
   /**
    * Unpins the specified buffer.
    * @param buff the buffer to be unpinned
    */
   synchronized void unpin(Buffer buff) {
	  //System.out.println("unpinned is called");
  	  //bufferPoolMap.remove(buff.block());
      buff.unpin();
      if (!buff.isPinned())
         numAvailable++;
      //System.out.println("Unpin-> Number of buffers available: " + numAvailable);

   }
   
   /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   int available() {
      return numAvailable;
   }
   
   private Buffer findExistingBuffer(Block blk) {
      for (Buffer buff : bufferpool) {
         Block b = buff.block();
         if (b != null && b.equals(blk))
            return buff;
      }
      return null;
	   //return bufferPoolMap.get(blk);
   }
   
   private Buffer chooseUnpinnedBuffer() {
	   int unmodified_count = 0;
	   int highestLSN = Integer.MIN_VALUE;
	   Buffer returnBuff=null;
	   if (numAvailable>0){
		  for (Buffer buff : bufferpool) {
		     if (!buff.isPinned() && buff.getLSN()==-1) {
		    	 unmodified_count++;
		    	 
		     }
	   	  }
		  if (numAvailable == unmodified_count) {
			  for (Buffer buff : bufferpool)
				  if (!buff.isPinned())
			         returnBuff=buff;    
		  } else {
			  for (Buffer buff : bufferpool) {
				  if (!buff.isPinned() && buff.getLSN()>=0) {
					  if(buff.getLSN()>highestLSN) {
						  highestLSN = buff.getLSN();
						  returnBuff = buff;
					  }
				  }
			  }
		  }
		 
	   }
	   return returnBuff;
   }
   
   public ArrayList<String> returnBufferStatistics(){
	   ArrayList<String> returnString = new ArrayList<String>();
	   //System.out.println(bufferPoolMap);
	   //System.out.println(bufferPoolMap.size());
	   for(Buffer buff : bufferpool) {
		   //System.out.println("Im here");
		  //System.out.println("buffer :" + buff + " Read count: "+buff.getreadcount()+" Write count: "+buff.getwritecount());
		   returnString.add("buffer :" + buff + " Read count: "+buff.getreadcount()+" Write count: "+buff.getwritecount());
	   }
	   return returnString;
   }

   
/**  
* Determines whether the map has a mapping from  
* the block to some buffer.  
* @paramblk the block to use as a key  
* @return true if there is a mapping; false otherwise  
*/  
   boolean containsMapping(Block blk) {  
	   return bufferPoolMap.containsKey(blk);  
   }
/**  
* Returns the buffer that the map maps the specified block to.  
* @paramblk the block to use as a key  
* @return the buffer mapped to if there is a mapping; null otherwise  
*/  
   Buffer getMapping(Block blk) {  
	   return bufferPoolMap.get(blk);  
   } 

}