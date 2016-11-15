import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class CatVsDog {
	
	public static void main(String[] args) {		
		Scanner sc = new Scanner(System.in);
		
		// parse test info
		int numCases = sc.nextInt();
		sc.nextLine();
		
		for (int i = 0; i < numCases; i++) {
			
			// parse case info
			int c = sc.nextInt();
			int d = sc.nextInt();
			int v = sc.nextInt();
			sc.nextLine();
			
			// parse preferences
			List<String[]> voterPreferences = new ArrayList<String[]>();
			for (int j = 0; j < v; j++) {
				String keep = sc.next();
				String remove = sc.next();
				voterPreferences.add(new String[] {keep, remove});	
				sc.nextLine();
			}
			
			// analyze preferences
			CatVsDog cvd = new CatVsDog();
			List<VoterPreference> vps = cvd.convertPreferences(voterPreferences);
			Graph g = cvd.new Graph(vps);
			cvd.buildGraph(g, vps);
			int maxFlow = cvd.ekMaxFlow(g);
			System.out.println(v - maxFlow);
		}
		
		sc.close();
	}
	
	void buildGraph(Graph g, List<VoterPreference> vps) {
		// sort preferences into cat and dog lovers
		List<VoterPreference> dlps = new ArrayList<VoterPreference>();
		List<VoterPreference> clps = new ArrayList<VoterPreference>();
		for (VoterPreference vp : vps) {
			if (vp.isCatLover) {
				clps.add(vp);
			} else {
				dlps.add(vp);
			}
		}
		
		// connect incompatible cat and dog lovers
		for (VoterPreference clp : clps) {
			for (VoterPreference dlp : dlps) {
				if (clp.keepNumber == dlp.removeNumber || clp.removeNumber == dlp.keepNumber) {
					g.setCapacity(g.getIndex(clp), g.getIndex(dlp), 1);
				}
			}
		}
		
		// connect source to cat lovers
		for (VoterPreference clp : clps) {
			g.setCapacity(g.getSourceIndex(), g.getIndex(clp), 1);
		}
		
		// connect dog lovers to sink
		for (VoterPreference dlp : dlps) {
			g.setCapacity(g.getIndex(dlp), g.getSinkIndex(), 1);
		}	
	}
	
	List<VoterPreference> convertPreferences(List<String[]> preferences) {
		// parse raw preferences into VoterPreference objects
		List<VoterPreference> vps = new ArrayList<VoterPreference>();
		for (String[] preference : preferences) {
			boolean isCatLover = preference[0].charAt(0) == 'C';
			int keepNumber = Integer.parseInt(preference[0].substring(1));
			int removeNumber = Integer.parseInt(preference[1].substring(1));
			vps.add(new VoterPreference(isCatLover, keepNumber, removeNumber));
		}
		return vps;
	}
	
	List<Integer> getBFSPath(Graph g) {
		int size = g.getSize();
		char[] color = new char[size];
		int[] d = new int[size];
		int[] pi = new int[size];
		Queue<Integer> q = new LinkedList<Integer>();	
		for (int i = 0; i < size; i++) {
			if (i == g.getSourceIndex()) {
				color[i] = 'G';
				d[i] = 0;
			} else {
				color[i] = 'W';
				d[i] = Integer.MAX_VALUE;
			}
			pi[i] = -1;
		}
		q.add(g.getSourceIndex());
		while (q.peek() != null) {
			int u = q.remove();
			for (int v : g.getAdjacentNodes(u)) {
				if (color[v] == 'W') {
					color[v] = 'G';
					d[v] = d[u] + 1;
					pi[v] = u;
					q.add(v);
				}
			}
			color[u] = 'B';
		}
		List<Integer> path = new LinkedList<Integer>();
		int v = g.getSinkIndex();
		while(pi[v] != -1) {
			path.add(0, v);
			v = pi[v];
		}
		if (v == g.getSourceIndex()) {
			path.add(0, v);
		}
		return path;
	}
	
	Integer ekMaxFlow(Graph g) {
		int f = 0;
		List<Integer> path = getBFSPath(g);
		while (!path.isEmpty()) {
			List<Integer[]> edges = new ArrayList<Integer[]>();
			int minRCapacity = Integer.MAX_VALUE;
			for (int i = 1; i < path.size(); i++) {
				int edgeRCapacity = g.getResidualCapacity(path.get(i-1), path.get(i));
				if (edgeRCapacity < minRCapacity) {
					minRCapacity = edgeRCapacity;
				}
				edges.add(new Integer[] {path.get(i-1), path.get(i)});
			}
			
			for (Integer[] edge : edges) {
				if (g.getCapacity(edge[0], edge[1]) != 0) {
					int currFlow = g.getFlow(edge[0], edge[1]);
					g.setFlow(edge[0], edge[1], currFlow + minRCapacity);
				} else {
					int currFlow = g.getFlow(edge[1], edge[0]);
					g.setFlow(edge[1], edge[0], currFlow - minRCapacity);
				}
			}
			path = getBFSPath(g);
			f += minRCapacity;
		}
		return f;
	}
	
	class Graph {
		int[][] f;
		int[][] c;
		int size;
		HashMap<VoterPreference, Integer> indexMap = new HashMap<VoterPreference, Integer>();
		int sourceIndex;
		int sinkIndex;

		public Graph(List<VoterPreference> vps) {
			size = vps.size() + 2;
			f = new int[size][size];
			c = new int[size][size];
			
			int i;
			for (i = 0; i < vps.size(); i++) {
				indexMap.put(vps.get(i), i);
			}
			sourceIndex = i++;
			sinkIndex = i++;
		}
		
		public int getSize() {
			return size;
		}
		
		public List<Integer> getAdjacentNodes(int v) {
			List<Integer> l = new ArrayList<Integer>();
			for (int i = 0; i < getSize(); i++) {
				int rCap = getResidualCapacity(v, i);
				if (rCap > 0) {
					l.add(i);
				}
			}
			return l;
		}
		
		public Integer getResidualCapacity(int v1, int v2) {
			return getCapacity(v1, v2) - getFlow(v1, v2) + getFlow(v2, v1);
		}
		
		public int getSourceIndex() {
			return sourceIndex;
		}

		public int getSinkIndex() {
			return sinkIndex;
		}
		
		public int getIndex(VoterPreference vp) {
			return indexMap.get(vp);
		}
		
		public void setCapacity(int v1, int v2, int value) {
			c[v1][v2] = value;
		}
		
		public int getCapacity(int v1, int v2) {
			return c[v1][v2];
		}
		
		public void setFlow(int v1, int v2, int value) {
			f[v1][v2] = value;
		}
		
		public int getFlow(int v1, int v2) {
			return f[v1][v2];
		}
	}
	
	class VoterPreference {
		
		boolean isCatLover;
		int keepNumber;
		int removeNumber;
	
		public VoterPreference(boolean isCatLover, int keepNumber, int removeNumber) {
			this.isCatLover = isCatLover;
			this.keepNumber = keepNumber;
			this.removeNumber = removeNumber;
		}
	}
}
