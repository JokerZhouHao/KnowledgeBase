package zhou.hao.yago2s.entity;

import java.util.ArrayList;

/**
 * @author Monica
 * @since 2018/03/15
 * 功能 ： 路径树节点
 */
public class PNode {
	private int id = -1;
	private ArrayList<PNode> childs = null;
	
	public PNode(int id) {
		this.id = id;
	}
	public PNode(int id, ArrayList<PNode> childs) {
		this.id = id;
		this.childs = childs;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public ArrayList<PNode> getChilds() {
		return childs;
	}
	public void setChilds(ArrayList<PNode> childs) {
		this.childs = childs;
	}
	
}
