package search.basic;

import org.jgrapht.EdgeFactory;

public  class BorderFactory implements EdgeFactory<Node,Border>
{
	public Border createEdge(Node sourceVertex, Node targetVertex)
	{
		return new Border(sourceVertex,targetVertex);
	}	
}