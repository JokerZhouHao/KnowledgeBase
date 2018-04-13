package spatialindex.rtree;

import java.util.HashSet;

import spatialindex.spatialindex.IEntry;
import java.io.Serializable;
public class NNEntry implements Serializable{

	public IEntry m_pEntry;
	public double m_minDist;
	public int level;
	public int type;
	public int rid = -1;
	public String shapetype;

	public NNEntry(IEntry e, double f) { m_pEntry = e; m_minDist = f; }
	
	public NNEntry(IEntry e, double f, int l) { m_pEntry = e; m_minDist = f; level = l; }
	
	public double ox, oy, radius;
	
	public double linek, linex1, liney1, linex2, liney2;
	
	public double threshold;
	
	public boolean pruned;
}
