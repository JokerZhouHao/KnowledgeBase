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
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import entity.sp.WordRadiusNeighborhood;
import entity.sp.reach.CReach;
import entity.sp.DatesWIds;
import entity.sp.NNEntryMapHeap;
import entity.sp.NidToDateWidIndex;
import entity.sp.NidToDateWidIndex.DateWid;
import entity.sp.RTreeWithGI;
import entity.sp.SortedDateWidIndex;
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
 * 使用索引来操作
 * @author Monica
 * @since 2018/6/9
 */
public class KSPIndexMultiThread {
	protected RTreeWithGI rgi;
	private Set<Integer>[] rtreeNode2Pid = null;
	private CReach cReach = null;
	private Map<Integer, DatesWIds> nIdDateWidMap = null;
	private SortedDateWidIndex[] wid2DateNidPair = null;
	private Set<Integer>[] w2pReachable = null;
	private HashMap<Integer, WordRadiusNeighborhood> wordPNMap = null;
	
	// 供多线程计算的公共变量
	private int[] sortQwords = null;
	private int date = -1;
	
	double kthScore = Double.POSITIVE_INFINITY;

	public KSPIndexMultiThread(RTreeWithGI rgi, Set<Integer>[] rtreeNode2Pid, CReach cReach,
			Map<Integer, DatesWIds> nIdDateWidMap, SortedDateWidIndex[] wid2DateNidPair,
			Set<Integer>[] w2pReachable, HashMap<Integer, WordRadiusNeighborhood> wordPNMap) {
		super();
		this.rgi = rgi;
		this.rtreeNode2Pid = rtreeNode2Pid;
		this.cReach = cReach;
		this.nIdDateWidMap = nIdDateWidMap;
		this.wid2DateNidPair = wid2DateNidPair;
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
	
	// 用多线程计算widsMinDataSpan
	class KV{
		int k;
		double w = 0;
		double v = 0;
		Map<Integer, Integer> minDateSpan = null;
		public KV(int k, double w, double v, Map<Integer, Integer> mds) {
			this.k = k;
			this.w = w;
			this.v = v;
			this.minDateSpan = mds;
		}
	}
	
	class ThreadCalMinDateSpan extends Thread{
		ArrayBlockingQueue<KV> queue = null;
		ArrayBlockingQueue<KV> resultQueue = null;
		public ThreadCalMinDateSpan(ArrayBlockingQueue<KV> qu, ArrayBlockingQueue<KV> rq) {
			this.queue = qu;
			this.resultQueue = rq;
		}
		
		public void run() {
			KV tkv = null;
			Map<Integer, Integer> mds = null;
			try {
				while(true) {
					tkv = queue.take();
					if(tkv.k == Integer.MIN_VALUE)	break;
					else if(tkv.k < 0) {
						mds = getRTreeWidMinDateSpan(-(tkv.k + 1), sortQwords, date);
					} else {
						mds = getPidWidMinDateSpan(tkv.k, sortQwords, date);
					}
					double alphaRankingScoreBound = tkv.w * getAlphaLoosenessBound(tkv.k, Global.radius, mds, sortQwords, date);
					resultQueue.put(new KV(tkv.k, tkv.w, alphaRankingScoreBound, mds));
				}
			} catch (Exception e) {
				System.err.println("> ThreadCalMinDateSpan异常而退出！！！");
				e.printStackTrace();
				System.exit(0);
			}
		}
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
		HashMap<Integer, Map<Integer, Integer>> recMinDateSpanMap = new HashMap<>();
		
		// 创建多个线程计算minDateSpan
		this.sortQwords = sortQwords;
		this.date = date;
		int numThread = 20;
		ArrayBlockingQueue<KV> nidQueue = new ArrayBlockingQueue<>(numThread);
		ArrayBlockingQueue<KV> resultQueue = new ArrayBlockingQueue<>(numThread);
		for(int i=0; i<numThread; i++) {
			new ThreadCalMinDateSpan(nidQueue, resultQueue).start();
		}
		int numCurCalThread = 0;
		KV tkv = null;
		
		rgi.readLock();

		try {
			/* I need a priority queue here. It turns out that TreeSet sorts unique keys only and since I am
		 	   sorting according to distances, it is not assured that all distances will be unique. TreeMap
			   also sorts unique keys. Thus, I am simulating a priority queue using an ArrayList and binarySearch. */
//			ArrayList queue = new ArrayList();
//			NNEntryMapHeap heap = new NNEntryMapHeap();
			
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
					Data firstData = (Data) first.m_pEntry;
					n = rgi.readNode(firstData.getIdentifier());
					
					for (int cChild = 0; cChild < n.m_children; cChild++) {
						double minSpatialDist = qpoint.getMinimumDistance(n.m_pMBR[cChild]) + 1;
						double alphaLoosenessBound = 0;
						nid = n.getChildIdentifier(cChild);
						if (n.m_level == 0) {
							//children of n are places
							if(Global.isTest) {
								Global.rr.setFrontTime();
							}
							Global.rr.numCptTotalPid2Wids++;
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
							
							minDateSpanMap = this.getPidWidMinDateSpan(nid, sortQwords, date);
							
							if(Global.isTest) {
								Global.rr.timeCptGetMinDateSpan += Global.rr.getTimeSpan();
							}
							
							recMinDateSpanMap.put(nid, minDateSpanMap);
							alphaLoosenessBound = this.getAlphaLoosenessBound(nid, alphaRadius, minDateSpanMap, sortQwords, date);
						} else {
							//ATTENTION: children of n are nodes that have -id-1 as identifier in alpha index
							if(Global.isTest) {
								Global.rr.setFrontTime();
							}
							Global.rr.numCptTotalPid2Wids++;
							if (this.placeReachablePrune(-nid-1, sortQwords)) {
								if(Global.isTest) {
									Global.rr.timeCptPid2Wids += Global.rr.getTimeSpan();
									Global.rr.setFrontTime();
									Global.rr.numCptPruneRTree2Wid++;
								}
								continue;
							}
							if(Global.isTest) {
								Global.rr.timeCptPid2Wids += Global.rr.getTimeSpan();
//								Global.rr.setFrontTime();
							}
							
							nid = -nid -1;
							
//							minDateSpanMap = this.getRTreeWidMinDateSpan(nid, sortQwords, date);
//							
//							if(Global.isTest) {
//								Global.rr.timeCptGetMinDateSpan += Global.rr.getTimeSpan();
//							}
//							
//							alphaLoosenessBound = this.getAlphaLoosenessBound(-nid-1, alphaRadius, minDateSpanMap, sortQwords, date);
						}
						
						if(Global.isTest) {
							Global.rr.tempT = System.nanoTime();
						}
						nidQueue.put(new KV(nid, minSpatialDist, 0, null));
						numCurCalThread++;
						if(numCurCalThread == numThread) {
							while(numCurCalThread != 0) {
								tkv = resultQueue.take();
								numCurCalThread--;
								if (tkv.v > kthScore) {
									if(n.m_level == 0) {
										Global.rr.numCptPruneRTreePid++;
									} else {
										Global.rr.numCptPruneRTeeNode++;
									}
//									continue;
								} else {
									if(n.m_level == 0) {
										recMinDateSpanMap.put(tkv.k, tkv.minDateSpan);
									}
									int t = tkv.k;
									if(t < 0)	t = -(t+1);
									IEntry eChild = new Data(tkv.w, null,
											t, -1);
									NNEntry eChild2 = new NNEntry(eChild, tkv.v, n.m_level - 1);
									if(Global.isTest) {
										Global.rr.setFrontTime();
									}
//									insertIntoHeapH(queue, eChild2);
									heap.put(eChild2);
									
									if(Global.isTest) {
										Global.rr.numCptQueuePut++;
										if(Global.rr.numCptMaxQueueSize < heap.size())	Global.rr.numCptMaxQueueSize = heap.size();
										Global.rr.timeCptQueuePut += Global.rr.getTimeSpan();
									}
								}
							}
						}
//						if(Global.isTest) {
//							Global.rr.timeCptGetMinDateSpan += System.nanoTime() - Global.rr.tempT;
//						}
//						
//						
//						double alphaRankingScoreBound = minSpatialDist * alphaLoosenessBound;
//						if (alphaRankingScoreBound > kthScore) {
//							if(n.m_level == 0) {
//								Global.rr.numCptPruneRTreePid++;
//							} else {
//								Global.rr.numCptPruneRTeeNode++;
//							}
//							continue;
//						}
//						IEntry eChild = new Data(minSpatialDist, n.m_pMBR[cChild],
//								n.m_pIdentifier[cChild], n.m_identifier);
//						NNEntry eChild2 = new NNEntry(eChild, alphaRankingScoreBound, n.m_level - 1);
//						if(Global.isTest) {
//							Global.rr.setFrontTime();
//						}
////						insertIntoHeapH(queue, eChild2);
//						heap.put(eChild2);
//						
//						if(Global.isTest) {
//							Global.rr.numCptQueuePut++;
//							if(Global.rr.numCptMaxQueueSize < heap.size())	Global.rr.numCptMaxQueueSize = heap.size();
//							Global.rr.timeCptQueuePut += Global.rr.getTimeSpan();
//						}
					}
					if(Global.isTest) {
						Global.rr.tempT = System.nanoTime();
					}
					while(numCurCalThread != 0) {
						tkv = resultQueue.take();
						numCurCalThread--;
						if (tkv.v > kthScore) {
							if(n.m_level == 0) {
								Global.rr.numCptPruneRTreePid++;
							} else {
								Global.rr.numCptPruneRTeeNode++;
							}
//							continue;
						} else {
							if(n.m_level == 0) {
								recMinDateSpanMap.put(tkv.k, tkv.minDateSpan);
							}
							int t = tkv.k;
							if(t < 0)	t = -(t+1);
							IEntry eChild = new Data(tkv.w, null,
									t, -1);
							NNEntry eChild2 = new NNEntry(eChild, tkv.v, n.m_level - 1);
							if(Global.isTest) {
								Global.rr.setFrontTime();
							}
//							insertIntoHeapH(queue, eChild2);
							heap.put(eChild2);
							
							if(Global.isTest) {
								Global.rr.numCptQueuePut++;
								if(Global.rr.numCptMaxQueueSize < heap.size())	Global.rr.numCptMaxQueueSize = heap.size();
								Global.rr.timeCptQueuePut += Global.rr.getTimeSpan();
							}
						}
					}
					if(Global.isTest) {
						Global.rr.timeCptGetMinDateSpan += System.nanoTime() - Global.rr.tempT;
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
					if(Global.isTest) {
						Global.rr.numGetSemanticTree++;
						Global.rr.setFrontTime();
					}
					List<List<Integer>> semanticTree = new ArrayList<List<Integer>>();
					double looseness = this.rgi.getGraph().getSemanticPlaceP(nid,
							sortQwords, date, loosenessThreshold, nIdDateWidMap, recMinDateSpanMap.get(nid), semanticTree);
//					double looseness = this.rgi.getGraph().getSemanticPlaceP(nid,
//							qwords, date, loosenessThreshold, nIdDateWidMap, minDateSpanMap, semanticTree);
					
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
//			System.out.println(queue.size());
			if(Global.isTest) {
//				Global.rr.numLastQueue = queue.size();
				Global.rr.numLastQueue = heap.size();
				Global.rr.kthScore = this.kthScore;
				Global.rr.queueLastValue = first.m_minDist;
			}
		} finally {
			rgi.readUnlock();
		}
		
		// 结束线程
		for(int i=0; i<numThread; i++) {
			nidQueue.put(new KV(Integer.MIN_VALUE, 0, 0, null));
		}
		
		recMinDateSpanMap.clear();
		if(Global.isTest) {
			System.out.println("numCptGetMinDateSpanLeftSpan : " + Global.rr.numCptGetMinDateSpanLeftSpan + " numCptGetMinDateSpanRightSpan : " + Global.rr.numCptGetMinDateSpanRightSpan + " timeCptGetMinDateSpan : " + Global.rr.timeCptGetMinDateSpan/1000);
//			System.out.println(
//					"timeCptQueuePut = " + Global.rr.timeCptQueuePut/1000 + " " + 
//					"timeCptQueueRemove = " + Global.rr.timeCptQueueRemove/1000 + " " + 
//					"timeCptPid2Wids = " + Global.rr.timeCptPid2Wids/1000 + " " + 
//					"timeCptGetMinDateSpan = " + Global.rr.timeCptGetMinDateSpan/1000 + " " + 
//					"timeCptGetSemanticTree = " + Global.rr.timeCptGetSemanticTree/1000);
			System.out.println(
					"numCptMaxQueueSize timeCptQueuePut timeCptQueueRemove timeCptPid2Wids timeCptGetMinDateSpan timeCptGetSemanticTree timeKSPComputation\n"+ 
					Global.rr.numCptMaxQueueSize + " " + 
					Global.rr.timeCptQueuePut/Global.rr.timeBase + " " + 
					Global.rr.timeCptQueueRemove/Global.rr.timeBase + " " + 
					Global.rr.timeCptPid2Wids/Global.rr.timeBase + " " + 
					Global.rr.timeCptGetMinDateSpan/Global.rr.timeBase + " " + 
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
		int i=0;
		if(Global.isTest) {
			Global.rr.numCptGetMinDateSpan += sortQwords.length;
		}
		HashSet<Integer> rec = new HashSet<>();
		for(i=0; i<sortQwords.length; i++) {
			widMinDateSpan.put(sortQwords[i], wid2DateNidPair[i].getMinDateSpan(rec, date, id, cReach));
		}
		rec.clear();
		return widMinDateSpan;
	}
	
	public HashMap<Integer, Integer> getRTreeWidMinDateSpan(int id, int[] sortQwords, int date) throws IOException {
		HashMap<Integer, Integer> widMinDateSpan = new HashMap<>();
		int i=0;
		if(Global.isTest) {
			Global.rr.numCptGetMinDateSpan += sortQwords.length;
		}
		HashSet<Integer> rec = new HashSet<>();
		for(i=0; i<sortQwords.length; i++) {
//			widMinDateSpan.put(sortQwords[i], wid2DateNidPair[i].getMinDateSpan(rec, date, id));
			widMinDateSpan.put(sortQwords[i], wid2DateNidPair[i].getMinDateSpan(date));
		}
		rec.clear();
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
