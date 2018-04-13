package file.writer.freebase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import entity.freebase.GoogleFreebaseMap;
import entity.freebase.Node;
import utility.LocalFileInfo;

public class GoogleFreebaseMapWriteService {
	private GoogleFreebaseMap freebaseMap = null;

	public GoogleFreebaseMapWriteService(GoogleFreebaseMap freebaseMap) {
		super();
		this.freebaseMap = freebaseMap;
	}
	
	public boolean writeMap() {
		BufferedWriter resultZipWriter = null;
		try {
			// 创建文件类
			File resultZipFile = new File(LocalFileInfo.getResultZipGoogleFreebasePath());
			if(!resultZipFile.exists()) {
				Boolean res = resultZipFile.createNewFile();
				if(false==res) {
					System.out.println("创建文件" + LocalFileInfo.getResultZipGoogleFreebasePath() + "失败！");
					return false;
				}
			}
			
			// 创建压缩输出writer
			FileOutputStream fos = new FileOutputStream(resultZipFile);
			CheckedOutputStream cos = new CheckedOutputStream(fos, new Adler32());
			ZipOutputStream zos = new ZipOutputStream(cos);
			zos.setComment("google-freebase处理结果");
			resultZipWriter = new BufferedWriter(new OutputStreamWriter(zos));
			
			// 写入文件nodeIdMapGoogleFreebase.txt
			zos.putNextEntry(new ZipEntry("nodeIdMapGoogleFreebase.txt"));
			resultZipWriter.write(String.valueOf(freebaseMap.getSize() + "#" + "\n"));
			Node node = freebaseMap.getHead().getNext();
			while(null!=node) {
				StringBuffer sb = new StringBuffer();
				sb.append(node.getNodeId() + ": " + node.getNodeName() + "\n");
				resultZipWriter.write(sb.toString());
				node = node.getNext();
			}
			resultZipWriter.flush();
			
			// 写入文件keywordIdMapGoogleFreebase.txt
			zos.putNextEntry(new ZipEntry("keywordIdMapGoogleFreebase.txt"));
			resultZipWriter.write(String.valueOf(freebaseMap.getSize() + "#" + freebaseMap.getTotalKeyword() + "#\n"));
			node = freebaseMap.getHead().getNext();
			while(null!=node) {
				StringBuffer sb = new StringBuffer();
				if(null != node.getKeywordList() && !node.getKeywordList().isEmpty()) {
					sb.append(node.getNodeId() + ":");
					for(String s : node.getKeywordList()) {
						sb.append(" ");
						sb.append(s);
						sb.append(" [^\\]"); // 暂时使用[^\\]分开关键字
					}
					resultZipWriter.write(sb.append('\n').toString());
				}	
				node = node.getNext();
			}
			resultZipWriter.flush();
			
			// 写入文件edgeGoogleFreebase.txt
			zos.putNextEntry(new ZipEntry("edgeGoogleFreebase.txt"));
			resultZipWriter.write(String.valueOf(freebaseMap.getSize() + "#" + freebaseMap.getTotalEdge()+ "#\n"));
			node = freebaseMap.getHead().getNext();
			while(null!=node) {
				StringBuffer sb = new StringBuffer();
				if(null != node.getPointToNodeIdList() && !node.getPointToNodeIdList().isEmpty()) {
					sb.append(node.getNodeId() + ": ");
					for(Integer it : node.getPointToNodeIdList()) {
						sb.append(String.valueOf(it));
						sb.append(",");
					}
					resultZipWriter.write(sb.append('\n').toString());
				}
				node = node.getNext();
			}
			resultZipWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if(null != resultZipWriter) {
				resultZipWriter.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
