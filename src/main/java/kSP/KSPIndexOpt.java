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
import entity.OptMethod;
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
public class KSPIndexOpt {
	protected RTreeWithGI rgi;
	private Set<Integer>[] rtreeNode2Pid = null;
	private CReach cReach = null;
	private DatesWIds searchedDatesWids[] = null;
	private SortedDateWidIndex[] wid2DateNidPair = null;
	private Map<Integer, Short>[] w2pReachable = null;
	private HashMap<Integer, WordRadiusNeighborhood> wordPNMap = null;
	private int[] pid2RtreeLeafNode = null;
	private MinMaxDateService minMaxDateSer = null;
	private HashMap<Integer, int[][]> recMinDateSpanMap = new HashMap<>();
	private HashMap<Integer, int[]> recMinPid2WidDis = new HashMap<>();
	
	// 记录pid到wid的最小路径距离
	private int[] pid2WidPathDis = new int[2];
	
	public KSPIndexOpt(RTreeWithGI rgi, Set<Integer>[] rtreeNode2Pid, int[] pid2RtreeLeafNode, CReach cReach,
			DatesWIds searchedDatesWids[], SortedDateWidIndex[] wid2DateNidPair, MinMaxDateService minMaxDateSer,
			Map<Integer, Short>[] w2pReachable, HashMap<Integer, WordRadiusNeighborhood> wordPNMap) {
		super();
		this.rgi = rgi;
		this.rtreeNode2Pid = rtreeNode2Pid;
		this.pid2RtreeLeafNode = pid2RtreeLeafNode;
		this.cReach = cReach;
		this.searchedDatesWids = searchedDatesWids;
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
		
		if(pid2WidPathDis.length < sortQwords.length + 1) {
			pid2WidPathDis = new int[sortQwords.length + 1];	// pid2WidPathDis[sortQwords.length]用于决定是否裁剪掉对应pid
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
		
		NNEntry first = null;
		
		NNEntryMapHeap heap = new NNEntryMapHeap();
		Node n = null;
		
		double kthScore = Double.POSITIVE_INFINITY;
		
		int[][] minDateSpans = null;
		
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
							
							if(Global.optMethod == OptMethod.O2) {
								this.placeReachablePrune(nid, sortQwords);
							} else 
								this.placeReachablePruneOld(nid, sortQwords);
							
							if (1==this.pid2WidPathDis[sortQwords.length]) {
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
								Global.rr.numCptPidGetMinDateSpan += 1;
								Global.rr.setFrontTime();
							}
							
							if(Global.optMethod == OptMethod.O1)
								minDateSpans = this.getPidWidMinDateSpan(nid, sortQwords, date);
							else
								minDateSpans = this.getPidWidMinDateSpan(sortQwords, date);
							
							if(Global.isTest) {
								Global.rr.timeCptPidGetMinDateSpan += Global.rr.getTimeSpan();
							}
							
							alphaLoosenessBound = this.getAlphaLoosenessBound(nid, alphaRadius, minDateSpans, sortQwords, date);
						} else {
							//ATTENTION: children of n are nodes that have -id-1 as identifier in alpha index
							if(Global.isTest) {
								Global.rr.setFrontTime();
							}
							
							if(Global.optMethod == OptMethod.O4) {
								this.placeReachablePrune(-nid-1, sortQwords);
								if (1==this.pid2WidPathDis[sortQwords.length]) {
									if(Global.isTest) {
										Global.rr.timeCptRTree2Wids += Global.rr.getTimeSpan();
										Global.rr.numCptPruneRTree2Wids++;
										Global.rr.setFrontTime();
									}
									continue;
								}
							} else {
								this.pid2WidPathDis[sortQwords.length] = 0;
								for(int i=0; i<sortQwords.length; i++) {
									this.pid2WidPathDis[i] = -1;
								}
							}
							
							if(Global.isTest) {
								Global.rr.timeCptRTree2Wids += Global.rr.getTimeSpan();
								Global.rr.setFrontTime();
							}
							
							if(Global.optMethod == OptMethod.O1) {
								if(null==rtreeNode2Pid[nid]) {
									if(Global.isTest) {
										Global.rr.timeCptRTree2Wids += Global.rr.getTimeSpan();
										Global.rr.numCptPruneRTree2Wids++;
										Global.rr.setFrontTime();
									}
									continue;
								}
							}
							
							if(Global.optMethod == OptMethod.O1)
								minDateSpans = this.getRTreeWidMinDateSpan(nid, sortQwords, date);
							else
								minDateSpans = this.getPidWidMinDateSpan(sortQwords, date);
							
							if(null == minDateSpans) {
								if(Global.isTest) {
									Global.rr.timeCptRTree2Wids += Global.rr.getTimeSpan();
									Global.rr.numCptPruneRTree2Wids++;
									Global.rr.setFrontTime();
								}
								continue;
							}
							
							
							if(Global.isTest) {
								Global.rr.timeCptRTreeGetMinDateSpan += Global.rr.getTimeSpan();
							}
							
							alphaLoosenessBound = this.getAlphaLoosenessBound(-nid-1, alphaRadius, minDateSpans, sortQwords, date);
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
						
						if(n.m_level==0) {	// pid
							recMinDateSpanMap.put(nid, minDateSpans);
							int[] dis = new int[this.pid2WidPathDis.length];
							for(int s=0; s<this.pid2WidPathDis.length; s++) {
								dis[s] = this.pid2WidPathDis[s];
							}
							recMinPid2WidDis.put(nid, dis);
						} else { // rtree node
							recMinDateSpanMap.put(-nid-1, minDateSpans);
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
					
					//////////////////////////////////////////////////////////////////
//					if(kthScore != Double.POSITIVE_INFINITY) {
//						System.out.println("kthScore = " + String.valueOf(kthScore) + "      " + 
//											"placeData.getWeight = " + String.valueOf(placeData.getWeight()) + "      " + 
//											"loosenessThreshold = " + String.valueOf(loosenessThreshold));
//					}
//					
					
					
					// compute shortest path between place and qword
					if(Global.isTest) {
						Global.rr.numGetSemanticTree++;
						Global.rr.setFrontTime();
					}
					List<List<Integer>> semanticTree = new ArrayList<List<Integer>>();
					double looseness = this.rgi.getGraph().getSemanticPlaceP(nid,
							sortQwords, date, loosenessThreshold, searchedDatesWids, recMinDateSpanMap.get(nid), recMinPid2WidDis.get(nid), semanticTree);
					
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
		
		if(pid2WidPathDis.length < sortQwords.length + 1) {
			pid2WidPathDis = new int[sortQwords.length + 1];	// pid2WidPathDis[sortQwords.length]用于决定是否裁剪掉对应pid
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
							
							if(Global.optMethod == OptMethod.O2) 
								this.placeReachablePrune(nid, sortQwords);
							else 
								this.placeReachablePruneOld(nid, sortQwords);
							
							if (1==this.pid2WidPathDis[sortQwords.length]) {
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
							
							if(Global.optMethod == OptMethod.O4) {
								this.placeReachablePrune(-nid-1, sortQwords);
								if (1==this.pid2WidPathDis[sortQwords.length]) {
									if(Global.isTest) {
										Global.rr.timeCptRTree2Wids += Global.rr.getTimeSpan();
										Global.rr.numCptPruneRTree2Wids++;
										Global.rr.setFrontTime();
									}
									continue;
								}
							} else {
								this.pid2WidPathDis[sortQwords.length] = 0;
								for(int i=0; i<sortQwords.length; i++) {
									this.pid2WidPathDis[i] = -1;
								}
							}
							
							if(Global.isTest) {
								Global.rr.timeCptPid2Wids += Global.rr.getTimeSpan();
								Global.rr.setFrontTime();
							}
							
							// 判断RTree节点是否能到达所有matchNids
							if(Global.optMethod == OptMethod.O4) {
								if(null==rtreeNode2Pid[nid]) {
									if(Global.isTest) {
										Global.rr.timeCptRTree2Wids += Global.rr.getTimeSpan();
										Global.rr.numCptPruneRTree2Wids++;
										Global.rr.setFrontTime();
									}
									continue;
								}
							}
							
							if(Global.optMethod == OptMethod.O4) {
								if(rTreeNodeReachable(matchNids, rtreeNode2Pid[nid], sortQwords)) {
									if(Global.isTest) {
										Global.rr.numCptRangeRNodePrune++;
										Global.rr.timeCptRangeRNode += Global.rr.getTimeSpan();
										Global.rr.setFrontTime();
									}
									continue;
								}
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
							sortQwords, sDate, eDate, loosenessThreshold, searchedDatesWids, semanticTree);
					
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
	private int[] placeReachablePruneOld(int place, int[] sortQwords) {
		/*
		 * For unqualified place pruning, based on the observation that infrequent query keywords have a high chance to make a place unqualified, 
		 * we prioritize them when issuing reachability queries.
		 * Furthermore, the least Frequent query keyword is powerful enough for pruning.
		 * */
		this.pid2WidPathDis[sortQwords.length] = 1;
		for(int i=0; i<sortQwords.length; i++) {
			if(!this.cReach.queryReachable(place, sortQwords[i]))	return this.pid2WidPathDis;
			else this.pid2WidPathDis[i] = -1;
		}
		this.pid2WidPathDis[sortQwords.length] = 0;
		return this.pid2WidPathDis;
	}
	
	/**
	 * Unqualified place pruning
	 * @param place
	 * @param qwords
	 * @return
	 */
	private int[] placeReachablePrune(int place, int[] sortQwords) {
		/*
		 * For unqualified place pruning, based on the observation that infrequent query keywords have a high chance to make a place unqualified, 
		 * we prioritize them when issuing reachability queries.
		 * Furthermore, the least Frequent query keyword is powerful enough for pruning.
		 * */
		this.pid2WidPathDis[sortQwords.length] = 1;
		Short distance = -1;
		for(int i=0; i<sortQwords.length; i++) {
			if(null == w2pReachable[i]) {
				if(place >=0) {
					if(!cReach.queryReachable(place, sortQwords[i])) return this.pid2WidPathDis;
					else this.pid2WidPathDis[i] = -1;
				} else this.pid2WidPathDis[i] = -1;
			} else if(null == (distance=w2pReachable[i].get(place))) {
				return this.pid2WidPathDis;
			} else	this.pid2WidPathDis[i] = distance;
		}
		this.pid2WidPathDis[sortQwords.length] = 0;
		return this.pid2WidPathDis;
	}
	
	/**
	 * 
	 * @param id
	 * @param qwords
	 * @param date
	 * @return
	 * @throws IOException
	 */
	public int[][] getPidWidMinDateSpan(int[] sortQwords, int date) throws IOException {
		if(Global.isTest) {
			Global.rr.numCptPidGetMinDateSpan += sortQwords.length;
		}
		int widMinDateSpans[][] = new int[sortQwords.length][1];
		for(int i=0; i<sortQwords.length; i++) {
			widMinDateSpans[i][0] = wid2DateNidPair[i].getMinDateSpan(date);
		}
		return widMinDateSpans;
	}
	
	/**
	 * 
	 * @param id
	 * @param qwords
	 * @param date
	 * @return
	 * @throws IOException
	 */
	public int[][] getPidWidMinDateSpan(int id, int[] sortQwords, int date) throws Exception {
		int widMinDateSpans[][] = new int[sortQwords.length][3];
		int i=0;
		int rtreeLeafDateSpans[][] =  recMinDateSpanMap.get(pid2RtreeLeafNode[id]);
		
		if(null==rtreeLeafDateSpans) {
			System.out.println(id + "  " + recMinDateSpanMap.size());
			throw new IOException("in getPidWidMinDateSpan null==rtreeLeafDateSpans");
		}
		
		for(i=0; i<sortQwords.length; i++) {
			for(int j=0; j<3; j++) {
				widMinDateSpans[i][j] = rtreeLeafDateSpans[i][j];
			}
		}
		
		HashSet<Integer> rec = new HashSet<>();
		for(i=0; i<sortQwords.length; i++) {
			if(widMinDateSpans[i][0]>=Global.maxDateSpan)	continue;
			wid2DateNidPair[i].getMinDateSpan(rec, date, id, cReach, widMinDateSpans[i][0], widMinDateSpans[i]);
		}
		rec.clear();
		return widMinDateSpans;
	}
	
	public int[][] getRTreeWidMinDateSpan(int id, int[] sortQwords, int date) throws Exception {
		int widMinDateSpans[][] = new int[sortQwords.length][3];
		int i=0;
		int parentNodeDateSpans[][] =  recMinDateSpanMap.get(pid2RtreeLeafNode[-id - 1 + Global.numPid]);
		
		for(i=0; i<sortQwords.length; i++) {
			for(int j=0; j<3; j++) {
				if(null==parentNodeDateSpans)	widMinDateSpans[i][j] = -1;
				else widMinDateSpans[i][j] = parentNodeDateSpans[i][j];
			}
		}
		
		if(Global.isTest) {
			Global.rr.numCptRTreeGetMinDateSpan += sortQwords.length;
		}
		
		for(i=0; i<sortQwords.length; i++) {
			wid2DateNidPair[i].getMinDateSpan(rtreeNode2Pid[id], date, widMinDateSpans[i]);
		}
		return widMinDateSpans;
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
	public double getAlphaLoosenessBound(int id, int alphaRadius, int[][] widMinDateSpans, int[] sortQwords, int date) throws IOException {
		double alphaLoosenessBound = 0;
		double tempd1 = 0;
		double tempd2 = 0;
		for(int i=0; i<sortQwords.length; i++) {
			if(this.pid2WidPathDis[i] != -1) {
				alphaLoosenessBound += this.pid2WidPathDis[i] * widMinDateSpans[i][0];
			} else if (null == wordPNMap.get(sortQwords[i])) {
				alphaLoosenessBound += widMinDateSpans[i][0];
			} else {
				tempd1 = (alphaRadius + 2) *  widMinDateSpans[i][0];
				tempd2 = wordPNMap.get(sortQwords[i]).getLooseness(id, date);
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
		for(int i=0; i<sortQwords.length; i++) {
			if(null == wordPNMap.get(sortQwords[i])) {
				alphaLoosenessBound += 1;
			} else {
				alphaLoosenessBound += wordPNMap.get(sortQwords[i]).getLooseness(id, sDate, eDate);
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
		int i;
		int[] wids = null;
		
		List<Integer> recCurWidIndex = new ArrayList<>();
		for(i=0; i<sortQwords.length; i++) {
			recCurWidIndex.add(i);
		}
		List<Integer> tempList = new ArrayList<>();
		int numAccessWid = 0;
		
		for(int ni : matchNids) {
			if(rNids.contains(ni)) {
				wids = searchedDatesWids[ni].getWids();
				for(int curWidIndex : recCurWidIndex) {
					if(sortQwords[curWidIndex] == wids[curWidIndex]) {
						numAccessWid++;
						if(numAccessWid==sortQwords.length)	return Boolean.FALSE;
						tempList.add(curWidIndex);
					}
				}
				for(int in : tempList) {
					recCurWidIndex.remove((Object)in);
				}
			}
		}
		return Boolean.TRUE;
	}
}
