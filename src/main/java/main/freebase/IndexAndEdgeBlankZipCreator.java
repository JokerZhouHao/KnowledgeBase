package main.freebase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import precomputation.index.freebase.IndexNodeMapService;
import file.reader.ZipReader;
import file.writer.ZipWriter;
import utility.LocalFileInfo;
import utility.MemoryInfo;
import utility.TimeUtility;

/**
 * @author Monica
 *
 * @since 2018/01/22
 * 功能：创建指定节点数的索引，创建以空格隔开节点id包含的正向和反向边的zip文件
 */
public class IndexAndEdgeBlankZipCreator {
	
	// 放nodeNum和degreeNum
	private ArrayList<Integer> nodeNumList = null;
	private ArrayList<Integer> degreeNumList = null;
	
	
	// 构造
	public IndexAndEdgeBlankZipCreator() {}
	
	public IndexAndEdgeBlankZipCreator(ArrayList<Integer> nodeNumList, ArrayList<Integer> degreeNumList) {
		this.nodeNumList = nodeNumList;
		this.degreeNumList = degreeNumList;
	}
	
	
	/*
	 * 测试索引
	 */
	public void testIndex() {
		IndexNodeMapService indexService = new IndexNodeMapService(LocalFileInfo.getIndexPath(200));
//		indexService.openIndexWriter();
//		indexService.addDoc(1, "v1");
//		indexService.addDoc(2, "v2 v22 v24");
//		indexService.closeIndexWriter();
		
		indexService.openIndexReader();
//		System.out.println(indexService.searchNodeIdReAtt(2));
//		for(int s : indexService.searchWordReNodeIds("v24", 100))
//				System.out.println(s);
//		for(int i=0; i<100; i++) {
			System.out.println(200 + "  " + indexService.searchNodeIdReAtt(200));
//		}
		indexService.closeIndexReader();
		
	}
	
	/*
	 * 测试文件
	 */
	public void testZip() {
		ZipReader zipReader = new ZipReader(LocalFileInfo.getBasePath() + File.separator + "data" + File.separator + "Google-freebase-rdf-latestZH" + File.separator + "edgeBlank.zip");
		zipReader.readLine();
		zipReader.readLine();
		for(String s : zipReader.readLine().split(" ")) {
			System.out.println(s);
		}
		zipReader.close();
	}
	
	
	/**
	 * 创建索引
	 */
	public void createIndex() {
		int i, tempNodeNum;
		String indexPath = null;
		File indexDir = null;
		IndexNodeMapService indexService = null;
		String[] allAttArr = null;
		
		// 初始化allAttArr数组
		tempNodeNum = nodeNumList.get(0);
		System.out.println("  > 开始初始化allAttArr(" + tempNodeNum + "个点) . . . " + LocalFileInfo.getMemoryAndTime());
		allAttArr = new String[tempNodeNum];
		ZipReader reader = new ZipReader(LocalFileInfo.getBasePath() + File.separator + "data" + File.separator + "Google-freebase-rdf-latestZH" + File.separator + "keywordBlank.zip");
		reader.readLine();
		for(i=0; i<tempNodeNum; i++) {
			allAttArr[i] = reader.readLine();
		}
		reader.close();
		System.out.println("  > 完成初始化allAttArr(" + tempNodeNum + "个点) ! ! ! " + LocalFileInfo.getMemoryAndTime() + "\n");
		
		// 创建索引
		for(int in : nodeNumList) {
			indexPath = LocalFileInfo.getIndexPath(in);
			indexDir = new File(indexPath);
			System.out.println("  > 开始创建索引(" + in + "个点) : " + indexPath + LocalFileInfo.getMemoryAndTime());
			if(!indexDir.exists()) {
				indexDir.mkdir();
				indexService = new IndexNodeMapService(indexPath);
				indexService.openIndexWriter();
				for(i=0; i<in; i++) {
					indexService.addDoc(i, allAttArr[i]);
				}
				indexService.closeIndexWriter();
			} else	System.out.println("    > 该索引已存在 ! ! ! ");
			System.out.println("  > 完成创建索引(" + in + "个点) : " + indexPath + LocalFileInfo.getMemoryAndTime() + "\n");
		}
	}
	
