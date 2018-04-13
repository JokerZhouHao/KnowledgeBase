package entity.sp;

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
	
	// 更新路径
	public void updatePTree(double distance, PTree pTree) {
		MLinkedNode<DisPTree> insertedNode = new MLinkedNode<MinHeap.DisPTree>(new DisPTree(distance, pTree));
		MLinkedNode<DisPTree>	disPTreeListHead = disPTreeList.getHead();
		MLinkedNode<DisPTree> disPTreeListNext = disPTreeListHead.next;
		while(null != disPTreeListNext) {
			if(distance < disPTreeListNext.nodeInfo.distance) {
				disPTreeListHead.next = insertedNode;
				disPTreeListHead = insertedNode;
				insertedNode.next = disPTreeListNext;
				break;
			} else {
				disPTreeListHead = disPTreeListNext;
				disPTreeListNext = disPTreeListNext.next;
			}
		}
		// 删除最后一个点
		if(null != disPTreeListNext) {
			while(null != disPTreeListNext.next) {
				disPTreeListHead = disPTreeListNext;
				disPTreeListNext = disPTreeListNext.getNext();
			}
			disPTreeListHead.next = null;
		}
	}
	
	// 获得最后一个节点
	public MLinkedNode<DisPTree> getLast() {
		MLinkedNode<DisPTree> dp = this.disPTreeList.head;
		while(null != dp.next)	dp = dp.next;
		return dp;
	}
	
	public MLinkedList<DisPTree> getDisPTreeList() {
		return disPTreeList;
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
		mHeap.addPTree(4, new PTree(new PNode(0, false)));
//		mHeap.addPTree(3, new PTree(new PNode(1, false)));
		mHeap.addPTree(1, new PTree(new PNode(2, false)));
		mHeap.updatePTree(2, new PTree(new PNode(2, false)));
		mHeap.updatePTree(0.5, new PTree(new PNode(2, false)));
		mHeap.updatePTree(1.1, new PTree(new PNode(2, false)));
//		mHeap.addPTree(3, new PTree(new PNode(3, false)));
//		mHeap.addPTree(4, new PTree(new PNode(4, false)));
//		mHeap.addPTree(5, new PTree(new PNode(5, false)));
		mHeap.display();
	}
	
	// pTree和距离类
	public class DisPTree{
		private double distance = -1;
		private PTree pTree = null;
		public DisPTree(double distance, PTree pTree) {
			super();
			this.distance = distance;
			this.pTree = pTree;
		}
		public double getDistance() {
			return distance;
		}
		public PTree getpTree() {
			return pTree;
		}
	}
	
	// 链表节点
	public class MLinkedNode <T> {
		private T nodeInfo = null;
		private MLinkedNode<T> next = null;
		
		public MLinkedNode(T nodeInfo) {
			this.nodeInfo = nodeInfo;
		}

		public T getNodeInfo() {
			return nodeInfo;
		}

		public MLinkedNode<T> getNext() {
			return next;
		}
		
	}
	
	// 带头链表
	public class MLinkedList<T>{
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
