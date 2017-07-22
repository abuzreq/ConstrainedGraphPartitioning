package util;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import search.*;
import search.actions.PartitionChangeAction;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.Partition;
import search.basic.PartitionBorder;
import voronoi_test.VoronoiBuilder.FaceNode;

public class Util 
{

	public static double[] unit(double[] vector) {
		return times(vector, 1.0 / magnitude(vector));
	}

	public static double magnitude(double[] nums) {
		return Math.sqrt(dot(nums, nums));
	}

	public static double[] times(double[] data, double factor) {
		double[] c = new double[data.length];
		for (int i = 0; i < data.length; i++)
			c[i] = factor * data[i];
		return c;
	}

	public static double dot(double[] a, double[] b) {
		double result = 0.0;
		for (int r = 0; r < a.length; r++) {
			result += a[r] * b[r];
		}
		return result;
	}

	public static double product(double[] a) {
		double result = 1;
		for (int r = 0; r < a.length; r++) {
			result *= a[r];
		}
		return result;
	}

	public static double mean(ArrayList<Double> nums) {
		double sum = 0;
		for (int i = 0; i < nums.size(); i++) {
			sum += nums.get(i);
		}
		return sum / nums.size();
	} 

	public static double deviation(ArrayList<Double> nums) {
		double mean = mean(nums);
		double squareSum = 0;
		for (int i = 0; i < nums.size(); i++) {
			squareSum += Math.pow(nums.get(i) - mean, 2);
		}
		return Math.sqrt((squareSum) / (nums.size() - 1));
	}
	public static int sum(int[] arr) 
	{
		int sum = 0 ;
		for(int i =0 ; i < arr.length;i++)
		{
			sum += arr[i];
		}
		return sum;
	}
	public static float sum(float[] arr) 
	{
		float sum = 0 ;
		for(int i =0 ; i < arr.length;i++)
		{
			sum += arr[i];
		}
		return sum;
	}

	public static double sum(double[] arr) {
		float sum = 0 ;
		for(int i =0 ; i < arr.length;i++)
		{
			sum += arr[i];
		}
		return sum;		
	}
	public static Partition search(Partition[] arr, Partition obj) {
		for(int i = 0 ; i <arr.length;i++)
		{
			if(arr[i].equals(obj))
				return arr[i];
		}
		return null;
	}
	public static void colorize(GraphPartitioningState partitions,GraphPartitioningState wantedGraph)
	{
		Partition[] pars = GraphUtil.getPartitions(partitions);
		System.out.println(Arrays.deepToString(pars));
		for (int i = 0; i < pars.length; i++) 
		{			
			Color c = randomColor(1.0f);
			
			ArrayList<Node> nodes = pars[i].getMembers();

			for (int j = 0; j < nodes.size(); j++) 
			{						
				if(nodes.get(j).isClusterEmpty())
				{
					if(nodes.get(j) instanceof FaceNode)
					{
						FaceNode fn = ((FaceNode) nodes.get(j));
						fn.face.color = c;								
					}					
				}
				else
				{
					for(int  k= 0 ; k < nodes.get(j).getCluster().size();k++)
					{
						if(nodes.get(j).getCluster().get(k) instanceof FaceNode)
						{
							FaceNode fn = (FaceNode)nodes.get(j).getCluster().get(k);
							fn.face.color = c;	
						}
					}
				}
			}
		}
		ArrayList<Node> removed = partitions.getRemoved();
		for (int j = 0; j < removed.size(); j++) 
		{
			if(removed.get(j).getCluster().isEmpty())
			{
				if(removed.get(j) instanceof FaceNode)
				{
					((FaceNode) removed.get(j)).face.color = new Color(255,255,255);
				}
			}
			else
			{
				for(int  k= 0 ; k < removed.get(j).getCluster().size();k++)
				{
					if(removed.get(j).getCluster().get(k) instanceof FaceNode)
					{
						((FaceNode)removed.get(j).getCluster().get(k)).face.color = new Color(255,255,255);
					}

				}
			}	
		}	
	}	
	private static Random rand = new Random();
	public static Color randomColor(float alpha) {
		return new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(),alpha);
	}
	public static ArrayList<GraphPartitioningState> readGraphs(String filePath)
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
}