	public String getDegreeAndNodeStr(int tempDegreeNum, int tempNodeNum, boolean isStart) {
		if(isStart) 
			return "(" + tempDegreeNum + "度, " + tempNodeNum + "点) . . . " + LocalFileInfo.getMemoryAndTime();
		else
			return "(" + tempDegreeNum + "度, " + tempNodeNum + "点) ! ! ! " + LocalFileInfo.getMemoryAndTime();
		
	}
	
	/**
	 * 写EdgeBlankZip
	 */
	public void writeEdgeBlankZip() {
		ArrayList<Integer>[] allEdgeArr = null;
		ArrayList<Integer>[] allREdgeArr = null;
		int i, j, tempNodeNum, tempDegreeNum, frontNodeNum, tempLen;
		String tempZipPath = null, tempStr = null;
		ArrayList<Integer> tempList = null;
		File tempFile = null;
		ZipWriter zipWriter = null;
		StringBuffer sBuf = null;
		
		tempNodeNum = nodeNumList.get(0);
		tempDegreeNum = degreeNumList.get(0);
		
		/**********************
		 * 初始化allEdgeArr
		 **********************/
		System.out.println("  > 开始初始化allEdgeArr(" + tempNodeNum + "点) . . . " + LocalFileInfo.getMemoryAndTime());
		ZipReader zipReader = new ZipReader(LocalFileInfo.getBasePath() + File.separator + "data" + File.separator + "Google-freebase-rdf-latestZH" + File.separator + "edgeBlank.zip");
		zipReader.readLine();
		allEdgeArr = new ArrayList[tempNodeNum];
		for(i=0; i<tempNodeNum; i++) {
			tempStr = zipReader.readLine();
			if(!tempStr.equals("")) {
				tempList = new ArrayList<>();
				for(String s : tempStr.split(" ")) {
					j = Integer.parseInt(s);
					if(j<tempNodeNum)	tempList.add(j);
				}
				if(!tempList.isEmpty())	allEdgeArr[i] = tempList;
			}
		}
		zipReader.close();
		System.out.println("  > 完成初始化allEdgeArr(" + tempNodeNum + "点) ! ! ! " + LocalFileInfo.getMemoryAndTime());
		
		/**********************
		 * 初始化allREdgeArr
		 **********************/
		System.out.println("  > 开始初始化allREdgeArr(" + tempNodeNum + "点) . . . " + LocalFileInfo.getMemoryAndTime());
		allREdgeArr = new ArrayList[tempNodeNum];
		for(i=0; i<tempNodeNum; i++) {
			if(null != allEdgeArr[i]) {
				for(int in : allEdgeArr[i]) {
					if(null==allREdgeArr[in])
						allREdgeArr[in] = new ArrayList<>();
					allREdgeArr[in].add(i);
				}
			}
		}
		System.out.println("  > 完成初始化allREdgeArr(" + tempNodeNum + "点) ! ! ! " + LocalFileInfo.getMemoryAndTime() + "\n");
		
		// 设置front
		frontNodeNum = tempNodeNum;
		
		tempLen = nodeNumList.size();
		for(i=0; i<tempLen; i++) {
			// 判断zip是否存在
			tempNodeNum = nodeNumList.get(i);
			tempDegreeNum = degreeNumList.get(i);
			tempZipPath = LocalFileInfo.getEdgeBlankZipPath(tempDegreeNum, tempNodeNum);
			tempFile = new File(tempZipPath);
			if(tempFile.exists()) {
				System.out.println("  > 第" + (i+1) + "个zip已存在 ! ! ! " + TimeUtility.getTime() + "\n");
				continue;
			}
			
			// 再次计算allEdgeArr和allREdgeArr
			System.out.println("  > 开始计算第" + (i+1) + " 个zip的allEdgeArr和allREdgeArr" + this.getDegreeAndNodeStr(tempDegreeNum, tempNodeNum, Boolean.TRUE));
			if(frontNodeNum != tempNodeNum) {
				for(j=0; j<tempNodeNum; j++) {
					// 计算allEdgeArr
					if(null != allEdgeArr[j]) {
						tempList = new ArrayList<>();
						for(int in : allEdgeArr[j]) {
							if(in < tempNodeNum)
								tempList.add(in);
						}
						if(0 == tempList.size())	tempList = null;
						allEdgeArr[j].clear();
						allEdgeArr[j] = tempList;
					}
					
					// 计算allREdgeArr
					if(null != allREdgeArr[j]) {
						tempList = new ArrayList<>();
						for(int in : allREdgeArr[j]) {
							if(in < tempNodeNum)
								tempList.add(in);
						}
						if(0 == tempList.size())	tempList = null;
						allREdgeArr[j].clear();
						allREdgeArr[j] = tempList;
					}
				}
				for(j = tempNodeNum; j<frontNodeNum; j++) {
					if(null != allEdgeArr[j]) allEdgeArr[j].clear();
					allEdgeArr[j] = null;
					if(null != allREdgeArr[j])	allREdgeArr[j].clear();
					allREdgeArr[j] = null;
				}
			}
			System.out.println("  > 完成计算第" + (i+1) + " 个zip的allEdgeArr和allREdgeArr" + this.getDegreeAndNodeStr(tempDegreeNum, tempNodeNum, Boolean.FALSE));
			
			// 设置front
			frontNodeNum = tempNodeNum;
			
			/**********************
			 * 开始写zip
			 **********************/
			// 根据是否设置了度计算zip
			if(tempDegreeNum ==0){	// 没有设置度
				// 开始写zip
				zipWriter = new ZipWriter(tempZipPath);
				// 写正向边
				System.out.println("  > 开始写第" + (i+1) + " 个zip的pEdge.txt" + this.getDegreeAndNodeStr(tempDegreeNum, tempNodeNum, Boolean.TRUE));
				zipWriter.addZipEntity("pEdge.txt");
				zipWriter.write(String.valueOf(tempNodeNum) + "#\n");
				for(j=0; j<tempNodeNum; j++) {
					sBuf = new StringBuffer();
					if(allEdgeArr[j]!=null) {
						for(int in : allEdgeArr[j]) {
							sBuf.append(in);
							sBuf.append(' ');
						}
					}
					sBuf.append('\n');
					zipWriter.write(sBuf.toString());
				}
				zipWriter.flush();
				System.out.println("  > 完成写第" + (i+1) + " 个zip的pEdge.txt" + this.getDegreeAndNodeStr(tempDegreeNum, tempNodeNum, Boolean.FALSE));
				
				// 写反向边
				System.out.println("  > 开始写第" + (i+1) + " 个zip的rEdge.txt" + this.getDegreeAndNodeStr(tempDegreeNum, tempNodeNum, Boolean.TRUE));
				zipWriter.addZipEntity("rEdge.txt");
				zipWriter.write(String.valueOf(tempNodeNum) + "#\n");
				for(j=0; j<tempNodeNum; j++) {
					sBuf = new StringBuffer();
					if(allREdgeArr[j]!=null) {
						for(int in : allREdgeArr[j]) {
							sBuf.append(in);
							sBuf.append(' ');
						}
					}
					sBuf.append('\n');
					zipWriter.write(sBuf.toString());
				}
				zipWriter.flush();
				zipWriter.close();
				System.out.println("  > 完成写第" + (i+1) + " 个zip的rEdge.txt" + this.getDegreeAndNodeStr(tempDegreeNum, tempNodeNum, Boolean.FALSE) + "\n");
				
			} else if (tempDegreeNum>0) {
				HashMap<Integer, HashMap<Integer, Boolean>> recordMap = new HashMap<Integer, HashMap<Integer, Boolean>>();
				HashMap<Integer, Boolean> tempMap = null;
				
				// 开始写zip
				zipWriter = new ZipWriter(tempZipPath);
				// 写反向边
				System.out.println("  > 开始写第" + (i+1) + " 个zip的rEdge.txt" + this.getDegreeAndNodeStr(tempDegreeNum, tempNodeNum, Boolean.TRUE));
				zipWriter.addZipEntity("rEdge.txt");
				zipWriter.write(String.valueOf(tempNodeNum) + "#\n");
				for(j=0; j<tempNodeNum; j++) {
					sBuf = new StringBuffer();
					if(null!=allREdgeArr[j]) {
						if(allREdgeArr[j].size() > tempDegreeNum) {
							for(int in : allREdgeArr[j]) {
								if((tempMap = recordMap.get(in))==null) {
									tempMap = new HashMap<>();
									recordMap.put(in, tempMap);
								}
								tempMap.put(j, Boolean.TRUE);
							}
						}	else {
							for(int in : allREdgeArr[j]) {
								sBuf.append(in);
								sBuf.append(' ');
							}
						}
					}
					sBuf.append('\n');
					zipWriter.write(sBuf.toString());
				}
				zipWriter.flush();
				System.out.println("  > 完成写第" + (i+1) + " 个zip的rEdge.txt" + this.getDegreeAndNodeStr(tempDegreeNum, tempNodeNum, Boolean.FALSE));
				
				// 写正向边
				System.out.println("  > 开始写第" + (i+1) + " 个zip的pEdge.txt" + this.getDegreeAndNodeStr(tempDegreeNum, tempNodeNum, Boolean.TRUE));
				zipWriter.addZipEntity("pEdge.txt");
				zipWriter.write(String.valueOf(tempNodeNum) + "#\n");
				for(j=0; j<tempNodeNum; j++) {
					sBuf = new StringBuffer();
					if((tempMap=recordMap.get(j)) != null) {
						for(int in : allEdgeArr[j]) {
							if(null == tempMap.get(in)) {
								sBuf.append(in);
								sBuf.append(" ");
							}
						}
					} else {
						if(null != allEdgeArr[j]) {
							for(int in : allEdgeArr[j]) {
								sBuf.append(in);
								sBuf.append(" ");
							}
						}
					}
					sBuf.append('\n');
					zipWriter.write(sBuf.toString());
				}
				zipWriter.flush();
				zipWriter.close();
				System.out.println("  > 完成写第" + (i+1) + " 个zip的pEdge.txt" + this.getDegreeAndNodeStr(tempDegreeNum, tempNodeNum, Boolean.FALSE) + "\n");
			}
		}
	}
	
	
	/**
	 * @param args
	 * 主函数
	 */
	public static void main(String[] args) {
//		IndexAndEdgeBlankZipCreator creatorT = new IndexAndEdgeBlankZipCreator();
//		creatorT.testIndex();
//		System.out.println(LocalFileInfo.getIndexPath(1000));
//		System.out.println(LocalFileInfo.getIndexPath(0));
//		System.out.println(LocalFileInfo.getIndexPath(0));
//		creatorT.testZip();
		
		String tempStr = null;
		ArrayList<Integer> nodeNumList = new ArrayList<>();
		ArrayList<Integer> degreeNumList = new ArrayList<>();
		int i, j, len, dealType;
		
		Scanner scanner = new Scanner(System.in);
		
		// 输入参数
		System.out.print("> 请输入处理类型 0表示索引, 1表示edgeBlankZip, 2表示两者 : ");
		dealType =scanner.nextInt(); 
		System.out.print("> 请输入参数，格式为   节点数,度数;节点数,度数  : ");
		scanner.nextLine();
		tempStr = scanner.nextLine();
		for(String s : tempStr.split(";")) {
			String[] arr = s.split(",");
			nodeNumList.add(Integer.parseInt(arr[0])*10000);
			degreeNumList.add(Integer.parseInt(arr[1]));
		}
		
		IndexAndEdgeBlankZipCreator creator = new IndexAndEdgeBlankZipCreator(nodeNumList, degreeNumList);
		if(nodeNumList.get(0)==0)	nodeNumList.set(0, LocalFileInfo.gFreeBaseNodeNum);
		if(nodeNumList.size()>1 && nodeNumList.get(1)==0)	nodeNumList.set(1, LocalFileInfo.gFreeBaseNodeNum);
		
		/******************
		 * 	写索引
		 ******************/
		if(0==dealType || 2==dealType) {
			System.out.println("> 开始创建索引 . . . " + LocalFileInfo.getMemoryAndTime() + "\n");
			creator.createIndex();
			System.out.println("> 完成创建索引  ! ! ! " + LocalFileInfo.getMemoryAndTime() + "\n");
		}
		
		/******************
		 * 	写edgeBlankZip
		 ******************/
		if(1==dealType || 2==dealType) {
			System.out.println("> 开始写edgeBlankZip . . . " + LocalFileInfo.getMemoryAndTime() + "\n");
			creator.writeEdgeBlankZip();
			System.out.println("> 完成写edgeBlankZip ! ! ! " + LocalFileInfo.getMemoryAndTime() + "\n");
		}
		
		System.out.println("> 完成处理 ! ! ! " + TimeUtility.getTime());
	}
}


















