This program solves the Kattis catvsdog problem that is available here: https://open.kattis.com/problems/catvsdog. It also prints trace output to stderr that identifies the animals that can be kept and the people satisfied in the optimal solution.

The graph class used in this implementation, named FlowNetwork, is quite unusual. The graph edges are stored in an adjacency matrix. The implementation actually uses two matricies, one for capacity, one for flow. These matricies completely define the graph. Additionally, an adjacencyList is kept. However, this list does not mirrror the information in the matricies. Rather, it stores nodes with residual flow that are adjacent. This is done so that the residual capacity adjacency lsit
can be returned in constant time. This design came about because iterating through the matricies to determine residual flow adjacency caused the program to time out.

Also, the FlowNetwork class includes a convenience feature that maps VoterPreferences (the internal representation of people) to an integer index. The integer is then used to index the matricies and lists that represent the graph. This is not strictly necessary, but was convenient.

Basically, the program parses the input and, for each test case, builds a list of the preferences for the voters. The voters are divided into two groups: dog lovers and cat lovers. A FlowNetwork is initialized as a bipartite graph with a soure connected to the cat lovers and the dog lovers connected to the sink. The cat lovers are connected to the dog lovers if their preferences conflict.

On this graph, we run Edmunds-Karp max flow algorithm. The max flow represents the minimum number of dissatisfied voters. Thus the maximum number of satisfied voters is the number of voters minus the max flow. To produce the trace informatoin we run BFS (which is also used by Edmunds-Karp) one last time. From this BFS, the satisfied voters are the reachable cat lovers and the unreachable dog lovers. The animals that can be kept are the preferred animals of the satisfied voters.

There are no known problems with the correctness of the code.
