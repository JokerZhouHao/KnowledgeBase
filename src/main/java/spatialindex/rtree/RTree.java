// Spatial Index Library
//
// Copyright (C) 2002  Navel Ltd.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License aint with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// Contact information:
//  Mailing address:
//    Marios Hadjieleftheriou
//    University of California, Riverside
//    Department of Computer Science
//    Surge Building, Room 310
//    Riverside, CA 92521
//
//  Email:
//    marioh@cs.ucr.edu

package spatialindex.rtree;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

import neustore.base.LRUBuffer;
import spatialindex.spatialindex.IData;
import spatialindex.spatialindex.IEntry;
import spatialindex.spatialindex.INearestNeighborComparator;
import spatialindex.spatialindex.INode;
import spatialindex.spatialindex.INodeCommand;
import spatialindex.spatialindex.IQueryStrategy;
import spatialindex.spatialindex.IShape;
import spatialindex.spatialindex.ISpatialIndex;
import spatialindex.spatialindex.IStatistics;
import spatialindex.spatialindex.IVisitor;
import spatialindex.spatialindex.Point;
import spatialindex.spatialindex.RWLock;
import spatialindex.spatialindex.Region;
import spatialindex.spatialindex.SpatialIndex;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IBuffer;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.InvalidPageException;
import spatialindex.storagemanager.PropertySet;
import spatialindex.storagemanager.TreeLRUBuffer;
import utility.Global;
import utility.Utility;

public class RTree implements ISpatialIndex
{
	protected RWLock m_rwLock;

	IStorageManager m_pStorageManager;

	protected int m_rootID;
	int m_headerID;

	int m_treeVariant;

	double m_fillFactor;

	int m_indexCapacity;

	int m_leafCapacity;

	int m_nearMinimumOverlapFactor;
		// The R*-Tree 'p' constant, for calculating nearly minimum overlap cost.
		// [Beckmann, Kriegel, Schneider, Seeger 'The R*-tree: An efficient and Robust Access Method
		// for Points and Rectangles, Section 4.1]

	double m_splitDistributionFactor;
		// The R*-Tree 'm' constant, for calculating spliting distributions.
		// [Beckmann, Kriegel, Schneider, Seeger 'The R*-tree: An efficient and Robust Access Method
		// for Points and Rectangles, Section 4.2]

	double m_reinsertFactor;
		// The R*-Tree 'p' constant, for removing entries at reinserts.
		// [Beckmann, Kriegel, Schneider, Seeger 'The R*-tree: An efficient and Robust Access Method
		//  for Points and Rectangles, Section 4.3]

	protected int m_dimension;

	Region m_infiniteRegion;

	public Statistics m_stats;

	ArrayList m_writeNodeCommands = new ArrayList();
	ArrayList m_readNodeCommands = new ArrayList();
	ArrayList m_deleteNodeCommands = new ArrayList();

	public RTree(PropertySet ps, IStorageManager sm)
	{
		m_rwLock = new RWLock();
		m_pStorageManager = sm;
		m_rootID = IStorageManager.NewPage;
		m_headerID = IStorageManager.NewPage;
		m_treeVariant = SpatialIndex.RtreeVariantRstar;
		m_fillFactor = 0.7f;
		m_indexCapacity = 100;
		m_leafCapacity = 100;
		m_nearMinimumOverlapFactor = 32;
		m_splitDistributionFactor = 0.4f;
		m_reinsertFactor = 0.3f;
		m_dimension = 2;

		m_infiniteRegion = new Region();
		m_stats = new Statistics();

		Object var = ps.getProperty("IndexIdentifier");
		if (var != null)
		{
			if (! (var instanceof Integer)) throw new IllegalArgumentException("Property IndexIdentifier must an Integer");
			m_headerID = ((Integer) var).intValue();
			try
			{
				initOld(ps);
			}
			catch (IOException e)
			{
				System.err.println(e);
				throw new IllegalStateException("initOld failed with IOException");
			}
		}
		else
		{
			try
			{
				initNew(ps);
			}
			catch (IOException e)
			{
				System.err.println(e);
				throw new IllegalStateException("initNew failed with IOException");
			}
			Integer i = new Integer(m_headerID);
			ps.setProperty("IndexIdentifier", i);
		}
	}

	//
	// ISpatialIndex interface
	//

	public void insertData(final byte[] data, final IShape shape, int id)
	{
		if (shape.getDimension() != m_dimension) throw new IllegalArgumentException("insertData: Shape has the wrong number of dimensions.");

		m_rwLock.write_lock();

		try
		{
			Region mbr = shape.getMBR();

			byte[] buffer = null;

			if (data != null && data.length > 0)
			{
				buffer = new byte[data.length];
				System.arraycopy(data, 0, buffer, 0, data.length);
			}

			insertData_impl(buffer, mbr, id);
				// the buffer is stored in the tree. Do not delete here.
		}
		finally
		{
			m_rwLock.write_unlock();
		}
	}

