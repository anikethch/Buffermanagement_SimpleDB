package simpledb;

import java.nio.ByteBuffer;

import simpledb.buffer.Buffer;
import simpledb.buffer.BufferMgr;
import simpledb.file.Block;
import simpledb.file.*;
import simpledb.server.SimpleDB;

public class TestBuffer {

	public static void TestBufferMgr() {
		BufferMgr bufferMgr = new BufferMgr(8);
		Block[] blocks = new Block[12];
		//FileMgr fileMgr = new FileMgr("studentdb");
		for (int i=0 ; i< blocks.length; i++) {
			blocks[i]= new Block("file-block",i+1);
		}
		//System.out.println("File Blocks Created");
		
		//System.out.println("number of Buffers available: "+bufferMgr.available());
		
		Buffer[] buffers = new Buffer[10];
		for (String outputstring : bufferMgr.getBufferStatistics()) {
	    	 // System.out.println(outputstring);
	      }
		for (int i = 0; i < 8; i++) {
			buffers[i] = bufferMgr.pin(blocks[i]);
			buffers[i].setInt(10, 20, 1, 8-i);
			buffers[i].getInt(10);
			//System.out.println("Available buffers : " + bufferMgr.available() + "\n");
		}
		   for (int i=0;i<blocks.length;i++) {
		    	  System.out.println(blocks[i].toString() +" : " +bufferMgr.getMapping(blocks[i]));
		      }
		buffers[4].setInt(11, 21, 2, 100);
		//bufferMgr.unpin(buffers[1]);
		bufferMgr.unpin(buffers[4]);
		bufferMgr.unpin(buffers[1]);
		buffers[8]=bufferMgr.pin(blocks[8]);
		buffers[8].setInt(12, 22, 2, 100);
		
	        
	      for (int i=0;i<blocks.length;i++) {
	    	  System.out.println(blocks[i].toString() +" : " + bufferMgr.getMapping(blocks[i]));
	      }
	      for (String outputstring : bufferMgr.getBufferStatistics()) {
	    	  //System.out.println(outputstring);
	      }
	      Page p = new Page();
	      //System.out.println(blocks[1]);
	      //p.write(blocks[1]);
	      //p.write(blocks[2]);
	      bufferMgr.flushAll(5);
	      p.read(blocks[1]);
	      //for (String outputstring : simpledb.FileMgr().getFileStatistics()) {
		    //	  System.out.println(outputstring);
		      //}
	}

}
