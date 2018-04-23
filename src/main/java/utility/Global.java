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
	
	/* input file path */
	public static String inputDirectoryPath = LocalFileInfo.getDataSetPath() + "test" + File.separator;
	public static String edgeFile = "edgeYagoVB.txt";
	/*nidKeywordsListMapFile is the file containing the documents of all rdf graph vertices*/
	public static String nodeIdKeywordListOnIntDateFile = "nodeIdKeywordListOnIntDateMapYagoVB.txt";
	public static String pidCoordFile = "pidCoordYagoVB.txt";
	public static String invertedIndexFile = null;
	
	/* output file path */
	public static String outputDirectoryPath = LocalFileInfo.getDataSetPath() + "testIndex" + File.separator;
	public static String rTreePath = outputDirectoryPath + "rtree" + File.separator;
	public static String nidToDateWidFile = "nodeIdKeywordListOnIntDateMapYagoVB.txt";
	public static String placeWN = "placeWN";
	public static String alphaPN = "alphaPN";
	public static String alphaIindexFile = null;
	public static int alphaIindexRTNodeBufferSize = -1;
	public static String dagFile = "DAG";
	public static String tfindexDirectoryPath = null;
	
	/*	index path	*/
	public static String indexNIdWordDate = "nid_date_wid" + File.separator;
	public static String indexWIdDate = "wid_date" + File.separator;
	public static String indexTFLabel = "tf-label" + File.separator;
	public static String indexWidToPlaceNeighborhood = "wid_pn" + File.separator;
	public static String indexWidPN = "wid_pn" + File.separator;
	
	/*   flag      */
	public static String rtreeFlag = ".rtree.";
	public static String diskFlag = ".disk.";
	public static String sccFlag = ".SCC";
	public static String keywordFlag = ".keyword";
	public static String sccIndexFlag = "p2p_scc";
	
	/* graph statistic info */
	public static int numPlaces = 12;
	public static int numNodes = 12;//include nodes that are places
	public static int numKeywords = 18;
	public static int numEdges = 16;
	public static int numSCCs = numNodes + numKeywords;// # of vertx SCCs + # of keywords
	
	/* rtree index setting parameters */
	public static int rtreeBufferSize = 4096000;
	public static int rtreePageSize = 16384;
	public static int rtreeFanout = 5;
	public static int iindexBufferSize = -1;
	public static int iindexPageSize = -1;
	public static boolean iindexIsCreate = false;
	public static boolean iindexIsWeighted = false;
	
	/* file content delimiter sign */
	public static String delimiterLevel1 = ": ";
	public static String delimiterLevel2 = ",";
	public static String delimiterDate = "@";
	public static String delimiterPound = "#";// comment symbol in our configuration.
	public static String delimiterSpace = " ";
	public static String delimiterLayer = "L";
	public static String signEmptyLayer = "N";
}