	public boolean deleteData(final IShape shape, int id)
	{
		if (shape.getDimension() != m_dimension) throw new IllegalArgumentException("deleteData: Shape has the wrong number of dimensions.");

		m_rwLock.write_lock();

		try
		{
			Region mbr = shape.getMBR();
			return deleteData_impl(mbr, id);
		}
		finally
		{
			m_rwLock.write_unlock();
		}
	}

	public void containmentQuery(final IShape query, final IVisitor v)
	{
		if (query.getDimension() != m_dimension) throw new IllegalArgumentException("containmentQuery: Shape has the wrong number of dimensions.");
		rangeQuery(SpatialIndex.ContainmentQuery, query, v);
	}

	public void intersectionQuery(final IShape query, final IVisitor v)
	{
		if (query.getDimension() != m_dimension) throw new IllegalArgumentException("intersectionQuery: Shape has the wrong number of dimensions.");
		rangeQuery(SpatialIndex.IntersectionQuery, query, v);
	}

	public void pointLocationQuery(final IShape query, final IVisitor v)
	{
		if (query.getDimension() != m_dimension) throw new IllegalArgumentException("pointLocationQuery: Shape has the wrong number of dimensions.");
		
		Region r = null;
		if (query instanceof Point)
		{
			r = new Region((Point) query, (Point) query);
		}
		else if (query instanceof Region)
		{
			r = (Region) query;
		}
		else
		{
			throw new IllegalArgumentException("pointLocationQuery: IShape can be Point or Region only.");
		}

		rangeQuery(SpatialIndex.IntersectionQuery, r, v);
	}

	public void nearestNeighborQuery(int k, final IShape query, final IVisitor v, final INearestNeighborComparator nnc)
	{
		if (query.getDimension() != m_dimension) throw new IllegalArgumentException("nearestNeighborQuery: Shape has the wrong number of dimensions.");

		m_rwLock.read_lock();

		try
		{
			// I need a priority queue here. It turns out that TreeSet sorts unique keys only and since I am
			// sorting according to distances, it is not assured that all distances will be unique. TreeMap
			// also sorts unique keys. Thus, I am simulating a priority queue using an ArrayList and binarySearch.
			ArrayList queue = new ArrayList();

			Node n = readNode(m_rootID);
			queue.add(new NNEntry(n, 0.0));

			int count = 0;
			double knearest = 0.0;

			while (queue.size() != 0)
			{
				NNEntry first = (NNEntry) queue.remove(0);

				if (first.m_pEntry instanceof Node)
				{
					n = (Node) first.m_pEntry;
					v.visitNode((INode) n);

					for (int cChild = 0; cChild < n.m_children; cChild++)
					{
						IEntry e;

						if (n.m_level == 0)
						{
							e = new Data(n.m_pData[cChild], n.m_pMBR[cChild], n.m_pIdentifier[cChild]);
						}
						else
						{
							e = (IEntry) readNode(n.m_pIdentifier[cChild]);
						}

						NNEntry e2 = new NNEntry(e, nnc.getMinimumDistance(query, e));

						// Why don't I use a TreeSet here? See comment above...
						int loc = Collections.binarySearch(queue, e2, new NNEntryComparator());
						if (loc >= 0) queue.add(loc, e2);
						else queue.add((-loc - 1), e2);
					}
				}
				else
				{
					// report all nearest neighbors with equal furthest distances.
					// (neighbors can be more than k, if many happen to have the same
					//  furthest distance).
					if (count >= k && first.m_minDist > knearest) break;

					v.visitData((IData) first.m_pEntry);
					m_stats.m_queryResults++;
					count++;
					knearest = first.m_minDist;
					System.out.println(first.m_minDist);
				}
			}
		}
		finally
		{
			m_rwLock.read_unlock();
		}
	}

	public void nearestNeighborQuery(int k, final IShape query, final IVisitor v)
	{
		if (query.getDimension() != m_dimension) throw new IllegalArgumentException("nearestNeighborQuery: Shape has the wrong number of dimensions.");
		NNComparator nnc = new NNComparator();
		nearestNeighborQuery(k, query, v, nnc);
	}

	public void queryStrategy(final IQueryStrategy qs)
	{
		m_rwLock.read_lock();

		int[] next = new int[] {m_rootID};

		try
		{
			while (true)
			{
				Node n = readNode(next[0]);
				boolean[] hasNext = new boolean[] {false};
				qs.getNextEntry(n, next, hasNext);
				if (hasNext[0] == false) break;
			}
		}
		finally
		{
			m_rwLock.read_unlock();
		}
	}

