package precomputation.dbpedia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utility.Global;
import utility.IOUtility;
import utility.LocalFileInfo;
import utility.RandomNumGenerator;
import utility.TimeUtility;

public class DateInfoSpider implements Runnable{
	private String souPath = null;
	private String desPath = null;
	
	public static boolean containMiami = Boolean.TRUE;
	
	private static String signMaxConnections = "Maximum number";
	
	private static  RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(5000)
            .setSocketTimeout(5000).build();
	
	private static HttpClientBuilder httpClientBuilders[] = null;
	private static int indexOfHttpClientBuilders = 0;
	
	private static int spiderOver = 0;
	
	static {
		Map<String, Integer> proxyUrls = new HashMap<>();
		
		proxyUrls.put("172.31.72.114", 1081);
		proxyUrls.put("localhost", 1080);
		
		httpClientBuilders = new HttpClientBuilder[proxyUrls.size()];
		int i=0;
		for(Entry<String, Integer> url : proxyUrls.entrySet()) {
			HttpHost proxy = new HttpHost(url.getKey(), url.getValue());
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
			httpClientBuilders[i++] = HttpClients.custom().setRoutePlanner(routePlanner);
		}
	}
	
	private static int numDealLines = 0;
	private static int numDateNode = 0;
	private static int numSpider = 20;
	
	private ArrayBlockingQueue<String> queueTuple = null; 
	private ArrayBlockingQueue<List<String>> queueDates = null;
	private int typeThread = 1;
	private static String signReadOver = "E";
	private static List<String> signSpiderOver = new ArrayList<>();
	
	public DateInfoSpider(String souPath, String desPath, ArrayBlockingQueue<String> queueTuple) {
		this.souPath = souPath;
		this.desPath = desPath;
		this.queueTuple = queueTuple;
		typeThread = 1;
	}
	
	public DateInfoSpider(ArrayBlockingQueue<String> queueTuple, ArrayBlockingQueue<List<String>> queueDates) {
		this.queueTuple = queueTuple;
		this.queueDates = queueDates;
		typeThread = 2;
	}
	
	public DateInfoSpider(ArrayBlockingQueue<List<String>> queueDates, String desPath) {
		this.desPath = desPath;
		this.queueDates = queueDates;
		typeThread = 3;
	}
	
	public DateInfoSpider(String souPath, String desPath) {
		this.souPath = souPath;
		this.desPath = desPath;
	}
	
	public static int getNid(String tuple) {
		return Integer.parseInt(tuple.substring(0, tuple.indexOf(Global.delimiterLevel1)));
	}
	
	public static int getStartNid(String fp, int spiderNum) throws Exception{
		int nodeN = 0;
		BufferedReader br = IOUtility.getBR(fp);
		int nid = 0;
		int maxNide = 0;
		String line = null;
		br.readLine();
		while(null != (line = br.readLine())) {
			if(line.contains(": <")) {
				nodeN++;
				nid = getNid(line);
				if(maxNide < nid)	maxNide = nid;
			}
		}
		br.close();
		numDateNode = nodeN;
		return maxNide;
	}
	
	public static HttpClientBuilder getHttpClientBuilder() {
		indexOfHttpClientBuilders++;
		if(indexOfHttpClientBuilders==Integer.MAX_VALUE)	indexOfHttpClientBuilders = 0;
		return httpClientBuilders[indexOfHttpClientBuilders%httpClientBuilders.length];
	}
	
