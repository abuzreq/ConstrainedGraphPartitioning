package search.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import org.jgrapht.graph.SimpleGraph;

import util.*;

/**
 * Forms the basis of a search state, should be inherited if the state is to be exteneded
 * @author abuzreq
 *
 */
public class GraphPartitioningState extends SimpleGraph<Partition,PartitionBorder>
{
	//The removed nodes (through NodeRemoval actions)
	private ArrayList<Node> removed = new ArrayList<Node>();
		
	public void addToRemoved(Node node)
	{
		removed.add(node);
	}
	public void addAllToRemoved(ArrayList<Node> nodes)
	{
		removed.addAll(nodes);
	}	
	public boolean removedContains(Node node)
	{
		return removed.contains(node);
	}
	public ArrayList<Node> getRemoved() {
		return removed;
	}

	/*
	public int getRemovedCellsSize() 
	{	
		int sum = 0;
		
		for(int i=0; i <removed.size();i++)
		{
			sum += removed.get(i).cluster.size();
		}
		return sum;
	}*/
	public int getRemovedSize() 
	{	
		return removed.size();
	}
	//Metrics**********************
	private int numNodesExpanded;
	private double pathCost;
	private int lastCourseningValueUsed;
	
	
	public int getNumNodesExpanded() {
		return numNodesExpanded;
	}
	public void setNumNodesExpanded(int numNodesExpanded) {
		this.numNodesExpanded = numNodesExpanded;
	}
	public double getPathCost() {
		return pathCost;
	}
	public void setPathCost(double pathCost) {
		this.pathCost = pathCost;
	}
	public int getLastCourseningValueUsed() {
		return lastCourseningValueUsed;
	}
	public void setLastCourseningValueUsed(int lastCourseningValueUsed) {
		this.lastCourseningValueUsed = lastCourseningValueUsed;
	}
	//********************
	@Override
	public boolean equals(Object obj )
	{
		GraphPartitioningState state = (GraphPartitioningState)obj;
		for(Partition par : this.vertexSet())
		{
			for(Partition other : state.vertexSet())
			{
				if(par.equals(other))
				{					
					boolean areEqual=GraphUtil.areEdgesEqualIgnoreDirection( GraphUtil.getEdgesOf(this, par),GraphUtil.getEdgesOf(state, other));					 
					 if(!areEqual)
						 return false;					
					break;
				}
			}
		}
		return true;		
	}
	public GraphPartitioningState(PartitionBorderFactory factory) {
		super(factory);
	}
	public GraphPartitioningState() 
	{
		super(PartitionBorder.class);
	}
	public GraphPartitioningState clone()
	{
		GraphPartitioningState gps = (GraphPartitioningState)super.clone();
		gps.addAllToRemoved(removed);
		return gps;	
	}
	public String graphString()
	{
		return GraphUtil.sizeOf(this) +" Vertices "+vertexSet() + " Edges "+edgeSet();
		
	}
	
	@Override
	public String toString()
	{
		return hashCode()+" "+GraphUtil.sizeOf(this);
	}

	@Override
	public int hashCode()
	{
		
		String str= "";
		Partition[] pars = GraphUtil.getPartitions(this);
		Arrays.sort(pars,new Comparator<Partition>() {

			public int compare(Partition p1, Partition p2) {
				return p1.hashCode() - p2.hashCode();
			}
		});
		for(int i = 0 ;i < pars.length;i++)
		{
			str += pars[i].hashCode() ;
		}
		
		PartitionBorder[] borders = GraphUtil.getPartitionsBorders(this);
		Arrays.sort(borders,new Comparator<PartitionBorder>() {

			public int compare(PartitionBorder pb1, PartitionBorder pb2) {
				return pb1.hashCode() - pb2.hashCode();
			}
		});
		for(int i = 0 ;i < borders.length;i++)
		{
			str += borders[i].hashCode() ;
		}
		return super.hashCode();		
	}

}
