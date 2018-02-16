package examples;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import org.jgrapht.graph.SimpleGraph;

import search.basic.Border;
import search.basic.ConstrainedGraphPartitioning;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.SearchConfiguration;
import util.GraphUtil;

public class InitialStateSensitivityRunsAnalysis 
{
	static int sizeOfBasicGraph = 120;
	static int initialLimitOnMaxNodesExpanded = 10;
	static int increamentInLimit = 50;
	static int afterCoarseningSize = 40;
	
	static Random rand =  new Random();
	static int numRuns = 10;
	
	static boolean silentMode = true; //Whether to draw the generated maps

	static boolean writeToCSV = true;
	static String csvFilename = "initialStateRuns.csv" ;
	static boolean appendToFile = false;
	
	public static void main(String[] args)
	{
		/*
		 * using the same constrain graph while changing the basic graph (with a deterministic partitioning algorithm)
		 * shows how the algorithm is dependent the initial state
		 * this property is seen in many combinatorial search algorithms, data collected summary is in the paper
		 * the solution is to use a Random Restart Policy, for more on this refer to : 
		 * https://www.cs.cornell.edu/gomes/pdf/1997_gomes_cp_distributions.pdf
		 */
		VoronoiGenerator generator = new VoronoiGenerator();
		GraphPartitioningState C  = GraphUtil.generateChainGraph(7);
		//Setting up the generator and generating the basic graph
		generator.setupGenerator(sizeOfBasicGraph, silentMode, false, 500, 500, false, false, false);
		//For a value of -1, the search will continue until no more memory is available or if no solution is found
		//Alternatively, a random restart policy can be implemented by updating this variable before each run. What's applied here is a fixed increment policy
		int initialNodeExpansionsThreshold = 2; 

		ArrayList<RunResult> runsArray =  new ArrayList<RunResult>();
		
		for(int i = 0; i < numRuns ; i++)
		{
			System.out.println("Run "+(i+1));
			int nodeExpansionsThreshold = initialNodeExpansionsThreshold;
			int totalNodesExpansions = 0;
			long time = System.currentTimeMillis();	

			while(true)
			{
				SimpleGraph<Node,Border> G = generator.generate(sizeOfBasicGraph,rand); //A random state
				if(nodeExpansionsThreshold >= 0)// if -1 is chosen (no random restart, this will be ignored)
				{
					nodeExpansionsThreshold *= 2; //Change this into your desired restart policy. (e.g. += 50 , *= 10, or =pow(nodeExpansionsThreshold,2)
				}
				GraphPartitioningState result = ConstrainedGraphPartitioning.partitionConstrainedWithCoarsening(new SearchConfiguration(G, C),rand, afterCoarseningSize,nodeExpansionsThreshold);	
				if(result != null)
				{
					time = System.currentTimeMillis() - time;
					totalNodesExpansions += result.getNumNodesExpanded();
					System.out.println("Total Nodes expanded = "+totalNodesExpansions + " ,Nodes expanded to get this solution only = "+result.getNumNodesExpanded());
					runsArray.add(new RunResult(totalNodesExpansions,result.getNumNodesExpanded(),result.getPathLength(),time));
					break;
				}
				totalNodesExpansions += nodeExpansionsThreshold;
			}
		}
		
		
		if(writeToCSV)
		{
			PrintWriter pw;
			try {
				
				pw = new PrintWriter(new FileOutputStream(csvFilename,appendToFile));//true for appending
			
		        StringBuilder sb = new StringBuilder();
		        if(!appendToFile)
		        {
			        sb.append("total nodes expanded");
		        	sb.append(',');
		        	sb.append("nodes expanded");
		        	sb.append(',');
		        	sb.append("path length");
		        	sb.append(',');
		        	sb.append("time (ms)");
		        	sb.append('\n');
		        }
		        for(RunResult run : runsArray)
				{
		        	sb.append(run.totalNumExpanded);
		        	sb.append(',');
		        	sb.append(run.numExpanded);
		        	sb.append(',');
		        	sb.append(run.pathCost);
		        	sb.append(',');
		        	sb.append(run.time);
		        	sb.append('\n');
				}
		        pw.write(sb.toString());
		        pw.close();
				System.out.println("#Data Writter to the file = " + csvFilename);

			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			for(RunResult run : runsArray)
			{
				System.out.println(run);
			}
		}
	}
	
	
	static class RunResult
	{
		int numExpanded,totalNumExpanded;
		double pathCost;
		long time;
		public RunResult(int totalNumExpanded,int numExpanded, double pathCost, long time)
		{
			this.totalNumExpanded = totalNumExpanded;
			this.numExpanded = numExpanded;
			this.pathCost = pathCost;
			this.time = time;
		}
		public String toString()
		{
			return totalNumExpanded+" , "+numExpanded + "  ,"+pathCost+" , "+time;
		}
		
	}
}
