package test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import entity.OptMethod;
import utility.Global;
import utility.IOUtility;
import utility.LoopQueue;
import utility.RandomNumGenerator;
import utility.TimeUtility;

public class ATest {
	
	
	
	public static void set(int[] a) {
		a[0] = 100;
	}
	
	
	public static void main(String[] args) throws Exception{
		
		
//		int a[][] = new int[1][3];
//		set(a[0]);
//		for(int i=0; i<a.length; i++) {
//			for(int j=0; j<a[i].length; j++) {
//				System.out.print(a[i][j] + " ");
//			}
//			System.out.println();
//		}
		
		
//		OptMethod om = OptMethod.O1;
//		System.out.println(om);
		
//		Map<Integer, Short> mp = new HashMap<>();
//		Short st = 1;
//		mp.put(1, st);
//		mp.put(2, st);
//		st = 4;
//		System.out.println(mp);
		
//		System.out.println(TimeUtility.getDateByIntDate(1106685));
//		System.out.println(TimeUtility.getIntDate(TimeUtility.getDate("1-1-1")));
//		System.out.println(TimeUtility.getIntDate(TimeUtility.getDate("9999-12-30")));
//		System.out.println(TimeUtility.getDateByIntDate(73059854));
		
//		ArrayList<Integer> list = new ArrayList<>();
//		System.out.println(list.get(100));
		
		
//		RandomNumGenerator pidGe = new RandomNumGenerator(0, Global.numPid);
//		for(int i=0; i<10; i++) {
//			System.out.println(pidGe.getRandomInt());
//		}
		
		
		
//		List<Integer> li = new ArrayList<>();
//		li.add(-100000000);
//		System.out.println(li.get(0));
//		System.out.println(Integer.MAX_VALUE);
//		System.out.println(Global.MAX_PN_LENGTH);
//		System.out.println(Global.MAX_PN_LENGTH/50);
//		System.out.println(Global.MAX_PN_LENGTH/5000);
		
//		int[] its = new int[10000];
//		for(int i=0; i<its.length; i++) {
//			if(its[i]!=0) {
//				System.out.println(its[i]);
//				break;
//			}
//		}
//		System.out.println("OK");
		
//		Map<Integer, Integer> mp = new HashMap<>();
//		mp.put(1, 2);
//		mp.put(2, 5);
//		System.out.println(mp.toString());
		
		
//		List<Integer> li = new ArrayList<>();
//		for(int i=-128; i<=127; i++) {
//			li.add(i);
//		}
//		byte bt = 0;
//		for(int it : li) {
//			System.out.println((byte)it);
//		}
		
//		System.out.println(Byte.MAX_VALUE + " " + Byte.MIN_VALUE);
		
//		List<Integer> lis = new ArrayList<>();
//		lis.add(5);
//		lis.add(6);
//		lis.add(8);
//		lis.remove((Object)8);
//		System.out.println(lis);
		
//		int nid = 1;
//		List<Integer> nids = new ArrayList<>();
//		nids.add(nid);
//		nid = 3;
//		nids.add(nid);
//		nid = 10;
//		nids.add(nid);
//		System.out.println(nids);
		
		
		/***************
		 *
		 */
		
//		System.out.format("> 已处理%d%%，用时%d\n", 1212, TimeUtility.getSpanSecond(23233L, System.currentTimeMillis()));
		
//		ArrayBlockingQueue<Integer> qu = new ArrayBlockingQueue<>(5);
//		System.out.println(qu.size());
		
//		Set<Integer> set1 = new HashSet<>();
//		Set<Integer> set2 = new HashSet<>();
//		set1.add(1);
//		set1.add(2);
//		set2.add(2);
//		set2.add(-1);
//		set1.addAll(set2);
//		set2.clear();
//		for(int in : set1) {
//			System.out.println(in);
//		}
		
//		ArrayList<Integer> li = new ArrayList<>();
//		ArrayList<Integer> li1 = new ArrayList<>();
//		li.add(1);
//		li1.add(2);
//		li.addAll(li1);
//		int[] ins = new int[li.size()];
//		for(int i=0; i<ins.length; i++) {
//			ins[i] = li.get(i);
//			System.out.println(ins[i]);
//		}
		
//		TreeMap<Integer, Integer> tm = new TreeMap<>();
//		tm.put(3, 3);
//		tm.put(2, 2);
//		tm.put(1, 9);
//		tm.put(4, 4);
//		for(int en : tm.values()) {
//			System.out.println(en);
//		}
		
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
