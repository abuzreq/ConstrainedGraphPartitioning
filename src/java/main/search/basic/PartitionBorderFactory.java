package search.basic;

import org.jgrapht.EdgeFactory;

public  class PartitionBorderFactory implements EdgeFactory<Partition,PartitionBorder>
{
	public PartitionBorder createEdge(Partition sourceVertex, Partition targetVertex)
	{
		return new PartitionBorder(sourceVertex,targetVertex);
	}	
}