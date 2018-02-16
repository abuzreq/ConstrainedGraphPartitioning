package search.basic;


public class PartitionNodePair {
	private Partition container;
	private Node node;

	public PartitionNodePair(Partition c, Node n) {
		node = n;
		container = c;
	}

	public Partition getContainer() {
		return container;
	}

	public void setContainer(Partition container) {
		this.container = container;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}
	
	
}
