package examples;

import java.util.ArrayList;
import java.util.Random;

import org.jgrapht.graph.SimpleGraph;

import search.basic.Border;
import search.basic.ConstrainedGraphPartitioning;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.SearchConfiguration;
import util.GraphUtil;
import util.Util;

public class InitialStateSensitivityRunsAnalysis 
{
	static int sizeOfBasicGraph = 120;
	static int initialLimitOnMaxNodesExpanded = 10;
	static int increamentInLimit = 50;
	static int afterCoarseningSize = 40;
	
	static Random rand =  new Random();
	static int numRuns = 10;
	
	public static void main(String[] args)
	{
		/*
		 * using the same constrain graph while changing the basic graph (with a deterministic partitioning algorithm)
		 * shows how the algorithm is dependent the initial state
		 * this property is seen in many combinatorial search algorithms, data collected summary is in the paper
		 * the solution is to use a Random Restart Policy
		 * 
		 */
		VoronoiGenerator generator = new VoronoiGenerator();
		GraphPartitioningState C  = GraphUtil.generateChainGraph(5);
		//Setting up the generator and generating the basic graph
		generator.setupGenerator(sizeOfBasicGraph, true, false, 500, 500, false, false, false);
		
		ArrayList<RunResult> runsArray =  new ArrayList<RunResult>();
		
		for(int i = 0; i < numRuns ; i++)
		{
			System.out.println("Run "+(i+1));
			long time = System.currentTimeMillis();	
			SimpleGraph<Node,Border> G = generator.generate(sizeOfBasicGraph,rand);
			GraphPartitioningState result = ConstrainedGraphPartitioning.partitionConstrainedWithCoarsening(new SearchConfiguration(G, C),rand, afterCoarseningSize,-1);	
			time = System.currentTimeMillis() - time;
			if(result != null)
				runsArray.add(new RunResult(sizeOfBasicGraph,result.getNumNodesExpanded(),result.getPathLength(),time));			
		}
		System.out.println("***Data***");
		
		//Suggestion, put the data in a spreadsheets program
		for(RunResult run : runsArray)
		{
			System.out.println(run);
		}
		
	}
	
	
	static class RunResult
	{
		int numExpanded,numNodes;
		double pathCost;
		long time;
		public RunResult(int numNodes,int numExpanded, double pathCost, long time)
		{
			this.numNodes = numNodes;
			this.numExpanded = numExpanded;
			this.pathCost = pathCost;
			this.time = time;
		}
		public String toString()
		{
			return numNodes+" "+numExpanded + " "+pathCost+" "+time;
		}
		
	}
}
