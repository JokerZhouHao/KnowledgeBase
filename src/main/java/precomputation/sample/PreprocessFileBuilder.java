package precomputation.sample;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import entity.BFSWidRecoder;
import entity.sp.AllPidWid;
import entity.sp.date.MinMaxDateService;
import entity.sp.date.Wid2DateNidPairIndex;
import entity.sp.reach.P2NRTreeReach;
import entity.sp.reach.RTreeLeafNodeContainPids;
import entity.sp.reach.W2PIndex;
import precomputation.alpha.WordPNIndexBuilder;
import precomputation.rechable.TFlabelDataFormatter;
import precomputation.rtree.RTreeService;
import precomputation.sp.IndexNidKeywordsListService;
import utility.Global;
import utility.LocalFileInfo;

public class PreprocessFileBuilder {
	public static void build(int nodeNum) throws Exception{
		System.out.println("> 开始创建" + String.valueOf(nodeNum) + "个节点的子图所需的文件 . . . . ");
		
		// 创建nid dates wids index
//		IndexNidKeywordsListService.mainToCreateNidWidDataIndex(true);
		
		// wid nid_date_pair index
//		String indexPath = Global.indexWid2DateNid;
//		String filePath = Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile;
//		Wid2DateNidPairIndex indexWDNP = new Wid2DateNidPairIndex(indexPath);
//		indexWDNP.createIndex(filePath);
		
		// 创建RTree
//		RTreeService.build();
		
		// PN
		int[] radius = {2};
		int[] lens = {1000000, 2147483631};
		for(int r : radius) {
			for(int l : lens) {
				WordPNIndexBuilder.main(new String[] {String.valueOf(r), String.valueOf(l)});
			}
		}
		
//		WordPNIndexBuilder.batchBuildingWN(radius);
		
		// TF-Label
//		TFlabelDataFormatter.build();
		
		// rtree_leaf_node_contain_pid
//		RTreeLeafNodeContainPids.main(null);
		
		// 创建词频文件
//		IndexNidKeywordsListService.buildWordFrequencyFile();
//		Global.wordFrequency = IndexNidKeywordsListService.loadWordFrequency(Global.outputDirectoryPath + Global.wordFrequencyFile);
		
		// 各节点（包括rtree node）到wid的可达性
//		Global.numPid = AllPidWid.getAllPid().size();
//		Global.orgBFSWidRecoder = new BFSWidRecoder(AllPidWid.getAllWid());
//		List<Integer> fres = new ArrayList<>();
//		fres.add(100);
//		fres.add(250);
//		fres.add(500);
//		fres.add(1000);
//		fres.add(10000);
//		fres.add(100000);
//		fres.add(1000000);
//		W2PIndex.batchBuildW2PIndex(fres);
		
		// rtreeNode到pid的可达情况
		P2NRTreeReach.main(null);
		
		// MinMaxDate
		MinMaxDateService.main(null);
		
		System.out.println("> Over创建" + String.valueOf(nodeNum) + "个节点的子图所需的文件! ! ! ");
	}
	
	public static void main(String[] args) throws Exception{
		build(Global.numNodes);
	}
}
