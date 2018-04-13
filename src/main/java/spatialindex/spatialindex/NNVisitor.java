package spatialindex.spatialindex;

import java.util.Vector;

import spatialindex.rtree.Data;

public class NNVisitor implements IVisitor{
	
	public Vector<IData> result;
	//graph instance
	//invertedindex instance
	//do the weighted computation here
	//not natural....
	//better to extend RTree class
	
	public NNVisitor(){
		result = new Vector<IData>();
	}

	public void visitNode(INode n) {
		System.out.println("visiting node " + n.getIdentifier() + " "+ ((Region)n.getShape()).toString());
	}

	public void visitData(IData d) {
		System.out.println("one more kNN result is found " + ((Data)d).getShape().toString());
		result.add(d);
	}
}
