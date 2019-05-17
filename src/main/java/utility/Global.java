/**
 * 
 */
package utility;

import java.io.BufferedWriter;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.ArrayUtil;

import entity.BFSWidRecoder;
import entity.OptMethod;
import entity.sp.GraphWithWids;
import entity.sp.RunRecord;

/**
 * Global variables for statistics, configurations, file directories, etc.
 * Feel free to custom your own Global variables.
 * @author jieming
 *
 */
public class Global {
	// 用于DBpedia创建wid2pid索引
	public static GraphWithWids graphWithWids = null;
	
	// optimization method
	public static OptMethod optMethod = OptMethod.O1;
	public static String INFINITE_PN_LENGTH_STR = "2147483631";
	
	public static final double WEIGHT_PATH = 0.8;
	public static final double WEIGHT_REV_PATH = 1 - WEIGHT_PATH;
	
	// variable array for counting
	public static long globalStartTime = System.currentTimeMillis();
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
	public static int radius = 1;
	
	/* the maximum runtime threshold for the queries */
	public static long runtimeThreshold = -1;
	
	/* the keyword with least frequency */
	public static int leastFrequentQword = -1;
	
	/* data version suffix of files if you have multiple versions of the same dataset */
	public static String dataVersion = "";
	public static String dataVersionWithoutExtension = "";
	
	public static String TYPE_TEST = "SPBest";
	
	/*   flag      */
	public static String rtreeFlag = ".rtree.";
	public static String diskFlag = ".disk.";
	public static String sccFlag = ".SCC";
	public static String keywordFlag = ".keyword.";
	public static String sccIndexFlag = "p2p_scc";
	public static String dagFile = "DAG";
	
	/* graph statistic info */
	/******************      yago2s       ***************/
//	public static int numPlaces = 4774796;
////	public static int numNodes = 12;//include nodes that are places
//	public static int numNodes0 = 8091179;	//include nodes that are places
//	public static int numNodes = 8091179;	//include nodes that are places
////	public static int numKeywords = 18;
//	public static int numKeywords = 3778457;
//	public static int numSCCs = numNodes + numKeywords;// # of vertx SCCs + # of keywords
//	public static long numSCCs0 = (long)numSCCs;// # of vertx SCCs + # of keywords
////	public static int numSCCs = 30;
////	public static int numContainCoordWordDate = 12;
//	public static int numContainCoordWordDate = 812532;
//	public static int numPid = 4774796;
//	public static int numWid = 518314;
	
	/*******************     DBpedia      ***************/
	public static int numPlaces = 883665;
//	public static int numNodes = 12;//include nodes that are places
	public static int numNodes0 = 8099955;	//include nodes that are places
	public static int numNodes = 8099955;	//include nodes that are places
	public static int numKeywords = 2927050; // 准确2927024
	public static int numSCCs = numNodes + numKeywords;// # of vertx SCCs + # of keywords
	public static long numSCCs0 = (long)numSCCs;// # of vertx SCCs + # of keywords
	public static int numContainCoordWordDate = 1138751;	// 不确定
	public static int numPid = 883665;
	public static int numWid = 518314;
	public static int numEdges = 72193833;
	
	/* rtree index setting parameters */
	public static int rtreeBufferSize = 4096000;
//	public static int rtreePageSize = 16384;	// 200
//	public static int rtreePageSize = 32768;	// 400
	public static int rtreePageSize = 65536;	// 800
//	public static int rtreeFanout = 5;
//	public static int rtreeFanout = 1500;
//	public static int rtreeFanout = 200; // 200
//	public static int rtreeFanout = 400; // 400
	public static int rtreeFanout = 800; // 800
	public static int iindexBufferSize = 4096000;
	public static int iindexPageSize = 128;
	public static boolean iindexIsCreate = false;
	public static boolean iindexIsWeighted = false;
	
