/**
 * 
 */
package kSP.candidate;

import java.util.List;
import spatialindex.rtree.NNEntry;

/**
 * A TQSP candidate, including the place id, and its spatial distance to query point, and its ranking score to the input query.
 * @author jieming
 *
 */
public class KSPCandidate {
	NNEntry placeEntry;//root place id, spatial distance, ranking score
	List<List<Integer>> pathsofPlace; // paths from the root place id to each query keyword.
	
	public KSPCandidate(NNEntry pcand, List<List<Integer>> paths) {
		placeEntry = pcand;
		pathsofPlace = paths;
	}
	
	public NNEntry getPlaceEntry(){
		return placeEntry;
	}

	public List<List<Integer>> getPathsofPlace() {
		return pathsofPlace;
	}
}
