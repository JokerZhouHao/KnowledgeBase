package utility;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ByteUtiltiy {
	
	public static byte[] listToBytes(List<Integer> li) {
		int byteNum = (1 + li.size()) * 4;
		ByteBuffer bb = ByteBuffer.allocate(byteNum);
		bb.rewind();
		bb.putInt(li.size());
		for(int in : li) {
			bb.putInt(in);
		}
		return bb.array();
	}
	
	public static List<Integer> bytesToList(byte[] bytes){
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		List<Integer> li = new ArrayList<>();
		int size = bb.getInt();
		for(int i=0; i<size; i++) {
			li.add(bb.getInt());
		}
		return li;
	}
}
