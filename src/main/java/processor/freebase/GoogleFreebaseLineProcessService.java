package processor.freebase;

import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import utility.ReadLine;

/**
 * 
 * @author Monica
 *
 * 处理从google-freebase数据压缩包中的一行数据
 * 2017/11/06
 */

public class GoogleFreebaseLineProcessService {
	
	public GoogleFreebaseLineProcessService() {}
	
	public GoogleFreebaseLineProcessService(ArrayBlockingQueue<String> lq, ArrayBlockingQueue<Node> lnq) {
		ExecutorService exc = Executors.newCachedThreadPool();
		exc.execute(new ProductLineNodeThread(lq, lnq));
		exc.execute(new ProductLineNodeThread(lq, lnq));
		exc.shutdown();
	}
	// 放置节点属性
	public static class Node{
		private String nodeStr = null;
		private String nodeAttr = null;
		private Node next = null;
		public Node() {}
		public Node(String nodeStr) {
			super();
			this.nodeStr = nodeStr;
		}
		public Node(String nodeStr, String nodeAttr) {
			super();
			this.nodeStr = nodeStr;
			this.nodeAttr = nodeAttr;
		}
		public String getNodeStr() {
			return nodeStr;
		}
		public void setNodeStr(String nodeStr) {
			this.nodeStr = nodeStr;
		}
		public String getNodeAttr() {
			return nodeAttr;
		}
		public void setNodeAttr(String nodeAttr) {
			this.nodeAttr = nodeAttr;
		}
		public Node getNext() {
			return next;
		}
		public void setNext(Node next) {
			this.next = next;
		}
		// 添加节点
		public void addNode(Node node) {
			this.next = node;
			node.next = null;
		}
	}
	
	// 节点所在位置
	class NodePosition{
		Character s = null;
		Integer index = null;
		
		public NodePosition() {
			super();
		}
		
		public NodePosition(Character s, Integer index) {
			super();
			this.s = s;
			this.index = index;
		}

		public Character getS() {
			return s;
		}

		public void setS(Character s) {
			this.s = s;
		}

		public Integer getIndex() {
			return index;
		}

		public void setIndex(Integer index) {
			this.index = index;
		}
	}
	
	// 处理行，返回带头链表
	public GoogleFreebaseLineProcessService.Node processLineStr(String lineStr){
		Node head = new Node();
		Stack<NodePosition> nodePostionStack = new Stack<NodePosition>();
		Integer i=1, j=0, k=0;
		while(i<4) {
			for(k=j; k<lineStr.length(); k++) {
				NodePosition stackTop = null;
				if(nodePostionStack.isEmpty())	stackTop = null;
				else stackTop =  nodePostionStack.peek();
				if('<'==lineStr.charAt(k) && (null==stackTop || (null!=stackTop && '\"'!= stackTop.getS()))) {
					NodePosition node = new NodePosition('<', k);
					nodePostionStack.push(node);
				} else if('>' == lineStr.charAt(k) && null!=stackTop && '\"' != stackTop.getS() && '<' == stackTop.getS()) {
					if(2==i) {	// 忽略谓语
						nodePostionStack.pop();
						j = k + 1;
						i++;
						break;
					}
					Node tail = new Node(lineStr.substring(nodePostionStack.pop().getIndex(), k+1));
					Node p = head;
					while(null != p.getNext()) p = p.getNext();
					p.setNext(tail);
					tail.setNext(null);
					j = k + 1;
					i++;
					break;
				} else if('\"' == lineStr.charAt(k) && (null==stackTop || (null!=stackTop && '\"'!=stackTop.getS() && '\\' != stackTop.getS()))) {
					NodePosition node = new NodePosition('\"', k);
					nodePostionStack.push(node);
				}else if('\"' == lineStr.charAt(k) && null!=stackTop && '\"' == stackTop.getS()) {
					Integer k1 = k;
					do { // 处理类似  "chat"@fr 的节点，或""^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral>
						k++;
					}while(k!=lineStr.length() && ' '!=lineStr.charAt(k) && '\t'!=lineStr.charAt(k) && '^'!=lineStr.charAt(k) && '.'!=lineStr.charAt(k));
					if(k==lineStr.length())	k = k1;
					else {
						if('\"'==lineStr.charAt(k-1)) k--;
					}
					String lineSubStr = lineStr.substring(nodePostionStack.pop().getIndex()+1, k);
					if(!lineSubStr.trim().isEmpty()) {
						head.getNext().nodeAttr = lineSubStr;
						head.getNext().setNext(null);
					}
					return head;
				}else if('\"' == lineStr.charAt(k) && null!=stackTop && '\\' == stackTop.getS()) {	// 处理转义符
					nodePostionStack.pop();
				}else if('\\' == lineStr.charAt(k) && null!=stackTop && '\\' != stackTop.getS()) {  // 处理转义符
					nodePostionStack.push(new NodePosition('\\', k));
				} else if(null!=stackTop && '\\' == stackTop.getS()) {	// 处理转义符
					nodePostionStack.pop();
				}
			}
			if(k==lineStr.length()) break;
		}
		return head;
	}
	
	public void displayNodeLink(Node head) {
		while(null!=head) {
			System.out.println("nodeStr = " + head.getNodeStr() + " ---- nodeAttr = " + head.getNodeAttr());
			head = head.next;
		}
	}
	
	public static void main(String[] args) {
//		ReadLine readLine = new ReadLine("./data/normal-test-data.txt");
//		GoogleFreebaseLineProcessService process = new GoogleFreebaseLineProcessService();
//		String str = null;
//		while((str=readLine.readLine())!=null) {
//			Node head = process.processLineStr(str);
//			System.out.println("lineStr : " + str);
//			process.displayNodeLink(head.getNext());
//			System.out.println();
//		}
	}

	class ProductLineNodeThread implements Runnable{
		private ArrayBlockingQueue<String> lineQueue = null;
		private ArrayBlockingQueue<GoogleFreebaseLineProcessService.Node> lineNodeQueue = null;
		
		public ProductLineNodeThread(ArrayBlockingQueue<String> qu, ArrayBlockingQueue<GoogleFreebaseLineProcessService.Node> lq) {
			this.lineQueue = qu;
			this.lineNodeQueue = lq;
		}
		
		@Override
		public void run() {
			String lineStr = null;
			while(true) {
				try {
					lineStr = lineQueue.take();
					if(lineStr.equals("[^\\]")) {
						Node n = new Node();
						n.setNext(null);
						lineNodeQueue.put(n);
						break; //	读完了
					} else {
//						Node no = processLineStr(lineStr);
//						System.out.println(no.getNodeStr());
						lineNodeQueue.put(processLineStr(lineStr));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
	}
}
