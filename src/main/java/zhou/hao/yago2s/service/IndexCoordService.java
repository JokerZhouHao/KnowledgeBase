package zhou.hao.yago2s.service;

import java.util.ArrayList;

import org.apache.lucene.queryparser.flexible.standard.config.PointsConfigListener;

import sil.rtree.RTree;
import sil.spatialindex.IEntry;
import sil.spatialindex.IShape;
import sil.spatialindex.Point;
import sil.storagemanager.MemoryStorageManager;
import sil.storagemanager.PropertySet;
import zhou.hao.service.ZipBase64ReaderService;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.TimeStr;

/**
 * @author Monica
 * @since 2018/03/08
 * 功能：为YagoVB.zip里的坐标数据建立RTree
 */
public class IndexCoordService {
	
	private RTree rTree = null;
	private String zipPath = null;
	private String entryName = null;
	
	public IndexCoordService(String zipPath, String entryName) {
		this.zipPath = zipPath;
		this.entryName = entryName;
	}
	
	// 建立RTree
	public void buildRTree() {
		long startTime = System.currentTimeMillis();
		ZipBase64ReaderService reader = new ZipBase64ReaderService(zipPath, entryName);
		String lineStr = reader.readLine();
		int nodeNum = Integer.parseInt(lineStr.substring(0, lineStr.length()-1));
		System.out.println("> 开始为zip文件" + zipPath + "里的文件" + entryName + "中的"  + nodeNum + "个坐标创建RTree . . .  " + TimeStr.getTime());
		
		rTree = new RTree(new PropertySet(), new MemoryStorageManager());
		int k, nodeId, i;
		String[] tempStr = null;
		
		// 开始解析行
		while(null != (lineStr = reader.readLine())) {
			double[] coord = new double[2];
			k = lineStr.indexOf(':');
			nodeId = Integer.parseInt(lineStr.substring(0, k));
			tempStr = lineStr.substring(k+2, lineStr.length()).split(" ");
			for(i=0; i<2; i++)
				coord[i] = Double.parseDouble(tempStr[i]);
			rTree.insertData(null, new Point(coord), nodeId);
		}
		System.out.println("> 完成为zip文件" + zipPath + "里的文件" + entryName + "中的" + nodeNum + "个坐标创建RTree ！！！  " + TimeStr.getTime() + ", 花时" + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()));
		System.out.println();
		reader.close();
	}
	
	// 初始化查询
	public void initGetNext(final IShape query) {
		rTree.initGetNext(query);
	}
	
	// getNext实现
	public IEntry getNext() {
		return rTree.getNext();
	}
	
	// 主函数
	public static void main(String[] args) {
//		IndexCoordService ser = new IndexCoordService(LocalFileInfo.getDataSetPath() + "YagoVB.zip", "pidCoordYagoVB.txt");
		IndexCoordService ser = new IndexCoordService(LocalFileInfo.getDataSetPath() + "test.zip", "Coord");
		ser.buildRTree();
		
		double[] pCoord = new double[2];
		pCoord[0] = 3;
		pCoord[1] = 2;
		IEntry ie = null;
		ser.initGetNext(new Point(pCoord));
		double[] co1 = {5, 7};
		Point po = new Point(co1);
		while(null != (ie = ser.getNext())) {
			System.out.print(ie.getIdentifier() + " : ( " + ie.getShape().getCenter()[0] + ", " + ie.getShape().getCenter()[1] + " )  ");
			System.out.println(po.getMinimumDistance(ie.getShape()));
		}
		
	}
}