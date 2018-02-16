package search.actions;

import aima.core.agent.Action;
import search.basic.Node;

public class NodeRemovalAction implements Action{
	private Node nodeToBeRemoved;
	public Node getNodeToBeRemoved() {
		return nodeToBeRemoved;
	}
	public NodeRemovalAction(Node nodeToBeRemoved)
	{
		this.nodeToBeRemoved = nodeToBeRemoved;
	}
	public boolean isNoOp()
	{	
		return nodeToBeRemoved == null;
	}
	@Override
	public String toString() 
	{
		return "[Action:Remove Node= "+nodeToBeRemoved+ "]";
	}
}
