package zhou.hao.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import zhou.hao.entry.MMap.CanPathList;
import zhou.hao.processor.KShortestPathsProcessor;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.MemoryInfo;
import zhou.hao.tools.TimeStr;

/**
 * 
 * @author Monica
 * @since 2018/01/23
 * 功能：计算最近的k个点
 */
public class KShortestPathMain {
	
	/***********
	 * 主函数
	 * 
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		String paramsStr = "";
		int curDegreeNum, curNodeNum, i, j;
		String indexPath, edgeZipPath;
		while(true) {
			/*******************
			 * 指定文件
			 *******************/
			System.out.print("> 请设置指定的文件  节点数,度数  (节点数为-1退出,单位为万) : ");
			paramsStr = scanner.nextLine();
			System.out.println();
			if(paramsStr.contains(",")) {
				curNodeNum = Integer.parseInt(paramsStr.split(",")[0]);
				curDegreeNum = Integer.parseInt(paramsStr.split(",")[1]);
			} else {
				curNodeNum = Integer.parseInt(paramsStr);
				curDegreeNum = 0;
			}
			if(-1==curNodeNum)	break;
			if(curNodeNum==0)	curNodeNum = LocalFileInfo.gFreeBaseNodeNum;
			else curNodeNum *= 10000;
			
			indexPath = LocalFileInfo.getIndexPath(curNodeNum);
			if(!(new File(indexPath).exists())) {
				System.out.println("> 不存在index : " + indexPath + "\n");
				continue;
			}
			edgeZipPath = LocalFileInfo.getEdgeBlankZipPath(curDegreeNum, curNodeNum);
			if(!(new File(edgeZipPath).exists())) {
				System.out.println("> 不存在edgeZip : " + edgeZipPath + "\n");
				continue;
			}
			
			/*******************
			 * 开始初始化工作
			 *******************/
			System.out.println("> 开始初始化工作 . . . " + TimeStr.getTime() + " " + MemoryInfo.getTotalFreeUsedAvailable() + "\n");
			KShortestPathsProcessor processor = new KShortestPathsProcessor(indexPath, curDegreeNum, curNodeNum);
			processor.init();
			System.out.println("> 完成初始化工作！！！" + TimeStr.getTime() + " " + MemoryInfo.getTotalFreeUsedAvailable() + "\n");
			
			/*******************
			 * 输入k和wordNum
			 *******************/
			while(true) {
				System.out.print("\n> 请输入运行参数(k=0重新设置文件, wordNum=0手动输入), 例如 ：k=10, wordNum=12 : ");
				HashMap<String, String> paramsMap = new HashMap<String, String>();
				int k, wordNum;
				ArrayList<String> wordList = null;
				paramsStr = scanner.nextLine();
				System.out.println();
				args = paramsStr.split(",");
				int argsLen = args.length;
				for(i=0; i<argsLen; i++) {
					String[] strArr = args[i].split("=");
					paramsMap.put(strArr[0].trim(), strArr[1].trim());
				}
				if(paramsMap.get("k")==null) {
					System.out.println("> 请输入参数k !!! ");
					continue;
				}
				k = Integer.parseInt(paramsMap.get("k"));
				if(k<1) {
					System.out.println();
					break;
				}
				if(paramsMap.get("wordNum")==null) {
					System.out.println("> 请输入参数wordNum !!! ");
					continue;
				}
				wordNum = Integer.parseInt(paramsMap.get("wordNum"));
				if(wordNum==0) {
					System.out.print("> 请输入要查找的词，以$隔开 : ");
					paramsStr = scanner.nextLine();
					wordList = new ArrayList<>();
					for(String s : paramsStr.split("\\$"))
						wordList.add(s);
				} else {
					wordList = processor.getSearchedWords(wordNum);
				}
				
//				for(String s: wordList) {
//					System.out.println(s);
//				}
				
				/*******************
				 * 计算k最短路径
				 *******************/
				Long startTime = System.currentTimeMillis();
				System.out.println("> 开始计算shortest的" + k + "个点 . . . " + LocalFileInfo.getMemoryAndTime() + "\n");
				LinkedList<CanPathList> list = processor.findKShortestPaths(k, wordList);
				if(null==list) {
					System.out.println("  > 有searchedWord不在当前的node中！！！");
					continue;
				}
				System.out.println("> 共获得" + list.size() + "个shortest点," + TimeStr.getTime() + " : ");
				if(list.size()!=0) {
					i = 0;
					for(CanPathList li : list) {
						System.out.print((++i) + ". ");
						li.display();
						System.out.println();
					}
				}
				// 释放内存
				System.out.println("\n> 查找shortest的" + k + "个点结束, " + TimeStr.getTime() + ", 花时(包含查词时间) : " + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()));
			}
			
			
		}
		System.out.println("\n> 退出成功  ! ! ! " + TimeStr.getTime());
	}
}
