package zhou.hao.main;

import java.io.File;
import java.util.Date;

import zhou.hao.service.IndexNodeMapService;
import zhou.hao.service.ZipReaderService;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.TimeStr;

public class IndexNodeMapMain {

	public static void buildIndex() {
		Long startTime = System.currentTimeMillis();
		ZipReaderService keywordReader = new ZipReaderService(LocalFileInfo.getKeywordBlankZipPath());
		ZipReaderService edgeReader = new ZipReaderService(LocalFileInfo.getEdgeBlankZipPath());
		
//		IndexNodeMapService indexNodeMapService = new IndexNodeMapService(LocalFileInfo.getIndexPath());
		IndexNodeMapService indexNodeMapService = new IndexNodeMapService(LocalFileInfo.getSampleIndexPath());
		
		indexNodeMapService.openIndexWriter();
		String[] nodeNumAndEdgeNum = edgeReader.readLine().split("#");
		int nodeNum = Integer.parseInt(nodeNumAndEdgeNum[0]);
		int edgeNum = Integer.parseInt(nodeNumAndEdgeNum[1]);
		int keywordNum = Integer.parseInt(keywordReader.readLine().split("#")[1]);
		System.out.println("nodeNum = " + nodeNum);
		for(int i=0; i<nodeNum; i++) {
			indexNodeMapService.addDoc(i, keywordReader.readLine(), edgeReader.readLine());
			if(i%19000000==0) System.out.println("> 已处理：" + i + "条, 用时" + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()) + "\n");
			if(i==2000000) {
				System.out.println("> 已经处理完" + i + "条的索引，退出！");
				break;
			}
		}
		keywordReader.close();
		edgeReader.close();
		indexNodeMapService.closeIndexWriter();
		
		indexNodeMapService.openIndexReader();
		System.out.println("总节点数：" + nodeNum + 
							"\n总keyword数：" + keywordNum +
							"\n总边数：" + edgeNum +
							"\nkeywords的平均长度：" + indexNodeMapService.getKeywordAvgLen() +
							"\nkeywords分词后------------------------------" + 
							"\n总word数（n个相同的word算1个）：" + indexNodeMapService.getWordNumNoContainSame() +
							"\n总word数（n个相同的word算n个）：" + indexNodeMapService.getWordNumContainSame() +
							"\n总word数（一个keyword中包含n个相同的word只算为一个）：" + indexNodeMapService.getSumDocFreq());
//		indexNodeMapService.diplayAllWords();
//		System.err.println(indexNodeMapService.searchWordReNodeIds("american").get(0));
//		System.err.println(indexNodeMapService.searchNodeIdReEdges(2));
		indexNodeMapService.closeIndexReader();
	}
	
	// 创建20000000个点的索引
	public static void buildIndex20000000() {
		System.out.println("> 开始创建前2000万个节点的索引  . . . " + TimeStr.getTime());
		Long startTime = System.currentTimeMillis();
		ZipReaderService keywordReader = new ZipReaderService(LocalFileInfo.getKeywordBlankZipPath());
		ZipReaderService edgeReader = new ZipReaderService(LocalFileInfo.getEdgeBlankZipPath());
		IndexNodeMapService indexNodeMapService = new IndexNodeMapService(LocalFileInfo.getBasePath() + File.separator + "data" + File.separator + "index2000");
		
		keywordReader.readLine();
		edgeReader.readLine();
		
		indexNodeMapService.openIndexWriter();
		for(int i=0; i<20000000; i++) {
			indexNodeMapService.addDoc(i, keywordReader.readLine(), edgeReader.readLine());
			if(i%19000000==0) System.out.println("> 已处理：" + i + "条, 用时" + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()) + "\n");
			if(i==2000000) {
				System.out.println("> 已经处理完" + i + "条的索引，退出！");
				break;
			}
		}
		keywordReader.close();
		indexNodeMapService.closeIndexWriter();
		
	}
	public static void main(String[] args) {
//		buildIndex();
//		IndexNodeMapService inms = new IndexNodeMapService(LocalFileInfo.getIndexPath());
//		inms.openIndexReader();
//		inms.getSumDocFreq();
//		System.err.println(inms.getWordNumNoContainSame());
//		inms.closeIndexReader();
	}
}