	public PropertySet getIndexProperties()
	{
		PropertySet pRet = new PropertySet();

		// dimension
		pRet.setProperty("Dimension", new Integer(m_dimension));

		// index capacity
		pRet.setProperty("IndexCapacity", new Integer(m_indexCapacity));

		// leaf capacity
		pRet.setProperty("LeafCapacity", new Integer(m_leafCapacity));

		// R-tree variant
		pRet.setProperty("TreeVariant", new Integer(m_treeVariant));

		// fill factor
		pRet.setProperty("FillFactor", new Double(m_fillFactor));

		// near minimum overlap factor
		pRet.setProperty("NearMinimumOverlapFactor", new Integer(m_nearMinimumOverlapFactor));

		// split distribution factor
		pRet.setProperty("SplitDistributionFactor", new Double(m_splitDistributionFactor));

		// reinsert factor
		pRet.setProperty("ReinsertFactor", new Double(m_reinsertFactor));

		return pRet;
	}

	public void addWriteNodeCommand(INodeCommand nc)
	{
		m_writeNodeCommands.add(nc);
	}

	public void addReadNodeCommand(INodeCommand nc)
	{
		m_readNodeCommands.add(nc);
	}

	public void addDeleteNodeCommand(INodeCommand nc)
	{
		m_deleteNodeCommands.add(nc);
	}

	public boolean isIndexValid()
	{
		boolean ret = true;
		Stack st = new Stack();
		Node root = readNode(m_rootID);

		if (root.m_level != m_stats.m_treeHeight - 1)
		{
			System.err.println("Invalid tree height");
			return false;
		}

		HashMap nodesInLevel = new HashMap();
		nodesInLevel.put(new Integer(root.m_level), new Integer(1));

		ValidateEntry e = new ValidateEntry(root.m_nodeMBR, root);
		st.push(e);

		while (! st.empty())
		{
			e = (ValidateEntry) st.pop();

			Region tmpRegion = (Region) m_infiniteRegion.clone();

			for (int cDim = 0; cDim < m_dimension; cDim++)
			{
				tmpRegion.m_pLow[cDim] = Double.POSITIVE_INFINITY;
				tmpRegion.m_pHigh[cDim] = Double.NEGATIVE_INFINITY;

				for (int cChild = 0; cChild < e.m_pNode.m_children; cChild++)
				{
					tmpRegion.m_pLow[cDim] = Math.min(tmpRegion.m_pLow[cDim], e.m_pNode.m_pMBR[cChild].m_pLow[cDim]);
					tmpRegion.m_pHigh[cDim] = Math.max(tmpRegion.m_pHigh[cDim], e.m_pNode.m_pMBR[cChild].m_pHigh[cDim]);
				}
			}

			if (! (tmpRegion.equals(e.m_pNode.m_nodeMBR)))
			{
				System.err.println("Invalid parent information");
				ret = false;
			}
			else if (! (tmpRegion.equals(e.m_parentMBR)))
			{
				System.err.println("Error in parent");
				ret = false;
			}

			if (e.m_pNode.m_level != 0)
			{
				for (int cChild = 0; cChild < e.m_pNode.m_children; cChild++)
				{
					ValidateEntry tmpEntry = new ValidateEntry(e.m_pNode.m_pMBR[cChild], readNode(e.m_pNode.m_pIdentifier[cChild]));

					if (! nodesInLevel.containsKey(new Integer(tmpEntry.m_pNode.m_level)))
					{
						nodesInLevel.put(new Integer(tmpEntry.m_pNode.m_level), new Integer(1));
					}
					else
					{
						int i = ((Integer) nodesInLevel.get(new Integer(tmpEntry.m_pNode.m_level))).intValue();
						nodesInLevel.put(new Integer(tmpEntry.m_pNode.m_level), new Integer(i + 1));
					}

					st.push(tmpEntry);
				}
			}
		}

		int nodes = 0;
		for (int cLevel = 0; cLevel < m_stats.m_treeHeight; cLevel++)
		{
			int i1 = ((Integer) nodesInLevel.get(new Integer(cLevel))).intValue();
			int i2 = ((Integer) m_stats.m_nodesInLevel.get(cLevel)).intValue();
			if (i1 != i2)
			{
				System.err.println("Invalid nodesInLevel information");
				ret = false;
			}

			nodes += i2;
		}

		if (nodes != m_stats.m_nodes)
		{
			System.err.println("Invalid number of nodes information");
			ret = false;
		}

		return ret;
	}

	public IStatistics getStatistics()
	{
		return (IStatistics) m_stats.clone();
	}

	public void flush() throws IllegalStateException
	{
		try
		{
			storeHeader();
			m_pStorageManager.flush();
		}
		catch (IOException e)
		{
			System.err.println(e);
			throw new IllegalStateException("flush failed with IOException");
		}
	}

	//
	// Internals
	//

