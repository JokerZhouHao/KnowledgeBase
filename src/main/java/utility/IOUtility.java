package utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 简化创建IO相关类
 * @author Monica
 *
 */
public class IOUtility {
	public static BufferedReader getBR(String fp) throws Exception{
		return new BufferedReader(new FileReader(fp));
	}
	
	public static BufferedWriter getBW(String fp) throws Exception{
		return new BufferedWriter(new FileWriter(fp));
	}
	
	public static DataOutputStream getDos(String fp) throws Exception{
		return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fp)));
	}
	
	public static DataInputStream getDis(String fp) throws Exception{
		return new DataInputStream(new BufferedInputStream(new FileInputStream(fp)));
	}
	
	public static DataOutputStream getDGZos(String fp) throws Exception{
		return new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(fp))));
	}
	
	public static DataInputStream getDGZis(String fp) throws Exception{
		return new DataInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(fp))));
	}
}
