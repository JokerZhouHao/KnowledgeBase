package zhou.hao.yago2s.entity;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Monica
 * @since 2018/03/15
 * 功能 ： 按照距离函数计算的指定点到路径树的距离，从小到大添加路径树
 */
public class MinHeap {
	
	private MLinkedList<DisPTree> disPTreeList = new MLinkedList<>();
	
	public MinHeap() {}
	
	// 添加路径树
	public void addPTree(double distance, PTree pTree) {
		MLinkedNode<DisPTree> insertedNode = new MLinkedNode<MinHeap.DisPTree>(new DisPTree(distance, pTree));
		MLinkedNode<DisPTree>	disPTreeListHead = disPTreeList.getHead();
		MLinkedNode<DisPTree> disPTreeListNext = disPTreeListHead.next;
		while(null != disPTreeListNext) {
			if(distance < disPTreeListNext.nodeInfo.distance) {
				disPTreeListHead.next = insertedNode;
				insertedNode.next = disPTreeListNext;
				break;
			} else {
				disPTreeListHead = disPTreeListNext;
				disPTreeListNext = disPTreeListNext.next;
			}
		}
		if(null == disPTreeListNext) {
			disPTreeListHead.next = insertedNode;
			insertedNode.next = null;
		}
		disPTreeList.size++;
	}
	
	// 获得size
	public int size()	{
		return disPTreeList.size;
	}
	
	// 打印
	public void display() {
		MLinkedNode<DisPTree>	disPTreeListHead = disPTreeList.getHead();
		while(null != (disPTreeListHead = disPTreeListHead.next)) {
			System.out.println(disPTreeListHead.nodeInfo.distance + " : root > " + disPTreeListHead.nodeInfo.pTree.getRoot().getId());
		}
	}
	
	// 主函数
	public static void main(String[] args) {
		MinHeap mHeap = new MinHeap();
		mHeap.addPTree(4, new PTree(new PNode(0)));
		mHeap.addPTree(3, new PTree(new PNode(1)));
		mHeap.addPTree(1, new PTree(new PNode(2)));
		mHeap.addPTree(3, new PTree(new PNode(3)));
		mHeap.addPTree(4, new PTree(new PNode(4)));
		mHeap.addPTree(5, new PTree(new PNode(5)));
		mHeap.display();
	}
	
	// pTree和距离类
	class DisPTree{
		private double distance = -1;
		private PTree pTree = null;
		public DisPTree(double distance, PTree pTree) {
			super();
			this.distance = distance;
			this.pTree = pTree;
		}
	}
	
	// 链表节点
	class MLinkedNode <T> {
		private T nodeInfo = null;
		private MLinkedNode<T> next = null;
		
		public MLinkedNode(T nodeInfo) {
			this.nodeInfo = nodeInfo;
		}
		
	}
	
	// 带头链表
	class MLinkedList<T>{
		private MLinkedNode<T>	head = null;
		private int size = 0;
		public MLinkedList() {
			head = new MLinkedNode<T>(null);
		}
		public MLinkedNode<T> getHead() {
			return head;
		}
		
		// 获得第index个元素
		public MLinkedNode<T> get(int index){
			MLinkedNode<T> p = head;
			int i = -1;
			while(null != (p = p.next))
				if(index == ++i)	return p;
			return null;
		}
	}
}
