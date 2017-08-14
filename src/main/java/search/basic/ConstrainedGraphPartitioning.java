package search.basic;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.jgrapht.graph.SimpleGraph;

import aima.core.agent.Action;
import aima.core.search.framework.NodeExpander;
import aima.core.search.framework.NodeExpander.NodeListener;
import aima.core.search.framework.SearchForActions;
import aima.core.search.framework.evalfunc.HeuristicFunction;
import aima.core.search.framework.problem.ActionsFunction;
import aima.core.search.framework.problem.GoalTest;
import aima.core.search.framework.problem.Problem;
import aima.core.search.framework.problem.ResultFunction;
import aima.core.search.framework.qsearch.GraphSearch;
import aima.core.search.framework.qsearch.QueueSearch;
import aima.core.search.framework.qsearch.TreeSearch;
import aima.core.search.informed.AStarSearch;
import aima.core.search.informed.GreedyBestFirstSearch;
import aima.core.search.uninformed.BreadthFirstSearch;
import aima.core.search.uninformed.DepthFirstSearch;
import aima.core.util.CancelableThread;
import search.enums.PartitioningType;
import search.enums.SearchStrategy;
import search.enums.SearchType;
import util.GraphUtil;

public class ConstrainedGraphPartitioning
{
	/**
	 * Uses the idea of coarsening to reduce the size of the search space by reducing the size of the basic graph G.<br/>
	 * Employs a Random Restart policy (see Section 5.1 in the paper) by generating a different basic graph each run while using a deterministic algorithm to obtain the initial state.<br/> If the basic graph is not meant to change, use the other implementation.
	 * @param sc (See SearchConfiguration class)
	 * @param generator used to generate  different basic graphs each run in the Random Restart policy.
	 * @param rand the RNG which the generator will use.
	 * @param initialLimitOnMaxNodesExpanded the initial value for the limit on the number of nodes to be expanded before terminating the search and starting a new run.
	 * @param increamentInLimit how much the limit will increase, this method uses a linear increase policy.
	 * @param afterCoarseningSize the size of the basic graph G after coarsening (i.e. k in the paper).
	 * @return a partitioning of the basic graph G that is isomorphic to the constraint graph C.
	 */
	public static GraphPartitioningState partitionConstrainedWithCoarseningAndRandomRestart(SearchConfiguration sc,int basicGraphSize,BasicGraphGenerator generator,Random rand, int initialLimitOnMaxNodesExpanded,int increamentInLimit, int afterCoarseningSize )
	{
		if(sc.getBasicGraph() == null)
		{
			sc.setBasicGraph(generator.generate(basicGraphSize,rand));
		}
		
		int GSize = GraphUtil.sizeOf(sc.getBasicGraph());
		int limit = initialLimitOnMaxNodesExpanded;
		GraphPartitioningState result = null;
		while(result == null)
		{	
			result = partitionConstrainedWithCoarsening(sc,rand,afterCoarseningSize,limit);
			if(result == null)
			{
				limit += increamentInLimit;
				SimpleGraph<Node,Border> G = generator.generate(GSize,rand);
				sc.setBasicGraph(G);
			}
		}
		return result;	
	}
	/**
	 * Uses the idea of coarsening to reduce the size of the search space by reducing the size of the basic graph G.<br/>
	 * Employs a Random Restart policy (see Section 5.1 in the paper) by using the same basic graph but a different initial state each run through a stochastic algorithm.<br/> If different basic graphs is a more preferable way to obtain different initial states, see the other implementation.
	 * @param sc (See SearchConfiguration class)
	 * @param rand the RNG which the stochastic algorithm will use to obtain different initial states.
	 * @param initialLimitOnMaxNodesExpanded the initial value for the limit on the number of nodes to be expanded before terminating the search and starting a new run.
	 * @param increamentInLimit how much the limit will increase, this method uses a linear increase policy.
	 * @param afterCoarseningSize the size of the basic graph G after coarsening (i.e. k in the paper).
	 * @return a partitioning of the basic graph G that is isomorphic to the constraint graph C.
	 */
	public static GraphPartitioningState partitionConstrainedWithCoarseningAndRandomRestart(SearchConfiguration sc,Random rand, int initialLimitOnMaxNodesExpanded,int increamentInLimit,int afterCoarseningSize)
	{
		if(afterCoarseningSize != -1)
			sc.setBasicGraph(GraphUtil.partitionToNodeGraph(GraphUtil.partition(sc.getBasicGraph(), afterCoarseningSize, PartitioningType.KERNEL_DETERMINISTIC,rand,false)));
		
		int limit = initialLimitOnMaxNodesExpanded;
		GraphPartitioningState result = null;
		while(result == null)
		{		
			GraphPartitioningState initialState = GraphUtil.partition(sc.getBasicGraph(), GraphUtil.sizeOf(sc.getConstraintGraph()), PartitioningType.KMEANS_STOCHASTIC, rand,false);
			result =  partitionConstrained(sc,initialState, limit);
			if(result == null)
			{
				limit += increamentInLimit;
			}
		}
		return result;
	}
	/**
	 * Employs a Random Restart policy (see Section 5.1 in the paper) by using the same basic graph but a different initial state each run through a stochastic algorithm.<br/> 
	 * If different basic graphs is a more preferable way to obtain different initial states, see the other implementation.
	 * @param sc (See SearchConfiguration class)
	 * @param rand the RNG which the stochastic algorithm will use to obtain different initial states.
	 * @param initialLimitOnMaxNodesExpanded the initial value for the limit on the number of nodes to be expanded before terminating the search and starting a new run.
	 * @param increamentInLimit how much the limit will increase, this method uses a linear increase policy.
	 * @param afterCoarseningSize the size of the basic graph G after coarsening (i.e. k in the paper).
	 * @return a partitioning of the basic graph G that is isomorphic to the constraint graph C.
	 */
	public static GraphPartitioningState partitionConstrainedWithRandomRestart(SearchConfiguration sc,int basicGraphSize,BasicGraphGenerator generator,Random rand, int initialLimitOnMaxNodesExpanded,int increamentInLimit )
	{		
		return partitionConstrainedWithCoarseningAndRandomRestart(sc, basicGraphSize,generator,rand, initialLimitOnMaxNodesExpanded, increamentInLimit, -1);
	}
	/**
	 * Employs a Random Restart policy (see Section 5.1 in the paper) by using the same basic graph but a different initial state each run through a stochastic algorithm.<br/> 
	 * If different basic graphs is a more preferable way to obtain different initial states, see the other implementation.
	 * @param sc (See SearchConfiguration class)
	 * @param rand the RNG which the stochastic algorithm will use to obtain different initial states.
	 * @param initialLimitOnMaxNodesExpanded the initial value for the limit on the number of nodes to be expanded before terminating the search and starting a new run.
	 * @param increamentInLimit how much the limit will increase, this method uses a linear increase policy.
	 * @return a partitioning of the basic graph G that is isomorphic to the constraint graph C.
	 */
	public static GraphPartitioningState partitionConstrainedWithRandomRestart(SearchConfiguration sc,Random rand, int initialLimitOnMaxNodesExpanded,int increamentInLimit)
	{		
		return partitionConstrainedWithCoarseningAndRandomRestart(sc, rand, initialLimitOnMaxNodesExpanded, increamentInLimit, -1);
	}
	/**
	 * @param sc (See SearchConfiguration class)
	 * @param rand used in case a stochastic algorithm for obtaining the initial state was used.
	 * @param afterCoarseningSize the size of the basic graph G after coarsening (i.e. k in the paper).
	 * @param maxNodesExpanded the limit on the number of nodes to be expanded before terminating the search.
	 * @return a partitioning of the basic graph G that is isomorphic to the constraint graph C.
	 */
	public static GraphPartitioningState partitionConstrainedWithCoarsening(SearchConfiguration sc,Random rand,int afterCoarseningSize, int maxNodesExpanded )
	{
		if(afterCoarseningSize != -1)
			sc.setBasicGraph(GraphUtil.partitionToNodeGraph(GraphUtil.partition(sc.getBasicGraph(), afterCoarseningSize, PartitioningType.KERNEL_DETERMINISTIC,rand,true)));
		
		GraphPartitioningState initialState = GraphUtil.partition(sc.getBasicGraph(), GraphUtil.sizeOf(sc.getConstraintGraph()), sc.getInitialStatePartitioningType(), rand,true);
		return partitionConstrained(sc,initialState, maxNodesExpanded);
	}
	