	private void initNew(PropertySet ps) throws IOException
	{
		Object var;

		// tree variant.
		var = ps.getProperty("TreeVariant");
		if (var != null)
		{
			if (var instanceof Integer)
			{
				int i = ((Integer) var).intValue();
				if (i != SpatialIndex.RtreeVariantLinear &&  i != SpatialIndex.RtreeVariantQuadratic && i != SpatialIndex.RtreeVariantRstar)
					throw new IllegalArgumentException("Property TreeVariant not a valid variant");
				m_treeVariant = i;
			}
			else
			{
				throw new IllegalArgumentException("Property TreeVariant must be an Integer");
			}
		}

		// fill factor.
		var = ps.getProperty("FillFactor");
		if (var != null)
		{
			if (var instanceof Double)
			{
				double f = ((Double) var).doubleValue();
				if (f <= 0.0f || f >= 1.0f)
					throw new IllegalArgumentException("Property FillFactor must be in (0.0, 1.0)");
				m_fillFactor = f;
			}
			else
			{
				throw new IllegalArgumentException("Property FillFactor must be a Double");
			}
		}

		// index capacity.
		var = ps.getProperty("IndexCapacity");
		if (var != null)
		{
			if (var instanceof Integer)
			{
				int i = ((Integer) var).intValue();
				if (i < 3) throw new IllegalArgumentException("Property IndexCapacity must be >= 3 but is " + i);
				m_indexCapacity = i;
			}
			else
			{
				throw new IllegalArgumentException("Property IndexCapacity must be an Integer");
			}
		}

		// leaf capacity.
		var = ps.getProperty("LeafCapacity");
		if (var != null)
		{
			if (var instanceof Integer)
			{
				int i = ((Integer) var).intValue();
				if (i < 3) throw new IllegalArgumentException("Property LeafCapacity must be >= 3");
				m_leafCapacity = i;
			}
			else
			{
				throw new IllegalArgumentException("Property LeafCapacity must be an Integer");
			}
		}

		// near minimum overlap factor.
		var = ps.getProperty("NearMinimumOverlapFactor");
		if (var != null)
		{
			if (var instanceof Integer)
			{
				int i = ((Integer) var).intValue();
				if (i < 1 || i > m_indexCapacity || i > m_leafCapacity)
					throw new IllegalArgumentException("Property NearMinimumOverlapFactor must be less than both index and leaf capacities");
			m_nearMinimumOverlapFactor = i;
			}
			else
			{
				throw new IllegalArgumentException("Property NearMinimumOverlapFactor must be an Integer");
			}
		}

		// split distribution factor.
		var = ps.getProperty("SplitDistributionFactor");
		if (var != null)
		{
			if (var instanceof Double)
			{
				double f = ((Double) var).doubleValue();
				if (f <= 0.0f || f >= 1.0f)
					throw new IllegalArgumentException("Property SplitDistributionFactor must be in (0.0, 1.0)");
				m_splitDistributionFactor = f;
			}
			else
			{
				throw new IllegalArgumentException("Property SplitDistriburionFactor must be a Double");
			}
		}

		// reinsert factor.
		var = ps.getProperty("ReinsertFactor");
		if (var != null)
		{
			if (var instanceof Double)
			{
				double f = ((Double) var).doubleValue();
				if (f <= 0.0f || f >= 1.0f)
					throw new IllegalArgumentException("Property ReinsertFactor must be in (0.0, 1.0)");
				m_reinsertFactor = f;
			}
			else
			{
				throw new IllegalArgumentException("Property ReinsertFactor must be a Double");
			}
		}

		// dimension
		var = ps.getProperty("Dimension");
		if (var != null)
		{
			if (var instanceof Integer)
			{
				int i = ((Integer) var).intValue();
				if (i <= 1) throw new IllegalArgumentException("Property Dimension must be >= 1");
				m_dimension = i;
			}
			else
			{
				throw new IllegalArgumentException("Property Dimension must be an Integer");
			}
		}

		m_infiniteRegion.m_pLow = new double[m_dimension];
		m_infiniteRegion.m_pHigh = new double[m_dimension];

		for (int cDim = 0; cDim < m_dimension; cDim++)
		{
			m_infiniteRegion.m_pLow[cDim] = Double.POSITIVE_INFINITY;
			m_infiniteRegion.m_pHigh[cDim] = Double.NEGATIVE_INFINITY;
		}

		m_stats.m_treeHeight = 1;
		m_stats.m_nodesInLevel.add(new Integer(0));

		Leaf root = new Leaf(this, -1);
		m_rootID = writeNode(root);

		storeHeader();
	}

