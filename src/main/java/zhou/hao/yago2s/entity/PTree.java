package zhou.hao.yago2s.entity;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author Monica
 * @since 2018/03/15
 * 功能 ： 记录路径树
 */
public class PTree {
	private PNode root = null;
	
	public PTree(PNode root) {
		this.root = root;
	}
	
	// 获得根节点
	public PNode getRoot() {	return root;	}
	
	// 添加节点
	public void addNode(PNode parentNode, PNode insertedNode) {
		if(null == parentNode.getChilds()) {
			parentNode.setChilds(new ArrayList<>());
		}
		parentNode.getChilds().add(insertedNode);
	}
	
	// 打印路径
	public void displayPath() {
		this.displayPath(new LinkedList<Integer>(), root);
	}
	
	private void displayPath(LinkedList<Integer> pathList, PNode curNode) {
		if(null != curNode.getChilds()) {
			pathList.add(curNode.getId());
			for(PNode pn : curNode.getChilds()) {
				this.displayPath(pathList, pn);
			}
			pathList.removeLast();
		} else {
			for(int in : pathList)
				System.out.print(in + " > ");
			System.out.print(curNode.getId());
			System.out.println();
		}
	}
	
	// 主函数
	public static void main(String[] args) {
		PNode root = new PNode(0);
		PTree pTree = new PTree(root);
		
		PNode lev1 = new PNode(1);
		pTree.addNode(root, lev1);
		pTree.addNode(root, new PNode(2));
		
		pTree.addNode(lev1, new PNode(3));
		pTree.addNode(lev1, new PNode(4));
		
		pTree.displayPath();
	}
}
