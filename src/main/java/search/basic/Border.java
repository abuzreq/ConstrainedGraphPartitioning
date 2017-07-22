package search.basic;

import org.jgrapht.graph.DefaultEdge;

public class Border extends DefaultEdge implements Comparable<Border> {
	private Node n1;
	private Node n2;

	
	public Node getN1() {
		return n1;
	}

	public void setN1(Node n1) {
		this.n1 = n1;
	}

	public Node getN2() {
		return n2;
	}

	public void setN2(Node n2) {
		this.n2 = n2;
	}

	public Border(Node n1, Node n2) {
		this.n1 = n1;
		this.n2 = n2;
	}

	Border(int n1_value, int n2_value) {
		this.n1 = new Node(n1_value);
		this.n2 = new Node(n2_value);
	}

	void setCells(Node n1, Node n2) {
		this.n1 = n1;
		this.n2 = n2;
	}

	@Override
	public int hashCode() {
		return (n1.toString() + ":" + n2.toString()).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		Border other = (Border) obj;
		return this.n1.equals(other.n1) && this.n2.equals(other.n2) || this.n1.equals(other.n2)
				&& this.n2.equals(other.n1);
	}

	public int compareTo(Border b) {
		return ((this.n1.getValue() * 100) + (this.n2.getValue() * 10)) - ((b.n1.getValue() * 100) + (b.n2.getValue() * 10));
	}
}
