package zhou.hao.entry;

import java.io.Serializable;
import java.util.HashMap;

public class StringObject implements Serializable{
	private String str = null;
	public StringObject() {}
	public StringObject(String s) {
		this.str = s;
	}
	public String getStr() {
		return str;
	}
	public void setStr(String str) {
		this.str = str;
	}
	
	public int compareTo(StringObject so1) {
		return this.str.compareTo(so1.getStr());
	}
	
	// 返回最长共同前缀的长度
	public int getPrefixLen(StringObject strObj) {
		int i = 0;
		while(i<strObj.getStr().length() && i<this.getStr().length()) {
			if(str.charAt(i)==strObj.getStr().charAt(i)) {
				i++;
			} else break;
		}
		return i;
	}
	
	public int length() {
		return this.str.length();
	}
	
	public StringObject subString(int startIndex) {
		return new StringObject(this.str.substring(startIndex));
	}
	
	public StringObject subString(int startIndex, int endIndex) {
		return new StringObject(this.str.substring(startIndex, endIndex));
	}
	
	public boolean equals(Object anObject) {
		return this.str.equals(((StringObject)anObject).getStr());
	}
	
	public String toString() {
		return this.getStr();
	}
	
	public int hashCode() {
		return this.getStr().hashCode();
	}
	
	public static void main(String[] args) {
		HashMap<StringObject, Integer> map = new HashMap<StringObject, Integer>();
		StringObject st = new StringObject("2");
		map.put(st , 2);
		map.put(new StringObject("1"), 1);
		map.put(new StringObject("1"), 4);
		System.out.println(map.get(new StringObject("1")));
	}
}