	/* input file path */
//	public static String dateSetType = "yago2s" + File.separator;
	public static String dateSetType = "DBpedia" + File.separator;
	
//	public static String inputDirectoryPath = LocalFileInfo.getDataSetPath() + dateSetType + "test" + File.separator;
	public static String inputDirectoryPath = LocalFileInfo.getDataSetPath() + dateSetType + "orginal" + File.separator;
//	public static String inputDirectoryPath = LocalFileInfo.getDataSetPath() + dateSetType + "orginal_2000000" + File.separator;
//	public static String inputDirectoryPath = LocalFileInfo.getDataSetPath() + dateSetType + "orginal_4000000" + File.separator;
//	public static String inputDirectoryPath = LocalFileInfo.getDataSetPath() + dateSetType + "orginal_6000000" + File.separator;
	public static String edgeFile = "edgeYagoVB.txt";
	public static String edgeReverseFile = "edgeReverseYagoVB.txt";
	public static String degreeFile = "degreeYagoVB.txt";
	public static String nodeIdKeywordListFile = "nidKeywordsListMapYagoVB.txt";
	public static String nodeIdKeywordListOnDateFile = "nodeIdKeywordListOnDateMapYagoVB.txt";
	public static String nodeIdKeywordListOnIntDateFile = "nodeIdKeywordListOnIntDateMapYagoVB.txt";
	public static String widOnIntDateFile = "widOnIntDate.txt";
	public static String pidCoordFile = "pidCoordYagoVB.txt";
	public static String pidFile = "placeIdMapYagoVB.txt";
	public static String invertedIndexFile = null;
	public static String allPidFile = "allPid.bin";
	public static String allWidFile = "allWid.bin";
	public static String widMinMaxDateFile = "widMinMaxDate.txt";
	public static String wid2DateNidPairFile = "wid2DateNidPair.bin";
	public static String rtreeNode2PidsFile = "rtreeNode2Pids.bin";
	public static String minMaxDatesFile = "minMaxDates.bin";
	public static String wordFrequencyFile = "wordFrequency.txt";
	
	
	/* output file path */
//	public static String outputDirectoryPath = LocalFileInfo.getDataSetPath() + dateSetType + "testIndex" + File.separator;
	public static String outputDirectoryPath = LocalFileInfo.getDataSetPath() + dateSetType + "orginalIndex" + File.separator;
//	public static String outputDirectoryPath = LocalFileInfo.getDataSetPath() + dateSetType + "orginalIndex_2000000" + File.separator;
//	public static String outputDirectoryPath = LocalFileInfo.getDataSetPath() + dateSetType + "orginalIndex_4000000" + File.separator;
//	public static String outputDirectoryPath = LocalFileInfo.getDataSetPath() + dateSetType + "orginalIndex_6000000" + File.separator;
	public static String rTreePath = "rtree" + File.separator;
	public static String sccFile = "edgeYagoVB.SCC";
	public static String DAGFile = Global.dagFile + Global.sccFlag + Global.keywordFlag + Global.edgeFile;
	public static String placeWNFile = Global.outputDirectoryPath + "placeWN" + Global.rtreeFlag + Global.rtreeFanout + "." + Global.radius + Global.dataVersion;
	public static String wordPNFile = Global.outputDirectoryPath + "wordPN"+ Global.rtreeFlag
			+ Global.rtreeFanout + "." + Global.radius + Global.dataVersion;
	public static String alphaIindexFile = null;
	public static int alphaIindexRTNodeBufferSize = -1;
	public static String tfindexDirectoryPath = null;
	public static String pWReachTimesPath = outputDirectoryPath + "pWReachTimes.gz";
	public static String noRepWReachTimesPath= outputDirectoryPath + "NoRepWReachTimes.bin";
	public static String recPidWidReachPath = outputDirectoryPath + "recPidToWidReach.bin";
	public static String recWidPidReachPath = outputDirectoryPath + "recWidToPidReach.bin";
	public static String recP2NReachPath = outputDirectoryPath + "recP2NReach.bin";
	public static String recRTreeNode2NidReachPath = outputDirectoryPath + "recRTreeNode2NidReach.bin";
	public static String recRTreeLeafNodeContainPidsPath = Global.outputDirectoryPath + "recRtreeLeafNodeContainPids."  + Global.pidCoordFile + Global.rtreeFlag + Global.rtreeFanout + Global.dataVersion + ".bin";
	
