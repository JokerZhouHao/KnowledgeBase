package entity.freebase;

import java.util.ArrayList;

/**
 * 
 * @author Monica
 *
 * 用来缓存从文件中读取的一行行字符串
 * @param <T>
 * 
 * 2017/11/02
 */
public class LineList<T> extends ArrayList<T>{
	private int readPosition = 0;
	private int writePosition = 0;
	
	public LineList(){
		super();
	}
	
	public LineList(int initialCapacity) {
		for(int i=0; i<initialCapacity; i++) {
			this.add(null);
		}
	}
	
	public int getReadPosition() {
		return readPosition;
	}

	public void setReadPosition(int readPosition) {
		this.readPosition = readPosition;
	}

	public int getWritePosition() {
		return writePosition;
	}

	public void setWritePosition(int writePosition) {
		this.writePosition = writePosition;
	}

	// 扩大list中的元素数
	public int extendSize(int size) {
		for(int i=this.size(); i<size; i++)
			this.add(null);
		return this.size();
	}
	
	// 重置读位置
	public void resetReadPosition() {
		readPosition = 0;
	}
	
	// 重置写位置
	public void resetWritePosition() {
		writePosition = 0;
	}
	
	// 读数据
	public T read() {
		if(readPosition<this.size())
			return super.get(readPosition++);
		else return null;
	}
	
	// 写数据
	public T write(T t) {
		if(writePosition!=this.size()) {
			this.set(this.writePosition++, t);
			return t;
		} else return null;
	}
	
	// 判断是否已经读完
	public boolean readOver() {
		return this.size()==this.readPosition;
	}
	
	// 判断是否写完
	public boolean writeOver() {
		return this.size()==this.writePosition;
	}
}
