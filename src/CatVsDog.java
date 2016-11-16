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
			sc.nextInt();
			sc.nextInt();
			int numberOfPreferences = sc.nextInt();
			sc.nextLine();
			
			// parse preferences
			List<String[]> voterPreferences = new ArrayList<String[]>();
			for (int j = 0; j < numberOfPreferences; j++) {
				String keep = sc.next();
				String remove = sc.next();
				voterPreferences.add(new String[] {keep, remove});	
				sc.nextLine();
			}
			
			// analyze preferences
			CatVsDog cvd = new CatVsDog();
			List<VoterPreference> vps = cvd.convertPreferences(voterPreferences);
			FlowNetwork fn = cvd.new FlowNetwork(vps);
			cvd.initializeGraphCapacity(fn, vps);
			int minNumberUnsatisfiedPreferences = cvd.ekMaxFlow(fn);
			int numSatisfiedPreferences = numberOfPreferences - minNumberUnsatisfiedPreferences;
			System.out.println(numSatisfiedPreferences);
		}
		
		sc.close();
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
	
	void initializeGraphCapacity(FlowNetwork fn, List<VoterPreference> vps) {
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
					fn.setCapacity(fn.getIndex(clp), fn.getIndex(dlp), 1);
				}
			}
		}
		
		// connect source to cat lovers
		for (VoterPreference clp : clps) {
			fn.setCapacity(fn.getSourceIndex(), fn.getIndex(clp), 1);
		}
		
		// connect dog lovers to sink
		for (VoterPreference dlp : dlps) {
			fn.setCapacity(fn.getIndex(dlp), fn.getSinkIndex(), 1);
		}	
	}
	
	List<Integer> getBFSPath(FlowNetwork fn) {
		// book's pseudocode adapted to Java
		int size = fn.getSize();
		char[] color = new char[size];
		int[] d = new int[size];
		int[] pi = new int[size];
		Queue<Integer> q = new LinkedList<Integer>();	
		for (int i = 0; i < size; i++) {
			if (i == fn.getSourceIndex()) {
				color[i] = 'G';
				d[i] = 0;
			} else {
				color[i] = 'W';
				d[i] = Integer.MAX_VALUE;
			}
			pi[i] = -1;
		}
		q.add(fn.getSourceIndex());
		while (q.peek() != null) {
			int u = q.remove();
			for (int v : fn.getAdjacentVerticiesWithResidualCapacity(u)) {
				if (color[v] == 'W') {
					color[v] = 'G';
					d[v] = d[u] + 1;
					pi[v] = u;
					q.add(v);
				}
			}
		}
		
		// use pi array to find vertex indices in path from sink to source
		List<Integer> path = new LinkedList<Integer>();
		int v = fn.getSinkIndex();
		while(pi[v] != -1) {
			path.add(0, v);
			v = pi[v];
		}	
		if (v == fn.getSourceIndex()) {
			path.add(0, v);
		}
		return path;
	}
	
	Integer ekMaxFlow(FlowNetwork fn) {
		// book's pseudocode adapted to Java
		int f = 0;
		List<Integer> path = getBFSPath(fn);
		while (!path.isEmpty()) {
			
			// special case for problem
			int flowAmount = 1;
			
			List<Integer[]> edges = new ArrayList<Integer[]>();
			for (int i = 1; i < path.size(); i++) {
				edges.add(new Integer[] {path.get(i-1), path.get(i)});
			}
			
			for (Integer[] edge : edges) {
				int v1 = edge[0];
				int v2 = edge[1];
				if (fn.getCapacity(v1, v2) != 0) {
					int currFlow = fn.getFlow(v1, v2);
					fn.setFlow(v1, v2, currFlow + flowAmount);
				} else {
					int currFlow = fn.getFlow(v2, v1);
					fn.setFlow(v2, v1, currFlow - flowAmount);
				}
			}
			path = getBFSPath(fn);
			f += flowAmount;
		}
		return f;
	}
	
	class FlowNetwork {
		// graph information
		int numberOfVerticies;
		int[][] flowMatrix;
		int[][] capacityMatrix;
		List<List<Integer>> residualCapacityAdjList;
		
		// convenient index information
		int sourceIndex;
		int sinkIndex;
		HashMap<VoterPreference, Integer> indexMap = new HashMap<VoterPreference, Integer>();

		public FlowNetwork(List<VoterPreference> vps) {
			numberOfVerticies = vps.size() + 2;
			
			flowMatrix = new int[numberOfVerticies][numberOfVerticies];
			capacityMatrix = new int[numberOfVerticies][numberOfVerticies];
			residualCapacityAdjList = new ArrayList<List<Integer>>();
			
			// build index map
			int i = 0;
			for ( ; i < vps.size(); i++) {
				indexMap.put(vps.get(i), i);
			}
			sourceIndex = i++;
			sinkIndex = i++;
			
			// initialize adjList
			for (int j = 0; j < numberOfVerticies; j++) {
				residualCapacityAdjList.add(new LinkedList<Integer>());
			}
		}
		
		public int getSize() {
			return numberOfVerticies;
		}
		
		public List<Integer> getAdjacentVerticiesWithResidualCapacity(int v) {
			return residualCapacityAdjList.get(v);
		}
		
		int getResidualCapacity(int v1, int v2) {
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
			capacityMatrix[v1][v2] = value;
			residualCapacityAdjList.get(v1).add(v2);
		}
		
		public int getCapacity(int v1, int v2) {
			return capacityMatrix[v1][v2];
		}
		
		public void setFlow(int v1, int v2, int value) {
			flowMatrix[v1][v2] = value;
			
			// modify residualCapacityAdjList to take new flow information into account
			// modify edge v1 -> v2
			List<Integer> adjList = residualCapacityAdjList.get(v1);
			if (getResidualCapacity(v1, v2) > 0) {
				if (!adjList.contains(v2)) {
					adjList.add(v2);
				}
			} else {
				adjList.remove(Integer.valueOf(v2));
			}
			// modify edge v2 -> v1
			adjList = residualCapacityAdjList.get(v2);
			if (getResidualCapacity(v2, v1) > 0) {
				if (!adjList.contains(v1)) {
					adjList.add(v1);
				}
			} else {
				adjList.remove(Integer.valueOf(v1));
			}
		}
		
		public int getFlow(int v1, int v2) {
			return flowMatrix[v1][v2];
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