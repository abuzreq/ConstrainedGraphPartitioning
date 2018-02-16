package search.basic;

import java.util.ArrayList;

public class Node implements Comparable<Node> 
{
	private static int num = 0;
	private int value;
	private ArrayList<Node> cluster = new ArrayList<Node>();
	private Partition container;
	
	
	public Node(int value) {
		this.value = value;
	}
	
	public Node()
	{
		
		this(num);
		num++;
	}
	
	public Node(int value,ArrayList<Node> cluster )
	{
		this.value = value;
		this.cluster.addAll(cluster);
		for(int i = 0 ; i < cluster.size();i++)
		{
			cluster.get(i).container = null;
		}
	}


	@Override
	public boolean equals(Object obj) {
		Node other = (Node) obj;
		return this.value == other.value;
	}

	@Override
	public Node clone()
	{
		Node nd = new Node(value);
		nd.cluster.addAll(this.cluster);
		nd.container = container;
		return nd;

	}

	@Override
	public String toString() 
	{
		if(container != null)
			return "[Node "+value+(cluster.isEmpty()?"":" ,cluster "+cluster)+" ,Cont. "+container.getNumber() +" ]";
		else
			return "[Node "+value+(cluster.isEmpty()?"":" ,cluster "+cluster)+" ]";
	}

	@Override
	public int hashCode() {
		return (value + "").hashCode();
	}

	public int compareTo(Node o) {
		return value - o.value;
	}
	public boolean isClusterEmpty()
	{
		return cluster.isEmpty();
	}

	public static int getNum() {
		return num;
	}

	public static void setNum(int num) {
		Node.num = num;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public ArrayList<Node> getCluster() {
		return cluster;
	}

	public void setCluster(ArrayList<Node> cluster) {
		this.cluster = cluster;
	}

	public Partition getContainer() {
		return container;
	}

	public void setContainer(Partition container) {
		this.container = container;
	}
	public ArrayList<Node> getSelfOrCluster()
	{
		if(isClusterEmpty())
		{
			ArrayList<Node> arr = new ArrayList<>();
			arr.add(this);
			return arr;
		}
		else
		{
			return cluster;
		}
	}
	
}
