/**
 * 
 */
package queryindex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import neustore.base.IntKey;
import neustore.base.KeyData;

/**
 * @author jieming
 * 
 *         Limit to integer only...
 *
 */
public class VertexQwordsMap<V> {
	public Map<V, Set<V>> vertexQwordsMap = null;

	public VertexQwordsMap(V[] qwords, Map<V, ArrayList> postinglists, boolean isWeigthed) {
		this.buildVertexQwordsMap(qwords, postinglists, isWeigthed);
	}

	/**
	 * @param qwords
	 * @param postinglists
	 * @return
	 */
	private void buildVertexQwordsMap(V[] qwords, Map<V, ArrayList> postinglists, boolean isWeighted) {
		vertexQwordsMap = new HashMap<V, Set<V>>();

		for (Entry<V, ArrayList> postinglistEntry : postinglists.entrySet()) {
			V qword = postinglistEntry.getKey();
			ArrayList postinglist = postinglistEntry.getValue();

			for (int i = 0; i < postinglist.size(); i++) {
				KeyData keydatai = (KeyData) postinglist.get(i);
				V vertex =(V) (Integer)((IntKey) keydatai.key).key;
				if (!vertexQwordsMap.containsKey(vertex)) {
					vertexQwordsMap.put(vertex, new HashSet<V>());
				}
				Set<V> qwordsContainedInVertex = vertexQwordsMap.get(vertex);
				qwordsContainedInVertex.add(qword);
			}
		}
	}
	
	public void clear(){
		for (Entry<V, Set<V>> entry : vertexQwordsMap.entrySet()) {
			if (entry.getValue() != null) {
				entry.getValue().clear();
			}
		}
		vertexQwordsMap.clear();
	}
}
