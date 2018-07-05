package precomputation.rtree;

import spatialindex.rtree.RTree;
import spatialindex.spatialindex.IEntry;
import spatialindex.spatialindex.Point;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IBuffer;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;
import spatialindex.storagemanager.TreeLRUBuffer;
import utility.Global;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.DirectoryNotEmptyException;

import file.reader.ZipReader;
import utility.LocalFileInfo;
import utility.TimeUtility;

/**
 * 提供与RTree索引相关的一些方法
 * @author Monica
 *
 */
public class RTreeService{
	
	/**
	 * 创建rTree索引
	 * @throws Exception
	 */
	public static void build() throws Exception{
		if(!new File(Global.inputDirectoryPath).exists()) {
			throw new DirectoryNotEmptyException("目录inputDirectoryPath ： " + Global.inputDirectoryPath + "不存在");
		}
		if(!new File(Global.outputDirectoryPath).exists()) {
			throw new DirectoryNotEmptyException("目录outputDirectoryPath ： " + Global.outputDirectoryPath + "不存在");
		}
		if(!new File(Global.outputDirectoryPath + Global.rTreePath).exists()) {
			new File(Global.outputDirectoryPath + Global.rTreePath).mkdir();
		}
		long startTime = System.currentTimeMillis();
		System.out.println("> 开始为" + Global.pidCoordFile + Global.dataVersion + "创建RTree索引 . . . ");
		String inputfile = Global.inputDirectoryPath + Global.pidCoordFile + Global.dataVersion;
		String treefile = Global.outputDirectoryPath + Global.rTreePath + Global.pidCoordFile + Global.rtreeFlag + Global.rtreeFanout + Global.dataVersion;
		int fanout = Global.rtreeFanout;
		int buffersize = Global.rtreeBufferSize;
		int pagesize = Global.rtreePageSize;
		RTree.build(inputfile, treefile, fanout, buffersize, pagesize);
		System.out.println("> 结束为" + Global.pidCoordFile + Global.dataVersion + "创建RT索引，用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()));
	}
	
	/**
	 * 测试getNext方法
	 * @throws Exception
	 */
	public static void testGetNext() throws Exception{
		String treefile = Global.outputDirectoryPath + Global.rTreePath + Global.pidCoordFile + Global.rtreeFlag + Global.rtreeFanout + Global.dataVersion;
		
		PropertySet psRTree = new PropertySet();
		psRTree.setProperty("FileName", treefile);
		psRTree.setProperty("PageSize", Global.rtreePageSize);
		psRTree.setProperty("fanout", Global.rtreeFanout);
		psRTree.setProperty("IndexIdentifier", 1);
		
		IStorageManager diskfile = new DiskStorageManager(psRTree);
		IBuffer file = new TreeLRUBuffer(diskfile, Global.rtreeBufferSize, false);
		RTree rTree = new RTree(psRTree, file);
		
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
	
	public static void main(String[] args) throws Exception{
		
		RTreeService.build();
//		RTreeService.testGetNext();
	}
}
