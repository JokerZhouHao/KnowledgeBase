/**
 * 
 */
package kSP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import entity.sp.WordRadiusNeighborhood;
import entity.sp.DatesWIds;
import entity.sp.NidToDateWidIndex;
import entity.sp.NidToDateWidIndex.DateWid;
import entity.sp.RTreeWithGI;
import entity.sp.SortedDateWid;
import kSP.candidate.KSPCandidate;
import kSP.candidate.KSPCandidateVisitor;
import precomputation.rechable.ReachableQueryService;
import precomputation.sp.IndexWordPNService;
import queryindex.VertexQwordsMap;
import spatialindex.rtree.Data;
import spatialindex.rtree.NNEntry;
import spatialindex.rtree.NNEntryComparator;
import spatialindex.rtree.Node;
import spatialindex.rtree.RTree.NNComparator;
import spatialindex.spatialindex.IEntry;
import spatialindex.spatialindex.INearestNeighborComparator;
import spatialindex.spatialindex.IShape;
import spatialindex.spatialindex.IVisitor;
import utility.Global;
import utility.TimeUtility;

/**
 * Implementation of SP
 * @author jmshi
 *
 */
public class kSP {
	protected RTreeWithGI rgi;
	Map<Integer, DatesWIds> nIdDateWidMap = null;
	HashMap<Integer, SortedDateWid> widDatesMap = null;
	HashMap<Integer, WordRadiusNeighborhood> wordPNMap = null;
	ReachableQueryService reachableQuerySer = null;
	
	double kthScore = Double.POSITIVE_INFINITY;

	public kSP(RTreeWithGI rgi, Map<Integer, DatesWIds> nIdDateWidMap, HashMap<Integer, SortedDateWid> widDatesMap,
			HashMap<Integer, WordRadiusNeighborhood> wordPNMap, ReachableQueryService reachableQuerySer) {
		super();
		this.rgi = rgi;
		this.nIdDateWidMap = nIdDateWidMap;
		this.widDatesMap = widDatesMap;
 		this.wordPNMap = wordPNMap;
		this.reachableQuerySer = reachableQuerySer;
		
	}

	public void kSPComputation(int k, int alphaRadius, final IShape qpoint, ArrayList<Integer> qwords, int date,
			final IVisitor result) throws Exception {
		if (qpoint.getDimension() != rgi.getM_dimensoin())
			throw new IllegalArgumentException(
					"kSemanticLocationQuery: Shape has the wrong number of dimensions.");
		NNComparator nnc = rgi.new NNComparator();
		if(Global.isDebug) {
			System.out.println("> 开始进入遍历RTree . . . ");
			Global.frontTime = System.currentTimeMillis();
		}
		kSPComputation(k, alphaRadius, qpoint, qwords, date, result, nnc);

	}

