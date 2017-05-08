package	simpledb;
import simpledb.tx.Transaction;
import simpledb.query.*;
import simpledb.server.SimpleDB;


public class TestDriver {
	public static void main(String[] args) {
		try {
			// analogous to the driver
			SimpleDB.init("studentdb");
			
			// analogous to the connection
			Transaction tx = new Transaction();
			
			TestBuffer.TestBufferMgr();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
