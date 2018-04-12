package precomputation.index;

import sil.rtree.RTree;
import sil.spatialindex.IEntry;
import sil.spatialindex.Point;
import sil.storagemanager.DiskStorageManager;
import sil.storagemanager.IStorageManager;
import sil.storagemanager.PropertySet;
import utility.Global;
import zhou.hao.service.ZipReaderService;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.TimeStr;

/**
 * 提供与RTree索引相关的一些方法
 * @author Monica
 *
 */
public class RTreeService extends RTree{
	
	public RTreeService() {}
	public RTreeService(PropertySet ps, IStorageManager sm) {
		super(ps, sm);
	}
	
	/**
	 * 创建索引
	 * @param zipPath
	 * @param entryName
	 * @param fanout
	 * @param rTreeIndexPath
	 * @param pagesize
	 * @throws Exception
	 */
	public void build(String zipPath, String entryName, int fanout, String rTreeIndexPath, int pagesize) throws Exception{
		long startTime = System.currentTimeMillis();
		System.out.println("> 开始创建" + zipPath + "的rTree索引 . . . ");
		
		ZipReaderService reader = new ZipReaderService(zipPath, entryName);
		int nodeId = 0, i, k;
		String[] tempStr = null;
		String lineStr = null;
		
		// 设置ps
		PropertySet ps = new PropertySet();
		ps.setProperty("Overwrite", true);
		ps.setProperty("IndexCapacity", fanout);
		ps.setProperty("LeafCapacity", fanout);
		ps.setProperty("FileName", rTreeIndexPath);
		ps.setProperty("PageSize", pagesize);
		// 设置放文件的地方
		IStorageManager diskfile = new sil.storagemanager.DiskStorageManager(ps);
		RTree rTree = new RTree(ps, diskfile);
		
		reader.readLine();
		while(null != (lineStr = reader.readLine())) {
			double[] coord = new double[2];
			k = lineStr.indexOf(':');
			nodeId = Integer.parseInt(lineStr.substring(0, k));
			tempStr = lineStr.substring(k+2, lineStr.length()).split(" ");
			for(i=0; i<2; i++)
				coord[i] = Double.parseDouble(tempStr[i]);
			rTree.insertData(null, new Point(coord), nodeId);
//			rTree.nodeIdMap.put(nodeId, coord);
		}
		
		boolean ret = rTree.isIndexValid();
		if (ret == false)
			System.err.println("Structure is INVALID!");

		rTree.flush();
		
		System.out.println("> ending创建" + zipPath + "的rTree索引. 用时：" + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()));
	}
	
	
	
	public static void main(String[] args) throws Exception{
		RTreeService ser = new RTreeService();
		
		int fanout = 5;
		String rTreeIndexPath = LocalFileInfo.getDataSetPath() + "testIndex/rtree/";
		int pageSize = 4096;
		// 创建索引
		ser.build(LocalFileInfo.getDataSetPath() + "test.zip", "Coord", fanout, 
				rTreeIndexPath, pageSize);
		
		// 读取索引
		PropertySet psRTree = new PropertySet();
		psRTree.setProperty("FileName", rTreeIndexPath);
		psRTree.setProperty("PageSize", pageSize);
		psRTree.setProperty("fanout", fanout);
		psRTree.setProperty("IndexIdentifier", 1);
		IStorageManager diskfile = new DiskStorageManager(psRTree);
		RTree rTree = new RTree(psRTree, diskfile);
		
		double[] pCoord = new double[2];
		pCoord[0] = 3;
		pCoord[1] = 2;
		IEntry ie = null;
		rTree.initGetNext(new Point(pCoord));
		int num = 0;
		while(null != (ie = rTree.getNext())) {
			System.out.print((++num) + ": " + ie.getIdentifier() + " : ( " + ie.getShape().getCenter()[0] + ", " + ie.getShape().getCenter()[1] + " )\n");
		}
	}
}
