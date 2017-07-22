package voronoi_test;
import java.util.Random;

import org.jgrapht.graph.SimpleGraph;

import search.basic.Border;
import search.basic.ConstrainedGraphPartitioning;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.SearchConfiguration;
import util.GraphUtil;
import util.Util;

public class Test {

	static int seed;
	static Random rand =  new Random();
	public static void main(String[] args) throws InterruptedException
	{
		VoronoiBuilder builder = new VoronoiBuilder();
		//GraphUtil.generateChainTree(8);
		final GraphPartitioningState C  = GraphUtil.generateChainGraph(8);//Util.readGraphs("src/test/java/test_graphs/benchmark_1.in").get(0);
		GraphPartitioningState result = null;
		seed = rand.nextInt(Integer.MAX_VALUE);
		SimpleGraph<Node,Border> G = builder.generateOffline(200, true, false, 500, 500, false, 1000, true,seed, true, false);
		//result = ConstrainedGraphPartitioning.partitionConstrainedWithRandomRestart(new SearchConfiguration(G, C),builder,rand,10,50,40);
		result = ConstrainedGraphPartitioning.partitionConstrainedWithRandomRestart(new SearchConfiguration(G, C),rand,10,50,40);
		System.out.println(result.getRemoved());
		Util.colorize(result,C);
	
	}


}
