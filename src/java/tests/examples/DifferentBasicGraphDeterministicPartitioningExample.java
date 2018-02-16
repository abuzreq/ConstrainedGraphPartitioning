package examples;

import java.awt.Color;
import java.util.Random;

import search.basic.ConstrainedGraphPartitioning;
import search.basic.GraphPartitioningState;
import search.basic.SearchConfiguration;
import util.GraphUtil;
import util.TestsUtil;

public class DifferentBasicGraphDeterministicPartitioningExample {

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
		//Setting up the generator
		generator.setupGenerator(sizeOfBasicGraph, true, false, 500, 500, true, true, false);

		//Note that we pass the generator as well. An initial basic graph can be passed or set to null
		result = ConstrainedGraphPartitioning.partitionConstrainedWithCoarseningAndRandomRestart(new SearchConfiguration(null, C),sizeOfBasicGraph,generator,rand, initialLimitOnMaxNodesExpanded, increamentInLimit, afterCoarseningSize);	
		System.out.println("Result Found");
		TestsUtil.colorizeRandom(result,Color.WHITE);
	}

}