	/**
	 * Uses the idea of coarsening to reduce the size of the search space by reducing the size of the basic graph G. An iterative increase on the value of the new size is made until a suitable value and a solution are found.<br/>
	 * <b>We suggest</b> to find an appropriate value for the new size (afterCoarseningSize or k) and then use one the of the implementations of partitionWithCoarseningAndRandomRestart() to fight the sensitivity to the initial state instead of this method.<br/>
	 * Intuition on which value of k to choose can be seen in Section 4.7 of the paper 
	 * @param sc (See SearchConfiguration class)
	 * @param rand used in case a stochastic algorithm for obtaining the initial state was used.
	 * @param afterCoarseningInitialSize the size of the basic graph G after coarsening (i.e. k in the paper).
	 * @param coarseningIncreament if no solution was found when coarsening to k, k is increased by this amount and the search is started again, this continues until k = |G|. 
	 * @param maxNodesExpanded  the limit on the number of nodes to be expanded before terminating the search.
	 * @return a partitioning of the basic graph G that is isomorphic to the constraint graph C.
	 */
	public static GraphPartitioningState partitionConstrainedWithIterativeCoarsening(SearchConfiguration sc,Random rand, int afterCoarseningInitialSize ,int coarseningIncreament, int maxNodesExpanded)
	{
		int GSize = GraphUtil.sizeOf(sc.getBasicGraph());
		for(int c = afterCoarseningInitialSize;c < GSize;c += coarseningIncreament)
		{
			GraphPartitioningState result = partitionConstrainedWithCoarsening(sc,rand,maxNodesExpanded,c);
			if(result  != null)
				return result;
		}
		return null;
	}

