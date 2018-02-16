package search.basic;

import java.util.ArrayList;

import org.jgrapht.graph.SimpleGraph;

import util.GraphUtil;

public class GraphPartitioningState extends SimpleGraph<Partition,PartitionBorder>
{
	/**The removed nodes (through NodeRemoval actions)**/
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
	public int getRemovedSize() 
	{	
		return removed.size();
	}
	//Metrics**********************
	private int numNodesExpanded;
	private double pathLength;
	
	
	public int getNumNodesExpanded() {
		return numNodesExpanded;
	}
	public void setNumNodesExpanded(int numNodesExpanded) {
		this.numNodesExpanded = numNodesExpanded;
	}
	public double getPathLength() {
		return pathLength;
	}
	public void setPathLength(double pathLength) {
		this.pathLength = pathLength;
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

	@Override
	public String toString()
	{
		return "[QuotientGraph: <Nodes>= "+vertexSet() + " <Edges>= "+edgeSet() +" ]";
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();		
	}

}
