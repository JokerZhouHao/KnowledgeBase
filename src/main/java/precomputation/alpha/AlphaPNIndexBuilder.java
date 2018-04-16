/**
 * 
 */
package precomputation.alpha;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import entity.sp.RadiusNeighborhood;
import entity.sp.SortedList;
import entity.sp.SortedList.SortedListNode;
import precomputation.sp.IndexAlphaPNService;
import utility.Global;
import utility.GraphUtility;
import utility.Utility;

/**
 * Build the alpha wn inverted index part by part.
 * @author jmshi
 *
 */
public class AlphaPNIndexBuilder {

	class AlphaPN{
		private HashMap<Integer, String>[] eachLayerWN = null;
		
		public AlphaPN(int radius) {
			eachLayerWN = new HashMap[radius + 1];
		}
		
		public void clear() {
			for(HashMap<Integer, String> hm : eachLayerWN) {
				if(null != hm)	hm.clear();
			}
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for(HashMap<Integer, String> pIdToDateMap : eachLayerWN) {
				if(null == pIdToDateMap || pIdToDateMap.isEmpty()) {
					sb.append(Global.signEmptyLayer + Global.delimiterLayer);
					continue;
				}
				for(Entry<Integer, String> en : pIdToDateMap.entrySet()) {
					sb.append(en.getKey() + Global.delimiterLevel2 + en.getValue() + Global.delimiterSpace);
				}
				sb.append(Global.delimiterLayer);
			}
			return sb.toString();
		}
	}
	
	/**
	 * 输出alphaPN
	 * @param writer
	 * @param radius
	 * @param vid
	 * @param radiusWN
	 */
	private void outputAlphaPN(PrintWriter writer, int radius, int vid, AlphaPN alphaPN) {
		writer.print(vid + Global.delimiterLevel1);
//		for(HashMap<Integer, String> pIdToDateMap : alphaPN.eachLayerWN) {
//			if(null == pIdToDateMap || pIdToDateMap.isEmpty()) {
//				writer.print(Global.signEmptyLayer + Global.delimiterLayer);
//				continue;
//			}
//			for(Entry<Integer, String> en : pIdToDateMap.entrySet()) {
//				writer.print(en.getKey() + Global.delimiterLevel2 + en.getValue() + Global.delimiterSpace);
//			}
//			writer.print(Global.delimiterLayer);
//		}
		writer.print(alphaPN.toString());
		writer.println();
		writer.flush();
	}
	
	public static void main(String[] args) throws Exception {
//		if (args.length != 4) {
//			throw new Exception("\n Usage: runnable configFile inputAlphaWNfile outputAlphaIindexFile keywordInterval");
//		}
//		Utility.loadInitialConfig(args[0]);
//		String inputDocFile = args[1];
//		String outputIindexFile = args[2];
//		int interval = Integer.parseInt(args[3]);
		String inputDocFile = Global.outputDirectoryPath + Global.placeWN + Global.rtreeFlag
				+ Global.rtreeFanout + "." + Global.radius + Global.dataVersion;
		String outputIindexFile = Global.outputDirectoryPath + Global.alphaPN + Global.rtreeFlag
				+ Global.rtreeFanout + "." + Global.radius + Global.dataVersion;
		int startKeyword = Global.numNodes;
		int endKeyword = Global.numNodes + Global.numKeywords;
		int interval = endKeyword - startKeyword - 1;

		PrintWriter writer = new PrintWriter(outputIindexFile);
		PrintWriter writerstat = new PrintWriter(outputIindexFile + ".stat.txt");
		int iindexSize = 0;
		int iindexTotalLength = 0;
//		writer.println(Global.delimiterPound);// just output #
		long start = System.currentTimeMillis();
		
		AlphaPNIndexBuilder alphaPNBuilder = new AlphaPNIndexBuilder();
		IndexAlphaPNService alphaIndexSer = new IndexAlphaPNService(Global.outputDirectoryPath + Global.indexWidToPlaceNeighborhood);
		alphaIndexSer.openIndexWriter();
		
		HashMap<Integer, AlphaPN> alphaPNMap = new HashMap<>();
		AlphaPN radiusPN = null;
		while (startKeyword < endKeyword) {
			System.out.println("processing keywords [" + startKeyword + "," + (startKeyword + interval) + "]");
			// build partial inverted index
			alphaPNBuilder.buildAlphaPN(startKeyword,
					startKeyword + interval, inputDocFile, alphaPNMap);
			// output partial inverted index
			for (int kid = startKeyword; kid <= startKeyword + interval; kid++) {
				if(null == (radiusPN = alphaPNMap.get(kid)))	continue;
				//a new keyword with nonempty posting list.
				iindexSize++;
				iindexTotalLength += 1;
				alphaPNBuilder.outputAlphaPN(writer, Global.radius, kid, radiusPN);
				alphaIndexSer.addDoc(kid, radiusPN.toString());
				radiusPN.clear();
			}
			// clear and go to next batch
			alphaPNMap.clear();
			startKeyword += interval + 1;
		}
		long end = System.currentTimeMillis();
		System.out.println("Revision Minutes: " + ((end - start) / 1000.0f) / 60.0f);
		
		writer.flush();
		writer.close();
		
		alphaIndexSer.closeIndexWriter();
		
		writerstat.println(iindexSize + Global.delimiterPound + iindexTotalLength + Global.delimiterPound);
		writerstat.close();
	}

	private void buildAlphaPN(int startKeyword,
			int endKeyword, String inputDocFile, HashMap<Integer, AlphaPN> alphaPNMap) throws Exception {
		
		// Map<Integer, Set<String>> invertedIndex = new HashMap<Integer,
		// Set<String>>();
		// read nidKeywordListMap line by line to build inverted index
		BufferedReader reader = Utility.getBufferedReader(inputDocFile);
		String line;

		int cntlines = 0;
		String[] layers = null;
		String[] widDates = null;
		String dates = null;
		int i, j, pid, wid;
		AlphaPN alphaPN = null;
		HashMap<Integer, String> tempMap = null;
		while ((line = reader.readLine()) != null) {
			cntlines++;
//			if (line.contains(Global.delimiterPound)) {
//				continue;
//			}

//			if (cntlines % 10000 == 0) {
//				System.out.print(cntlines + ",");
//			}
			i = line.indexOf(Global.delimiterLevel1);
			pid = Integer.parseInt(line.substring(0, i));
			layers = line.substring(i + Global.delimiterLevel1.length()).split(Global.delimiterLayer);
			if(layers.length != Global.radius+1)	continue;
			
			for(i = 0; i<layers.length; i++) {
				if(!layers[i].equals(Global.signEmptyLayer)) {
					widDates = layers[i].split(Global.delimiterSpace);
					for(String st : widDates) {
						j = st.indexOf(Global.delimiterLevel2);
						wid = Integer.parseInt(st.substring(0, j));
						if(wid < startKeyword || wid > endKeyword) {
							continue;
						}
						
						dates = st.substring(j + 1);
						if(null == (alphaPN = alphaPNMap.get(wid))) {
							alphaPN = new AlphaPN(Global.radius);
							alphaPNMap.put(wid, alphaPN);
						}
						if(null == (tempMap = alphaPN.eachLayerWN[i])) {
							tempMap = alphaPN.eachLayerWN[i] = new HashMap<>();
						}
						tempMap.put(pid, dates);
					}
				}
			}
		}
		reader.close();
	}
}