	private void kSPComputation(int k, int alphaRadius, final IShape qpoint, ArrayList<Integer> qwords, int date,
			final IVisitor result, final INearestNeighborComparator nnc) throws Exception {
		if (qpoint.getDimension() != rgi.getM_dimensoin())
			throw new IllegalArgumentException(
					"kSemanticLocationQuery: Shape has the wrong number of dimensions.");
		
		double minDist = 0;
		int nid;
		Boolean sign = Boolean.FALSE;
		HashMap<Integer, HashMap<Integer, Integer>> recMinDateSpanMap = new HashMap<>();
		HashMap<Integer, Integer> minDateSpanMap = null;
		
		rgi.readLock();

		try {
			/* I need a priority queue here. It turns out that TreeSet sorts unique keys only and since I am
		 	   sorting according to distances, it is not assured that all distances will be unique. TreeMap
			   also sorts unique keys. Thus, I am simulating a priority queue using an ArrayList and binarySearch. */
			ArrayList queue = new ArrayList();

			Node n = null;
			Data nd = new Data(0.0, null, rgi.getRoot(), -1);

			if (rgi.getTreeHeight() < 0) {
				throw new Exception("rtree height " + rgi.getTreeHeight() + " invalid");
			}
			queue.add(new NNEntry(nd, 0.0, rgi.getTreeHeight()));
			
			while (queue.size() != 0) {
				if(Global.isTest) {
					Global.tempTime = System.currentTimeMillis();
				}
				NNEntry first = (NNEntry) queue.remove(0);
				if(Global.isTest) {
					Global.timePTree[4] += System.currentTimeMillis() - Global.tempTime;
				}
				minDist = first.m_minDist;
				if (kthScore < first.m_minDist) {
					break;
				}
				if (first.level >= 0) {// node
					Data firstData = (Data) first.m_pEntry;
					n = rgi.readNode(firstData.getIdentifier());
					Global.count[0]++;
					for (int cChild = 0; cChild < n.m_children; cChild++) {
						double minSpatialDist = qpoint.getMinimumDistance(n.m_pMBR[cChild]) + 1;
						double alphaLoosenessBound = 0;
						nid = n.getChildIdentifier(cChild);
						if(Global.isTest) {
							Global.tempTime = System.currentTimeMillis();
						}
						if (n.m_level == 0) {
							//children of n are places
							Global.recCount[1]++;
							if (this.placeReachablePrune(nid, qwords)) {
								if(Global.isTest) {
									Global.timePTree[0] += System.currentTimeMillis() - Global.tempTime;
									Global.tempTime = System.currentTimeMillis();
								}
								if(Global.isDebug) {
									System.out.println("> 不可达，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()) + "\n");
									Global.frontTime = System.currentTimeMillis();
								}
								if(Global.isTest && (System.currentTimeMillis() - Global.bspStartTime) > Global.limitTime1) {
									sign = Boolean.TRUE;
									break;
								}
								Global.count[5]++;// pruned
								continue;
							}
							if(Global.isTest) {
								Global.timePTree[0] += System.currentTimeMillis() - Global.tempTime;
								Global.tempTime = System.currentTimeMillis();
							}
							minDateSpanMap = this.getWidMinDateSpan(Boolean.TRUE, nid, qwords, date);
							recMinDateSpanMap.put(nid, minDateSpanMap);
							alphaLoosenessBound = this.getAlphaLoosenessBound(nid, alphaRadius, minDateSpanMap, qwords, date);
//							alphaLoosenessBound = this.getAlphaLoosenessBound(nid,
//													alphaRadius, qpoint, qwords, date);
							if(Global.isTest) {
								Global.timePTree[1] += System.currentTimeMillis() - Global.tempTime;
								Global.tempTime = System.currentTimeMillis();
							}
						} else {
							//ATTENTION: children of n are nodes that have -id-1 as identifier in alpha index
							alphaLoosenessBound = this.getAlphaLoosenessBound((-nid - 1),
									alphaRadius, qpoint, qwords, date);
						}
						double alphaRankingScoreBound = minSpatialDist * alphaLoosenessBound;
						if (alphaRankingScoreBound > kthScore) {
							continue;
						}
						IEntry eChild = new Data(minSpatialDist, n.m_pMBR[cChild],
								n.m_pIdentifier[cChild], n.m_identifier);
						NNEntry eChild2 = new NNEntry(eChild, alphaRankingScoreBound, n.m_level - 1);
						if(Global.isTest) {
							Global.tempTime = System.currentTimeMillis();
						}
						insertIntoHeapH(queue, eChild2);
						if(Global.isTest) {
							Global.timePTree[2] += System.currentTimeMillis() - Global.tempTime;
							Global.tempTime = System.currentTimeMillis();
						}
					}
					if(sign)	break;
				} else {
					if(Global.isTest) {
						Global.tempTime = System.currentTimeMillis();
					}
					if(Global.isDebug && Global.isFirstRTree) {
						System.out.println("> 遍历完RTree所有非叶子节点，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()));
						Global.frontTime = System.currentTimeMillis();
						Global.isFirstRTree = false;
					}
					
					if(Global.isDebug) {
						Global.frontTime = System.currentTimeMillis();
					}
					
					Data placeData = (Data) first.m_pEntry;
					nid = placeData.getIdentifier();
					
					// unqualified place pruning
					if(Global.isDebug) {
						System.out.println("> 可达，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()));
						Global.frontTime = System.currentTimeMillis();
					}
					if(Global.isDebug) {
						System.out.println("> 开始计算Tree . . . ");
						Global.frontTime = System.currentTimeMillis();
					}
					
					double loosenessThreshold = Double.POSITIVE_INFINITY;
					if (kthScore != Double.POSITIVE_INFINITY) {
						loosenessThreshold = kthScore / placeData.getWeight();
					}
					
					
//					if(Global.isTest) {
//						Global.tempTime = System.currentTimeMillis();
//					}
//					minDateSpanMap = this.getWidMinDateSpan(Boolean.TRUE, nid, qwords, date);
//					if(Global.isTest) {
//						Global.timePTree[1] += System.currentTimeMillis() - Global.tempTime;
//						Global.tempTime = System.currentTimeMillis();
//					}
//					if(kthScore <= this.getAlphaLoosenessBound(nid, alphaRadius, minDateSpanMap, qwords, date)) {
//						continue;
//					}
					
					// compute shortest path between place and qword
					Global.count[3]++;
					List<List<Integer>> semanticTree = new ArrayList<List<Integer>>();
					long start = System.currentTimeMillis();
					double looseness = this.rgi.getGraph().getSemanticPlaceP(nid,
							qwords, date, loosenessThreshold, nIdDateWidMap, recMinDateSpanMap.get(nid), semanticTree);
//					double looseness = this.rgi.getGraph().getSemanticPlaceP(nid,
//							qwords, date, loosenessThreshold, nIdDateWidMap, minDateSpanMap, semanticTree);
					
					if(Global.isTest) {
						if(System.currentTimeMillis() - Global.bspStartTime > Global.limitTime) {
							break;
						}
					}
					
					long end = System.currentTimeMillis();
					Global.runtime[1] += end - start;

					Global.count[1]++;
					if (looseness < 1) {
						throw new Exception("semantic score " + looseness + " < 1, for place"
								+ placeData.getIdentifier());
					}
					// place is a valid candidate that connects to all qwords
					if (looseness != Double.POSITIVE_INFINITY) {
						Global.count[2]++;// number of valid place candidate
						double rankingScore = placeData.getWeight() * looseness;
						KSPCandidate candidate = new KSPCandidate(new NNEntry(placeData, rankingScore),
								semanticTree);

						if(((KSPCandidateVisitor) result).addPlaceCandidate(candidate)) {
						}
						kthScore = ((KSPCandidateVisitor) result).size() >= k ? ((KSPCandidateVisitor) result)
								.getWorstRankingScore() : Double.POSITIVE_INFINITY;
						
						if(Global.isDebug) {
							System.out.println("> 成功一个可用Tree，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()) + "\n");
							Global.frontTime = System.currentTimeMillis();
						}
					} else {
						if(Global.isDebug) {
							System.out.println("> 失败一个可用Tree，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()) + "\n");
							Global.frontTime = System.currentTimeMillis();
						}
					}
					if(Global.isTest) {
						Global.timePTree[3] += System.currentTimeMillis() - Global.tempTime;
						Global.tempTime = System.currentTimeMillis();
					}
				}
			}
//			System.out.println(queue.size());
			Global.queueSize = queue.size();
		} finally {
			rgi.readUnlock();
		}
		recMinDateSpanMap.clear();
		if(Global.isTest) {
			for(int i=0; i<5; i++) {
				System.out.print(String.valueOf(Global.timePTree[i]) + " ");
//				Global.timePTree[i] = 0;
			}
			System.out.println();
			Global.bspRes[0] = String.valueOf(minDist);
			Global.bspRes[1] = String.valueOf(kthScore);
		}
	}

	/**
	 * @param queue
	 * @param e2
	 * @return
	 */
	public int insertIntoHeapH(ArrayList queue, NNEntry e2) {
		int loc = Collections.binarySearch(queue, e2, new NNEntryComparator());
		if (loc >= 0)
			queue.add(loc, e2);
		else
			queue.add((-loc - 1), e2);
		return loc;
	}

	/**
	 * Unqualified place pruning
	 * @param place
	 * @param qwords
	 * @return
	 */
	private boolean placeReachablePrune(int place, ArrayList<Integer> qwords) {
		boolean isPruned = false;
		/*
		 * For unqualified place pruning, based on the observation that infrequent query keywords have a high chance to make a place unqualified, 
		 * we prioritize them when issuing reachability queries.
		 * Furthermore, the least Frequent query keyword is powerful enough for pruning.
		 * */
		Global.count[4]++;
		for(Integer in : qwords) {
			if(!reachableQuerySer.queryReachable(place, in)){
				isPruned = true;
				break;
			}
		}
		return isPruned;
	}

	/**
	 * 
	 * @param id
	 * @param qwords
	 * @param date
	 * @return
	 * @throws IOException
	 */
	public HashMap<Integer, Integer> getWidMinDateSpan(Boolean testReachable, int id, ArrayList<Integer> qwords, int date) throws IOException {
		HashMap<Integer, Integer> widMinDateSpan = new HashMap<>();
		HashMap<Integer, Boolean> rec = new HashMap<>();
		for(int wid : qwords) {
			if(testReachable)	widMinDateSpan.put(wid, widDatesMap.get(wid).getMinDateSpan(rec, date, id, reachableQuerySer));
			else 	widMinDateSpan.put(wid, widDatesMap.get(wid).getMinDateSpan(date));
		}
		rec.clear();
		return widMinDateSpan;
	}
	
	/**
	 * 
	 * @param id
	 * @param alphaRadius
	 * @param qpoint
	 * @param qwords
	 * @return
	 * @throws IOException
	 */
	public double getAlphaLoosenessBound(int id, int alphaRadius, final IShape qpoint, ArrayList<Integer> qwords, int date) throws IOException {
		double alphaLoosenessBound = 0;
		double tempd1 = 0;
		double tempd2 = 0;
		for(int wid : qwords) {
			if(widDatesMap.get(wid) == null)	return Double.POSITIVE_INFINITY;
			tempd1 = (alphaRadius + 2) * widDatesMap.get(wid).getMinDateSpan(date);
			if(null == wordPNMap.get(wid)) {
				alphaLoosenessBound += tempd1;
			} else {
				tempd2 = wordPNMap.get(wid).getLooseness(id, date);
				alphaLoosenessBound += (tempd1 >= tempd2 ? tempd2 : tempd1);
			}
		}
		return alphaLoosenessBound;
	}
	
	/**
	 * 
	 * @param id
	 * @param alphaRadius
	 * @param widMinDateSpanMap
	 * @param qwords
	 * @param date
	 * @return
	 * @throws IOException
	 */
	public double getAlphaLoosenessBound(int id, int alphaRadius, HashMap<Integer, Integer> widMinDateSpanMap, ArrayList<Integer> qwords, int date) throws IOException {
		double alphaLoosenessBound = 0;
		double tempd1 = 0;
		double tempd2 = 0;
		for(int wid : qwords) {
			tempd1 = (alphaRadius + 2) * widMinDateSpanMap.get(wid);
			if(null == wordPNMap.get(wid)) {
				alphaLoosenessBound += tempd1;
			} else {
				tempd2 = wordPNMap.get(wid).getLooseness(id, date);
				alphaLoosenessBound += (tempd1 >= tempd2 ? tempd2 : tempd1);
			}
		}
		return alphaLoosenessBound;
	}
}
