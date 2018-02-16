package util;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import org.jgrapht.graph.SimpleGraph;

import examples.VoronoiGenerator.FaceNode;
import search.basic.Border;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.Partition;
import search.basic.PartitionBorder;

public class TestsUtil 
{
	static ArrayList<Color> colors;
	public static void colorizeFixed(GraphPartitioningState partitions,Color removedNodesColor)
	{	
		Partition[] pars = GraphUtil.getPartitions(partitions);
		if(colors == null)
		{
			colors = new ArrayList<Color>();
			for (int i = 0; i < pars.length; i++) 
			{			
				colors.add(randomColor(1.0f));
			}
		}
		if(colors.size() < pars.length)
		{
			for (int i = 0; i < pars.length - colors.size() ; i++) 
			{			
				colors.add(randomColor(1.0f));
			}
		}
		for (int i = 0; i < pars.length; i++) 
		{			
			Color c = colors.get(i);
			ArrayList<Node> nodes = pars[i].getMembers();
			colorize(nodes,c);
		}
		ArrayList<Node> removed = partitions.getRemoved();
		colorize(removed,removedNodesColor);
	}	
	public static void colorizeRandom(GraphPartitioningState partitions,Color removedNodesColor)
	{
		Partition[] pars = GraphUtil.getPartitions(partitions);
		for (int i = 0; i < pars.length; i++) 
		{			
			Color c = randomColor(1.0f);
			
			ArrayList<Node> nodes = pars[i].getMembers();
			colorize(nodes,c);
		}
		ArrayList<Node> removed = partitions.getRemoved();
		colorize(removed,removedNodesColor);
	}	
	/**
	 * Colors the nodes with the color, the details of how the coloring is done has to currently be added to this method.
	 * @param nodes
	 * @param color
	 */
	public static void colorize(ArrayList<Node> nodes,Color color)
	{
		for (int j = 0; j < nodes.size(); j++) 
		{			
			ArrayList<Node> arr = nodes.get(j).getSelfOrCluster();
			for(int  k= 0 ; k < arr.size();k++)
			{
				if(arr.get(k) instanceof FaceNode) // FaceNode extends Node and is the result of the VoronoiBuilder generator
				{
					FaceNode fn = (FaceNode)arr.get(k);
					fn.face.color = color;	
				}
			}				
		}	
	}
	private static Random rand = new Random();
	public static Color randomColor(float alpha) {
		return new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(),alpha);
	}
	

	/**
	 * Reads constraint graphs from a file, the format of each graph is: <br/>
	 
		 NumNodes NumEdges  <br/>
		 Si Di              <br/>
	 * 
	 * 
	 * where Si and Di are the numbers of nodes that form an edge
	 * e.g. a cycle of 3 nodes is the following, (nodes are numbered 1,2,3)<br/>
	 
	   3 3   <br/>
	   1 2   <br/>
	   2 3   <br/>
	   3 1   <br/>
	   
	 * @param filePath
	 * @return
	 */
	public static ArrayList<GraphPartitioningState> readConstraintGraphs(String filePath)
	{
		Scanner scan;
		ArrayList<GraphPartitioningState> graphs = null;
		try 
		{
			scan = new Scanner(new File(filePath));
			graphs = new ArrayList<GraphPartitioningState>();//PartitionBorder.class
			while(scan.hasNext())
			{
				GraphPartitioningState graph = new GraphPartitioningState();
				
				int n = scan.nextInt();
				int e = scan.nextInt();
				for(int i = 0 ; i < n;i++)
					graph.addVertex(new Partition(i));
				for(int i = 0 ; i < e;i++)
				{
					PartitionBorder b = new PartitionBorder(new Partition(scan.nextInt()),new Partition(scan.nextInt()));
					graph.addEdge(b.getP1(),b.getP2(),b);
				}
				graphs.add(graph);
			}
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		return graphs;
	}
	/**
	 * Reads basic graphs from a file, the format of each graph is: <br/>
	 
		 NumNodes NumEdges  <br/>
		 Si Di              <br/>
	 * 
	 * 
	 * where Si and Di are the numbers of nodes that form an edge
	 * e.g. a cycle of 3 nodes is the following, (nodes are numbered 1,2,3)<br/>
	 
	   3 3   <br/>
	   1 2   <br/>
	   2 3   <br/>
	   3 1   <br/>
	   
	 * @param filePath
	 * @return
	 */
	public static ArrayList<SimpleGraph<Node, Border>> readBasicGraphs(String filePath)
	{
		Scanner scan;
		ArrayList<SimpleGraph<Node, Border>> graphs = null;
		try 
		{
			scan = new Scanner(new File(filePath));
			graphs = new ArrayList<SimpleGraph<Node, Border>>();//PartitionBorder.class
			while(scan.hasNext())
			{
				SimpleGraph<Node, Border> graph = new SimpleGraph<Node, Border>(Border.class);
				
				int n = scan.nextInt();
				int e = scan.nextInt();
				for(int i = 0 ; i < n;i++)
					graph.addVertex(new Node(i));
				for(int i = 0 ; i < e;i++)
				{
					Border b = new Border(new Node(scan.nextInt()),new Node(scan.nextInt()));
					graph.addEdge(b.getN1(),b.getN2(),b);
				}
				graphs.add(graph);
			}
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		return graphs;
	}
}
