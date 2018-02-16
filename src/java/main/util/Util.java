package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.jgrapht.graph.SimpleGraph;

import search.basic.Border;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.Partition;
import search.basic.PartitionBorder;

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
	

	
}
