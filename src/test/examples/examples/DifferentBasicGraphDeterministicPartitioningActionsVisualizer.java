package examples;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import org.jgrapht.graph.SimpleGraph;

import aima.core.agent.Action;
import search.basic.Border;
import search.basic.ConstrainedGraphPartitioningReturnActions;
import search.basic.ConstrainedGraphPartitioningReturnActions.InitialStateActionsPair;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.SearchConfiguration;
import util.GraphUtil;
import util.TestsUtil;

/**
 * Press Right arrow to get the next state, left arrow to get the previous
 * 
 * @author abuzreq
 *
 */
public class DifferentBasicGraphDeterministicPartitioningActionsVisualizer {

	static int sizeOfBasicGraph = 200;
	static int initialLimitOnMaxNodesExpanded = 10;
	static int increamentInLimit = 50;
	static int afterCoarseningSize = 40;
	
	static Random rand =  new Random();
	public static void main(String[] args) 
	{
		VoronoiGenerator generator = new VoronoiGenerator();
		//Generating the constrain graph
		final GraphPartitioningState C  = GraphUtil.generateChainGraph(7);
		InitialStateActionsPair result = null;
		//Setting up the generator
		generator.setupGenerator(sizeOfBasicGraph, true, false, 500, 500, true, true, false);

		//Note that we pass the generator as well. An initial basic graph can be passed or set to null
		result = ConstrainedGraphPartitioningReturnActions.partitionConstrainedWithCoarseningAndRandomRestart(new SearchConfiguration(null, C),sizeOfBasicGraph,generator,rand, initialLimitOnMaxNodesExpanded, increamentInLimit, afterCoarseningSize);	
		System.out.println("Result Found");
		//System.out.println(result);
		current = result.getInitialState();
		actions  = result.getActions();
		SimpleGraph<Node,Border> G = result.getG();
		TestsUtil.colorizeFixed(current,Color.WHITE);
		states.add(current);

		Callable onRight = new Callable() {

			@Override
			public Object call() throws Exception
			{
				if(index  < states.size()-1)
				{
					index += 1;
					current = states.get(index);
					TestsUtil.colorizeFixed(current,Color.white);
				}
				else if(actions.size() != 0)
				{	
					Action a = actions.get(0);
					actions.remove(0);
					final GraphPartitioningState next;
					next = applyAction(G,current,a);					
					states.add(next);
					current = next;
					index = states.size()-1;
					TestsUtil.colorizeFixed(current,Color.white);
				}
				return null;
			}
		};
		Callable onLeft = new Callable() {
			@Override
			public Object call() throws Exception
			{
				index = index -1;
				if(index < 0)
					index = 0;
				if(index >= states.size())
				{
					index = states.size()-1;
				}
				current = states.get(index);
				TestsUtil.colorizeFixed(current,Color.white);
				return null;
			}
		};
		VoronoiGeneratorDrawer.onRight = onRight;
		VoronoiGeneratorDrawer.onLeft = onLeft;
	}
	static int index = 0;
	static List<Action> actions;
	static ArrayList<GraphPartitioningState> states = new ArrayList<>() ;
	static GraphPartitioningState current;
	static GraphPartitioningState applyAction(SimpleGraph<Node,Border> G,GraphPartitioningState s,Action a)
	{
		List<Action> actions = new LinkedList<Action>();
		actions.add(a);
		GraphPartitioningState result =  GraphUtil.applyActions(s, G, actions);
		return result;
	}

}
