/**
 * 
 */
package utility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Utility functions
 * @author jmshi
 *
 */
public class Utility<T1, T2> {

	/**
	 * Read the configurations of the whole program, including 
	 * 		the settings of indexes, 
	 * 		the input and output directories of the program
	 * 		statistics of datasets,
	 * 		thresholds,
	 * 		ETC.
	 * Feel free to custom it.
	 * 
	 * Each line is a configuration in format: "configName: configValue"
	 * numPlaces: number of places
	 * numNodes: number of nodes in the RDF graph
	 * numEdges: number of edges in the RDF graph
	 * numKeywords: size of keyword dictionary
	 * numSCCs: number of the strong connected components in the RDF graph
	 * runtimeThreshold: max runtime threshold for each query
	 * ......
	 * @param filePath
	 * @throws IOException
	 */
	public static void loadInitialConfig(String filePath) throws IOException {
		BufferedReader reader = Utility.getBufferedReader(filePath);
		String line;

		while ((line = reader.readLine()) != null) {
			String[] splits = line.split(Global.delimiterLevel1);
			if (splits[0].compareTo("numPlaces") == 0) {
				Global.numPlaces = Integer.parseInt(splits[1]);
			} else if (splits[0].compareTo("numNodes") == 0) {
				Global.numNodes = Integer.parseInt(splits[1]);
			} else if (splits[0].compareTo("numKeywords") == 0) {
				Global.numKeywords = Integer.parseInt(splits[1]);
			} else if (splits[0].compareTo("numEdges") == 0) {
				Global.numEdges = Integer.parseInt(splits[1]);
			} else if (splits[0].compareTo("numSCCs") == 0) {
				Global.numSCCs = Integer.parseInt(splits[1]);
			} else if (splits[0].compareTo("runtimeThreshold") == 0) {
				Global.runtimeThreshold = Integer.parseInt(splits[1]);}
			// rtree
			else if (splits[0].compareTo("rtreeBufferSize") == 0) {
				Global.rtreeBufferSize = Integer.parseInt(splits[1]);
			} else if (splits[0].compareTo("rtreePageSize") == 0) {
				Global.rtreePageSize = Integer.parseInt(splits[1]);
			} else if (splits[0].compareTo("rtreeFanout") == 0) {
				Global.rtreeFanout = Integer.parseInt(splits[1]);
			}
			// inverted index
			else if (splits[0].compareTo("iindexBufferSize") == 0) {
				Global.iindexBufferSize = Integer.parseInt(splits[1]);
			} else if (splits[0].compareTo("iindexPageSize") == 0) {
				Global.iindexPageSize = Integer.parseInt(splits[1]);
			} else if (splits[0].compareTo("iindexIsCreate") == 0) {
				if (splits[1].contains("true")) {
					Global.iindexIsCreate = true;
				}
			} else if (splits[0].compareTo("iindexIsWeighted") == 0) {
				if (splits[1].contains("true")) {
					Global.iindexIsWeighted = true;
				}
			}
			else if (splits[0].compareTo("alphaIindexRTNodeBufferSize") == 0) {
				Global.alphaIindexRTNodeBufferSize = Integer.parseInt(splits[1]);
			} else if (splits[0].compareTo("alphaIindexFile") == 0) {
				Global.alphaIindexFile = splits[1];
			} else if (splits[0].compareTo("dataVersion") == 0) {
				Global.dataVersion = splits[1];
				Global.dataVersionWithoutExtension = splits[1].substring(0, splits[1].lastIndexOf('.'));
			}

			// input/output configuration section
			else if (splits[0].compareTo("inputDirectoryPath") == 0) {
				Global.inputDirectoryPath = splits[1];
			} else if (splits[0].compareTo("nidKeywordsListMapFile") == 0) {
				Global.nidKeywordsListMapFile = splits[1];
			} else if (splits[0].compareTo("invertedIndexFile") == 0) {
				Global.invertedIndexFile = splits[1];
			} else if (splits[0].compareTo("pidCoordFile") == 0) {
				Global.pidCoordFile = splits[1];
			} else if (splits[0].compareTo("edgeFile") == 0) {
				Global.edgeFile = splits[1];
			} else if (splits[0].compareTo("tfindexDirectoryPath") == 0) {
				Global.tfindexDirectoryPath = splits[1];
			} else if (splits[0].compareTo("outputDirectoryPath") == 0) {
				Global.outputDirectoryPath = splits[1];
			}
		}
	}

	/**
	 * Get the buffered reader of inputFile
	 * @param inputFile
	 * @return
	 */
	public static BufferedReader getBufferedReader(String inputFile) {
		FileInputStream fin;
		try {
			fin = new FileInputStream(inputFile);
			InputStreamReader isreader = new InputStreamReader(fin);
			return new BufferedReader(isreader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(inputFile + " invalid");
			return null;
		}
	}

	/**
	 * Output the content of map inputMap into file outFileName in format:
	 * key: value
	 * @param outFileName
	 * @param inputMap
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public void outputMap(String outFileName, Map<T1, T2> inputMap) throws FileNotFoundException,
			UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(outFileName);
		for (Entry<T1, T2> entry : inputMap.entrySet()) {
			writer.print(entry.getKey() + ": ");
			writer.println(entry.getValue());
		}
		writer.close();
	}

	/**
	 * Output a map with sets of strings as values, mapOfSets, into file outFileName
	 * The first line is the metadata 
	 * @param outFileName
	 * @param mapOfSets
	 * @param numSCCs
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public void outputMapOfSetsTFLabelFormat(String outFileName, Map<T1, Set<T2>> mapOfSets,
			int numSCCs) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(outFileName);
		int totalCount = 0;
		for (Entry<T1, Set<T2>> entry : mapOfSets.entrySet()) {
			totalCount += entry.getValue().size();
		}

		writer.println(numSCCs + Global.delimiterSpace + totalCount + Global.delimiterSpace);
		for (Entry<T1, Set<T2>> entry : mapOfSets.entrySet()) {
			if (entry.getValue().size() == 0) {
				continue;
			}
			writer.print(entry.getKey() + Global.delimiterSpace);
			writer.print(entry.getValue().size() + Global.delimiterSpace);
			for (T2 value : entry.getValue()) {
				writer.print(value + Global.delimiterSpace);
			}
			writer.println();
		}
		writer.close();
	}

	// output: OVERWRITE a MAP with Sets as values to file
	public void outputListOfSets(String outFileName, List<Set<T1>> inputListWithSetValues)
			throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(outFileName);
		writer.println(inputListWithSetValues.size() + Global.delimiterPound);
		for (int i = 0; i < inputListWithSetValues.size(); i++) {
			Set<T1> values = inputListWithSetValues.get(i);
			if (values.size() == 0) {
				continue;
			}
			writer.print(i + Global.delimiterLevel1);
			for (T1 value : values) {
				writer.print(value + Global.delimiterLevel2);
			}
			writer.println();
		}
		writer.close();
	}
}
