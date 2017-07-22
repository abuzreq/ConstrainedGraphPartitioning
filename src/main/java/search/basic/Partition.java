package search.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public  class Partition
{	
	private int number ;
	private ArrayList<Node> neighbors = new ArrayList<Node>();
	private ArrayList<Node> members = new ArrayList<Node>();
	
	
	public ArrayList<Node> getNeighbors() {
		return neighbors;
	}

	public ArrayList<Node> getMembers() {
		return members;
	}

	public Partition(int num ,Node... values) {
		members.addAll(Arrays.asList(values));
		for (int i = 0; i < members.size(); i++) {
			members.get(i).setContainer(this);
		}
		this.number = num;
	}
	
	public Partition(int num ,ArrayList<Node> values) {
		members.addAll(values);
		for (int i = 0; i < members.size(); i++) {
			members.get(i).setContainer(this);
		}
		this.number = num;
	}

	public Partition(int num) 
	{
		this.number = num;
	}
	// Creates a copy of the contructor passed
	public Partition(Partition partition) {
		this.members = new ArrayList<Node>(partition.members);
		for (int i = 0; i < members.size(); i++) {
			members.get(i).setContainer(this);
		}
		this.neighbors = new ArrayList<Node>(partition.neighbors);
	}

	
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}


	/*
	 * The responsibility of setting the container of the node is left to
	 * addMemberRemoveNeighbor, the container is set to null here
	 */
	public void removeMemberAddNeighbor(Node node) 
	{
		members.remove(node);
		neighbors.add(node);
		node.setContainer(null);
	}

	/**
	 * Removes the node of the passed value from members and returns it
	 * without changing its container
	 * */
	public Node removeMemberLeaveContainer(int nodeValue) 
	{
		int toBeRemoved = 0;
		for (int i = 0; i < members.size(); i++) {
			if (members.get(i).getValue() == nodeValue) {
				toBeRemoved = i;
			}
		}
		return members.remove(toBeRemoved);
	}

	/*
	 * Adds the node to members and assign its container to the this
	 * partition *
	 */
	public void addMember(Node node)
	{
		members.add(node);
		node.setContainer(this);

	}
	public void addMembers(Node[] nodes)
	{
		for(int i = 0 ; i < nodes.length;i++)
		{
			members.add(nodes[i]);
			nodes[i].setContainer(this);
		}
	}
	public void addMemberRemoveNeighbor(Node node) {
		neighbors.remove(node);
		members.add(node);
		node.setContainer(this);
	}

	public void addNeighbor(Node node) {
		if (!neighbors.contains(node))
			neighbors.add(node);
	}
	public void removeMember(Node nd) 
	{
		members.remove(nd);
		nd.setContainer(null);
	}
	/**
	 * Creates a copy of the partition without copying the neighbors
	 * */
	public static Partition CreateCopyWithoutNeighbors(Partition partition) {
		Partition p = new Partition(partition.number);
		p.members = new ArrayList<Node>(partition.members);
		for (int i = 0; i < p.members.size(); i++) {
			p.members.set(i, p.members.get(i).clone());
			p.members.get(i).setContainer(p);
		}
		return p;
	}
	/**
	 * Creates a copy of the partition and copy the neighbors
	 * */
	public static Partition CreateCopy(Partition partition) {
		Partition p = new Partition(partition.number);
		p.members = new ArrayList<Node>(partition.members);
		for (int i = 0; i < p.members.size(); i++) {
			p.members.set(i, p.members.get(i).clone());
			p.members.get(i).setContainer(p);
		}
		p.neighbors = new ArrayList<Node>(partition.neighbors);
		for (int i = 0; i < p.neighbors.size(); i++) {
			p.neighbors.set(i, p.neighbors.get(i).clone());
			p.neighbors.get(i).setContainer(p);
		}
		return p;
	}
	public int getNumMembers()
	{
		return members.size();
	}
	public int getNumAllCells()
	{
		int sum = 0;
		for(int i = 0 ; i < members.size();i++)
		{
			if(members.get(i).isClusterEmpty())
				sum +=1 ;
			else 
				sum += members.get(i).getCluster().size() ;
		}
		return sum;
	}
	public ArrayList<Node> getAllCells()
	{
		ArrayList<Node> cells = new ArrayList<Node>(getNumAllCells());
		for(int i = 0 ; i < members.size();i++)
		{
			if(members.get(i).isClusterEmpty())
				cells.add(members.get(i));
			else 
				{
					for(int j =0 ; j < members.get(i).getCluster().size();j++)
					{
						cells.add(members.get(i).getCluster().get(j));
					}			
				}
		}
		return cells;
	}
	public Set<Node> getAllCellsSet()
	{
		Set<Node> cells = new HashSet<Node>();
		for(int i = 0 ; i < members.size();i++)
		{
			if(members.get(i).isClusterEmpty())
				cells.add(members.get(i));
			else 
				{
					for(int j =0 ; j < members.get(i).getCluster().size();j++)
					{
						cells.add(members.get(i).getCluster().get(j));
					}
				
				}
		}
		return cells;
	}



	@Override
	public String toString() 
	{
		
		return "Num"+number ;//+" Members"+printMembers()+")";//+"{members : " + members + ",Neighbors: " + neighbors + "}";
	}

	public String printMembers() {
		String str = "[ ";
		for (int i = 0; i < members.size(); i++) {
			str += ", " + members.get(i).getValue() +(members.get(i).getContainer() == null?"F":"T"+members.get(i).getContainer().number);
		}
		return str + " ]";
	}

	@Override
	public boolean equals(Object obj) 
	{
		return obj.hashCode() == this.hashCode();
	}

	public int hashCode()
	{
		return number;
	}
	public int compareTo(Partition p) {  
		
		return this.hashCode() - p.hashCode();
	}

	public void removeMemberAt(int index)
	{
		members.remove(index);
	}

	public void removeNeighbor(Node node)
	{
		neighbors.remove(node);
	}

}