	private void initOld(PropertySet ps) throws IOException
	{
		loadHeader();

		// only some of the properties may be changed.
		// the rest are just ignored.

		Object var;

		// tree variant.
		var = ps.getProperty("TreeVariant");
		if (var != null)
		{
			if (var instanceof Integer)
			{
				int i = ((Integer) var).intValue();
				if (i != SpatialIndex.RtreeVariantLinear &&  i != SpatialIndex.RtreeVariantQuadratic && i != SpatialIndex.RtreeVariantRstar)
					throw new IllegalArgumentException("Property TreeVariant not a valid variant");
				m_treeVariant = i;
			}
			else
			{
				throw new IllegalArgumentException("Property TreeVariant must be an Integer");
			}
		}

		// near minimum overlap factor.
		var = ps.getProperty("NearMinimumOverlapFactor");
		if (var != null)
		{
			if (var instanceof Integer)
			{
				int i = ((Integer) var).intValue();
				if (i < 1 || i > m_indexCapacity || i > m_leafCapacity)
					throw new IllegalArgumentException("Property NearMinimumOverlapFactor must be less than both index and leaf capacities");
				m_nearMinimumOverlapFactor = i;
			}
			else
			{
				throw new IllegalArgumentException("Property NearMinimumOverlapFactor must be an Integer");
			}
		}

		// split distribution factor.
		var = ps.getProperty("SplitDistributionFactor");
		if (var != null)
		{
			if (var instanceof Double)
			{
				double f = ((Double) var).doubleValue();
				if (f <= 0.0f || f >= 1.0f)
					throw new IllegalArgumentException("Property SplitDistributionFactor must be in (0.0, 1.0)");
				m_splitDistributionFactor = f;
			}
			else
			{
				throw new IllegalArgumentException("Property SplitDistriburionFactor must be a Double");
			}
		}

		// reinsert factor.
		var = ps.getProperty("ReinsertFactor");
		if (var != null)
		{
			if (var instanceof Double)
			{
				double f = ((Double) var).doubleValue();
				if (f <= 0.0f || f >= 1.0f)
					throw new IllegalArgumentException("Property ReinsertFactor must be in (0.0, 1.0)");
				m_reinsertFactor = f;
			}
			else
			{
				throw new IllegalArgumentException("Property ReinsertFactor must be a Double");
			}
		}

		m_infiniteRegion.m_pLow = new double[m_dimension];
		m_infiniteRegion.m_pHigh = new double[m_dimension];

		for (int cDim = 0; cDim < m_dimension; cDim++)
		{
			m_infiniteRegion.m_pLow[cDim] = Double.POSITIVE_INFINITY;
			m_infiniteRegion.m_pHigh[cDim] = Double.NEGATIVE_INFINITY;
		}
	}

	private void storeHeader() throws IOException
	{
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		DataOutputStream ds = new DataOutputStream(bs);

		ds.writeInt(m_rootID);
		ds.writeInt(m_treeVariant);
		ds.writeDouble(m_fillFactor);
		ds.writeInt(m_indexCapacity);
		ds.writeInt(m_leafCapacity);
		ds.writeInt(m_nearMinimumOverlapFactor);
		ds.writeDouble(m_splitDistributionFactor);
		ds.writeDouble(m_reinsertFactor);
		ds.writeInt(m_dimension);
		ds.writeLong(m_stats.m_nodes);
		ds.writeLong(m_stats.m_data);
		ds.writeInt(m_stats.m_treeHeight);

		for (int cLevel = 0; cLevel < m_stats.m_treeHeight; cLevel++)
		{
			ds.writeInt(((Integer) m_stats.m_nodesInLevel.get(cLevel)).intValue());
		}

		ds.flush();
		m_headerID = m_pStorageManager.storeByteArray(m_headerID, bs.toByteArray());
	}

	private void loadHeader() throws IOException
	{
		byte[] data = m_pStorageManager.loadByteArray(m_headerID);
		DataInputStream ds = new DataInputStream(new ByteArrayInputStream(data));

		m_rootID = ds.readInt();
		m_treeVariant = ds.readInt();
		m_fillFactor = ds.readDouble();
		m_indexCapacity = ds.readInt();
		m_leafCapacity = ds.readInt();
		m_nearMinimumOverlapFactor = ds.readInt();
		m_splitDistributionFactor = ds.readDouble();
		m_reinsertFactor = ds.readDouble();
		m_dimension = ds.readInt();
		m_stats.m_nodes = ds.readLong();
		m_stats.m_data = ds.readLong();
		m_stats.m_treeHeight = ds.readInt();

		for (int cLevel = 0; cLevel < m_stats.m_treeHeight; cLevel++)
		{
			m_stats.m_nodesInLevel.add(new Integer(ds.readInt()));
		}
	}

	protected void insertData_impl(byte[] pData, Region mbr, int id)
	{
		assert mbr.getDimension() == m_dimension;

		boolean[] overflowTable;

		Stack pathBuffer = new Stack();

		Node root = readNode(m_rootID);

		overflowTable = new boolean[root.m_level];
		for (int cLevel = 0; cLevel < root.m_level; cLevel++) overflowTable[cLevel] = false;

		Node l = root.chooseSubtree(mbr, 0, pathBuffer);
		l.insertData(pData, mbr, id, pathBuffer, overflowTable);

		m_stats.m_data++;
	}