	public void run() {
		try {
			if(typeThread==1)	threadReadFile();
			else if(typeThread==2)	threadSpider();
			else if(typeThread==3)	threadOutput();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * 读线程
	 * @throws Exception
	 */
	public void threadReadFile() throws Exception{
		System.out.println("> 开始抓取DBpedia的date数据 . . . ");
		
		int startNid = getStartNid(desPath, numSpider);
		System.out.println("startNid = " + startNid);
		numDealLines = startNid;
		
		BufferedReader br = IOUtility.getBR(this.souPath);
		br.readLine();
		int i = 0;
		String line = null;
		
		while(null != (line = br.readLine()) && !line.isEmpty()) {
			if(startNid==getNid(line))	break;
		}
		if(null != line && !line.isEmpty()) {
			do {
				queueTuple.put(line);
			}while(null != (line = br.readLine()) && !line.isEmpty());
		}
		
		System.out.println("read thread start ending ");
		System.out.println(line);
		for(i=0; i<DateInfoSpider.numSpider; i++)	queueTuple.put(signReadOver);
		System.out.println("read thread end ending ");
		
		br.close();
	}
	
	/**
	 * 抓取数据线程
	 * @throws Exception
	 */
	public void threadSpider() throws Exception{
		CloseableHttpClient httpClient = null;
		HttpGet httpGet = null;
		CloseableHttpResponse httpResponse = null;
		HttpEntity httpEntity = null;
		String html = null;
		String url = null;
		List<String> dates = null;
		List<String> tupleDates = null;
		
		Boolean needTake = Boolean.TRUE;
		
		String tuple = null;
		while(true) {
			if(needTake)	{
				tuple = queueTuple.take();
			} else needTake = Boolean.TRUE;
			
			if(tuple.charAt(0) == signReadOver.charAt(0)) break;
			
			try {
				url = getURL(tuple);
				if(!url.startsWith("http://dbpedia.org"))	continue;
				
				httpClient = getHttpClientBuilder().build();
				
				httpGet = new HttpGet(url);
//				httpGet.setConfig(requestConfig);
				httpResponse = httpClient.execute(httpGet);
				
				Header[] headers = httpResponse.getAllHeaders();
//				if(2==headers.length) {
//					System.out.print(url + " > ");
//					for(Header hd : headers) {
//						System.out.print(hd + " ");
//					}
//					System.out.println();
//				}
				
				httpEntity = httpResponse.getEntity();
				html = EntityUtils.toString(httpEntity, "utf-8");
				
//				if(headers.length==2)	System.out.println(html + "\n");
				
				if(html.startsWith(signMaxConnections)) {
					httpClient.close();
					needTake = Boolean.FALSE;
					Thread.sleep(RandomNumGenerator.getRInt(3000));
					continue;
				}
				
				httpClient.close();
				
				dates = parseHtml(html);
				
			} catch (Exception e) {
				httpClient.close();
				continue;
			}
			if(null != dates)  {
				tupleDates = new ArrayList<>();
				tupleDates.add(tuple);
				tupleDates.addAll(dates);
				queueDates.put(tupleDates);
			}
			if((++numDealLines)%1000==0)	System.out.println("> 已处理" + numDealLines + "行");
		}
		System.out.println("spider over " + (++spiderOver));
		queueDates.put(signSpiderOver);
	}
	
	/**
	 * 文件输出线程
	 * @throws Exception
	 */
	public void threadOutput() throws Exception{
		int numSignSpiderOver = 0;
		List<List<String>> tuples = new ArrayList<>();
		List<String> tupleDates = null;
		BufferedWriter bw = IOUtility.getBW(this.desPath, Boolean.TRUE);
		if(numDealLines==0)	bw.write("           \n");
		while(true) {
			tupleDates = queueDates.take();
			if(signSpiderOver == tupleDates) {
				if(++numSignSpiderOver==DateInfoSpider.numSpider)	break;
				else continue;
			}
			tuples.add(tupleDates);
			numDateNode++;
			if(tuples.size()==100) {
				for(List<String> tuple : tuples) {
					for(String st : tuple) {
						bw.write(st + "\n");
					}
					tuple.clear();
				}
				tuples.clear();
				bw.flush();
			}
		}
		
		for(List<String> tuple : tuples) {
			for(String st : tuple) {
				bw.write(st + "\n");
			}
		}
		tuples.clear();
		
		System.out.println("Output thread over ");
		
		bw.close();
		
		RandomAccessFile raf = new RandomAccessFile(desPath, "rw");
		raf.writeBytes(String.valueOf(numDateNode));
		raf.close();
		System.out.println("> Over抓取DBpedia的date数据，" + TimeUtility.getTailTime());
	}
	
	public String getURL(String tuple) {
		return tuple.substring(tuple.indexOf('<') + 1, tuple.lastIndexOf('>'));
	}
	
	public List<String> parseHtml(String html) throws Exception{
		Document doc = Jsoup.parse(html);
		Elements trEles = doc.select("table[class=description table table-striped]>tbody>tr");
		Elements tEles = null;
		List<String> dates = null;
		for(Element tr : trEles) {
			Elements tds = tr.select("td");
			if(tds.size() < 1)	continue;
			String property = tds.get(0).text();
			String value = null;
			if(property.contains("Date")) {
				tEles = tds.get(1).select("span[class=literal]>span");
				if(tEles.isEmpty())	continue;
				value = tEles.get(0).text();
			}
			if(null != value) {
				if(null==dates)	dates = new ArrayList<>();
				dates.add(value);
			}
		}
		return dates;
	}
	
	/**
	 * 抓取date数据
	 * @throws Exception
	 */
	public void captureDates() throws Exception{
		System.out.println("> 开始抓取DBpedia的date数据");
		BufferedReader br = IOUtility.getBR(this.souPath);
		BufferedWriter bw = IOUtility.getBW(this.desPath);
		br.readLine();
		bw.write("           \n");
		
		CloseableHttpClient httpClient = getHttpClientBuilder().build();
		HttpGet httpGet = null;
		CloseableHttpResponse httpResponse = null;
		HttpEntity httpEntity = null;
		String html = null;
		String url = null;
		List<String> dates = null;
		
		String line = null;
		while(null != (line = br.readLine())) {
			url = getURL(line);
			httpGet = new HttpGet(url);
			httpResponse = httpClient.execute(httpGet);
			httpEntity = httpResponse.getEntity();
			html = EntityUtils.toString(httpEntity, "utf-8");
			dates = parseHtml(html);
			if(null != dates) {
				numDateNode++;
				bw.write(line + "\n");
				for(String dt : dates) {
					bw.write(dt + "\n");
				}
			}
//			if((++numDealLines)%100==0)	System.out.println("> 已处理" + numDealLines + "条");
			System.out.println("> 已处理" + (++numDealLines) + "条");
			bw.flush();
		}
		
		br.close();
		bw.close();
		
		RandomAccessFile raf = new RandomAccessFile(desPath, "rw");
		raf.writeBytes(String.valueOf(numDateNode));
		raf.close();
		
		System.out.println("> Over抓取DBpedia的date数据，" + TimeUtility.getTailTime());
	}
	
	/**
	 * 测试JSoup
	 * @param html
	 * @throws Exception
	 */
	public static void testJsoup(String html) throws Exception{
		Document doc = Jsoup.parse(html);
		Elements trEles = doc.select("table[class=description table table-striped]>tbody>tr");
		for(Element tr : trEles) {
			Elements tds = tr.select("td");
			if(tds.size() < 2)	continue;
			String property = tds.get(0).text();
			String value = null;
			if(property.contains("Date")) {
				value = tds.get(1).select("span[class=literal]>span").get(0).text();
			}
//			for(Element td : tds) {
//				System.out.print(td.html() + "\t\t\t");
//			}
			if(null != value) {
				System.out.print(property + "\t\t" + value);
				System.out.println("\n\n");
			}
			
		}
		
	}
	
	/**
	 * 测试HttpClient
	 * @throws Exception
	 */
	public static void testHttpClient() throws Exception{
		HttpHost proxy = new HttpHost("localhost", 1080);
		DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
		
		CloseableHttpClient httpClient = HttpClients.custom().setRoutePlanner(routePlanner).build();
		HttpGet httpGet = new HttpGet("http://dbpedia.org/page/Alexander_Deubner");
		CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
		HttpEntity httpEntity = httpResponse.getEntity();
		String html = EntityUtils.toString(httpEntity, "utf-8");
//		System.out.println(html);
		testJsoup(html);
		httpClient.close();
	}
	
	/**
	 * 单线程
	 * @throws Exception
	 */
	public static void singleThread() throws Exception{
//		DateInfoSpider.testHttpClient();
		String basePath = LocalFileInfo.getDataSetPath() + "DBpedia" + File.separator;
		String souPath = basePath + "orginal" + File.separator + "nodeIdMapDBpediaVB.txt";
		String desPath = basePath + "date_node" + File.separator + "nodeIdDateMapDBpediaVB.txt";
		DateInfoSpider spider = new DateInfoSpider(souPath, desPath);
		spider.captureDates();
	}
	
	/**
	 * 多线程
	 * @param numSpider
	 * @throws Exception
	 */
	public static void mutilThread(int numSpider) throws Exception{
		DateInfoSpider.numSpider = numSpider;
		String basePath = LocalFileInfo.getDataSetPath() + "DBpedia" + File.separator;
		String souPath = basePath + "orginal" + File.separator + "nodeIdMapDBpediaVB.txt";
		String desPath = basePath + "date_node" + File.separator + "nodeIdDateMapDBpediaVB.txt";
		ArrayBlockingQueue<String> queueTuple = new ArrayBlockingQueue<>(DateInfoSpider.numSpider);
		ArrayBlockingQueue<List<String>> queueDates = new ArrayBlockingQueue<>(DateInfoSpider.numSpider);
		
		new Thread(new DateInfoSpider(souPath, desPath, queueTuple)).start();
		for(int i=0; i<DateInfoSpider.numSpider; i++) {
			new Thread(new DateInfoSpider(queueTuple, queueDates)).start();
		}
		new Thread(new DateInfoSpider(queueDates, desPath)).start();
	}
	
	/**
	 * 抓取数据
	 * @param args
	 * @throws Exception
	 */
	public static void dateCapture(String[] args) throws Exception{
//		DateInfoSpider.singleThread();
		String basePath = LocalFileInfo.getDataSetPath() + "DBpedia" + File.separator;
		String souPath = basePath + "orginal" + File.separator + "nodeIdMapDBpediaVB.txt";
		String desPath = basePath + "date_node" + File.separator + "nodeIdDateMapDBpediaVB.txt";
		DateInfoSpider.numDealLines = 1;
		
		int num = 120;
//		System.out.println(getStartNid(desPath, num));
//		System.out.println(DateInfoSpider.numDateNode);
		
		if(args.length>1) {
			if(0!=Integer.parseInt(args[0])) {
			}
			num = Integer.parseInt(args[1]);
		} 
//		else proxyUrls.put("172.31.72.114", 1080);
		
		DateInfoSpider.mutilThread(num);
	}
	
	public static String getDateStr(String str) {
//		System.out.println(str);
		if(str.contains(" ") || str.contains(".") || str.contains("c") || str.contains("/") || str.contains("?"))	return null;
		else if(4==str.length() && str.charAt(0)>'0' && str.charAt(0)<='9')	return str += "-1-1";
		else if(str.startsWith("-"))	return null;
		else if(str.contains("-")) {
			String arr[] = str.split("-");
//			System.out.println(arr[0]);
			if(arr.length==1)	return str+"1-1";
			else if(arr[1].length()>2 || (arr.length>2 && arr[2].length()>2))	return null;
			else if(arr[0].charAt(0)<'0' || arr[0].charAt(0)>'9')	return null;
			else if(arr.length==2) return str+"-1";
			else return str;
		}
		else return null;
	}
	
	public static int getId(String line) {
		return Integer.parseInt(line.substring(0, line.indexOf(Global.delimiterLevel1)));
	}
	
	/**
	 * 提取Date信息
	 * @throws Exception
	 */
	public static void extractDateInfo() throws Exception{
		String basePath = LocalFileInfo.getDataSetPath() + "DBpedia" + File.separator;
		String souPath = basePath + "date_node" + File.separator + "nodeIdDateMapDBpediaVB.txt";
		String desPath = basePath + "date_node" + File.separator + "nodeIdIntDateMapDBpediaVB.txt";
		System.out.println("> 开始从 " + souPath + " 提取date信息 . . .  ");
		HashSet<Integer> recIds = new HashSet<>();
		Map<Integer, Integer> recIdDate = new TreeMap<>(); 
		BufferedReader br = IOUtility.getBR(souPath);
		br.readLine();
		String line = null;
		int id = 0;
		String date = null;
		Boolean hasDate = Boolean.FALSE;
		while(true) {
			if(!recIds.contains(id))	hasDate = Boolean.FALSE;
			else hasDate = Boolean.TRUE;
			while(true) {
				line = br.readLine();
				if(null==line || line.contains("<http"))	break;
				if(hasDate)	continue;
				date = getDateStr(line);
				if(null==date)	continue;
				recIdDate.put(id, TimeUtility.getIntDate(TimeUtility.getDate(date)));
				recIds.add(id);
				hasDate = Boolean.TRUE;
			}
			if(null==line)	break;
			id = getId(line);
		}
		br.close();
		
		BufferedWriter bw = IOUtility.getBW(desPath);
		bw.write(Integer.valueOf(recIdDate.size()) + Global.delimiterPound + "\n");
		for(Entry<Integer, Integer> en : recIdDate.entrySet()) {
			bw.write(String.valueOf(en.getKey()) + Global.delimiterLevel1 + String.valueOf(en.getValue()) + "\n");
		}
		bw.close();
		System.out.println("> Over");
	}
	
	/**
	 * 主方法
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		DateInfoSpider.extractDateInfo();
	}
}
