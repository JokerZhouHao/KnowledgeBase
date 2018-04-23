package kSP.candidate;

import java.util.Comparator;

import spatialindex.rtree.NNEntry;

public 	class KSPCandidateComparator implements Comparator
{
	public int compare(Object o1, Object o2)
	{
		KSPCandidate c1 = (KSPCandidate) o1;
		KSPCandidate c2 = (KSPCandidate) o2;
		
		NNEntry n1 = c1.placeEntry;
		NNEntry n2 = c2.placeEntry;


		if (n1.m_minDist < n2.m_minDist) return -1;
		if (n1.m_minDist > n2.m_minDist) return 1;
		return 0;		
	}
}