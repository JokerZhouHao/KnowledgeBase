/**
 * 
 */
package kSP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import entity.sp.WordRadiusNeighborhood;
import entity.sp.reach.CReach;
import entity.sp.DatesWIds;
import entity.sp.NNEntryMapHeap;
import entity.sp.date.MinMaxDateService;
import entity.sp.RTreeWithGI;
import entity.sp.RunRecord;
import entity.sp.SortedDateWidIndex;
import kSP.candidate.KSPCandidate;
import kSP.candidate.KSPCandidateVisitor;
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
 * 使用索引来操作
 * @author Monica
 * @since 2018/6/9
 */
public class KSPIndex {
	protected RTreeWithGI rgi;
	private Set<Integer>[] rtreeNode2Pid = null;
	private CReach cReach = null;
	private Map<Integer, DatesWIds> nIdDateWidMap = null;
	private SortedDateWidIndex[] wid2DateNidPair = null;
	private Set<Integer>[] w2pReachable = null;
	private HashMap<Integer, WordRadiusNeighborhood> wordPNMap = null;
	private int[] pid2RtreeLeafNode = null;
	private MinMaxDateService minMaxDateSer = null;
	private HashMap<Integer, Map<Integer, Integer>> recMinDateSpanMap = new HashMap<>();
	
	public KSPIndex(RTreeWithGI rgi, Set<Integer>[] rtreeNode2Pid, int[] pid2RtreeLeafNode, CReach cReach,
			Map<Integer, DatesWIds> nIdDateWidMap, SortedDateWidIndex[] wid2DateNidPair, MinMaxDateService minMaxDateSer,
			Set<Integer>[] w2pReachable, HashMap<Integer, WordRadiusNeighborhood> wordPNMap) {
		super();
		this.rgi = rgi;
		this.rtreeNode2Pid = rtreeNode2Pid;
		this.pid2RtreeLeafNode = pid2RtreeLeafNode;
		this.cReach = cReach;
		this.nIdDateWidMap = nIdDateWidMap;
		this.wid2DateNidPair = wid2DateNidPair;
		this.minMaxDateSer = minMaxDateSer;
		this.w2pReachable = w2pReachable;
 		this.wordPNMap = wordPNMap;
	}

	public void kSPComputation(int k, int alphaRadius, final IShape qpoint, int[] sortQwords, int date,
			final IVisitor result) throws Exception {
		if (qpoint.getDimension() != rgi.getM_dimensoin())
			throw new IllegalArgumentException(
					"kSemanticLocationQuery: Shape has the wrong number of dimensions.");
		NNComparator nnc = rgi.new NNComparator();
		if(Global.isDebug) {
			System.out.println("> 开始进入遍历RTree . . . ");
			Global.frontTime = System.currentTimeMillis();
		}
		kSPComputation(k, alphaRadius, qpoint, sortQwords, date, result, nnc);

	}
	
