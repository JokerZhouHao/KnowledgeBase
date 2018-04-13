package test;

import java.util.concurrent.*;


// 生产者
class ProductorB implements Runnable{
	private ArrayBlockingQueue<Character> sendQueue = null;
	private ArrayBlockingQueue<Character> recQueue = null;
	
	public ProductorB(ArrayBlockingQueue<Character> senQueue, ArrayBlockingQueue<Character> recQueue) {
		this.sendQueue = senQueue;
		this.recQueue = recQueue;
	}
	
	public void run() {
		while(true) {
			try {
				Character sign = recQueue.take();
				System.out.println("Pro get " + sign);
				Thread.sleep(1000);
				sendQueue.put('P');
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

class ConsumerB implements Runnable{
	private ArrayBlockingQueue<Character> sendQueue = null;
	private ArrayBlockingQueue<Character> recQueue = null;
	
	public ConsumerB(ArrayBlockingQueue<Character> senQueue, ArrayBlockingQueue<Character> recQueue) {
		this.sendQueue = senQueue;
		this.recQueue = recQueue;
	}
	
	public void run() {
		while(true) {
			try {
				Character sign = recQueue.take();
				System.out.println("Con get " + sign);
				Thread.sleep(1000);
				sendQueue.put('C');
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

public class SendAndReciver {
	public static void main(String[] args) {
		ArrayBlockingQueue<Character> que1 = new ArrayBlockingQueue<Character>(2);
		ArrayBlockingQueue<Character> que2 = new ArrayBlockingQueue<Character>(2);
		try {
			que2.put('S');
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		ExecutorService exec = Executors.newCachedThreadPool();
		exec.execute(new ProductorB(que1, que2));
		exec.execute(new ConsumerB(que2, que1));
		exec.shutdown();
	}
}
