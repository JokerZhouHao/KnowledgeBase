package zhou.hao.entry;

import java.util.ArrayList;

/**
 * 
 * @author Monica
 *
 * 图的节点
 * 2017/11/06
 */

public class Node{
	private Integer nodeId = null;
	private StringObject nodeName = null;
	private ArrayList<String> keywordList = null;
	private ArrayList<Integer> pointToNodeIdList = null;
	private Node next = null;
	
	public Node() {}
	
	public Node(Integer nodeId, StringObject nodeName) {
		super();
		this.nodeId = nodeId;
		this.nodeName = nodeName;
	}
	
	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	public StringObject getNodeName() {
		return nodeName;
	}

	public void setNodeName(StringObject nodeName) {
		this.nodeName = nodeName;
	}

	public ArrayList<String> getKeywordList() {
		return keywordList;
	}

	public void setKeywordList(ArrayList<String> keywordList) {
		this.keywordList = keywordList;
	}

	public ArrayList<Integer> getPointToNodeIdList() {
		return pointToNodeIdList;
	}

	public void setPointToNodeIdList(ArrayList<Integer> pointToNodeIdList) {
		this.pointToNodeIdList = pointToNodeIdList;
	}

	public Node getNext() {
		return next;
	}

	public void setNext(Node next) {
		this.next = next;
	}
	
	// 添加关键字, true表示不存在，false表示存在
	public Boolean addKeyword(String keyword) {
		if(null == this.keywordList) this.keywordList = new ArrayList<String>(2);
		Integer i = 0;
		for(i=0; i<this.keywordList.size(); i++) {
			if(keyword.length()==this.keywordList.get(i).length() && keyword.equals(this.keywordList.get(i))) break;
		}
		if(i==this.keywordList.size()) {
			this.keywordList.add(keyword);
			return true;
		}
		return false;
	}
	
	// 添加连接的点，true表示不存在，false表示不存在
	public Boolean addPointToNodeId(Integer id) {
		if(null == this.pointToNodeIdList) this.pointToNodeIdList = new ArrayList<Integer>(2);
		Integer i = 0;
		for(i=0; i<this.pointToNodeIdList.size(); i++) {
			if(id == this.pointToNodeIdList.get(i)) break;
		}
		if(i==this.pointToNodeIdList.size()) {
			this.pointToNodeIdList.add(id);
			return true;
		}
		return false;
	}
	
}