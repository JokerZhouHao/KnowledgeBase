package test;

import java.io.File;

import utility.LocalFileInfo;

public class FilePathTest {
	public static void main(String[] args) {
		System.out.println("base : " + LocalFileInfo.getBasePath());
		System.out.println("gzipDataFilePath : " + LocalFileInfo.getGzipDataFilePath());
		System.out.println("resultZipGoogleFreebase : " + LocalFileInfo.getResultZipGoogleFreebasePath());
		File gzipDataFile = new File(LocalFileInfo.getGzipDataFilePath());
		if(gzipDataFile.exists()) System.out.println("gzipDataFile exits");
		else System.out.println("gzipDataFile not exits");
		File resultFile = new File(LocalFileInfo.getTestGzipPath());
		if(resultFile.exists()) System.out.println("resultFile exits");
		else System.out.println("resultFile not exits");
	}
}