	private void kSPComputation(int k, int alphaRadius, final IShape qpoint, int[] sortQwords, int date,
			final IVisitor result, final INearestNeighborComparator nnc) throws Exception {
		if (qpoint.getDimension() != rgi.getM_dimensoin())
			throw new IllegalArgumentException(
					"kSemanticLocationQuery: Shape has the wrong number of dimensions.");
		
		int nid;
		Boolean sign = Boolean.FALSE;
		HashMap<Integer, Integer> minDateSpanMap = null;
		
		NNEntry first = null;
		
		NNEntryMapHeap heap = new NNEntryMapHeap();
		Node n = null;
		
		double kthScore = Double.POSITIVE_INFINITY;
		
		rgi.readLock();

		try {
			/* I need a priority queue here. It turns out that TreeSet sorts unique keys only and since I am
		 	   sorting according to distances, it is not assured that all distances will be unique. TreeMap
			   also sorts unique keys. Thus, I am simulating a priority queue using an ArrayList and binarySearch. */
//			ArrayList queue = new ArrayList();
			
			Data nd = new Data(0.0, null, rgi.getRoot(), -1);

			if (rgi.getTreeHeight() < 0) {
				throw new Exception("rtree height " + rgi.getTreeHeight() + " invalid");
			}
			
			heap.put(new NNEntry(nd, 0.0, rgi.getTreeHeight()));
			
			while(heap.size() != 0) {
				if(Global.isTest) {
					Global.rr.setFrontTime();
				}
				first = heap.poll();
				if(Global.isTest) {
					Global.rr.timeCptQueueRemove += Global.rr.getTimeSpan();
					Global.rr.numCptQueueRemove++;
				}
				if (kthScore < first.m_minDist) {
					break;
				}
				if (first.level >= 0) {// node
					if(Global.isTest) {
						Global.rr.numCptAccessedRTreeNode++;
					}
					Data firstData = (Data) first.m_pEntry;
					n = rgi.readNode(firstData.getIdentifier());
					for (int cChild = 0; cChild < n.m_children; cChild++) {
						double minSpatialDist = qpoint.getMinimumDistance(n.m_pMBR[cChild]) + 1;
						double alphaLoosenessBound = 0;
						nid = n.getChildIdentifier(cChild);
						if(Global.isTest)	Global.rr.numCptTotalReach2Wids++;
						if (n.m_level == 0) {
							//children of n are places
							if(Global.isTest) {
								Global.rr.setFrontTime();
							}
							if (this.placeReachablePrune(nid, sortQwords)) {
								if(Global.isTest) {
									Global.rr.timeCptPid2Wids += Global.rr.getTimeSpan();
									Global.rr.numCptPrunePid2Wids++;
									Global.rr.setFrontTime();
								}
								if(Global.isDebug) {
									System.out.println("> 不可达，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()) + "\n");
									Global.frontTime = System.currentTimeMillis();
								}
								if(Global.isTest && Global.rr.isCptOverTime()) {
									sign = Boolean.TRUE;
									break;
								}
								continue;	// pruned
							}
							if(Global.isTest) {
								Global.rr.timeCptPid2Wids += Global.rr.getTimeSpan();
								Global.rr.setFrontTime();
							}
							
							minDateSpanMap = this.getPidWidMinDateSpan(nid, sortQwords, date);
							
							if(Global.isTest) {
								Global.rr.timeCptPidGetMinDateSpan += Global.rr.getTimeSpan();
							}
							
							alphaLoosenessBound = this.getAlphaLoosenessBound(nid, alphaRadius, minDateSpanMap, sortQwords, date);
						} else {
							//ATTENTION: children of n are nodes that have -id-1 as identifier in alpha index
							if(Global.isTest) {
								Global.rr.setFrontTime();
							}
							if (this.placeReachablePrune(-nid-1, sortQwords)) {
								if(Global.isTest) {
									Global.rr.timeCptRTree2Wids += Global.rr.getTimeSpan();
									Global.rr.numCptPruneRTree2Wids++;
									Global.rr.setFrontTime();
								}
								continue;
							}
							if(Global.isTest) {
								Global.rr.timeCptRTree2Wids += Global.rr.getTimeSpan();
								Global.rr.setFrontTime();
							}
							
							minDateSpanMap = this.getRTreeWidMinDateSpan(nid, sortQwords, date);
							
							if(Global.isTest) {
								Global.rr.timeCptRTreeGetMinDateSpan += Global.rr.getTimeSpan();
							}
							
							alphaLoosenessBound = this.getAlphaLoosenessBound(-nid-1, alphaRadius, minDateSpanMap, sortQwords, date);
						}
						
						double alphaRankingScoreBound = minSpatialDist * alphaLoosenessBound;
						if (alphaRankingScoreBound > kthScore) {
							if(n.m_level == 0) {
								Global.rr.numCptBoundPidPrune++;
							} else {
								Global.rr.numCptBoundRTreePrune++;
							}
							continue;
						}
						
						if(n.m_level == 0) {
							recMinDateSpanMap.put(nid, minDateSpanMap);
						} else if(n.m_level == 1) {
							recMinDateSpanMap.put(-nid-1, minDateSpanMap);
						}
						
						IEntry eChild = new Data(minSpatialDist, n.m_pMBR[cChild],
								n.m_pIdentifier[cChild], n.m_identifier);
						NNEntry eChild2 = new NNEntry(eChild, alphaRankingScoreBound, n.m_level - 1);
						if(Global.isTest) {
							Global.rr.setFrontTime();
						}
						
						heap.put(eChild2);
						
						if(Global.isTest) {
							Global.rr.numCptQueuePut++;
							if(Global.rr.numCptMaxQueueSize < heap.size())	Global.rr.numCptMaxQueueSize = heap.size();
							Global.rr.timeCptQueuePut += Global.rr.getTimeSpan();
						}
					}
					if(sign)	break;
				} else {
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
					
					// compute shortest path between place and qword
					if(Global.isTest) {
						Global.rr.numGetSemanticTree++;
						Global.rr.setFrontTime();
					}
					List<List<Integer>> semanticTree = new ArrayList<List<Integer>>();
					double looseness = this.rgi.getGraph().getSemanticPlaceP(nid,
							sortQwords, date, loosenessThreshold, nIdDateWidMap, recMinDateSpanMap.get(nid), semanticTree);
					
					if(Global.isTest) {
						Global.rr.timeCptGetSemanticTree += Global.rr.getTimeSpan();
						if(Global.rr.isCptOverTime())	break;
					}

					if (looseness < 1) {
						throw new Exception("semantic score " + looseness + " < 1, for place"
								+ placeData.getIdentifier());
					}
					// place is a valid candidate that connects to all qwords
					if (looseness != Double.POSITIVE_INFINITY) {
						// number of valid place candidate
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
				}
			}
			if(Global.isTest) {
				Global.rr.numLastQueue = heap.size();
				Global.rr.kthScore = kthScore;
				Global.rr.queueLastValue = first.m_minDist;
			}
		} finally {
			rgi.readUnlock();
		}
		
		recMinDateSpanMap.clear();
		if(Global.isTest && Global.isOutputTestInfo) {
			System.out.println("numCptGetMinDateSpanLeftSpan : " + Global.rr.numCptGetMinDateSpanLeftSpan + " numCptGetMinDateSpanRightSpan : " + Global.rr.numCptGetMinDateSpanRightSpan + " timeCptGetMinDateSpan : " + (Global.rr.timeCptPidGetMinDateSpan + Global.rr.timeCptRTreeGetMinDateSpan)/RunRecord.timeBase);
			System.out.println(
					"numCptMaxQueueSize timeCptQueuePut timeCptQueueRemove timeCptTotalReach2Wids timeCptTotalGetMinDateSpan timeCptGetSemanticTree timeKSPComputation\n"+ 
					Global.rr.numCptMaxQueueSize + " " + 
					Global.rr.timeCptQueuePut/Global.rr.timeBase + " " + 
					Global.rr.timeCptQueueRemove/Global.rr.timeBase + " " + 
					(Global.rr.timeCptPid2Wids+Global.rr.timeCptRTree2Wids)/Global.rr.timeBase + " " + 
					(Global.rr.timeCptPidGetMinDateSpan + Global.rr.timeCptRTreeGetMinDateSpan)/Global.rr.timeBase + " " + 
					Global.rr.timeCptGetSemanticTree/Global.rr.timeBase + " " +
					Global.rr.setTimeKSPComputation()/Global.rr.timeBase);
		}
	}
	
	/**
	 * 时间范围查找
	 * @param k
	 * @param alphaRadius
	 * @param qpoint
	 * @param sortQwords
	 * @param sDate
	 * @param eDate
	 * @param result
	 * @throws Exception
	 */
	public void kSPComputation(int k, int alphaRadius, List<Integer> matchNids, final IShape qpoint, int[] sortQwords, int sDate, int eDate,
			final IVisitor result) throws Exception {
		if (qpoint.getDimension() != rgi.getM_dimensoin())
			throw new IllegalArgumentException(
					"kSemanticLocationQuery: Shape has the wrong number of dimensions.");
		NNComparator nnc = rgi.new NNComparator();
		if(Global.isDebug) {
			System.out.println("> 开始进入遍历RTree . . . ");
			Global.frontTime = System.currentTimeMillis();
		}
		kSPComputation(k, alphaRadius, matchNids, qpoint, sortQwords, sDate, eDate, result, nnc);
	}
	
	/**
	 * 时间范围查找
	 * @param k
	 * @param alphaRadius
	 * @param qpoint
	 * @param sortQwords
	 * @param sDate
	 * @param eDate
	 * @param result
	 * @param nnc
	 * @throws Exception
	 */
	private void kSPComputation(int k, int alphaRadius, List<Integer> matchNids, final IShape qpoint, int[] sortQwords, int sDate, int eDate,
			final IVisitor result, final INearestNeighborComparator nnc) throws Exception {
		if (qpoint.getDimension() != rgi.getM_dimensoin())
			throw new IllegalArgumentException(
					"kSemanticLocationQuery: Shape has the wrong number of dimensions.");
		int nid;
		Boolean sign = Boolean.FALSE;
		
		NNEntry first = null;
		
		NNEntryMapHeap heap = new NNEntryMapHeap();
		Node n = null;
		
		double kthScore = Double.POSITIVE_INFINITY;
		
		rgi.readLock();

		try {
			/* I need a priority queue here. It turns out that TreeSet sorts unique keys only and since I am
		 	   sorting according to distances, it is not assured that all distances will be unique. TreeMap
			   also sorts unique keys. Thus, I am simulating a priority queue using an ArrayList and binarySearch. */
//			ArrayList queue = new ArrayList();
			
			Data nd = new Data(0.0, null, rgi.getRoot(), -1);

			if (rgi.getTreeHeight() < 0) {
				throw new Exception("rtree height " + rgi.getTreeHeight() + " invalid");
			}
			
			heap.put(new NNEntry(nd, 0.0, rgi.getTreeHeight()));
			
			while(heap.size() != 0) {
				if(Global.isTest) {
					Global.rr.setFrontTime();
				}
				first = heap.poll();
				if(Global.isTest) {
					Global.rr.timeCptQueueRemove += Global.rr.getTimeSpan();
					Global.rr.numCptQueueRemove++;
				}
				if (kthScore < first.m_minDist) {
					break;
				}
				if (first.level >= 0) {// node
					if(Global.isTest) {
						Global.rr.numCptAccessedRTreeNode++;
					}
					Data firstData = (Data) first.m_pEntry;
					n = rgi.readNode(firstData.getIdentifier());
					for (int cChild = 0; cChild < n.m_children; cChild++) {
						double minSpatialDist = qpoint.getMinimumDistance(n.m_pMBR[cChild]) + 1;
						double alphaLoosenessBound = 0;
						nid = n.getChildIdentifier(cChild);
						if(Global.isTest)	Global.rr.numCptTotalReach2Wids++;
						if (n.m_level == 0) {
							//children of n are places
							if(Global.isTest) {
								Global.rr.setFrontTime();
							}
							if (this.placeReachablePrune(nid, sortQwords)) {
								if(Global.isTest) {
									Global.rr.timeCptPid2Wids += Global.rr.getTimeSpan();
									Global.rr.setFrontTime();
									Global.rr.numCptPrunePid2Wids++;
								}
								if(Global.isDebug) {
									System.out.println("> 不可达，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()) + "\n");
									Global.frontTime = System.currentTimeMillis();
								}
								if(Global.isTest && Global.rr.isCptOverTime()) {
									sign = Boolean.TRUE;
									break;
								}
								continue;	// pruned
							}
							if(Global.isTest) {
								Global.rr.timeCptPid2Wids += Global.rr.getTimeSpan();
								Global.rr.setFrontTime();
							}
							
							alphaLoosenessBound = this.getAlphaLoosenessBound(nid, alphaRadius, sortQwords, sDate, eDate);
						} else {
							//ATTENTION: children of n are nodes that have -id-1 as identifier in alpha index
							if(Global.isTest) {
								Global.rr.setFrontTime();
							}
							if (this.placeReachablePrune(-nid-1, sortQwords)) {
								if(Global.isTest) {
									Global.rr.timeCptRTree2Wids += Global.rr.getTimeSpan();
									Global.rr.numCptPruneRTree2Wids++;
									Global.rr.setFrontTime();
								}
								continue;
							}
							if(Global.isTest) {
								Global.rr.timeCptPid2Wids += Global.rr.getTimeSpan();
								Global.rr.setFrontTime();
							}
							
							// 判断RTree节点是否能到达所有matchNids
							if(rTreeNodeReachable(matchNids, rtreeNode2Pid[nid], sortQwords)) {
								if(Global.isTest) {
									Global.rr.numCptRangeRNodePrune++;
									Global.rr.timeCptRangeRNode += Global.rr.getTimeSpan();
									Global.rr.setFrontTime();
								}
								continue;
							}
							if(Global.isTest) {
								Global.rr.timeCptRangeRNode += Global.rr.getTimeSpan();
								Global.rr.setFrontTime();
							}
							
							alphaLoosenessBound = this.getAlphaLoosenessBound(-nid-1, alphaRadius, sortQwords, sDate, eDate);
						}
						
						double alphaRankingScoreBound = minSpatialDist * alphaLoosenessBound;
						if (alphaRankingScoreBound > kthScore) {
							if(n.m_level == 0) {
								Global.rr.numCptBoundPidPrune++;
							} else {
								Global.rr.numCptBoundRTreePrune++;
							}
							continue;
						}
						
						IEntry eChild = new Data(minSpatialDist, n.m_pMBR[cChild],
								n.m_pIdentifier[cChild], n.m_identifier);
						NNEntry eChild2 = new NNEntry(eChild, alphaRankingScoreBound, n.m_level - 1);
						if(Global.isTest) {
							Global.rr.setFrontTime();
						}
						
						heap.put(eChild2);
						
						if(Global.isTest) {
							Global.rr.numCptQueuePut++;
							if(Global.rr.numCptMaxQueueSize < heap.size())	Global.rr.numCptMaxQueueSize = heap.size();
							Global.rr.timeCptQueuePut += Global.rr.getTimeSpan();
						}
					}
					if(sign)	break;
				} else {
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
					
					// compute shortest path between place and qword
					if(Global.isTest) {
						Global.rr.numGetSemanticTree++;
						Global.rr.setFrontTime();
					}
					List<List<Integer>> semanticTree = new ArrayList<List<Integer>>();
					double looseness = this.rgi.getGraph().getSemanticPlaceP(nid,
							sortQwords, sDate, eDate, loosenessThreshold, nIdDateWidMap, semanticTree);
					
					if(Global.isTest) {
						Global.rr.timeCptGetSemanticTree += Global.rr.getTimeSpan();
						if(Global.rr.isCptOverTime())	break;
					}

					if (looseness < 1) {
						throw new Exception("semantic score " + looseness + " < 1, for place"
								+ placeData.getIdentifier());
					}
					// place is a valid candidate that connects to all qwords
					if (looseness != Double.POSITIVE_INFINITY) {
						// number of valid place candidate
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
				}
			}
			if(Global.isTest) {
				Global.rr.numLastQueue = heap.size();
				Global.rr.kthScore = kthScore;
				Global.rr.queueLastValue = first.m_minDist;
			}
		} finally {
			rgi.readUnlock();
		}
		
		if(Global.isTest && Global.isOutputTestInfo) {
			System.out.println(Global.rr.numCptRangeRNodePrune);
			System.out.println("numCptGetMinDateSpanLeftSpan : " + Global.rr.numCptGetMinDateSpanLeftSpan + " numCptGetMinDateSpanRightSpan : " + Global.rr.numCptGetMinDateSpanRightSpan + " timeCptGetMinDateSpan : " + (Global.rr.timeCptPidGetMinDateSpan + Global.rr.timeCptRTreeGetMinDateSpan)/1000);
			System.out.println(
					"numCptMaxQueueSize timeCptQueuePut timeCptQueueRemove timeCptPid2Wids timeCptGetMinDateSpan timeCptGetSemanticTree timeKSPComputation\n"+ 
					Global.rr.numCptMaxQueueSize + " " + 
					Global.rr.timeCptQueuePut/Global.rr.timeBase + " " + 
					Global.rr.timeCptQueueRemove/Global.rr.timeBase + " " + 
					(Global.rr.timeCptPid2Wids+Global.rr.timeCptRTree2Wids)/Global.rr.timeBase + " " + 
					(Global.rr.timeCptPidGetMinDateSpan + Global.rr.timeCptRTreeGetMinDateSpan)/Global.rr.timeBase + " " + 
					Global.rr.timeCptGetSemanticTree/Global.rr.timeBase + " " +
					Global.rr.setTimeKSPComputation()/Global.rr.timeBase);
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
	private Boolean placeReachablePrune(int place, int[] sortQwords) {
		/*
		 * For unqualified place pruning, based on the observation that infrequent query keywords have a high chance to make a place unqualified, 
		 * we prioritize them when issuing reachability queries.
		 * Furthermore, the least Frequent query keyword is powerful enough for pruning.
		 * */
		for(int i=0; i<sortQwords.length; i++) {
			if(!w2pReachable[i].contains(place))	return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * 
	 * @param id
	 * @param qwords
	 * @param date
	 * @return
	 * @throws IOException
	 */
	public HashMap<Integer, Integer> getPidWidMinDateSpan(int id, int[] sortQwords, int date) throws IOException {
		HashMap<Integer, Integer> widMinDateSpan = new HashMap<>();
		widMinDateSpan.putAll(recMinDateSpanMap.get(pid2RtreeLeafNode[id]));
		int i=0;
		if(Global.isTest) {
			Global.rr.numCptPidGetMinDateSpan += sortQwords.length;
		}
		HashSet<Integer> rec = new HashSet<>();
		int j1, j2, k;
		for(i=0; i<sortQwords.length; i++) {
//			widMinDateSpan.put(sortQwords[i], wid2DateNidPair[i].getMinDateSpan(rec, date, id, cReach));
			k = widMinDateSpan.get(sortQwords[i]);
			if(k>=Global.maxDateSpan)	continue;
			j1= wid2DateNidPair[i].getMinDateSpan(rec, date, id, cReach, k-1);
//			j2 = wid2DateNidPair[i].getMinDateSpan(date);
			widMinDateSpan.put(sortQwords[i], j1);
//			if(j1 < j2) {
//				System.out.println("pid dis less !!! --> " + "k=" + k + " j1=" + j1 + " j2=" + j2);
//				System.exit(0);
//			}
		}
		rec.clear();
		return widMinDateSpan;
	}
	
	public HashMap<Integer, Integer> getRTreeWidMinDateSpan(int id, int[] sortQwords, int date) throws IOException {
		HashMap<Integer, Integer> widMinDateSpan = new HashMap<>();
		int i=0;
		if(Global.isTest) {
			Global.rr.numCptRTreeGetMinDateSpan += sortQwords.length;
		}
		int j1, j2;
		for(i=0; i<sortQwords.length; i++) {
			j1= wid2DateNidPair[i].getMinDateSpan(rtreeNode2Pid[id], date);
//			j2 = wid2DateNidPair[i].getMinDateSpan(date);
//			widMinDateSpan.put(sortQwords[i], wid2DateNidPair[i].getMinDateSpan(rec, date, id));
//			widMinDateSpan.put(sortQwords[i], wid2DateNidPair[i].getMinDateSpan(date));
			widMinDateSpan.put(sortQwords[i], j1);
//			widMinDateSpan.put(sortQwords[i], -1);
//			if(j1 < j2) {
//				System.out.println("rtree dis less !!! --> " + "j1=" + j1 + " j2=" + j2);
//				System.exit(0);
//			}
		}
		return widMinDateSpan;
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
	public double getAlphaLoosenessBound(int id, int alphaRadius, Map<Integer, Integer> widMinDateSpanMap, int[] sortQwords, int date) throws IOException {
		double alphaLoosenessBound = 0;
		double tempd1 = 0;
		double tempd2 = 0;
		for(int wid : sortQwords) {
			if(null == wordPNMap.get(wid)) {
				alphaLoosenessBound += widMinDateSpanMap.get(wid);
			} else {
				tempd1 = (alphaRadius + 2) * widMinDateSpanMap.get(wid);
				tempd2 = wordPNMap.get(wid).getLooseness(id, date);
				alphaLoosenessBound += (tempd1 >= tempd2 ? tempd2 : tempd1);
			}
		}
		return alphaLoosenessBound;
	}
	
	/**
	 * 范围查找获得阈值
	 * @param id
	 * @param alphaRadius
	 * @param widMinDateSpanMap
	 * @param sortQwords
	 * @param sDate
	 * @param eDate
	 * @return
	 * @throws IOException
	 */
	public double getAlphaLoosenessBound(int id, int alphaRadius, int[] sortQwords, int sDate, int eDate) throws IOException {
		double alphaLoosenessBound = 0;
		for(int wid : sortQwords) {
			if(null == wordPNMap.get(wid)) {
				alphaLoosenessBound += 1;
			} else {
				alphaLoosenessBound += wordPNMap.get(wid).getLooseness(id, sDate, eDate);
			}
		}
		return alphaLoosenessBound;
	}
	
	/**
	 * 判断rtree节点能否到达既在时间范围内，又包含全部查询词的点集
	 * @param matchNids
	 * @param rNids
	 * @param sortQwords
	 * @return
	 */
	public Boolean rTreeNodeReachable(List<Integer> matchNids, Set<Integer> rNids, int[] sortQwords) {
		int i, j;
		Boolean rSign = Boolean.TRUE;
		boolean hasAccess[] = new boolean[sortQwords.length];
		List<Integer> wids = null;
		int numHasAccess = 0;
		for(int ni : matchNids) {
			if(rNids.contains(ni)) {
				wids = nIdDateWidMap.get(ni).getwIdList();
				j=0;
				for(i=0; i<sortQwords.length; i++) {
					if(hasAccess[i])	continue;
					for(; j<wids.size(); j++) {
						if(sortQwords[i]>wids.get(j)) {
							continue;
						} else if (sortQwords[i]<wids.get(j)) {
							break;
						} else {
							hasAccess[i] = Boolean.TRUE;
							if((++numHasAccess)==sortQwords.length) {
								rSign = Boolean.FALSE;
								break;
							}
						}
					}
					if(j==wids.size() || !rSign)	break;
				}
//				rSign = Boolean.FALSE;
//				break;
			}
			if(!rSign)	break;
		}
		
		return rSign;
	}
}
