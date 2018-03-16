package zhou.hao.yago2s.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

/**
 * @author Monica
 * @since 2018/03/15
 * 功能 ： 记录路径树
 */
public class PTree {
	private PNode root = null;
	private PNode tail = null;
	private int nodeNum = 0;
	
	public PTree(PNode root) {
		this.root = root;
		this.tail = root;
		nodeNum++;
	}
	
	// 获得根节点
	public PNode getRoot() {	return root;	}
	
	// 添加节点
	public void addNode(PNode parentNode, PNode insertedNode) {
		this.tail.setNext(insertedNode);
		this.tail = insertedNode;
		insertedNode.setParentNode(parentNode);
		nodeNum++;
	}
	
	// 删除多余节点
	public void deleteUnnecessaryNode() {
		PNode p1 = root, p2 = null;
		HashMap<PNode, Boolean> savedNodeMap = new HashMap<>();
		// 标记非叶子节点
		while(null != p1) {
			if(p1.isLeaf()) {
				savedNodeMap.put(p1, true);
				p2 = p1.getParentNode();
				while(null != p2 && !p2.isLeaf()) {
					savedNodeMap.put(p2, true);
					p2 = p2.getParentNode();
				}
				
			}
			p1 = p1.getNext();
		}
		
		// 删除节点
		p1 = root;
		p2 = p1.getNext();
		while(null != p2) {
			if(null == savedNodeMap.get(p2)) {
				p1.setNext(p2.getNext());
				p2 = p1.getNext();
				nodeNum--;
			} else {
				p1 = p2;
				p2 = p2.getNext();
			}
		}
	}
	
	
	// 打印路径
	public void displayPath() {
		PNode p1 = root, p2;
		Stack<PNode> pathStack = new Stack<>();
		while(null != p1) {
			if(p1.isLeaf()) {
				p2 = p1;
				pathStack.push(p2);
				while(null != (p2 = p2.getParentNode()))
					pathStack.push(p2);
				while(!pathStack.isEmpty()) {
					p2 = pathStack.pop();
					System.out.print(p2.getId() + " ");
				}
				System.out.println();
			}
			p1 = p1.getNext();
		}
	}
	
	// 获得总点数
	public int getNodeNum() {
		return nodeNum;
	}
	
	// 主函数
	public static void main(String[] args) {
		PNode root = new PNode(0, false);
		PTree pTree = new PTree(root);
		
		PNode lev11 = new PNode(1, true);
		pTree.addNode(root, lev11);
		PNode lev12 = new PNode(2, true);
		pTree.addNode(root, lev12);
		PNode lev13 = new PNode(3, true);
		pTree.addNode(root, lev13);
		
		PNode lev21 = new PNode(4, false);
		pTree.addNode(lev11, lev21);
		PNode lev22 = new PNode(5, false);
		pTree.addNode(lev12, lev22);
		PNode lev23 = new PNode(6, false);
		pTree.addNode(lev13, lev23);
		
		PNode lev32 = new PNode(7, true);
		pTree.addNode(lev22, lev32);
		
		System.out.println(pTree.getNodeNum());
		pTree.deleteUnnecessaryNode();
		System.out.println(pTree.getNodeNum());
		pTree.displayPath();
	}

}