	/**
	 * A convenience method over the other implementation. This is the vanilla version of the algorithm
	 * @param sc (See SearchConfiguration class)
	 * @param initialState
	 * @param maxNodesExpanded the limit on the number of nodes to be expanded before terminating the search.
	 * @return a partitioning of the basic graph G that is isomorphic to the constraint graph C.
	 */
	public static GraphPartitioningState partitionConstrained(SearchConfiguration sc,GraphPartitioningState initialState, int maxNodesExpanded)
	{
		return partitionConstrained(sc.getBasicGraph(), sc.getConstraintGraph(), initialState, sc.getGoalTest(), 
				sc.getHeuristicFunction(), sc.getSearchType(), sc.getSearchStrategy(), sc.isPreventGaps(), sc.getResultFunction(), 
				sc.getActionsFunction(), sc.isAllowNodeRemoval(), sc.isAllowEarlyGoalTest(),maxNodesExpanded);						
	}
	
	/**
	 * This is the vanilla version of the algorithm. Meaning of each parameter can be seen in each respective class or in SearchConfiguration 
	 */
	public static GraphPartitioningState partitionConstrained(SimpleGraph<Node,Border> basicGraph,GraphPartitioningState constraintGraph,GraphPartitioningState initialState,GoalTest goalTest,HeuristicFunction heuristicFunction,SearchType searchType,SearchStrategy searchStrategy,boolean preventGaps,ResultFunction resultsFunction,ActionsFunction actionsFunction,boolean allowVertexRemoval,boolean allowEarlyGoalTest,int maxNodesExpanded) 
	{
		Problem problem = new Problem(initialState, actionsFunction, resultsFunction, goalTest);
		QueueSearch queueSearch = getQueueSearchObject(searchType);
		NodeExpander ne = queueSearch.getNodeExpander();

		SearchForActions searchStratagyObject = getSearchStratagyObject(searchStrategy, queueSearch, heuristicFunction);
		List<Action> actions = new LinkedList<>();

		CancelableThread thread = new CancelableThread(new Runnable() 
		{
			
			@Override
			public void run() 
			{							
				actions.addAll(searchStratagyObject.findActions(problem));				
			}
		});
		
		ne.addNodeListener(new NodeListener() 
		{		
			@Override
			public void onNodeExpanded(aima.core.search.framework.Node node)
			{
				if(maxNodesExpanded != -1 && ne.getNumOfExpandCalls() > maxNodesExpanded)
				{
					System.out.println("Limit on num of nodes expansions reached = "+maxNodesExpanded);				
					thread.cancel();				
				}
			}
		});
		
		try {
			//Start the search
			thread.start();
			//Wait for the search to end or the maximum number of nodes are expanded (on which the thread is canceled)
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		catch(Exception e){e.printStackTrace();}
			
		if (actions.size() == 0) 
		{
			//System.out.println("No Solution");
			return null;
		}
		else if (actions.get(0).isNoOp()) 
		{
			//System.out.println("Already at a Solution");
			return initialState;
		}
		
		//Find the final state after applying the actions found on the initial state
		GraphPartitioningState solution = GraphUtil.applyActions(initialState,basicGraph,actions);
		solution.setNumNodesExpanded(ne.getNumOfExpandCalls());
		solution.setPathLength(actions.size());
		
		return solution;		
	}
	
	/***Internal Utility*****/
	private static QueueSearch getQueueSearchObject(SearchType searchType) {
		if (searchType == SearchType.GRAPH) {
			return new GraphSearch();
		} else if (searchType == SearchType.TREE) {
			return new TreeSearch();
		}
		return null;
	}

	private static SearchForActions getSearchStratagyObject(SearchStrategy searchStratagy, QueueSearch queueSearch, final HeuristicFunction hf) {
		if (searchStratagy == SearchStrategy.ASTAR) {
			return new AStarSearch(queueSearch, hf);
		} else if (searchStratagy == SearchStrategy.BFS) {
			return new BreadthFirstSearch(queueSearch);
		} else if (searchStratagy == SearchStrategy.DFS) {
			return new DepthFirstSearch(queueSearch);
		} else if (searchStratagy == SearchStrategy.GREEDY) {
			return new GreedyBestFirstSearch(queueSearch, hf);
		} else if (searchStratagy == SearchStrategy.GREEDYBESTFIRST) {
			return new GreedyBestFirstSearch(queueSearch, hf);
		}
		return null;
	}
	
	
	
}
