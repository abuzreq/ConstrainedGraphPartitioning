package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.Graph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.alg.isomorphism.IsomorphicGraphMapping;
import org.jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.graph.ListenableUndirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import com.trickl.graph.PartitionByKernelClustering;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import aima.core.agent.Action;
import search.actions.NodeRemovalAction;
import search.actions.PartitionChangeAction;
import search.basic.Border;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.Partition;
import search.basic.PartitionBorder;
import search.basic.PartitionNodePair;
import search.enums.PartitioningType;

public class GraphUtil
{
	private static Random secondaryRand = new Random();

	/**
	 * 
	 * @param initialState
	 * @param G
	 * @param actions
	 * Applies the list of actions on the initialState and returns the resulting state
	 */
	public static GraphPartitioningState applyActions(GraphPartitioningState initialState,SimpleGraph<Node,Border> G,List<Action> actions)
	{
		GraphPartitioningState result = initialState;
		for (int a = 0; a < actions.size(); a++) 
		{
			if (actions.get(a) instanceof PartitionChangeAction) {
				PartitionChangeAction act = (PartitionChangeAction) actions.get(a);
				result = GraphUtil.createHypotheticalGraph(result, G, act.getNode(), act.getPartition());

			} 
			else if (actions.get(a) instanceof NodeRemovalAction) 
			{
				NodeRemovalAction act = (NodeRemovalAction) actions.get(a);				
				result = GraphUtil.createHypotheticalGraph(result, G, act.getNodeToBeRemoved());

			}		
		}
		return result;
	}
	/**
	 * 
	 * @param edges1
	 * @param edges2
	 * @return Whether the two sets of PartitionBorder edges are equal regardless of their direction
	 */
	public static boolean areEdgesEqualIgnoreDirection(Set<PartitionBorder> edges1, Set<PartitionBorder> edges2) 
	{
		if(edges1.size() != edges2.size())return false;
		for(PartitionBorder pb1 : edges1)
		{
			boolean flag = false;
			for(PartitionBorder pb2 : edges2)
			{
				if(((pb1.getP1().equals(pb2.getP1()) && pb1.getP2().equals(pb2.getP2()) )||( pb1.getP2().equals(pb2.getP1()) && pb1.getP1().equals(pb2.getP2()))))
				{
					flag =  true;
					break;
				}
			}
			if(!flag)
				return false;		
		}
		return true;
	}
	public static int sizeOf(SimpleGraph graph) {
		return graph.vertexSet().size();
	}
	public static int numEdgesOf(SimpleGraph graph) {
		return graph.edgeSet().size();
	}
	/**	
	 * @return The edges which <b>node</b> is part of in the graph <b>G</b>
	 */
	public static Set<PartitionBorder> getEdgesOf(GraphPartitioningState G, Partition node) 
	{
		Set<PartitionBorder> edges  = new HashSet<PartitionBorder>();
		for(PartitionBorder pb : G.edgeSet())
		{
			if(pb.getP1().equals(node) ||pb.getP1().equals(node) )
				edges.add(pb);
		}
		return edges;
	}
	/**
	 * @param G
	 * @return An array of the nodes of the quotient graph <b>G</b>
	 */
	public static Partition[] getPartitions(Graph<Partition, PartitionBorder> G) {
		Set<Partition> vertexSet = G.vertexSet();
		Partition[] pars = new Partition[vertexSet.size()];
		vertexSet.toArray(pars);
		return pars;
	}
	/**
	 * @param G
	 * @return An ArrayList of the nodes of the quotient graph  <b>G</b>
	 */
	public static ArrayList<Partition> getPartitionsArrayList(Graph<Partition, PartitionBorder> G) {
		Set<Partition> vertexSet = G.vertexSet();		
		return new ArrayList<Partition>(vertexSet);
	}
	/**
	 * @param G
	 * @return An array of the nodes of the basic graph  <b>G</b>
	 */
	public static Node[] getNodes(Graph<Node, Border> G) {
		Set<Node> vertexSet = G.vertexSet();
		Node[] pars = new Node[vertexSet.size()];
		vertexSet.toArray(pars);
		return pars;
	}
	/**
	 * @param G
	 * @return An ArrayList of the nodes of the basic graph  <b>G</b>
	 */
	public static ArrayList<Node> getNodesArrayList(Graph<Node, Border> G) {
		Set<Node> vertexSet = G.vertexSet();		
		return new ArrayList<Node>(vertexSet);
	}
	
