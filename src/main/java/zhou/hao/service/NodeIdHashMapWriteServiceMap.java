package zhou.hao.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import zhou.hao.entry.Node;
import zhou.hao.entry.NodeIdHashMapTestMap;
import zhou.hao.entry.NodeIdHashMapTestList;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.MemoryInfo;
import zhou.hao.tools.TimeStr;

public class NodeIdHashMapWriteServiceMap {
	private NodeIdHashMapTestMap nodeHashMap = null;
	
	private String blockNumFileName = LocalFileInfo.getTempPath() + "blockNum";
	
	private String tempNodeNameFileName = LocalFileInfo.getTempPath() + "tempNodeName.gz";
	
	private String nodeKeywodAndEdgeZipFilePath = LocalFileInfo.getTempPath() + "nodeKeywodAndEdge-";
	private String keywordBlockFileName = "keyword-";
	private String edgeBlockFileName = "edge-";
	
	private String nodeIdFilePath = LocalFileInfo.getTempPath() + "nodeIdMapGoogleFreebase.txt";
	private String keywordFilePath = LocalFileInfo.getTempPath() + "keywordIdMapGoogleFreebase.txt";
	private String edgeFilePath = LocalFileInfo.getTempPath() + "edgeGoogleFreebase.txt";
	private String nodeIdAndKeywordAndEdgeZipPath = LocalFileInfo.getTempPath() + "nodeIdAndKeywordAndEdge.zip";
	
	private int nodeNum = 0;
	private int blockNum = 0;
	
	private Long startTime = 0L;
	private Long precessedLineNum = 0L;
	
	private static int currentThreadNum = 3;
	
	public NodeIdHashMapWriteServiceMap(NodeIdHashMapTestMap ni) {
		this.nodeHashMap = ni;
	}
	
