package isomorphism_mapping;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jgrapht.alg.isomorphism.IsomorphicGraphMapping;
import org.jgrapht.graph.SimpleGraph;

import examples.VoronoiGenerator;
import search.basic.Border;
import search.basic.ConstrainedGraphPartitioning;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.Partition;
import search.basic.PartitionBorder;
import search.basic.SearchConfiguration;
import util.GraphUtil;
import util.TestsUtil;
public class TerrainIsomorphismMappingExample
{
	
	static int sizeOfBasicGraph = 200;
	static int initialLimitOnMaxNodesExpanded = 10;
	static int increamentInLimit = 50;
	static int afterCoarseningSize = 40;
	
	public static void main(String[] args) 
	{
		//Building the constraint graph
		GraphPartitioningState C = new GraphPartitioningState();
		Partition p0 = new Partition(0);
		Partition p1 = new Partition(1);
		Partition p2 = new Partition(2);
		Partition p3 = new Partition(3);
		C.addVertex(p0);
		C.addVertex(p1);
		C.addVertex(p2);
		C.addVertex(p3);
				
		PartitionBorder pb1 = new PartitionBorder(p0, p1);
		PartitionBorder pb2 = new PartitionBorder(p1, p2);
		PartitionBorder pb3 = new PartitionBorder(p2, p3);
		
		C.addEdge(pb1.getP1(), pb1.getP2(),pb1);
		C.addEdge(pb2.getP1(), pb2.getP2(),pb2);
		C.addEdge(pb3.getP1(), pb3.getP2(),pb3);
		
		//Mapping each node in the constrain graph to a terrain type
		Map<Partition,TerrainType> terrainMap = new HashMap<>();
		terrainMap.put(p0, TerrainType.FORREST);
		terrainMap.put(p1, TerrainType.DESERT);
		terrainMap.put(p2, TerrainType.PASTURES);
		terrainMap.put(p3, TerrainType.DESERT);
		
		//Mapping each terrain type to a color
		Map<TerrainType,Color> colorMap = new HashMap<>();
		colorMap.put(TerrainType.DESERT,  Color.YELLOW);
		colorMap.put(TerrainType.FORREST,  Color.GREEN.darker());
		colorMap.put(TerrainType.PASTURES,  Color.GREEN.brighter());

		
		Random rand = new Random();
		
		//Generating the basic graph
		VoronoiGenerator generator = new VoronoiGenerator();
		generator.setupGenerator(sizeOfBasicGraph, true, false, 500, 500, true, true, false);
		SimpleGraph<Node,Border> G = generator.generate(sizeOfBasicGraph, rand);
		SearchConfiguration searchConfiguration = new SearchConfiguration(G, C);
		GraphPartitioningState result = ConstrainedGraphPartitioning.partitionConstrainedWithCoarseningAndRandomRestart(searchConfiguration, rand, initialLimitOnMaxNodesExpanded, increamentInLimit, afterCoarseningSize);
		
		//Now, through the isomorphism mapping we will color the partitions in the result according to the terrain type of their corresponding nodes in C (as described by the map we build earlier)
		IsomorphicGraphMapping<Partition,PartitionBorder> mapping = GraphUtil.getMapping(result, C);
		for(Partition parInConstraint : C.vertexSet())
		{
			Partition parInResult = mapping.getVertexCorrespondence(parInConstraint, false);
			TestsUtil.colorize(parInResult.getMembers(),colorMap.get(terrainMap.get(parInConstraint)));	
		}	
		//Lastly we color the removed cells
		TestsUtil.colorize(result.getRemoved(),Color.WHITE);
	}
	enum TerrainType
	{			
		DESERT,FORREST,PASTURES
	}


	
}
