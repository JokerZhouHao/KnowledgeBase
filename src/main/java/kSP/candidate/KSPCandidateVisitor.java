package kSP.candidate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import spatialindex.rtree.Data;
import spatialindex.spatialindex.IData;
import spatialindex.spatialindex.INode;
import spatialindex.spatialindex.IVisitor;

/**
 * The class that stores the top-k Semantic Places result.
 * @author jieming
 *
 */
public class KSPCandidateVisitor implements IVisitor{
	int k;
	/* I need a priority queue here. It turns out that TreeSet sorts unique keys only and since I am
 	   sorting according to distances, it is not assured that all distances will be unique. TreeMap
	   also sorts unique keys. Thus, I am simulating a priority queue using an ArrayList and binarySearch. */
	
	//i.e., the min heap H_k in the paper.
	ArrayList resultQ;
	
	public KSPCandidateVisitor(int k){
		this.k = k;
		resultQ = new ArrayList();
	}
	
	/**
	 * add a place candidate to resultQ
	 */
	public boolean addPlaceCandidate(KSPCandidate candidate) {
		if (resultQ.size() < k) {
			//if there is no enough candidates, add candidate to proper position of result queue
			int insertloc = Collections.binarySearch(resultQ, candidate, new KSPCandidateComparator());
			if (insertloc >= 0) resultQ.add(insertloc, candidate);//there is already a candidate with same distance
			else resultQ.add((-insertloc - 1), candidate);
			return true;
		}
		else
		{
			@SuppressWarnings("unchecked")
			int insertloc = Collections.binarySearch(resultQ, candidate, new KSPCandidateComparator());
			if (insertloc >= 0) {
				//If the ranking score of the candidate is <= kth score, ==> the candidate should be inserted into result
				resultQ.add(insertloc, candidate);
				//Remove the > kth invalid candidates to keep resultQ with size k.
				removeInvalidCandidate();
				if(candidate.placeEntry.m_minDist == ((KSPCandidate)resultQ.get(resultQ.size() -1)).placeEntry.m_minDist) {
					return false;
				}
				return true;
			}
			else
			{
				int realInsertloc = -insertloc - 1;
				if (realInsertloc == resultQ.size()) {
					//invalid place candidate, all the k results we have are better than the candidate, do nothing
					return false;
				}
				else
				{	//the candidate is vaild, add it to result and remove those become invalid
					resultQ.add(realInsertloc, candidate);
					removeInvalidCandidate();
					return true;
				}
			}
		}
	}
	
	/**
	 * get the size of result queue, maybe larger than k
	 * @return
	 */
	public int size() {
		return resultQ.size();
	}
	
	public KSPCandidate get(int index) {
		return (KSPCandidate) resultQ.get(index);
	}
	
	/**
	 * get the worst ranking score: 
	 * @return
	 */
	public double getWorstRankingScore() {
		KSPCandidate lastCand = (KSPCandidate)resultQ.get(resultQ.size() - 1);
		return lastCand.placeEntry.m_minDist;
	}

	/**
	 * Remove the > kth invalid candidates to keep resultQ with size k.
	 * Prerequisite of calling this function: resultQ is larger than k
	 */
	public void removeInvalidCandidate() {
		if (resultQ.size() > k) {
			KSPCandidate kthCand = (KSPCandidate) resultQ.get(k-1);
			KSPCandidate lastCand = (KSPCandidate) resultQ.get(resultQ.size() - 1);
			
			assert kthCand.placeEntry.m_minDist <= lastCand.placeEntry.m_minDist;
			
			if (kthCand.placeEntry.m_minDist <= lastCand.placeEntry.m_minDist) {
				//need to delete all the candidates with lastCand.m_minDist from index k to the end
				do {
					resultQ.remove(k);
				} while (resultQ.size() > k);
			}
		}
	}
	
	public String toString(){
		String result = "";
		for (int i = 0; i < resultQ.size(); i++) {
			KSPCandidate candidate = (KSPCandidate) resultQ.get(i);
			Data placeData = (Data) candidate.placeEntry.m_pEntry;
			result += placeData.getIdentifier() + " " + placeData.getParent() + " " + placeData.getShape() + ": sdist " 
			+ placeData.getWeight() + " sgdist " + candidate.placeEntry.m_minDist + "\n";
			
			List<List<Integer>> pathsofPlace = candidate.pathsofPlace;
			if (pathsofPlace != null) {
				for (int j = 0; j < pathsofPlace.size(); j++) {
					List<Integer> path = pathsofPlace.get(j);
					for (int j2 = 0; j2 < path.size(); j2++) {
						result += path.get(j2) + "\t";
					}
					result += "\n";
				}
			}
		}
		return result;
	}
	

	@Override
	public void visitNode(INode n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitData(IData d) {
		// TODO Auto-generated method stub
		
	}

	public ArrayList getResultQ() {
		return resultQ;
	}
	
}