	protected void insertData_impl(byte[] pData, Region mbr, int id, int level, boolean[] overflowTable)
	{
		assert mbr.getDimension() == m_dimension;

		Stack pathBuffer = new Stack();

		Node root = readNode(m_rootID);
		Node n = root.chooseSubtree(mbr, level, pathBuffer);
		n.insertData(pData, mbr, id, pathBuffer, overflowTable);
	}

	protected boolean deleteData_impl(final Region mbr, int id)
	{
		assert mbr.getDimension() == m_dimension;

		boolean bRet = false;

		Stack pathBuffer = new Stack();

		Node root = readNode(m_rootID);
		Leaf l = root.findLeaf(mbr, id, pathBuffer);

		if (l != null)
		{
			l.deleteData(id, pathBuffer);
			m_stats.m_data--;
			bRet = true;
		}

		return bRet;
	}

	protected int writeNode(Node n) throws IllegalStateException
	{
		byte[] buffer = null;

		try
		{
			buffer = n.store();
		}
		catch (IOException e)
		{
			System.err.println(e);
			throw new IllegalStateException("writeNode failed with IOException");
		}

		int page;
		if (n.m_identifier < 0) page = IStorageManager.NewPage;
		else page = n.m_identifier;

		try
		{
			page = m_pStorageManager.storeByteArray(page, buffer);
		}
		catch (InvalidPageException e)
		{
			System.err.println(e);
			throw new IllegalStateException("writeNode failed with InvalidPageException");
		}

		if (n.m_identifier < 0)
		{
			n.m_identifier = page;
			m_stats.m_nodes++;
			int i = ((Integer) m_stats.m_nodesInLevel.get(n.m_level)).intValue();
			m_stats.m_nodesInLevel.set(n.m_level, new Integer(i + 1));
		}

		m_stats.m_writes++;

		for (int cIndex = 0; cIndex < m_writeNodeCommands.size(); cIndex++)
		{
			((INodeCommand) m_writeNodeCommands.get(cIndex)).execute(n);
		}

		return page;
	}

	public Node readNode(int id)
	{
		byte[] buffer;
		DataInputStream ds = null;
		int nodeType = -1;
		Node n = null;

		try
		{
			buffer = m_pStorageManager.loadByteArray(id);
			ds = new DataInputStream(new ByteArrayInputStream(buffer));
			nodeType = ds.readInt();

			if (nodeType == SpatialIndex.PersistentIndex) n = new Index(this, -1, 0);
			else if (nodeType == SpatialIndex.PersistentLeaf) n = new Leaf(this, -1);
			else throw new IllegalStateException("readNode failed reading the correct node type information");

			n.m_pTree = this;
			n.m_identifier = id;
			n.load(buffer);			

			m_stats.m_reads++;
		}
		catch (InvalidPageException e)
		{
			System.err.println(e);
			throw new IllegalStateException("readNode failed with InvalidPageException");
		}
		catch (IOException e)
		{
			System.err.println(e);
			throw new IllegalStateException("readNode failed with IOException");
		}

		for (int cIndex = 0; cIndex < m_readNodeCommands.size(); cIndex++)
		{
			((INodeCommand) m_readNodeCommands.get(cIndex)).execute(n);
		}

		return n;
	}

	protected void deleteNode(Node n)
	{
		try
		{
			m_pStorageManager.deleteByteArray(n.m_identifier);
		}
		catch (InvalidPageException e)
		{
			System.err.println(e);
			throw new IllegalStateException("deleteNode failed with InvalidPageException");
		}

		m_stats.m_nodes--;
		int i = ((Integer) m_stats.m_nodesInLevel.get(n.m_level)).intValue();
		m_stats.m_nodesInLevel.set(n.m_level, new Integer(i - 1));

		for (int cIndex = 0; cIndex < m_deleteNodeCommands.size(); cIndex++)
		{
			((INodeCommand) m_deleteNodeCommands.get(cIndex)).execute(n);
		}
	}

	private void rangeQuery(int type, final IShape query, final IVisitor v)
	{
		m_rwLock.read_lock();

		try
		{
			Stack st = new Stack();
			Node root = readNode(m_rootID);

			if (root.m_children > 0 && query.intersects(root.m_nodeMBR)) st.push(root);

			while (! st.empty())
			{
				Node n = (Node) st.pop();

				if (n.m_level == 0)
				{
					v.visitNode((INode) n);

					for (int cChild = 0; cChild < n.m_children; cChild++)
					{
						boolean b;
						if (type == SpatialIndex.ContainmentQuery) b = query.contains(n.m_pMBR[cChild]);
						else b = query.intersects(n.m_pMBR[cChild]);

						if (b)
						{
							Data data = new Data(n.m_pData[cChild], n.m_pMBR[cChild], n.m_pIdentifier[cChild]);
							v.visitData(data);
							m_stats.m_queryResults++;
						}
					}
				}
				else
				{
					v.visitNode((INode) n);

					for (int cChild = 0; cChild < n.m_children; cChild++)
					{
						if (query.intersects(n.m_pMBR[cChild]))
						{
							st.push(readNode(n.m_pIdentifier[cChild]));
						}
					}
				}
			}
		}
		finally
		{
			m_rwLock.read_unlock();
		}
	}

