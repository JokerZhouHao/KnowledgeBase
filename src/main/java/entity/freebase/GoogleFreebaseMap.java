package entity.freebase;

import java.util.HashMap;

import processor.freebase.GoogleFreebaseLineProcessService;

/**
 * 
 * @author Monica
 *
 * google-freebase数据集节点图，采用带头邻接链表
 */
public class GoogleFreebaseMap {
	
	private HashMap<StringObject, Node> nodeHasMap = new HashMap<StringObject, Node>();
	
	private Node head = new Node();
	private Node tail = head;
	private Node lastestNodes[] = new Node[2]; // 元素0表示最近访问的主语，元素1表示最近访问的宾语 
	private Integer size = 0; // 节点编号从0开始
	private Integer totalKeyword = 0;
	private Integer totalEdge = 0;
	
	public GoogleFreebaseMap() { 
		this.lastestNodes[0] = this.lastestNodes[1] = new Node(-1, new StringObject("root"));
	}
	
	
	public Node getHead() {
		return head;
	}

	public void setHead(Node head) {
		this.head = head;
	}

	public Node getTail() {
		return tail;
	}

	public void setTail(Node tail) {
		this.tail = tail;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Integer getTotalKeyword() {
		return totalKeyword;
	}

	public void setTotalKeyword(Integer totalKeyword) {
		this.totalKeyword = totalKeyword;
	}

	public Integer getTotalEdge() {
		return totalEdge;
	}

	public void setTotalEdge(Integer totalEdge) {
		this.totalEdge = totalEdge;
	}

	// 查找节点
	public Node findNode(String nodeName) {
		
		for(Integer i = 0; i<2; i++)
			if(this.lastestNodes[i].getNodeName().getStr().equals(nodeName)) return this.lastestNodes[i];
		
		Node p = this.nodeHasMap.get(new StringObject(nodeName));
		if(null!=p)	return p;
		else return null;
	}
	
	// 添加节点，freeBaseLineNode是带头单链表
	public void addNode(GoogleFreebaseLineProcessService.Node freeBaseLineNode) {
		freeBaseLineNode = freeBaseLineNode.getNext();
		int index = 0;
		while(null != freeBaseLineNode) {
			Node findedNode = this.findNode(freeBaseLineNode.getNodeStr());
			if(null!=findedNode) {
				if(null != freeBaseLineNode.getNodeAttr()) {
					Boolean res = findedNode.addKeyword(freeBaseLineNode.getNodeAttr());
					if(true==res) this.totalKeyword++;
				}
			} else {
				if(size==Integer.MAX_VALUE) {
					System.out.println("节点数超过Integer最大值！！！！！！！！！！！！！！！！！！！！！！！！！！");
					System.exit(0);
				}
				findedNode = new Node(size++, new StringObject(freeBaseLineNode.getNodeStr()));
				this.nodeHasMap.put(findedNode.getNodeName(), findedNode);
				if(null != freeBaseLineNode.getNodeAttr()) {
					Boolean res = findedNode.addKeyword(freeBaseLineNode.getNodeAttr());
					if(true==res) this.totalKeyword++;
				}
				findedNode.setNext(null);
				tail.setNext(findedNode);
				tail = findedNode;
			}
			this.lastestNodes[index++] = findedNode;
			if(2==index) {	// freebaseLineNode代表的行中存在主 谓 宾
				Boolean res = this.lastestNodes[0].addPointToNodeId(this.lastestNodes[1].getNodeId());
				if(true==res) this.totalEdge++;
			}
			freeBaseLineNode = freeBaseLineNode.getNext();
		}
	}
}