	/*	index path	*/
	public static String indexNIdWordDate = "nid_date_wid" + File.separator;
	public static String indexWIdDate = "wid_date" + File.separator;
	public static String indexTFLabel = "tf_label" + File.separator;
//	public static String indexWidPN = "wid_pn_" + String.valueOf(Global.radius) + File.separator;
	public static String indexWidPN = "wid_pn";
	public static String indexRTree = Global.outputDirectoryPath + Global.rTreePath + Global.pidCoordFile + Global.rtreeFlag + Global.rtreeFanout + Global.dataVersion;
	public static int MAX_WORD_FREQUENCY = 1000;
	public static String indexWid2Pid = Global.outputDirectoryPath + "wid_2_pid_reachable_pidDis_fre=" + String.valueOf(Global.MAX_WORD_FREQUENCY) + File.separator;
	public static String indexWid2PidBase = Global.outputDirectoryPath + "wid_2_pid_reachable_pidDis_fre=" + String.valueOf(Global.MAX_WORD_FREQUENCY) + File.separator + "wids_block_";
	public static String indexWid2DateNid = Global.outputDirectoryPath + "wid_2_DateNids";
	
	/* file content delimiter sign */
	public static String delimiterLevel1 = ": ";
	public static String delimiterLevel2 = ",";
	public static String delimiterDate = "#";
	public static String delimiterPound = "#";
	public static String delimiterSpace = " ";
	public static String delimiterLayer = "L";
	public static String signEmptyLayer = "N";
	public static String delimiterCommont = "//"; // comment symbol in our configuration.
	
	/* using for test */
	public static Boolean isDebug = Boolean.FALSE;
	public static long startTime = 0;
	public static long bspStartTime = 0;
	public static long frontTime = 0;
	public static long tempTime = 0;
	public static boolean isFirstRTree = true;
	
	public static Boolean isTest = Boolean.TRUE;
	public static Boolean isOutputTestInfo = Boolean.FALSE;
	public static int testK = 10;
	public static int testOrgSampleNum = 500;
	public static int testSampleNum = testOrgSampleNum;
	
	public static String testSampleFile =  "sample" + File.separator + "testSample";
	public static String testSampleResultFile =  "sample_result" + File.separator + "testSampleResultFile";
	
	public static int curRecIndex = 0;
	public static boolean isFirstReadPn = false;
//	public static int MAX_STORED_STRING_LENGTH = IndexWriter.MAX_STORED_STRING_LENGTH/10;
//	public static int MAX_STORED_STRING_LENGTH = IndexWriter.MAX_STORED_STRING_LENGTH/50;
//	public static int MAX_STORED_STRING_LENGTH = IndexWriter.MAX_STORED_STRING_LENGTH;
	
//	public static int MAX_PN_LENGTH = 10000000;
	public static int MAX_PN_LENGTH = IndexWriter.MAX_STORED_STRING_LENGTH;
	
	public static HashMap<Integer, Integer> minDateSpan = null;
	public static BufferedWriter recReachBW = null;
	public static String fileReachGZip = Global.outputDirectoryPath + "recP2PReachable.gz";
	public static RunRecord rr = new RunRecord();
	
	public static int leftMaxIndexSpan = 50;
	public static int rightMaxIndexSpan = 50;
	public static int maxDateSpan = 1000;
	
	public static Boolean isTestRangeDate = Boolean.FALSE;
	
	public static Map<Integer, Integer> wordFrequency = null;
	public static int DATE_RANGE = 7;	
//	public static int[] WORD_FREQUENCYS = {0, 50, 100, 500, 1000, 5000, 10000, 50000, 100000, 1000000};
	public static int[] WORD_FREQUENCYS = null;
	public static String recordPid2WidSizePath = "recordPid2WidSize.txt";
	
	public static BFSWidRecoder orgBFSWidRecoder = null;
	
	public static void printInputOutputPath() {
		System.out.println(Global.inputDirectoryPath);
		System.out.println(Global.outputDirectoryPath);
	}
	
	public static void main(String[] args) throws Exception{
//		System.out.println(IndexWriter.MAX_STORED_STRING_LENGTH);
		System.out.println(ArrayUtil.MAX_ARRAY_LENGTH);
	}
	
}
