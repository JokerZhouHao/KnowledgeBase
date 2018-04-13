package neustore.base;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * An float data. 
 * @author Dingming Wu
 */
public class FloatData implements Data, Serializable {
	public float data;
	
	public FloatData( float _data ) {
		data = _data;
	}
	
	public Object clone() {
		FloatData newData = new FloatData(data);
		return newData;
	}
	
	public int size() { return 4; }
	
	public int maxSize() { return 4;}
	
	public void read(DataInputStream in) throws IOException {
		data = in.readFloat();
	}
	public void write(DataOutputStream out) throws IOException {
		out.writeFloat(data);
	}

	public int compareTo(Data data2) {
		Float i1 = new Float(data);
		Float i2 = new Float( ((FloatData)data2).data );
		return i1.compareTo(i2);
	}
	
	public boolean equals(Object data2) {
		float d = ((FloatData)data2).data;
		return data==d;
	}
}

