package spatialindex.rtree;

import spatialindex.spatialindex.IData;
import spatialindex.spatialindex.IShape;
import spatialindex.spatialindex.Region;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
public class Data implements IData, Serializable
	{
		int m_id;
		Region m_shape;
		byte[] m_pData;
		int parent;//the parent of the data in Rtree, must be leaf node
		double weight; // spatial distance

		public Data(byte[] pData, Region mbr, int id) { m_id = id; m_shape = mbr; m_pData = pData; }
		public Data(double w, Region mbr, int id) { m_id = id; m_shape = mbr; weight = w; parent = -1; }
		public Data(double w, Region mbr, int id, int parentid) { m_id = id; m_shape = mbr; weight = w; parent = parentid; }

		public int getIdentifier() { return m_id; }
		public int getParent() { return parent;}
		public IShape getShape() { return m_shape; }
		public byte[] getData()
		{
			byte[] data = new byte[m_pData.length];
			System.arraycopy(m_pData, 0, data, 0, m_pData.length);
			return data;
		}
		
		public double getWeight(){
			return weight;
		}
	
		public static void main(String[] args)  throws FileNotFoundException, UnsupportedEncodingException{
			PrintWriter writer = new PrintWriter("nodesAllSmall.txt", "UTF-8");
			for (int i = 0; i < 12081193; i++) {
				writer.println(i);
			}
			writer.close();
		}
}