	public String toString()
	{
		String s = "Dimension: " + m_dimension + "\n"
						 + "Fill factor: " + m_fillFactor + "\n"
						 + "Index capacity: " + m_indexCapacity + "\n"
						 + "Leaf capacity: " + m_leafCapacity + "\n";

		if (m_treeVariant == SpatialIndex.RtreeVariantRstar)
		{
			s += "Near minimum overlap factor: " + m_nearMinimumOverlapFactor + "\n"
				 + "Reinsert factor: " + m_reinsertFactor + "\n"
				 + "Split distribution factor: " + m_splitDistributionFactor + "\n";
		}

		s += "Utilization: " + 100 * m_stats.getNumberOfData() / (m_stats.getNumberOfNodesInLevel(0) * m_leafCapacity) + "%" + "\n"
			 + m_stats;

		return s;
	}

	

	public class NNComparator implements INearestNeighborComparator
	{
		public double getMinimumDistance(IShape query, IEntry e)
		{
			IShape s = e.getShape();
			return query.getMinimumDistance(s);
		}
	}

	class ValidateEntry
	{
		Region m_parentMBR;
		Node m_pNode;

		ValidateEntry(Region r, Node pNode) { m_parentMBR = r; m_pNode = pNode; }
	}

	public static void build(String placefile, String treefile, int fanout, int buffersize, int pagesize)throws Exception{
		// Create a disk based storage manager.
		PropertySet ps = new PropertySet();

		Boolean b = new Boolean(true);
		ps.setProperty("Overwrite", b);
		//overwrite the file if it exists.

		ps.setProperty("FileName", treefile);
		Integer i = new Integer(pagesize);
		ps.setProperty("PageSize", i);
		// specify the page size. Since the index may also contain user defined data
		// there is no way to know how big a single node may become. The storage manager
		// will use multiple pages per node if needed. Off course this will slow down performance.

		IStorageManager diskfile = new DiskStorageManager(ps);

		IBuffer file = new TreeLRUBuffer(diskfile, buffersize, false);
		// applies a main memory random buffer on top of the persistent storage manager
		// (LRU buffer, etc can be created the same way).

		// Create a new, empty, RTree with dimensionality 2, minimum load 70%, using "file" as
		// the StorageManager and the RSTAR splitting policy.

		Double f = new Double(0.7);
		ps.setProperty("FillFactor", f);

		i = fanout;
		ps.setProperty("IndexCapacity", i);
		ps.setProperty("LeafCapacity", i);
		// Index capacity and leaf capacity may be different.

		i = new Integer(2);
		ps.setProperty("Dimension", i);

		RTree rtree = new RTree(ps, file);
		
		
		BufferedReader reader = Utility.getBufferedReader(placefile);
		String line = reader.readLine();
		
		int cntLines = 0;
		double[] f1 = new double[2];
		double[] f2 = new double[2];

		long start = System.currentTimeMillis();
		while ((line = reader.readLine()) != null) {
			cntLines++;
			String[] pidCoord = line.split(Global.delimiterLevel1);
			int id = Integer.parseInt(pidCoord[0]);
			String[] coord = pidCoord[1].split(Global.delimiterSpace);
			float x = Float.parseFloat(coord[0]);
			float y = Float.parseFloat(coord[1]);
			if (x < -90 || x > 90 || y < -180 || y > 180) {
				continue;
			}
			f1[0] = f2[0] = x;
			f1[1] = f2[1] = y;
			Region r = new Region(f1, f2);
			rtree.insertData(null, r, id);

			if (cntLines % 100000 == 0)
				System.out.println(cntLines + " places inserted");
		}

		long end = System.currentTimeMillis();

		System.out.println("Revision Minutes: " + ((end - start) / 1000.0f) / 60.0f);

		boolean ret = rtree.isIndexValid();
		if (ret == false)
			System.err.println("Structure is INVALID!");

		rtree.flush();
		
		
		
//		if (cntLines != Global.numPlaces) {
//			throw new Exception("actual num places in file is " + cntLines + " but config is "
//					+ Global.numPlaces);
//		}
	}
	
	public int getM_dimensoin() {
		return this.m_dimension;
	}
	
	public void writeLock(){
		m_rwLock.write_lock();
	}
	public void readLock(){
		m_rwLock.read_lock();
	}
	
	public void readUnlock(){
		m_rwLock.read_unlock();
	}
	
	public int getRoot() {
		return this.m_rootID;
	}
	public int getTreeHeight(){
		return this.m_stats.m_treeHeight;
	}
	
