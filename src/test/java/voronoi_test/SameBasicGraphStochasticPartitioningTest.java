package voronoi_test;

import java.awt.Color;
import java.util.Random;

import org.jgrapht.graph.SimpleGraph;

import search.basic.Border;
import search.basic.ConstrainedGraphPartitioning;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.SearchConfiguration;
import util.GraphUtil;
import util.TestsUtil;

public class SameBasicGraphStochasticPartitioningTest {

	static int sizeOfBasicGraph = 200;
	static int initialLimitOnMaxNodesExpanded = 10;
	static int increamentInLimit = 50;
	static int afterCoarseningSize = 40;
	
	static Random rand =  new Random();
	public static void main(String[] args) 
	{
		VoronoiGenerator generator = new VoronoiGenerator();
		//Generating the constrain graph
		final GraphPartitioningState C  = GraphUtil.generateChainGraph(7);
		GraphPartitioningState result = null;
		//Setting up the generator and generating the basic graph
		generator.setupGenerator(sizeOfBasicGraph, true, false, 500, 500, true, true, false);
		SimpleGraph<Node,Border> G = generator.generate(sizeOfBasicGraph,rand);
		
		result = ConstrainedGraphPartitioning.partitionConstrainedWithRandomRestart(new SearchConfiguration(G, C),rand, initialLimitOnMaxNodesExpanded, increamentInLimit, afterCoarseningSize);	
		System.out.println("Result Found");
		System.out.println(result);
		TestsUtil.colorizeRandom(result,Color.WHITE);
		
	}

}
