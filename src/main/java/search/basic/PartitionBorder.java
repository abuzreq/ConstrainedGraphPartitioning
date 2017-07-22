package search.basic;

import org.jgrapht.graph.DefaultEdge;

public class PartitionBorder extends DefaultEdge implements Comparable<PartitionBorder> {
	private Partition p1, p2;

	public Partition getP1() {
		return p1;
	}

	public Partition getP2() {
		return p2;
	}

	public PartitionBorder(Partition p1, Partition p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	@Override
	public int hashCode() 
	{	
		return (p1.hashCode() + ":" + p2.hashCode()).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		PartitionBorder other = (PartitionBorder) obj;
		return( this.p1.equals(other.p1) && this.p2.equals(other.p2)) ||( this.p1.equals(other.p2)
				&& this.p2.equals(other.p1));
	}
	public int compareTo(PartitionBorder b) {
		return this.p1.compareTo(b.p1) + this.p2.compareTo(b.p2);
	}

	public String toString() {
		return "\n<PB " + p1 + " <#> " + p2 + ">("+hashCode()+")";
	}
}
