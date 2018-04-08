/**
 * 
 */
package utility;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author jieming
 *
 */
public class TFlabelUtility {
	public static Map<Integer, Set<Integer>> convertToDAG(String edgeFile, Map<Integer, Integer> vertexSCCMap)
			throws Exception {
		Map<Integer, Set<Integer>> DAGedges = new HashMap<Integer, Set<Integer>>();

		BufferedReader reader = Utility.getBufferedReader(edgeFile);
		String line = reader.readLine();// first line is statistics of graph

		String[] stat = line.split(Global.delimiterPound);
		int numEdges = Integer.parseInt(stat[1]);
		if (numEdges != Global.numEdges) {
			throw new Exception("numEdges in configuration is " + Global.numEdges
					+ " but stat in read-file is " + numEdges);
		}

		int cntLines = 0;
		while ((line = reader.readLine()) != null) {
			cntLines++;
			String[] nidOutNids = line.split(Global.delimiterLevel1);
			if (nidOutNids.length != 2) {
				throw new Exception("nid->nids direct edge splits wrong length, should be 2 but is "
						+ nidOutNids.length);
			}

			int startV = Integer.parseInt(nidOutNids[0]);
			Integer startSCC = vertexSCCMap.get(startV);
			Set<Integer> endSCCsOftheStartSCC = DAGedges.get(startSCC);
			if (endSCCsOftheStartSCC == null) {
				endSCCsOftheStartSCC = new HashSet<Integer>();
			}

			String[] endVsStr = nidOutNids[1].split(Global.delimiterLevel2);
			for (int i = 0; i < endVsStr.length; i++) {
				int endVi = Integer.parseInt(endVsStr[i]);
				Integer endSCC = vertexSCCMap.get(endVi);
				// there is an edge pointing from startSCC to endSCC
				if (endSCC.intValue() == startSCC.intValue()) {
					continue;
				}
				endSCCsOftheStartSCC.add(endSCC);
			}
			if (endSCCsOftheStartSCC.size() != 0) {
				DAGedges.put(startSCC, endSCCsOftheStartSCC);
			}
		}
		reader.close();
//		System.out.println(cntLines);

		return DAGedges;
	}

	public static void augmentKeywordsToDAG(String vertexDocumentFile, Map<Integer, Integer> vertexSCCMap,
			Map<Integer, Set<Integer>> DAGedges) throws Exception {

		BufferedReader reader = Utility.getBufferedReader(vertexDocumentFile);
		String line;

		int cntLines = 0;
		while ((line = reader.readLine()) != null) {
			cntLines++;
			if (cntLines % 100000 == 0) {
				System.err.println("processed " + cntLines + " vertexDoc");
			}
			if (line.contains(Global.delimiterPound)) {
				continue;
			}
			String[] nidDocument = line.split(Global.delimiterLevel1);
			if (nidDocument.length != 2) {
				throw new Exception("nid->doc splits wrong length, should be 2 but is "
						+ nidDocument.length);
			}

			int startV = Integer.parseInt(nidDocument[0]);
			Integer startSCC = vertexSCCMap.get(startV);
			Set<Integer> endSCCsOftheStartSCC = DAGedges.get(startSCC);
			if (endSCCsOftheStartSCC == null) {
				endSCCsOftheStartSCC = new HashSet<Integer>();
			}

			String[] endWordsStr = nidDocument[1].split(Global.delimiterLevel2);
			for (int i = 0; i < endWordsStr.length; i++) {
				int endWordSCC = Integer.parseInt(endWordsStr[i]);
				// there is an edge pointing from startSCC to endSCC
				if (endWordSCC == startSCC.intValue()) {
					continue;
				}
				endSCCsOftheStartSCC.add(endWordSCC);
			}
			if (endSCCsOftheStartSCC.size() != 0) {
				DAGedges.put(startSCC, endSCCsOftheStartSCC);
			}
		}
		reader.close();
//		System.out.println(cntLines);
	}

	// load the the vertexid-sccid map
	public static Map<Integer, Integer> loadVertexSCCMap(String inputFile) throws Exception {
		Map<Integer, Integer> vertexSCCMap = new HashMap<Integer, Integer>();
		BufferedReader reader = Utility.getBufferedReader(inputFile);
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains(Global.delimiterPound)) {
				continue;
			}

			String[] sccVertices = line.split(Global.delimiterLevel1);
			if (sccVertices.length != 2) {
				throw new Exception("scc does not contain any vertices? " + line);
			}
			int sccid = Integer.parseInt(sccVertices[0]);
			String[] vertices = sccVertices[1].split(Global.delimiterLevel2);
			for (int i = 0; i < vertices.length; i++) {
				int vertex = Integer.parseInt(vertices[i]);
				vertexSCCMap.put(vertex, sccid);
			}
		}
		if (vertexSCCMap.size() != Global.numNodes) {
			throw new Exception("there are " + vertexSCCMap.size() + " places in scc but should be "
					+ Global.numNodes);
		}
		return vertexSCCMap;
	}
}
