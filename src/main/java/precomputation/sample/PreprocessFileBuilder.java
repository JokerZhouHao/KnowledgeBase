package precomputation.sample;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementDecl.GLOBAL;

import entity.BFSWidRecoder;
import entity.sp.AllPidWid;
import entity.sp.GraphWithWids;
import entity.sp.date.MinMaxDateService;
import entity.sp.date.Wid2DateNidPairIndex;
import entity.sp.reach.P2NRTreeReach;
import entity.sp.reach.RTreeLeafNodeContainPids;
import entity.sp.reach.W2PIndex;
import precomputation.alpha.WordPNIndexBuilder;
import precomputation.alpha.WordPNNoDateIndexBuilder;
import precomputation.rechable.TFlabelDataFormatter;
import precomputation.rtree.RTreeService;
import precomputation.sp.IndexNidKeywordsListService;
import utility.FileMakeOrLoader;
import utility.Global;
import utility.LocalFileInfo;
import utility.MLog;

public class PreprocessFileBuilder {
	public static void build(int nodeNum) throws Exception{
		MLog.log("开始创建" + String.valueOf(nodeNum) + "个节点的子图所需的文件 . . . . ");
		MLog.log(Global.inputDirectoryPath);
		MLog.log(Global.outputDirectoryPath);
		Thread.sleep(15000);
		
		// 创建nid dates wids index
		IndexNidKeywordsListService.mainToCreateNidWidDataIndex(true);
		
		// wid nid_date_pair index
		String indexPath = Global.indexWid2DateNid;
		String filePath = Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile;
		Wid2DateNidPairIndex indexWDNP = new Wid2DateNidPairIndex(indexPath);
		indexWDNP.createIndex(filePath);
		
		// 创建记录带有时间关键词的文件
		FileMakeOrLoader.makeWidHasDateFile();
		
		// 创建RTree
		RTreeService.build();
		
		// PN-nodate
//		WordPNNoDateIndexBuilder.buildingWN(1, Integer.parseInt(Global.INFINITE_PN_LENGTH_STR));
//		WordPNNoDateIndexBuilder.buildingWN(2, Integer.parseInt(Global.INFINITE_PN_LENGTH_STR));
		WordPNNoDateIndexBuilder.buildingWN(3, Integer.parseInt(Global.INFINITE_PN_LENGTH_STR));
		
		
		// PN—date
//		int[] radius = {1, 2, 3};
		int[] radius = {3};
//		int[] lens = {100, 10000, 1000000, 2147483631};
//		int[] lens = {1000, 100000, 10000000};
		int[] lens = {10000000};
		for(int r : radius) {
			for(int l : lens) {
				WordPNIndexBuilder.main(new String[] {String.valueOf(r), String.valueOf(l)});
			}
		}
		
		// TF-Label
		TFlabelDataFormatter.build();
		
		// rtree_leaf_node_contain_pid
		RTreeLeafNodeContainPids.main(null);
		
		// 创建词频文件
		IndexNidKeywordsListService.buildWordFrequencyFile();
		Global.wordFrequency = IndexNidKeywordsListService.loadWordFrequency(Global.outputDirectoryPath + Global.wordFrequencyFile);
		
		// 各节点（包括rtree node）到wid的可达性
//		Global.graphWithWids = new GraphWithWids();	// 该变量仅用于DBpedia，因为DBpedia的pid2wid文件太大了，
//													// 如果是yago,请设置为null
//		Global.numPid = AllPidWid.getAllPid().size();	// 用于重新对nid排序后的文件，如果未排序则不需要
		Global.orgBFSWidRecoder = new BFSWidRecoder(AllPidWid.getAllWid());
		List<Integer> fres = new ArrayList<>();
////		 必须按照从小到大添加
		Global.MAX_WORD_FREQUENCY = 1000;	// 为添加的最小值
//		fres.add(0);
//		fres.add(50);
//		fres.add(100);
//		fres.add(250);
//		fres.add(500);
		fres.add(1000);
//		fres.add(10000);
//		fres.add(100000);
//		fres.add(1000000);
		W2PIndex.batchBuildW2PIndex(fres);
		
		// rtreeNode到pid的可达情况
		P2NRTreeReach.main(null);
		
		// MinMaxDate
		MinMaxDateService.main(null);
		
		MLog.log("Over创建" + String.valueOf(nodeNum) + "个节点的子图所需的文件! ! ! ");
	}
	
	public static void main(String[] args) throws Exception{
		build(Global.numNodes);
	}
}