	// 写getNext方法
	private Boolean hasInitGetNext = Boolean.FALSE;
	private IShape  query = null;
	private ArrayList<NNEntry> queue = new ArrayList();
	private Node n = null;
	private NNComparator nnc = null;
	
	public void initGetNext(final IShape query) {
		 nnc = new NNComparator();
		 this.query = query;
		 queue = new ArrayList();
		 n = readNode(m_rootID);
		 queue.add(new NNEntry(n, 0.0));
		 hasInitGetNext = Boolean.TRUE;
	}
	
	public IEntry getNext() {
		if (query.getDimension() != m_dimension) throw new IllegalArgumentException("nearestNeighborQuery: Shape has the wrong number of dimensions.");
		
		if(!hasInitGetNext)	throw new IllegalArgumentException("please init getNext parameters ! ! !");
		
		m_rwLock.read_lock();

		try
		{
			// I need a priority queue here. It turns out that TreeSet sorts unique keys only and since I am
			// sorting according to distances, it is not assured that all distances will be unique. TreeMap
			// also sorts unique keys. Thus, I am simulating a priority queue using an ArrayList and binarySearch.

			while (queue.size() != 0)
			{
				NNEntry first = (NNEntry) queue.remove(0);

				if (first.m_pEntry instanceof Node)
				{
					n = (Node) first.m_pEntry;

					for (int cChild = 0; cChild < n.m_children; cChild++)
					{
						IEntry e;

						if (n.m_level == 0)
						{
							e = new Data(n.m_pData[cChild], n.m_pMBR[cChild], n.m_pIdentifier[cChild]);
						}
						else
						{
							e = (IEntry) readNode(n.m_pIdentifier[cChild]);
						}

						NNEntry e2 = new NNEntry(e, nnc.getMinimumDistance(query, e));

						// Why don't I use a TreeSet here? See comment above...
						int loc = Collections.binarySearch(queue, e2, new NNEntryComparator());
						if (loc >= 0) queue.add(loc, e2);
						else queue.add((-loc - 1), e2);
					}
				}
				else
				{
					// report all nearest neighbors with equal furthest distances.
					// (neighbors can be more than k, if many happen to have the same
					//  furthest distance).
					return first.m_pEntry;
				}
			}
		}
		finally
		{
			m_rwLock.read_unlock();
		}
		hasInitGetNext = Boolean.FALSE;
		return null;
	}
	
	public static RTree getInstance() throws Exception {
		//buffer for alpha WN inverted index 
		 LRUBuffer buffer = new LRUBuffer(Global.alphaIindexRTNodeBufferSize, Global.rtreePageSize);
		
		//the data index structure of RDF data with R-tree, RDF Graph, and Inverted index of keywords
		PropertySet psRTree = new PropertySet();
		String indexRTree = Global.indexRTree;
		psRTree.setProperty("FileName", indexRTree);
		psRTree.setProperty("PageSize", Global.rtreePageSize);
		psRTree.setProperty("BufferSize", Global.rtreeBufferSize);
		psRTree.setProperty("fanout", Global.rtreeFanout);
		
		IStorageManager diskfile = new DiskStorageManager(psRTree);
		IBuffer file = new TreeLRUBuffer(diskfile, Global.rtreeBufferSize, false);
		
		Integer i = new Integer(1); 
		psRTree.setProperty("IndexIdentifier", i);
		
		return new RTree(psRTree, file);
	}
	
	
	
	// 主函数
	public static void main(String[] args) throws Exception{
		
		RTree tree = RTree.getInstance();
		IStatistics stat = tree.getStatistics();
		System.out.println(stat.getNumberOfNodes());
		System.out.println(stat.getNumberOfData());
		System.out.println(stat.getReads());
		System.out.println(stat.getWrites());
		System.out.println(tree.getTreeHeight());
		
		
//		if (args.length != 1)
//		{
//			System.err.println("Usage: runnable configFile");
//			System.exit(-1);
//		}
//		
//		Utility.loadInitialConfig(args[0]);
//		
//		String inputfile = Global.inputDirectoryPath + Global.pidCoordFile + Global.dataVersion;
//		String treefile = Global.outputDirectoryPath + Global.pidCoordFile + Global.rtreeFlag + Global.rtreeFanout + Global.dataVersion;
//		int fanout = Global.rtreeFanout;
//		int buffersize = Global.rtreeBufferSize;
//		int pagesize = Global.rtreePageSize;
//		
//		build(inputfile, treefile, fanout, buffersize, pagesize);
//		
//		double[] pCoord = new double[2];
//		pCoord[0] = 3;
//		pCoord[1] = 2;
//			IEntry ie = null;
//			rTree.initGetNext(new Point(pCoord));
//			while(null != (ie = rTree.getNext())) {
//				System.out.print(ie.getIdentifier() + " : ( " + ie.getShape().getCenter()[0] + ", " + ie.getShape().getCenter()[1] + " )\n");
//			}
	}
	
}
