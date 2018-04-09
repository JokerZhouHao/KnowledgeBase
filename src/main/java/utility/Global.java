/**
 * 
 */
package utility;


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
	
	public static long startTime = -1;
	/*the maximum runtime threshold for the queries*/
	public static long runtimeThreshold = -1;
	
	/*the keyword with least frequency*/
	public static int leastFrequentQword = -1;
	
	/*data version suffix of files if you have multiple versions of the same dataset*/
	public static String dataVersion = null;
	public static String dataVersionWithoutExtension = null;
	/*nidKeywordsListMapFile is the file containing the documents of all rdf graph vertices*/
	public static String nidKeywordsListMapFile = null;
	public static String invertedIndexFile = null;
	public static String pidCoordFile = null;
	public static String edgeFile = ".edge";
	public static String inputDirectoryPath = null;
	public static String outputDirectoryPath = null;
	public static String alphaWN = "alphaWN";
	public static String alphaIindexFile = null;
	public static int alphaIindexRTNodeBufferSize = -1;
	public static String dagFile = "DAG";
	public static String tfindexDirectoryPath = null;
	
	public static String rtreeFlag = ".rtree.";
	public static String diskFlag = ".disk.";
	public static String sccFlag = ".SCC";
	public static String keywordFlag = ".keyword";
	public static String sccIndexFlag = "p2p_scc";
	
	public static int numPlaces = -1;
	public static int numNodes = 12;//include nodes that are places
	public static int numKeywords = 18;
	public static int numEdges = 16;
	public static int numSCCs = numNodes + numKeywords;// # of vertx SCCs + # of keywords
	public static int rtreeBufferSize = -1;
	public static int rtreePageSize = -1;
	public static int rtreeFanout = -1;
	public static int iindexBufferSize = -1;
	public static int iindexPageSize = -1;
	public static boolean iindexIsCreate = false;
	public static boolean iindexIsWeighted = false;
	
	public static String delimiterLevel1 = ": ";
	public static String delimiterLevel2 = ",";
	public static String delimiterPound = "#";// comment symbol in our configuration.
	public static String delimiterSpace = " ";
}
