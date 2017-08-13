package isomorphism_mapping;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jgrapht.alg.isomorphism.IsomorphicGraphMapping;
import org.jgrapht.graph.SimpleGraph;

import search.basic.Border;
import search.basic.ConstrainedGraphPartitioning;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.Partition;
import search.basic.PartitionBorder;
import search.basic.SearchConfiguration;
import tests.VoronoiGenerator;
import util.GraphUtil;
import util.TestsUtil;
import util.Util;
public class ConvertingMissionGraphsTest
{
	
	static int sizeOfBasicGraph = 200;
	static int initialLimitOnMaxNodesExpanded = 10;
	static int increamentInLimit = 50;
	static int afterCoarseningSize = 40;
	
	public static void main(String[] args) 
	{
		//Building the Mission Graph, this is as seen in the PCGWorkshop stream presentation, 
		//an image can be found in test_graphs with the name MG.png
		GraphPartitioningState C = new GraphPartitioningState();
		Partition p0 = new Partition(0);
		Partition p1 = new Partition(1);
		Partition p2 = new Partition(2);
		Partition p3 = new Partition(3);
		Partition p4 = new Partition(4);
		Partition p5 = new Partition(5);
		Partition p6 = new Partition(6);
		Partition p7 = new Partition(7);
		C.addVertex(p0);
		C.addVertex(p1);
		C.addVertex(p2);
		C.addVertex(p3);
		C.addVertex(p4);
		C.addVertex(p5);
		C.addVertex(p6);
		C.addVertex(p7);
		
		PartitionBorder[] edges =
			{		
				new PartitionBorder(p0, p1),
				new PartitionBorder(p1, p2),
				new PartitionBorder(p2, p3),
				new PartitionBorder(p3, p4),
				new PartitionBorder(p4, p5),
				new PartitionBorder(p1, p5),
				new PartitionBorder(p5, p6),
				new PartitionBorder(p6, p7)
			};
		for(int i = 0 ; i < edges.length;i++)
		{
			C.addEdge(edges[i].getP1(), edges[i].getP2(),edges[i]);
		}
		
		//Mapping each node in the constrain graph to a terrain type
		Map<Partition,ActionType> actionsMap = new HashMap<>();
		actionsMap.put(p0, ActionType.Start);
		actionsMap.put(p1, ActionType.Fight);
		actionsMap.put(p2, ActionType.Trap);
		actionsMap.put(p3, ActionType.Loot);
		actionsMap.put(p4, ActionType.Fight);
		actionsMap.put(p5, ActionType.Puzzle);
		actionsMap.put(p6, ActionType.Boss);
		actionsMap.put(p7, ActionType.End);
		
		//Mapping each terrain type to a color
		Map<ActionType,Color> colorMap = new HashMap<>();
		colorMap.put(ActionType.Start,  Color.GRAY);
		colorMap.put(ActionType.End,  Color.GRAY);
		colorMap.put(ActionType.Fight,  Color.RED);
		colorMap.put(ActionType.Boss,  Color.RED);
		colorMap.put(ActionType.Puzzle,  Color.GREEN);
		colorMap.put(ActionType.Loot,  Color.YELLOW);
		colorMap.put(ActionType.Trap,  Color.BLUE);
		
	

		
		Random rand = new Random();
		
		//Generating the basic graph
		VoronoiGenerator generator = new VoronoiGenerator();
		generator.setupGenerator(sizeOfBasicGraph, true, false, 500, 500, false, false, false);
		SimpleGraph<Node,Border> G = generator.generate(sizeOfBasicGraph, rand);
		
		SearchConfiguration searchConfiguration = new SearchConfiguration(G, C);
		GraphPartitioningState result = ConstrainedGraphPartitioning.partitionConstrainedWithCoarseningAndRandomRestart(searchConfiguration, rand, initialLimitOnMaxNodesExpanded, increamentInLimit, afterCoarseningSize);
		
		//Now, through the isomorphism mapping we will color the partitions in the result according to the terrain type of their corresponding nodes in C (as described by the map we build earlier)
		IsomorphicGraphMapping<Partition,PartitionBorder> mapping = GraphUtil.getMapping(result, C);
		for(Partition parInConstraint : C.vertexSet())
		{
			Partition parInResult = mapping.getVertexCorrespondence(parInConstraint, false);
			TestsUtil.colorize(parInResult.getMembers(),colorMap.get(actionsMap.get(parInConstraint)));	
		}	
		//Lastly we color the removed cells
		TestsUtil.colorize(result.getRemoved(),Color.WHITE);
	}
	enum ActionType
	{			
		Start,End,Fight,Boss,Puzzle,Trap,Loot
	}


	
}
