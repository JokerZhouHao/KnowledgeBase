package zhou.hao.yago2s.service;

import java.util.ArrayList;

import org.apache.lucene.queryparser.flexible.standard.config.PointsConfigListener;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

import gnu.trove.TIntProcedure;
import zhou.hao.service.ZipBase64ReaderService;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.TimeStr;

/**
 * @author Monica
 * @since 2018/03/08
 * 功能：为YagoVB.zip里的坐标数据建立RTree
 */
public class IndexCoordService {
	
	private SpatialIndex rTree = null;
	private String zipPath = null;
	private String entryName = null;
	private ArrayList<Point> pointList = new ArrayList<>();
	
	public class PointAndId{
		private Point point = null;
		private int id = 0;
		public PointAndId(Point point, int id) {
			super();
			this.point = point;
			this.id = id;
		}
		public Point getPoint() {
			return point;
		}
		public void setPoint(Point point) {
			this.point = point;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		
	}
	
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
		
		int i, k1, k2;
		float x, y;
		int tempNodeId = 0;
		rTree = new RTree();
		rTree.init(null);
		
		for(i=0; i<nodeNum; i++)
			pointList.add(null);
		
		// 开始解析行
		for(i=0; i<nodeNum; i++) {
			lineStr = reader.readLine();
			k1 = lineStr.indexOf(':');
			tempNodeId = Integer.parseInt(lineStr.substring(0, k1));
			k1 += 2;
			k2 = lineStr.indexOf(' ', k1);
			x = Float.parseFloat(lineStr.substring(k1, k2));
			y = Float.parseFloat(lineStr.substring(k2+1, lineStr.length()));
//			System.out.println(tempNodeId + " (" + tempP.x + ", " + tempP.y + ")");
			rTree.add(new Rectangle(x, y, x, y), tempNodeId);
			pointList.set(tempNodeId, new Point(x, y));
		}
		System.out.println("> 完成为zip文件" + zipPath + "里的文件" + entryName + "中的" + nodeNum + "个坐标创建RTree ！！！  " + TimeStr.getTime() + ", 花时" + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()));
		System.out.println();
		reader.close();
	}
	
	// 计算两个点之间的距离
	protected float getTwoPointDis(Point p1, Point p2) {
		return (float)Math.sqrt((p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y));
	}
	
	// 查找最近的N个点
	public ArrayList<PointAndId> nearestN(Point p, int n){
		if(null == rTree)	this.buildRTree();
		ArrayList<PointAndId> resList = new ArrayList<>();
		rTree.nearestN(p, new TIntProcedure() {
			
			@Override
			public boolean execute(int i) {
//				System.out.println(("Point " + i + " " + pointList.get(i) + ", distance=" + getTwoPointDis(pointList.get(i), p)));
				resList.add(new PointAndId(pointList.get(i), i));
				return Boolean.TRUE;
			}
		}, n, Float.MAX_VALUE);
		return resList;
	}
	
	// 获得所有的点
	public Point getPoint(int poId) {
		return pointList.get(poId);
	}
	
	// 主函数
	public static void main(String[] args) {
		IndexCoordService ser = new IndexCoordService(LocalFileInfo.getDataSetPath() + "YagoVB.zip", "pidCoordYagoVB.txt");
//		IndexCoordService ser = new IndexCoordService(LocalFileInfo.getDataSetPath() + "test.zip", "test.txt");
//		ser.buildRTree();
		ser.nearestN(new Point(33.84833f, 35.58278f), 50);
//		ser.nearestN(new Point(2, 2), 3);
//		ser.testRectDis();
	}
}