	/**
	 * @param G
	 * @return An array of the edges of the quotient graph <b>G</b>
	 */
	public static PartitionBorder[] getPartitionsBorders(SimpleGraph<Partition, PartitionBorder> G) {
		Set<PartitionBorder> vertexSet = G.edgeSet();
		PartitionBorder[] pars = new PartitionBorder[vertexSet.size()];
		vertexSet.toArray(pars);
		return pars;
	}
	/**
	 * @param G
	 * @return An array of the edges of the basic graph <b>G</b>
	 */
	public static Border[] getBorders(SimpleGraph<Node, Border> G) {
		Set<Border> vertexSet = G.edgeSet();
		Border[] pars = new Border[vertexSet.size()];
		vertexSet.toArray(pars);
		return pars;
	}
	
	/**
	 * 
	 * @param node
	 * @param graph
	 * @return whether a path will exist between any pair of the neighbors of
	 *         the passed node after its removal from the graph G.Where G is the graph induced by the partition to which the node belongs
	 */
	public static boolean isCreatingAGap(Node node, SimpleGraph<Node, Border> graph) 
	{
		ListenableUndirectedGraph<Node, Border> parGraph = buildPartitionListenableGraph(node.getContainer(), graph);
		NeighborIndex<Node, Border> ni = new NeighborIndex<Node, Border>(parGraph);
		List<Node> adjacents = ni.neighborListOf(node);
		ConnectivityInspector<Node, Border> inspector = new ConnectivityInspector<Node, Border>(parGraph);
		parGraph.addGraphListener(inspector);
		parGraph.removeVertex(node);

		for (int i = 0; i < adjacents.size(); i++) {
			for (int j = 0; j < adjacents.size(); j++) {
				if (i != j) {
					if (!inspector.pathExists(adjacents.get(i), adjacents.get(j))) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Builds a listenable graph based on the adjacency of the members of the
	 * passed partition,deciding if an edge exist between any two members will
	 * be based on the passed graph
	 * 
	 * @param par
	 * @param graph
	 * @return a listenable graph
	 */
	public static ListenableUndirectedGraph<Node, Border> buildPartitionListenableGraph(Partition par, SimpleGraph<Node, Border> graph) {
		ListenableUndirectedGraph<Node, Border> parGraph = new ListenableUndirectedGraph<Node, Border>(Border.class);
		ArrayList<Node> members = par.getMembers();
		for (int i = 0; i < members.size(); i++) 
		{
			parGraph.addVertex(members.get(i));
		}
		for (int i = 0; i < members.size(); i++) {
			for (int j = 0; j < members.size(); j++) {
				if (i != j) {
					if (graph.containsEdge(members.get(i), members.get(j))) {
						parGraph.addEdge(members.get(i), members.get(j), new Border(members.get(i), members.get(j)));
					}
				}
			}
		}
		return parGraph;
	}
	/***
	 * The Eigenvalue method for comparing graphs similarity as described in
	 	"Danai Koutra, Ankur Parikh, Aaditya Ramdas, and Jing Xiang. 2011. Algorithms
		for Graph Similarity and Subgraph Matching. (2011)."
	 * @param GS
	 * @param wantedGS
	 * @return
	 */
	public static double laplacianSpectralError(SimpleGraph<Partition, PartitionBorder> graph, SimpleGraph<Partition, PartitionBorder> wantedGraph) 
	{
		double[][] adj1 = toAdjacencyMatrix(graph);
		double[][] deg1 = degreeMatrix(graph);
		double[][] lap1 = laplacianMatrix(deg1, adj1);

		double[][] adjWanted = toAdjacencyMatrix(wantedGraph);
		double[][] degWanted = degreeMatrix(wantedGraph);
		double[][] lapWanted = laplacianMatrix(degWanted, adjWanted);

		Matrix B = new Matrix(lapWanted);
		EigenvalueDecomposition eigWanted = B.eig();
		
		Matrix A = new Matrix(lap1);
		EigenvalueDecomposition eigA = A.eig();

		double error = 0;
		double[] a = eigA.getRealEigenvalues();
		double[] b = eigWanted.getRealEigenvalues();
		
		double prodA = a[0];
		double energyA = Util.sum(a);

		int k1 = 1;
		while (prodA / energyA < 0.9 && k1 < a.length) {
			prodA += a[k1];
			k1++;
		}

		double prodB = b[0];
		double energyB = Util.sum(b);
		int k2 = 1;
		while (prodB / energyB < 0.9 && k2 < b.length) {
			prodB += b[k2];
			k2++;
		}
		int k = Math.min(k1, k2);

		if (prodA < prodB)
			k = k1;
		else
			k = k2;

		for (int r = lap1.length - k; r < lap1.length; r++) {
			error += Math.pow(a[r] - b[r], 2);
		}
		return error;	
	}
	
	public static double[][] toAdjacencyMatrix(SimpleGraph graph) {
		Set set1 = graph.vertexSet();
		Iterator it1 = set1.iterator();
		double[][] adj = new double[set1.size()][set1.size()];
		int i = 0, j = 0;
		while (it1.hasNext()) {
			Object p1 = it1.next();
			Set set2 = graph.vertexSet();
			Iterator it2 = set2.iterator();
			j = 0;
			while (it2.hasNext()) {
				Object p2 = it2.next();
				if (graph.containsEdge(p1, p2)) {
					adj[i][j] = 1;
				} else {
					adj[i][j] = 0;
				}
				j++;
			}
			i++;
		}
		return adj;
	}

	public static double[][] laplacianMatrix(double[][] deg, double[][] adj) {
		double[][] laplacian = new double[deg.length][deg[0].length];
		for (int r = 0; r < laplacian.length; r++) {
			for (int c = 0; c < laplacian[r].length; c++) {
				laplacian[r][c] = deg[r][c] - adj[r][c];
			}
		}
		return laplacian;
	}
	public static double[][] degreeMatrix(SimpleGraph graph) {
		Set set = graph.vertexSet();
		Iterator it = set.iterator();

		double[][] deg = new double[set.size()][set.size()];
		int i = 0, j = 0;
		while (it.hasNext()) {
			Object p = it.next();
			deg[i][j] = graph.degreeOf(p);
			i++;
			j++;
		}
		return deg;
	}
	/**
	 * 
	 * @param graph
	 * @param node
	 * @return An array of nodes that are adjacent to the passed node in the graph
	 */
	public static Node[] adjacentsOf(SimpleGraph<Node, Border> graph, Node node) 
	{
		
		ArrayList<Border> edges = new ArrayList<Border>(graph.edgesOf(node));
		Node[] adjacents  = new Node[edges.size()];
		for(int i =0 ; i < edges.size();i++)
		{
			adjacents[i] = edges.get(i).getN1().equals(node)?edges.get(i).getN2():edges.get(i).getN1();
		}
		return adjacents;
	}
	/**
	 * 
	 * @param graph
	 * @param par
	 * @param node
	 * @return an adjacent node to the passed node among the members of the partition (adjacency described by the graph)
	 */
	public static Node adjacentInMembersTo(SimpleGraph<Node, Border> graph, Partition par, Node node)
	{
		return adjacentInMembersTo(graph,par.getMembers(),node);
	}
	public static Node adjacentInMembersTo(SimpleGraph<Node, Border> graph, ArrayList<Node> partitionMembers, Node node)
	{
		for (int i = 0; i < partitionMembers.size(); i++) {
			if (graph.containsEdge(partitionMembers.get(i), node))
				return partitionMembers.get(i);
		}
		return null;
	}
	/**
	 * 
	 * @param graph
	 * @param numPartitions
	 * @param partType
	 * @param rand
	 * @param randomBase
	 * @return a simple partitioning of the passed graph into numPartitions partitions. Using the algorithm indicated by the passed enum partType. 
	 * randomBase indicates whether the newly created partitions' numbers will be start from 0 or from a random number.
	 * The RNG rand is used by the stochastic partitioning algorithms
	 */
	public static GraphPartitioningState partition(SimpleGraph<Node, Border> graph, int numPartitions, PartitioningType partType, Random rand,boolean randomBase) {
		if (partType == PartitioningType.KERNEL_DETERMINISTIC)
			return partitionKernelClustering(graph, numPartitions,randomBase);
		else if (partType == PartitioningType.KMEANS_STOCHASTIC)
			return partitionKMeans(graph, numPartitions, rand,randomBase);
		return null;
	}
	/**
	 * Obtaines a graph partitioning of the passed graph using the algorithm described in:
	 * "Luh Yen, Francois Fouss, Christine Decaestecker, Pascal Francq, and Marco Saerens. 2007. 
		 Graph Nodes Clustering Based on the Commute-Time Kernel. Springer Berlin Heidelberg, Berlin, Heidelberg,
		 1037–1045. https://doi.org/10.1007/978-3-540-71701-0_117"
		 and implemented by https://github.com/trickl/trickl-graph
	 * @param graph
	 * @param numPartitions
	 * @param randomBase
	 */
	public static GraphPartitioningState partitionKernelClustering(SimpleGraph<Node, Border> graph, int numPartitions,boolean randomBase) {
		GraphPartitioningState partitionsGraph = new GraphPartitioningState();
		PartitionByKernelClustering<Node, Border> partioner = new PartitionByKernelClustering<Node, Border>();

		partioner.partition(graph, numPartitions);

		Map<Node, Integer> map = partioner.getPartition();
		Iterator<Entry<Node, Integer>> it = map.entrySet().iterator();

		int base = secondaryRand.nextInt(Integer.MAX_VALUE);
		Partition[] pars = new Partition[numPartitions];
		for (int i = 0; i < pars.length; i++) {
			if(randomBase)
				pars[i] = new Partition(base+i);
			else
				pars[i] = new Partition(i);

		}
		while (it.hasNext()) {
			Entry<Node, Integer> e = it.next();
			pars[e.getValue()].addMember(e.getKey());
		}
		for (int i = 0; i < pars.length; i++) {
			partitionsGraph.addVertex(pars[i]);
		}
		buildQuotientGraph(partitionsGraph, graph);
		return partitionsGraph;
	}

	/**
	 * Finds the partition that the passed node belong to its members
	 * 
	 * @param node
	 * @param partitions
	 * @return a PartitionNodePair of the passed node with the found partition
	 *         if any otherwise returns null
	 */
	public static PartitionNodePair findPartition(Node node, Partition[] partitions) 
	{
		for (int i = 0; i < partitions.length; i++) {
			int index = partitions[i].getMembers().indexOf(node);
			if (index != -1) {
				return new PartitionNodePair(partitions[i], partitions[i].getMembers().get(index));
			}
		}
		return null;
	}
	
	public static GraphPartitioningState generateChainGraph(int n) 
	{	
		GraphPartitioningState graph = new GraphPartitioningState();
		if(n == 0)
			return graph;
		int i = 0;
		Partition previous = new Partition(i);
		graph.addVertex(previous);
		for(i = 1 ; i < n;i++)
		{
			Partition node = new Partition(i);
			graph.addVertex(node);
			PartitionBorder border =  new PartitionBorder(previous,node);
			graph.addEdge(border.getP1(),border.getP2(),border);
			previous = node;		
		}
		return graph;
	}
	public static GraphPartitioningState generateCycleGraph(int n) 
	{		
		GraphPartitioningState graph = new GraphPartitioningState();
		if(n == 0)
			return graph;
		int i = 0;
		Partition previous = new Partition(i);
		Partition first = previous;
		graph.addVertex(previous);
		for(i = 1 ; i < n;i++)
		{
			Partition node = new Partition(i);
			graph.addVertex(node);
			PartitionBorder border =  new PartitionBorder(previous,node);
			graph.addEdge(border.getP1(),border.getP2(),border);
			previous = node;		
		}
		PartitionBorder border =  new PartitionBorder(previous,first);
		graph.addEdge(border.getP1(),border.getP2(),border);
		return graph;
	}
	/**
	 * 
	 * Convert the partitioning into another graph where the nodes are Node instead of Partition and where the members are part of the Node.cluster
	 * . Neighbors are NOT copied
	 * @param oldGraph
	 * @return Quotient Graph --> Basic Graph
	 */
	public static SimpleGraph<Node, Border> partitionToNodeGraph(SimpleGraph<Partition, PartitionBorder> oldGraph) {
		SimpleGraph<Node, Border> newGraph = new SimpleGraph<Node, Border>(Border.class);
		Partition[] pars = GraphUtil.getPartitions(oldGraph);
		PartitionBorder[] borders = GraphUtil.getPartitionsBorders(oldGraph);
		HashMap<Partition, Node> hashMap = new HashMap<Partition, Node>();
		for (int i = 0; i < pars.length; i++) 
		{		
			Node n = new Node(i, pars[i].getMembers());
			newGraph.addVertex(n);
			hashMap.put(pars[i], n);
		}
		for (int i = 0; i < borders.length; i++) {
			Border b = new Border(hashMap.get(borders[i].getP1()), hashMap.get(borders[i].getP2()));
			newGraph.addEdge(b.getN1(), b.getN2(), b);
		}
		return newGraph;
	}

	/**
	 * Obtains a graph partitioning of the passed graph. The algorithm is similar to KMeans (with K = 1) clustering and is stochastic (i.e. return a different partitioning per call for the same passed graph)
	 * @param graph
	 * @param numPartitions
	 * @param r
	 * @param randomBase
	 */
	public static GraphPartitioningState partitionKMeans(SimpleGraph<Node, Border> graph, int numPartitions, Random r,boolean randomBase)
	{
		GraphPartitioningState partitionsGraph = new GraphPartitioningState();
		ArrayList<Node> nodes =  GraphUtil.getNodesArrayList(graph);
		ArrayList<Integer> taken = generateRandomWithoutReplacment(r, nodes.size(), numPartitions);

		// Create partitions and initialize them with the randomly chosen nodes
		Partition[] partitions = new Partition[numPartitions];
		int base = 0;
		if(randomBase)
			base = secondaryRand.nextInt(Integer.MAX_VALUE);
		//assign the seeds to partitions
		for (int i = 0; i < partitions.length; i++) {
			partitions[i] = new Partition(base+i);
			partitions[i].addMember(nodes.get(taken.get(i)));
		}
		// assign each vertex to one of the partitions based on min path length
		for (int i = 0; i < nodes.size(); i++) {
			int closest = -1;
			int length = 0, minLength = Integer.MAX_VALUE;
			if (!taken.contains(i)) {
				for (int j = 0; j < taken.size(); j++) {
					List<Border> lst = BellmanFordShortestPath.findPathBetween(graph, nodes.get(i), nodes.get(taken.get(j))).getEdgeList();
					if (lst == null)// there is a vertex that has no path to any
									// of the means centers
					{
						System.out.println("Some vertices have no path to their assigned center");
					} else
						length = lst.size();
					if (length < minLength) {
						minLength = length;
						closest = j;
					}

				}
				partitions[closest].addMember(nodes.get(i));
			}
		}
		for (int i = 0; i < partitions.length; i++) 
		{
			partitionsGraph.addVertex(partitions[i]);
		}		
	
		buildQuotientGraph(partitionsGraph, graph);
		return partitionsGraph;
	}
	/**
	 * Returns a numSamples number of random integers between 0 and the passed
	 * size of the population. Based of Fisher-Yates shuffle.
	 * @param rand the RNG to be used
	 * @param numSamples
	 */
	private static ArrayList<Integer> generateRandomWithoutReplacment(Random rand, int numPopulation, int numSamples) {
		int[] nums = new int[numPopulation];
		for (int i = 0; i < numPopulation; i++) {
			nums[i] = i;
		}
		int j = 0;
		ArrayList<Integer> samples = new ArrayList<Integer>(numSamples);
		for (int i = 0; i < numSamples - 1; i++) {
			j = i + rand.nextInt(numPopulation - i);

			int tmp = nums[i];
			nums[i] = nums[j];
			nums[j] = tmp;
		}
		for (int i = 0; i < numSamples; i++) {
			samples.add(nums[i]);
		}
		return samples;
	}
	/**
	 * Creates and returns a new quotient graph that is the result of removing the
	 * passed node from the passed basic graph G. This method
	 * doesn't change the passed graphs but returns a fresh copy.
	 * @param quotientGraph
	 * @param basicGraph
	 * @param node
	 */
	public static GraphPartitioningState createHypotheticalGraph(GraphPartitioningState quotientGraph, SimpleGraph<Node, Border> basicGraph, Node node) {
		GraphPartitioningState newQuotientGraph = new GraphPartitioningState();
		// To preserve the removed nodes in the new hypothetical graph
		newQuotientGraph.addAllToRemoved(quotientGraph.getRemoved());

		Partition[] oldPars = GraphUtil.getPartitions(quotientGraph);
		newQuotientGraph.addToRemoved(node);
		for (int p = 0; p < oldPars.length; p++) {
			Partition tmp =oldPars[p].clone();
			int index = tmp.getMembers().indexOf(node);
			if (index != -1) {
				tmp.getMembers().remove(index);
			}
			newQuotientGraph.addVertex(tmp);

		}
		buildQuotientGraph(newQuotientGraph, basicGraph);
		return newQuotientGraph;
	}	
	/**
	 * Creates and returns a new quotient graph that is the result of joining the
	 * node newNeighbor (which belongs to the basicGraph) to the passed Partition newHost (newHost belongs to the passed quotientGraph). This method
	 * doesn't change the passed graphs but returns a fresh copy
	 * 
	 * @param partitionsGraph
	 * @param graph
	 * @param newNeighbor
	 * @param newHost
	 */
	public static GraphPartitioningState createHypotheticalGraph(			
	GraphPartitioningState quotientGraph, SimpleGraph<Node, Border> basicGraph, Node newNeighbor, Partition newHost) {
		GraphPartitioningState newQuotientGraph = new GraphPartitioningState();
		newQuotientGraph.addAllToRemoved(quotientGraph.getRemoved());

		// get the partitions before changes
		Partition[] oldPars = GraphUtil.getPartitions(quotientGraph);

		// copy the partitions to a new graph,adding the newNeighbor to the
		// appropriate partition
		Partition newHostCopy = null;
		Node newNeighborCopy = null;
		for (int p = 0; p < oldPars.length; p++) {
			Partition tmp = oldPars[p].clone();

			// if this is the partition that used to have the node , then remove
			// the node from the partition's copy (tmp)
			// Don't change the container now because the new
			// container\partition might have not been visited yet
			if (oldPars[p].equals(newNeighbor.getContainer())) {
				newNeighborCopy = tmp.removeMemberLeaveContainer(newNeighbor.getValue());
			}
			// if this is the partition that is supposed to have the node , then
			// add the node to the partition's copy (tmp)
			if (oldPars[p].equals(newHost)) {
				// keep a reference so as to assign the node to it later
				newHostCopy = tmp;
			}

			// adds the partition copy to the hypothetical graph
			newQuotientGraph.addVertex(tmp);
		}

		// This will affect the vertex already in the graph(g)
		newHostCopy.addMember(newNeighborCopy);

		// Recreate graph again using the newly created partitions
		buildQuotientGraph(newQuotientGraph, basicGraph);
		return newQuotientGraph;
	}
	
	/**
	 * Updates the neighbors and members arrays of the passed Partition par when the passed node is removed from its members
	 * @param graph
	 * @param par
	 * @param node
	 */
	public static void removeNodeFromMembersUpdatePartition(SimpleGraph<Node,Border> graph,Partition par,Node node)
	{
		par.removeMember(node);
		Node[] adjacents = adjacentsOf(graph,node);
		ArrayList<Node> updatedMembers = par.getMembers();
		//The adjacents are either members of the container or are members of
		//other partitions to which we need to assert whether after the node removal they are still neighbors to the container
		for(int i = 0 ; i < adjacents.length;i++)
		{
			if(!updatedMembers.contains(adjacents[i])) // This adjacent to the toBeRemovedNode belongs to another partition
			{
				if(adjacentInMembersTo(graph,updatedMembers,node) == null)
				{
					par.removeNeighbor(adjacents[i]);
				}
			}				
		}
	}
	/**
	 * Updates the neighbors and members arrays of the passed Partition par when the passed node is added to its members
	 * @param graph
	 * @param par
	 * @param node
	 */
	public static void addNodeToMembersUpdatePartition(SimpleGraph<Node,Border> graph,Partition par,Node node)
	{
		par.addMember(node);
		par.removeNeighbor(node);
		Node[] adjacents = adjacentsOf(graph,node);
		ArrayList<Node> updatedMembers = par.getMembers();
		//The adjacents are either members of the container or are members of
		//other partitions to which we need to assert whether after the node addition they become neighbors to the container
		for(int i = 0 ; i < adjacents.length;i++)
		{
			if(!updatedMembers.contains(adjacents[i])) // This adjacent to the toBeRemovedNode belongs to another partition
			{
				if(adjacentInMembersTo(graph,updatedMembers,node) == null)
				{
					par.addNeighbor(adjacents[i]);
				}
			}				
		}
	}

	/**
	 * Given a quotientGraph with only vertices (i.e. partitions) and a basicGraph, creates edges in the quotientGraph between the partitions 
	 * whose members are adjacent as dictated by the basicGraph. The neighbors array of the partitions are filled as well.
	 * @param quotientGraph
	 * @param basicGraph
	 */
	public static void buildQuotientGraph(GraphPartitioningState quotientGraph, SimpleGraph<Node, Border> basicGraph) 
	{
		final Partition[] newPars = GraphUtil.getPartitions(quotientGraph);
		for (int p = 0; p < newPars.length; p++) 
		{		
			// get all the borders of the members
			TreeSet<Border> set = new TreeSet<Border>();
			for (int m = 0; m < newPars[p].getMembers().size(); m++) {
				set.addAll(basicGraph.edgesOf(newPars[p].getMembers().get(m)));
			}

			// find if any border starts and ends in two different partitions,
			// if so ,add to the edges
			Iterator<Border> it = set.iterator();
			while (it.hasNext()) {
				Border bor = it.next();
				if (quotientGraph.removedContains(bor.getN1()) || quotientGraph.removedContains(bor.getN2()))
					continue;
				
				PartitionNodePair pair1 = GraphUtil.findPartition(bor.getN1(), newPars);
				PartitionNodePair pair2 = GraphUtil.findPartition(bor.getN2(), newPars);

				if (!pair1.getContainer().equals(pair2.getContainer())) 
				{
					PartitionBorder parBorder = new PartitionBorder(pair1.getContainer(), pair2.getContainer());
					quotientGraph.addEdge((Partition) parBorder.getP1(), (Partition) parBorder.getP2(), parBorder);
					// /above we made sure the edge link two different
					// partitions,
					// /next we add the node to the neighbors if its container
					// is not the partition
					if (pair1.getContainer().equals(newPars[p])) {
						pair2.getContainer().addNeighbor(pair1.getNode());
						newPars[p].addNeighbor(pair2.getNode());
					}
					else // then newPars[p] is the container of getN2()
					{
						newPars[p].addNeighbor(pair1.getNode());
						pair1.getContainer().addNeighbor(pair2.getNode());
					}
				}
			}

		}
	}
		
	
	public static IsomorphicGraphMapping<Partition, PartitionBorder> getMapping(GraphPartitioningState graph, GraphPartitioningState wanted) {
		VF2GraphIsomorphismInspector<Partition, PartitionBorder> inspector = new VF2GraphIsomorphismInspector<Partition, PartitionBorder>(graph,wanted);
		if(inspector.isomorphismExists())
		{
			Iterator it = inspector.getMappings();
			IsomorphicGraphMapping<Partition, PartitionBorder> isomorphisimMapping = (IsomorphicGraphMapping<Partition, PartitionBorder>) it.next();
			
			return isomorphisimMapping;
		}
		else
		{
			return null;
		}
		
	}
	public static Object nextState(GraphPartitioningState s, Action a,SimpleGraph<Node, Border> basicGraph) 
	{
		if (a instanceof PartitionChangeAction) 
		{
			PartitionChangeAction action = ((PartitionChangeAction) a);				
			GraphPartitioningState o = GraphUtil.createHypotheticalGraph((GraphPartitioningState) s, basicGraph, action.getNode(), action.getPartition());
			return o;
		} 
		else if (a instanceof NodeRemovalAction)
		{
			NodeRemovalAction action = ((NodeRemovalAction) a);
			GraphPartitioningState o = GraphUtil.createHypotheticalGraph((GraphPartitioningState) s, basicGraph, action.getNodeToBeRemoved());
			return o;
		}
		return null;
	}
}
