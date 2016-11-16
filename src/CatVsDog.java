import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

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
			
			FlowNetwork fn = cvd.new FlowNetwork(vps);
			cvd.initializeGraphCapacity(fn, clps, dlps);
			int minNumberUnsatisfiedPreferences = cvd.ekMaxFlow(fn);
			int numSatisfiedPreferences = numberOfPreferences - minNumberUnsatisfiedPreferences;
			System.out.println(numSatisfiedPreferences);
			cvd.printTraceToStdErr(fn, clps, dlps);
		}
		
		sc.close();
	}
	
	void printTraceToStdErr(FlowNetwork fn, List<VoterPreference> clps, List<VoterPreference> dlps) {
		List<VoterPreference> satisfiedPreferences = new ArrayList<VoterPreference>();
		Integer[] d = BFS(fn).get("d");

		for (VoterPreference clp : clps) {
			if (d[fn.getIndex(clp)] < Integer.MAX_VALUE) {
				satisfiedPreferences.add(clp);
			}
		}
		
		for (VoterPreference dlp : dlps) {
			if (d[fn.getIndex(dlp)] == Integer.MAX_VALUE) {
				satisfiedPreferences.add(dlp);
			}
		}
		
		Set<String> keepMessages = new HashSet<String>();
		List<String> satisfiedMessages = new ArrayList<String>();
		for (VoterPreference satisfiedPreference : satisfiedPreferences) {
			keepMessages.add("Keeping " + satisfiedPreference.keepId);
			satisfiedMessages.add("Happy person: +" + satisfiedPreference.keepId + ",-" + satisfiedPreference.removeId);
		}
		for (String keepMessage : keepMessages) {
			System.err.println(keepMessage);
		}
		for (String satisfiedMessage : satisfiedMessages) {
			System.err.println(satisfiedMessage);
		}
		System.err.println();
	}
	
	List<VoterPreference> convertPreferences(List<String[]> preferences) {
		// parse raw preferences into VoterPreference objects
		List<VoterPreference> vps = new ArrayList<VoterPreference>();
		for (String[] preference : preferences) {
			boolean isCatLover = preference[0].charAt(0) == 'C';
			int keepNumber = Integer.parseInt(preference[0].substring(1));
			int removeNumber = Integer.parseInt(preference[1].substring(1));
			vps.add(new VoterPreference(isCatLover, keepNumber, preference[0], removeNumber, preference[1]));
		}
		return vps;
	}
	
	void initializeGraphCapacity(FlowNetwork fn, List<VoterPreference> clps, List<VoterPreference> dlps) {
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
	
	Map<String, Integer[]> BFS(FlowNetwork fn) {
		// book's pseudocode adapted to Java
		int size = fn.getSize();
		char[] color = new char[size];
		Integer[] d = new Integer[size];
		Integer[] pi = new Integer[size];
		Queue<Integer> q = new LinkedList<Integer>();	
		for (int i = 0; i < size; i++) {
			if (i == fn.getSourceIndex()) {
				color[i] = 'G';
				d[i] = 0;
			} else {
				color[i] = 'W';
				d[i] = Integer.MAX_VALUE;
			}
			pi[i] = null;
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
		
		Map<String, Integer[]> answer = new HashMap<String, Integer[]>();
		answer.put("d", d);
		answer.put("pi", pi);
		return answer;
	}
	
	List<Integer> getBFSPathFromSourceToSink(FlowNetwork fn) {

		Integer[] pi = BFS(fn).get("pi");
		
		// use pi array to find vertex indices in path from sink to source
		List<Integer> path = new LinkedList<Integer>();
		int v = fn.getSinkIndex();
		while(pi[v] != null) {
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
		int totalFlow = 0;
		List<Integer> path = getBFSPathFromSourceToSink(fn);
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
			path = getBFSPathFromSourceToSink(fn);
			totalFlow += flowAmount;
		}
		return totalFlow;
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
		String keepId;
		int removeNumber;
		String removeId;
	
		public VoterPreference(boolean isCatLover, int keepNumber, String keepId, int removeNumber, String removeId) {
			this.isCatLover = isCatLover;
			this.keepNumber = keepNumber;
			this.keepId = keepId;
			this.removeNumber = removeNumber;
			this.removeId = removeId;
		}
	}
}