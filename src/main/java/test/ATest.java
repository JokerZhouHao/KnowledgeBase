package test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import utility.Global;
import utility.IOUtility;
import utility.LoopQueue;
import utility.TimeUtility;

public class ATest {
	public static void main(String[] args) throws Exception{
//		System.out.format("> 已处理%d%%，用时%d\n", 1212, TimeUtility.getSpanSecond(23233L, System.currentTimeMillis()));
		
		TreeMap<Integer, Integer> tm = new TreeMap<>();
		tm.put(3, 3);
		tm.put(2, 2);
		for(Entry<Integer, Integer> en : tm.entrySet()) {
			System.out.println(en.getKey() + " " + en.getValue());
		}
		
//		TreeSet<Integer> ts = new TreeSet<>();
//		ts.add(1);
//		ts.add(3);
//		ts.add(3);
//		ts.add(2);
//		TreeSet<Integer> ts1 = new TreeSet<>();
//		ts1.add(1);
//		ts1.add(2);
//		ts1.add(3);
//		System.out.println(ts.equals(ts1));
		
		
//		boolean[] b = new boolean[100];
//		for(int i=0; i<b.length; i++) {
//			System.out.println(b[i]);
//		}
//		
//		int[] a = new int[10];
//		for(int i=0; i< a.length; i++) {
//			System.out.println(a[i]);
//		}
		
		
//		BufferedWriter bw = IOUtility.getBW(Global.inputDirectoryPath + "test.txt");
//		bw.write(232389423);
//		bw.close();
		
//		LoopQueue<Integer> lq = new LoopQueue<>(3);
//		System.out.println(lq.push(1));
//		System.out.println(lq.push(2));
//		System.out.println(lq.push(3));
//		System.out.println(lq.push(4));
		
//		System.out.println(lq.poll());
//		System.out.println(lq.push(5));
//		System.out.println(lq.push(6));
//		System.out.println(lq.poll());
//		System.out.println(lq.poll());
//		System.out.println(lq.poll());
		
//		lq.display();
		
//		List<Integer> li1 = new ArrayList<>();
//		li1.add(null);
//		System.out.println(li1.get(0)==0);
		
//		List<Integer> li2 = new ArrayList<>();
//		
//		li1.add(1);
//		li1.add(2);
//		li1.add(34);
//		li1.add(38);
//		
//		li2.add(1);
//		li2.add(2);
//		li2.add(34);
//		
//		System.out.println(li1.equals(li2));
		
//		DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(Global.pWReachTimesPath))));
//		for(int i=0; i<10000; i++) {
//			dos.writeInt(3434);
//			dos.writeInt(2323);
//		}
//		dos.flush();
//		dos.close();
//		
//		DataInputStream dis = new DataInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(Global.pWReachTimesPath))));
//		System.out.println(dis.readInt());
//		System.out.println(dis.readInt());
//		dis.close();
	}
}
