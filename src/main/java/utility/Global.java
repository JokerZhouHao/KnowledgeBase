/**
 * 
 */
package utility;

import java.io.File;

/**
 * Global variables for statistics, configurations, file directories, etc.
 * Feel free to custom your own Global variables.
 * @author jieming
 *
 */
public class Global {
	// variable array for counting
	public static int[] count = new int[10];
	/**
	 * number of nodes accessed; 
	 * number of all places applied computeGraphDistance; 
	 * number of valid place candidate; 
	 * # of compute shortest path between place and qwords (same as the second one above)
	 * reachableTester invocation times
	 * reachable Pruned number of vertices
	 * */
	//The statistic for runtime
	public static long[] runtime = new long[10];
	
	// radius
	public final static int radius = 3;
	
	public static long startTime = -1;
	/* the maximum runtime threshold for the queries */
	public static long runtimeThreshold = -1;
	
	/* the keyword with least frequency */
	public static int leastFrequentQword = -1;
	
	/* data version suffix of files if you have multiple versions of the same dataset */
	public static String dataVersion = "";
	public static String dataVersionWithoutExtension = "";
	
	/*   flag      */
	public static String rtreeFlag = ".rtree.";
	public static String diskFlag = ".disk.";
	public static String sccFlag = ".SCC";
	public static String keywordFlag = ".keyword.";
	public static String sccIndexFlag = "p2p_scc";
	public static String dagFile = "DAG";
	
	/* graph statistic info */
//	public static int numPlaces = 12;
	public static int numPlaces = 4774796;
//	public static int numNodes = 12;//include nodes that are places
	public static int numNodes = 8091179;	//include nodes that are places
//	public static int numKeywords = 16;
	public static int numKeywords = 3778457;
	public static int numEdges = 50415307;
	public static int numSCCs = numNodes + numKeywords;// # of vertx SCCs + # of keywords
//	public static int numSCCs = 30;
//	public static int numContainCoordWordDate = 12;
	public static int numContainCoordWordDate = 812532;
	public static int numTestSample = 100;
	
	/* rtree index setting parameters */
	public static int rtreeBufferSize = 4096000;
	public static int rtreePageSize = 16384;
//	public static int rtreeFanout = 5;
	public static int rtreeFanout = 400;
	public static int iindexBufferSize = 4096000;
	public static int iindexPageSize = 128;
	public static boolean iindexIsCreate = false;
	public static boolean iindexIsWeighted = false;
	
	/* input file path */
//	public static String inputDirectoryPath = LocalFileInfo.getDataSetPath() + "test" + File.separator;
	public static String inputDirectoryPath = LocalFileInfo.getDataSetPath() + "orginal" + File.separator;
	public static String edgeFile = "edgeYagoVB.txt";
	public static String nodeIdKeywordListFile = "nidKeywordsListMapYagoVB.txt";
	public static String nodeIdKeywordListOnDateFile = "nodeIdKeywordListOnDateMapYagoVB.txt";
	public static String nodeIdKeywordListOnIntDateFile = "nodeIdKeywordListOnIntDateMapYagoVB.txt";
	public static String widOnIntDateFile = "widOnIntDate.txt";
	public static String pidCoordFile = "pidCoordYagoVB.txt";
	public static String invertedIndexFile = null;
	public static String testSampleFile = String.valueOf(numTestSample) + "." + "testSample";
	
	/* output file path */
//	public static String outputDirectoryPath = LocalFileInfo.getDataSetPath() + "testIndex" + File.separator;
	public static String outputDirectoryPath = LocalFileInfo.getDataSetPath() + "orginalIndex" + File.separator;
	public static String rTreePath = "rtree" + File.separator;
	public static String sccFile = "edgeYagoVB.SCC";
	public static String DAGFile = Global.dagFile + Global.sccFlag + Global.keywordFlag + Global.edgeFile;
	public static String placeWNFile = Global.outputDirectoryPath + "placeWN" + Global.rtreeFlag + Global.rtreeFanout + "." + Global.radius + Global.dataVersion;
	public static String wordPNFile = Global.outputDirectoryPath + "wordPN"+ Global.rtreeFlag
			+ Global.rtreeFanout + "." + Global.radius + Global.dataVersion;
	public static String alphaIindexFile = null;
	public static int alphaIindexRTNodeBufferSize = -1;
	public static String tfindexDirectoryPath = null;
	
	/*	index path	*/
	public static String indexNIdWordDate = "nid_date_wid" + File.separator;
	public static String indexWIdDate = "wid_date" + File.separator;
	public static String indexTFLabel = "tf_label" + File.separator;
	public static String indexWidPN = "wid_pn" + File.separator;
	public static String indexRTree = Global.outputDirectoryPath + Global.rTreePath + Global.pidCoordFile + Global.rtreeFlag + Global.rtreeFanout + Global.dataVersion;

	/* file content delimiter sign */
	public static String delimiterLevel1 = ": ";
	public static String delimiterLevel2 = ",";
	public static String delimiterDate = "#";
	public static String delimiterPound = "#";
	public static String delimiterSpace = " ";
	public static String delimiterLayer = "L";
	public static String signEmptyLayer = "N";
	public static String delimiterCommont = "//"; // comment symbol in our configuration.
}
