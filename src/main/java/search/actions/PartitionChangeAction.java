package search.actions;

import aima.core.agent.Action;
import search.basic.Node;
import search.basic.Partition;

public class PartitionChangeAction implements Action {
	private Partition partition;
	private Node node;
	public PartitionChangeAction(Partition partition, Node node) {
		this.node = node;
		this.partition = partition;
	}

	public Partition getPartition() {
		return partition;
	}

	public Node getNode() {
		return node;
	}

	public boolean isNoOp() {
		return node == null && partition == null;
	}
	public boolean equals(Object ob)
	{
		PartitionChangeAction other = (PartitionChangeAction)ob;
		return this.partition.equals(other.partition) && this.node.equals(other.node);
	}
	@Override
	public String toString() {
		return "[Action:Partition= " + partition + " ,Node= " + node +"]";
	}
}

