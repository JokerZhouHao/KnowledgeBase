package zhou.hao.main;

import java.io.File;
import java.util.HashMap;

import zhou.hao.service.ZipReaderService;
import zhou.hao.service.ZipWriterService;
import zhou.hao.tools.LocalFileInfo;

/**
 * 
 * @author Monica
 * 
 * 处理原Google-freebaseMap，只留下度不大于1000点，最后输出这些点的edge的zip包
 */


public class Product1000GFreebaseMapEdgeZip {
	
	public static void  product1000GFreebaseMapEdge() {
		ZipReaderService reader = new ZipReaderService(LocalFileInfo.getNodeIdAndKeywordAndEdgeZipPath(), "edges");
//		System.out.println(LocalFileInfo.getBasePath() + File.separator + "data" + File.separator + "edge1000Degree.zip");
//		System.exit(0);
		ZipWriterService writer = new ZipWriterService(LocalFileInfo.getBasePath() + File.separator + "data" + File.separator + "edge1000Degree.zip");
		writer.addZipEntity("edge1000Degree.txt");
		writer.write(reader.readLine());
		writer.write("\n");
		String lineStr = null;
		while((lineStr = reader.readLine()) != null) {
			if(lineStr.split(",").length<=1000) {
				writer.write(lineStr);
			}
			writer.write("\n");
		}
		reader.close();
		writer.close();
	}
	
	public static void main(String[] args) {
		
	}
}
