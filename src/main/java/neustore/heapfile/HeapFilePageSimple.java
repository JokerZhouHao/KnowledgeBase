/**
 * 
 */
package neustore.heapfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import neustore.base.DBPage;


/**
 * @author jmshi
 *
 */
public class HeapFilePageSimple extends DBPage {
	/**
	 * size for the reserved space.
	 * The first 20 bytes of the disk-version of the HeapFilePage are reserved for:
	 * nodeType=1, prev, next, availableBytes, numRecs.
	 */
	protected final int RESERVED = 28;
	
	
	/**
	 * the list of records
	 */
	protected Map<Integer, Float> records;
	
	/**
	 * the next page in the linked list.
	 * -1 if not exist.
	 * Whether this page is in the full-page list or the nonfull-page list
	 * depends on whether this page is full.
	 */
	protected long next;
	
	/**
	 * the previous page in the linked list.
	 * -1 if not exist.
	 * Whether this page is in the full-page list or the nonfull-page list
	 * depends on whether this page is full.
	 */
	protected long prev;
	
	/**
	 * available space in the page
	 */
	protected int availableBytes;
		
	/**
	 * Constructor fo HeapFilePage. Similar to the constructor of {@link HeapFile}, 
	 * here a sample key and a sample data are taken to enable the generic feature
	 * of the index.
	 * 
	 * @param _pageSize     page size
	 * @param _sampleKey    a sample key
	 * @param _sampleData   a sample data
	 */
	public HeapFilePageSimple( int _pageSize) {
		super(1, _pageSize);  // nodeType=1 for all heap file pages
		records = new HashMap<Integer, Float>();
		prev = next = -1;
		availableBytes = pageSize - RESERVED;
	}
	
	/**
	 * Returns the number of records in the page.
	 * @return number of records
	 */
	public int numRecs() { 
		return records.size();
	}
	
	
	/**
	 * Whether the page is full.
	 * 
	 * @return  whether full
	 */
	public boolean isFull() {
		if ( records.size() == 0 ) return false;
		return availableBytes < Integer.SIZE/Byte.SIZE + Float.SIZE/Byte.SIZE;
	}

	/**
	 * Inserts a new record into this page. The caller needs to make sure that
	 * the page has enough space.
	 * @param key   the key part of the new record
	 * @param data  the data part of the new record
	 */
	public void insert(int key, float data) {
		records.put(key, data);
		int usedBytes = Integer.SIZE/Byte.SIZE + Float.SIZE/Byte.SIZE;
		availableBytes -= usedBytes;
		assert availableBytes >= 0;
	}
	
	/**
	 * Searches for a record in the page.
	 * @param   key   the key to search for
	 * @return  the data if found; null otherwise.
	 */
	public Float search( int key ) {
		if (records.containsKey(key)) {
			return records.get(key);
		}
		return null;
	}
	
	
	
	public void read(byte[] page) throws IOException {
		ByteArrayInputStream byte_in = new ByteArrayInputStream(page);
		DataInputStream in = new DataInputStream(byte_in);
		in.readInt();  // skip nodeType
		prev = in.readLong();
		next = in.readLong();
		availableBytes = in.readInt();
		int numRecs = in.readInt();
		records.clear();
		for ( int i=0; i<numRecs; i++ ) {
			int key = in.readInt();
			float weight = in.readFloat();;
			records.put(key, weight);
		}
		
		in.close();
		byte_in.close();
	}

	protected void write(byte[] page) throws IOException {
		ByteArrayOutputStream byte_out = new ByteArrayOutputStream(pageSize);
		DataOutputStream out = new DataOutputStream(byte_out);
		out.writeInt( 1 );
		out.writeLong( prev );
		out.writeLong( next );
		out.writeInt( availableBytes );
		out.writeInt( records.size() );
		
		for (Entry<Integer, Float> entry : this.records.entrySet()) {
			out.writeInt(entry.getKey());
			out.writeFloat(entry.getValue());
		}
		
		byte[] result = byte_out.toByteArray();
		System.arraycopy(result, 0, page, 0, result.length );
		out.close();
		byte_out.close();
	}
	
	public boolean isLastPage(){
		if(next == -1) return true;
		else return false;
	}
	
	public long getNextPageID(){
		return next;
	}
	
	public int getAvailableBytes(){
		return availableBytes;
	}
	
	public void setNextPageID(long n){
		next = n;
	}
	
	public void reset(){
		records.clear();
		availableBytes = pageSize - RESERVED;
	}
	public Map<Integer, Float> getRecords(){
		return this.records;
	}

	/**
	 * A record which consists of a key and a data.
	 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
	class KeyData {
		public KeyData( Key _key, Data _data) {
			key = _key;
			data = _data;
		}
		public Key key;
		public Data data;
	}
	 */

}