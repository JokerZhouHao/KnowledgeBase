package zhou.hao.service;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipWriterService {
	
	private ZipOutputStream zos = null;
	private BufferedWriter bw = null;
	
	public ZipWriterService(String zipPath) {
		try {
			zos = new ZipOutputStream(new CheckedOutputStream(new BufferedOutputStream(new FileOutputStream(zipPath)), new Adler32()));
			bw = new BufferedWriter(new OutputStreamWriter(zos));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void addZipEntity(String entityName) {
		try {
			zos.putNextEntry(new ZipEntry(entityName));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void flush() {
		try {
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void write(String lineStr) {
		try {
			bw.write(lineStr);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void close() {
		try {
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
