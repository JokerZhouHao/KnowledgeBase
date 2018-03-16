package zhou.hao.yago2s.entity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Monica
 * @since 2018/03/15
 * 功能 ： 路径树节点
 */
public class PNode {
	private int id = -1;
	private boolean isLeaf = false;
	private PNode parentNode = null;
	private PNode next = null;
	public PNode(int id, boolean isLeaf) {
		super();
		this.id = id;
		this.isLeaf = isLeaf;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isLeaf() {
		return isLeaf;
	}
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	public PNode getParentNode() {
		return parentNode;
	}
	public void setParentNode(PNode parentNode) {
		this.parentNode = parentNode;
	}
	public PNode getNext() {
		return next;
	}
	public void setNext(PNode next) {
		this.next = next;
	}
	
	@Override
	public int hashCode() {
		return ((Integer)id).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PNode other = (PNode) obj;
		if (id != other.id)
			return false;
		if (isLeaf != other.isLeaf)
			return false;
		if (next == null) {
			if (other.next != null)
				return false;
		} else if (next != other.next)
			return false;
		if (parentNode == null) {
			if (other.parentNode != null)
				return false;
		} else if (parentNode != other.parentNode)
			return false;
		return true;
	}
	
	public static void main(String args[]) {
		HashMap<PNode, Boolean>	map = new HashMap<>();
		PNode no = new PNode(23, false);
		map.put(no, false);
		map.put(no, true);
		System.out.println(map.get(no));
	}
}