	// 以zip格式输出当前的Keyword和Edge
	public boolean writeKeywordAndEdgeBlock(int num) {
		BufferedWriter bw = null;
		String numStr = String.valueOf(num);
		try {
			// 创建写入文件流
			ZipOutputStream zos = new ZipOutputStream(new CheckedOutputStream(new BufferedOutputStream(new FileOutputStream(new File(this.nodeKeywodAndEdgeZipFilePath + numStr + ".zip"))) , new Adler32()));
			bw = new BufferedWriter(new OutputStreamWriter(zos));
			
			// 写入keyword文件
			zos.putNextEntry(new ZipEntry(this.keywordBlockFileName + numStr));
			HashMap<Integer, HashMap<String, Boolean>> keywordMap = this.nodeHashMap.getKeywordHashMap();
			int i=0, j=0;
			int nodeNum = this.nodeHashMap.getNodeNum();
			HashMap<String, Boolean> keywordM = null;
			bw.write(nodeNum + "\n"); // 写入节点数
			for(i=0; i<nodeNum; i++) {
				keywordM = keywordMap.get(i);
				if(null==keywordM)	bw.write('\n');
				else {
					StringBuffer sb = new StringBuffer();
					for(String s : keywordM.keySet()) {
						sb.append(s);
						sb.append("[^\\]");  // 以[^\]分开关键字
					}
					sb.append('\n');
					bw.write(sb.toString());
				}
			}
			bw.flush();
			keywordMap.clear();	// 清空关键字
			System.gc();
			
			// 写入文件edge文件
			zos.putNextEntry(new ZipEntry(this.edgeBlockFileName + numStr));
			HashMap<Integer, HashMap<Integer, Boolean>> edgeMap= this.nodeHashMap.getPointToNodeHashMap();
			HashMap<Integer, Boolean> edgeM = null;
			bw.write(nodeNum + "\n"); // 写入节点数
			for(i=0; i<nodeNum; i++) {
				edgeM = edgeMap.get(i);
				if(null==edgeM)	bw.write('\n');
				else {
					StringBuffer sb = new StringBuffer();
					for(Integer in : edgeM.keySet()) {
						sb.append(String.valueOf(in));
						sb.append(',');
					}
					sb.append('\n');
					bw.write(sb.toString());
				}
			}
			bw.flush();
			edgeMap.clear(); // 清空边
			System.gc();
			
			bw.close();
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// 记录创建记录块数的文件
	public boolean writeNodeNum(int blockNum) {
		this.blockNum = blockNum;
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(this.blockNumFileName))));
			bw.write(String.valueOf(blockNum));
			bw.flush();
			bw.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// 将nodeHashMap输出
	public boolean writeNodeHashMap() {
		BufferedWriter bw = null;
		HashMap<String, Integer> nodeNameMap = this.nodeHashMap.getNodeNameMap();
		this.nodeNum = nodeNameMap.size();
		try {
			// 写出nodeNameMap
			bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(new File(this.tempNodeNameFileName))))));
			bw.write(String.valueOf(nodeNameMap.size()));
			bw.write('\n');
			for(Entry<String, Integer> entry : nodeNameMap.entrySet()) {
				bw.write(String.valueOf(entry.getValue()));
				bw.write('\n');
				bw.write(entry.getKey());
				bw.write('\n');
			}
			// 清空nodeNameMap
			nodeNameMap.clear();
			nodeNameMap = null;
			System.gc();
			
			bw.flush();
			bw.close();
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// 读写入的nodeIdhashMap文件，然后以id从小顺序重新输出
	public boolean writeNodeIdFile() {
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			// 读取文件
			br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new BufferedInputStream(new FileInputStream(new File(this.tempNodeNameFileName))))));
			Integer nodeNum = Integer.parseInt(br.readLine());
			ArrayList<String> nodeIdList = new ArrayList<String>();
			int i = 0;
			for(i=0; i<nodeNum; i++) {
				nodeIdList.add(null);
			}
			for(i=0; i<nodeNum; i++) {
				nodeIdList.set(Integer.parseInt(br.readLine()), br.readLine());
			}
			br.close();
			
			// 写出文件
			bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(this.nodeIdFilePath)))));
			bw.write(String.valueOf(nodeNum));
			bw.write('#');
			bw.write('\n');
			for(i=0; i<nodeNum; i++) {
//				bw.write(String.valueOf(i));
//				bw.write(": ");
				bw.write(nodeIdList.get(i));
				bw.write('\n');
			}
			bw.flush();
			bw.close();
			
			// 清空nodeIdList
			nodeIdList.clear();
			nodeIdList = null;
			System.gc();
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// 输出所有文件
	public void writeAllFile(Long startTime, Long pro) {
		this.startTime = startTime;
		this.precessedLineNum = pro;
		ExecutorService exc = Executors.newCachedThreadPool();
		exc.execute(new WriteFileThead(0));
		exc.execute(new WriteFileThead(1));
		exc.execute(new WriteFileThead(2));
		exc.shutdown();
	}
	
	// 写文件线程
	class WriteFileThead implements Runnable {
		private int writeSort = -1;
		
		public WriteFileThead(int sort) {
			this.writeSort = sort;
		}
		
		public void run() {
			// 0表示写nodeIdMapGoogleFreebase.txt
			// 1表示写keywordIdMapGoogleFreebase.txt
			// 2表示写edgeGoogleFreebase.txt
			synchronized (WriteFileThead.class) {
				Long nowTime = System.nanoTime();
				switch(this.writeSort) {
				case 0:{
					System.out.println("> 当前时间" + TimeStr.getTime() + ", 内存占用情况: \n" + MemoryInfo.getTotalFreeUsedAvailable() + "\n  现输出nodeIdMapGoogleFreebase.txt文件 . . . ");
				} break;
				case 1:{
					System.out.println("> 当前时间" + TimeStr.getTime() + ", 内存占用情况: \n" + MemoryInfo.getTotalFreeUsedAvailable() + "\n  现输出keywordIdMapGoogleFreebase.txt文件 . . . ");
				} break;
				case 2:{
					System.out.println("> 当前时间" + TimeStr.getTime() + ", 内存占用情况: \n" + MemoryInfo.getTotalFreeUsedAvailable() + "\n  现输出edgeGoogleFreebase.txt文件 . . . ");
				} break;
			}
			}
			Boolean sign = Boolean.FALSE;
			switch(writeSort) {
				case(0):{
					sign = writeNodeIdFile();
				}break;
				case(1):{
					sign = writeKeywordFile();
				}break;
				case(2):{
					sign = writeEdgeFile();
				}break;
			}
			synchronized (WriteFileThead.class) {
				Long nowTime = System.nanoTime();
				switch(writeSort){
					case(0):{
						if(Boolean.TRUE == sign) {
							System.out.println("  输出文件nodeIdMapGoogleFreebase.txt成功, 共用: " + (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
									"m" + (nowTime - startTime)/1000000000%3600%60 + "s\n");
						} else {
							System.out.println("  输出文件nodeIdMapGoogleFreebase.txt失败而退出！！！");
							System.exit(0);
						}
					} break;
					case(1):{
						if(Boolean.TRUE == sign) {
							System.out.println("  输出文件keywordIdMapGoogleFreebase.txt成功, 共用: " + (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
									"m" + (nowTime - startTime)/1000000000%3600%60 + "s\n");
						} else {
							System.out.println("  输出文件keywordIdMapGoogleFreebase.txt失败而退出！！！");
							System.exit(0);
						}
					} break;
					case(2):{
						if(Boolean.TRUE == sign) {
							System.out.println("  输出文件edgeGoogleFreebase.txt成功, 共用: " + (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
									"m" + (nowTime - startTime)/1000000000%3600%60 + "s\n");
						} else {
							System.out.println("  输出文件edgeGoogleFreebase.txt失败而退出！！！");
							System.exit(0);
						}
					} break;
				};
				if(--currentThreadNum<=0) {
					// 输出压缩包
					System.out.println("> 当前时间" + TimeStr.getTime() + ", 内存占用情况: \n" + MemoryInfo.getTotalFreeUsedAvailable() + "\n  现输出nodeIdAndKeywordAndEdge.zip文件 . . . ");
					if(Boolean.TRUE==writeNodeIdAndKeywordAndEdge()) {
						nowTime = System.nanoTime();
						System.out.println("  输出nodeIdAndKeywordAndEdge.zip成功, 共用: " + (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
								"m" + (nowTime - startTime)/1000000000%3600%60 + "s\n");
					} else {
						System.out.println("  输出文件nodeIdAndKeywordAndEdge.zip失败而退出！！！");
						System.exit(0);
					}
					
					nowTime = System.nanoTime();
					System.out.println("> 处理成功^^^^^^^^^^^^^^^^^^");
					System.out.println("  共处理 " + precessedLineNum + "条, 共生成" + nodeNum + "个节点, 当前时间: " + TimeStr.getTime());
					System.out.println("  共用："+ (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
							"m" + (nowTime - startTime)/1000000000%3600%60 + "s");
				}
			}
		}
	}
	
	// 将nodeId，keyword和edge三文件打包输出
	public Boolean writeNodeIdAndKeywordAndEdge() {
		ZipOutputStream zos = null;
		BufferedInputStream bis = null;
		try {
			// 创建文件类
			File resultZipFile = new File(this.nodeIdAndKeywordAndEdgeZipPath);
			if(!resultZipFile.exists()) {
				Boolean res = resultZipFile.createNewFile();
				if(false==res) {
					System.out.println("创建文件" + LocalFileInfo.getResultZipGoogleFreebasePath() + "失败！");
					return false;
				}
			}
			
			// 创建压缩输出writer
			zos = new ZipOutputStream(new CheckedOutputStream(new BufferedOutputStream(new FileOutputStream(resultZipFile)), new Adler32()));
			zos.setComment("google-freebase process result");
			
			// 写入文件nodeIdMapGoogleFreebase.txt
			byte bt[] = new byte[1024];
			bis = new BufferedInputStream(new FileInputStream(new File(this.nodeIdFilePath)));
			zos.putNextEntry(new ZipEntry("nodeIdMapGoogleFreebase.txt"));
			int readLen = 0;
			while(-1!=(readLen=bis.read(bt))) {
//				System.out.println(readLen);
				zos.write(bt, 0, readLen);
			}
			bis.close();
			zos.flush();
			
			
			// 写入文件keywordIdMapGoogleFreebase.txt
			bis = new BufferedInputStream(new FileInputStream(new File(this.keywordFilePath)));
			zos.putNextEntry(new ZipEntry("keywordIdMapGoogleFreebase.txt"));
			while(-1!=(readLen=bis.read(bt))) {
				zos.write(bt, 0, readLen);
			}
			bis.close();
			zos.flush();
			
			// 写入文件edgeGoogleFreebase.txt
			bis = new BufferedInputStream(new FileInputStream(new File(this.edgeFilePath)));
			zos.putNextEntry(new ZipEntry("edgeGoogleFreebase.txt"));
			while(-1!=(readLen=bis.read(bt))) {
				zos.write(bt, 0, readLen);
			}
			bis.close();
			zos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if(null != zos) {
				zos.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	class OneBlock{
		BufferedReader br = null;
		int nodeNum = 0;
		
		public OneBlock(int blockNum, String fileName) {
			try {
				ZipInputStream zis = new ZipInputStream(new CheckedInputStream(new BufferedInputStream(new FileInputStream(new File(nodeKeywodAndEdgeZipFilePath + String.valueOf(blockNum) + ".zip"))), new Adler32()));
				br = new BufferedReader(new InputStreamReader(zis));
				if(!zis.getNextEntry().getName().contains(fileName)) zis.getNextEntry(); 
				nodeNum = Integer.parseInt(br.readLine());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void close() {
			try {
				this.br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class KeywordOneBlock extends OneBlock{
		public KeywordOneBlock(int blockNum, String fileName) {
			super(blockNum, fileName);
		}
		
		public HashMap<String, Boolean> readLineToMap(int readedNodeId, HashMap<String, Boolean> keywordMap){
			if(readedNodeId>=this.nodeNum)	{	// 已读完
				return new HashMap<String, Boolean>();
			} else {
				try {
					String str = this.br.readLine();
					if(str.isEmpty()) 	return null; 	// 该行没值
					else {
						String strArr[] = str.split("\\[\\^\\\\\\]");
						if(null==keywordMap) {
							keywordMap = new HashMap<String, Boolean>();
							for(int i=0; i<strArr.length; i++)
								keywordMap.put(strArr[i], Boolean.TRUE);
						} else {
							for(int i=0; i<strArr.length; i++) {
								if(null==keywordMap.get(strArr[i]))
									keywordMap.put(strArr[i], Boolean.TRUE);
							}
						}
						return keywordMap;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}
	
	class EdgeOneBlock extends OneBlock{
		public EdgeOneBlock(int blockNum, String fileName) {
			super(blockNum, fileName);
		}
		
		public HashMap<Integer, Boolean> readLineToMap(int readedNodeId, HashMap<Integer, Boolean> edgeMap){
			if(readedNodeId>=this.nodeNum)	{	// 已读完
				return new HashMap<Integer, Boolean>();
			} else {
				try {
					String str = this.br.readLine();
					if(str.isEmpty())	return null; 	// 该行没值
					else {
						String strArr[] = str.split(",");
						if(null==edgeMap) {
							edgeMap = new HashMap<Integer, Boolean>();
							for(int i=0; i<strArr.length; i++)
								edgeMap.put(Integer.parseInt(strArr[i]), Boolean.TRUE);
						} else {
							int x = 0;
							for(int i=0; i<strArr.length; i++) {
								x = Integer.parseInt(strArr[i]);
								if(null==edgeMap.get(x)) 
									edgeMap.put(x, Boolean.TRUE);
							}
						}
						return edgeMap;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}
	
	// 合并两个list
	public void mergeArrayList(ArrayList list1, ArrayList list2) {
		for(int i=0; i<list2.size(); i++) {
			if(!list1.contains(list2.get(i)))
				list1.add(list2.get(i));
		}
		list2.clear();
	}
	
	// 合并所有的keyword块，并输出文件
	public boolean writeKeywordFile() {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File(keywordFilePath)));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		KeywordOneBlock keywordBlocks[] = new KeywordOneBlock[this.blockNum];
		int i = 0;
		for(i=0; i<this.blockNum; i++) {
			keywordBlocks[i] = new KeywordOneBlock(i, "keyword");
		}
		
		// 写keywordIdMapGoogleFreebase.txt
		int hasProcessedBlock = 0;
		int currentNodeId = 0;
		Long keywordNum = 0L;
		Boolean sign = Boolean.FALSE;
		// 写入节点数，空格留作写关键字总数
		try {
			bw.write(String.valueOf(nodeNum) + "#                  \n");
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		while(hasProcessedBlock<blockNum) {
			HashMap<String, Boolean> keyMap= keywordBlocks[hasProcessedBlock].readLineToMap(currentNodeId, null);
			if(null==keyMap) {
				keyMap = new HashMap<String, Boolean>();
			} else {
				if(keyMap.isEmpty()) { // 该节点已读完
					keywordBlocks[hasProcessedBlock].close();
					keywordBlocks[hasProcessedBlock] = null;
					sign = Boolean.TRUE;
					if(hasProcessedBlock==blockNum-1)	break;
				}
			}
			for(i=hasProcessedBlock+1; i<blockNum; i++) {
//				ArrayList<String> strList = keywordBlocks[i].readLineToList(currentNodeId);
//				if(null!=strList && !strList.isEmpty())
//					mergeArrayList(keyList, strList);
				keywordBlocks[i].readLineToMap(currentNodeId, keyMap);
			}
			// 写入当前节点的关键字
			try {
				if(!keyMap.isEmpty()) {
//					bw.write(String.valueOf(currentNodeId) + ": ");
					StringBuffer sb = new StringBuffer();
					for(String s : keyMap.keySet()) {
						sb.append(s);
						sb.append("[^\\]");
					}
					bw.write(sb.append('\n').toString());
				} else {
					bw.write('\n');
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			keywordNum += keyMap.size();
			currentNodeId++;
			if(Boolean.TRUE == sign) {
				hasProcessedBlock++;
				sign = Boolean.FALSE;
			}
		}
		try {
			bw.flush();
			bw.close();
			
			// 写入总的keyword数
			RandomAccessFile raf = new RandomAccessFile(keywordFilePath, "rw");
			raf.write((String.valueOf(nodeNum) + "#" + String.valueOf(keywordNum) + "#").getBytes("utf-8"));
			raf.close();
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// 合并所有的edge块，并输出文件
	public boolean writeEdgeFile() {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File(edgeFilePath)));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		EdgeOneBlock edgeBlocks[] = new EdgeOneBlock[this.blockNum];
		int i = 0;
		for(i=0; i<this.blockNum; i++) {
			edgeBlocks[i] = new EdgeOneBlock(i, "edge");
		}
		
		// 写edgeGoogleFreebase.txt
		int hasProcessedBlock = 0;
		int currentNodeId = 0;
		Long edgeNum = 0L;
		Boolean sign = Boolean.FALSE;
		// 写入节点数，空格留作写边的总数
		try {
			bw.write(String.valueOf(nodeNum) + "#                  \n");
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		while(hasProcessedBlock<blockNum) {
			HashMap<Integer, Boolean> edgeMap = edgeBlocks[hasProcessedBlock].readLineToMap(currentNodeId, null);
			if(null==edgeMap) {
				edgeMap = new HashMap<Integer, Boolean>();
			} else {
				if(edgeMap.isEmpty()) { // 该节点已读完
					edgeBlocks[hasProcessedBlock].close();
					edgeBlocks[hasProcessedBlock] = null;
					sign = Boolean.TRUE;
					if(hasProcessedBlock==blockNum-1)	break;
				}
			}
			for(i=hasProcessedBlock+1; i<blockNum; i++) {
//				ArrayList<Integer> intList = edgeBlocks[i].readLineToList(currentNodeId);
//				if(null!=intList && !intList.isEmpty())
//					mergeArrayList(edgeList, intList);
				edgeBlocks[i].readLineToMap(currentNodeId, edgeMap);
			}
			// 写入当前节点的关键字
			try {
				if(!edgeMap.isEmpty()) {
//					bw.write(String.valueOf(currentNodeId) + ": ");
					StringBuffer sb = new StringBuffer();
					for(Integer in : edgeMap.keySet()) {
						sb.append(String.valueOf(in));
						sb.append(",");
					}
					bw.write(sb.append('\n').toString());
				} else bw.write('\n');
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			edgeNum += edgeMap.size();
			currentNodeId++;
			if(Boolean.TRUE == sign) {
				hasProcessedBlock++;
				sign = Boolean.FALSE;
			}
		}
		try {
			bw.flush();
			bw.close();
			
			// 写入总的keyword数
			RandomAccessFile raf = new RandomAccessFile(edgeFilePath, "rw");
			raf.write((String.valueOf(nodeNum) + "#" + String.valueOf(edgeNum) + "#").getBytes("utf-8"));
			raf.close();
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
}






















