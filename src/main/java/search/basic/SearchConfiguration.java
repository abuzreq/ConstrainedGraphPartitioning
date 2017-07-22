package search.basic;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import org.jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector;
import org.jgrapht.graph.SimpleGraph;

import aima.core.agent.Action;
import aima.core.search.framework.evalfunc.HeuristicFunction;
import aima.core.search.framework.problem.ActionsFunction;
import aima.core.search.framework.problem.GoalTest;
import aima.core.search.framework.problem.ResultFunction;
import search.actions.NodeRemovalAction;
import search.actions.PartitionChangeAction;
import search.enums.PartitioningType;
import search.enums.SearchStrategy;
import search.enums.SearchType;
import util.GraphUtil;

/**
 * Describes the configuration of the search. The default constructor uses the configurations described in the paper.<br/>
 * By changing these default configurations extensions or alterations to the algorithm is possible.
 * @author abuzreq
 *
 */
public class SearchConfiguration
{
		private SimpleGraph<Node,Border> basicGraph;
		private GraphPartitioningState constraintGraph;
		private GoalTest goalTest;
		private HeuristicFunction heuristicFunction;
		private ResultFunction resultFunction;
		private ActionsFunction actionsFunction;
		private SearchType searchType;
		private SearchStrategy searchStrategy;
		private boolean allowNodeRemoval;
		private boolean preventGaps;
		private boolean allowEarlyGoalTest;
		private PartitioningType initialStatePartitioningType;
			
		
		public SearchConfiguration(SimpleGraph<Node,Border> basicGraph, GraphPartitioningState constraintGraph,GoalTest  goalTest,
		HeuristicFunction heuristicFunction,ResultFunction resultFunction,ActionsFunction actionsFunction,PartitioningType initialStatePartitioningType,boolean allowNodeRemoval,boolean preventGaps,boolean allowEarlyGoalTest) 
		{
			 this.basicGraph = basicGraph;
			 this.constraintGraph = constraintGraph;
			 this.goalTest = goalTest;
			 this.heuristicFunction = heuristicFunction;
			 this.resultFunction = resultFunction;
			 this.actionsFunction = actionsFunction;
			 this.allowNodeRemoval =allowNodeRemoval;
			 this.preventGaps = preventGaps;
			 this.allowEarlyGoalTest = allowEarlyGoalTest;
			 this.initialStatePartitioningType = initialStatePartitioningType;
		}
		/**
		 * The default search configuration as described in the paper which this library implements.</br> 
		 * <b>Goal Test:</b> a graph isomorphism test.</br> 
		 * <b>Heuristic:</b> Since any two isomorphic graphs are isospectral, we take the difference in the energy of the Laplacian matrix of the current quotient graph (current state) and constraint graph (goal state), described in [1]</br> 
		 * <b>Initial state</b> is obtained through KernelClustering as described in [2]</br> 
		 * <b>SearchStratagy:</b> A-Star (A*) search</br> 
		 * <b>SearchType:</b> Graph search</br> 
		 * <b>(preventGaps = true)</b> actions that causes discontinuity in partitions are prevented</br> 
		 * <b>(allowNodeRemoval = true)</b> Node removal actions are allowed </br> 
		 * <b>(allowEarlyGoalTest = true)<b> whether states are tested through the GoalTest before being added to the frontier.<br/>
		 * 
		 * [1] Danai Koutra, Ankur Parikh, Aaditya Ramdas, and Jing Xiang. 2011. Algorithms for Graph Similarity and Subgraph Matching. (2011) </br> 
		 * [2] Luh Yen, Francois Fouss, Christine Decaestecker, Pascal Francq, and Marco Saerens. 2007. 
		 * Graph Nodes Clustering Based on the Commute-Time Kernel. Springer Berlin Heidelberg, Berlin, Heidelberg,
		 *  1037–1045. https://doi.org/10.1007/978-3-540-71701-0_117</br> 
		 * @param G The basic graph which is to be partitiong
		 * @param C The constraint graph
		 */
		public SearchConfiguration(final SimpleGraph<Node,Border> G,final GraphPartitioningState C)
		{
			this.basicGraph = G;
			this.constraintGraph = C;
			this.preventGaps = true;
			this.allowNodeRemoval = true;
			this.allowEarlyGoalTest = true;
			this.searchStrategy = SearchStrategy.ASTAR;
			this.searchType = SearchType.GRAPH;
			this.initialStatePartitioningType = PartitioningType.KERNEL_DETERMINISTIC;
			
			this.actionsFunction = new ActionsFunction() {
				
				public Set<Action> actions(Object state) {
					Set<Action> actions = new LinkedHashSet<Action>();
					Partition[] partitions = GraphUtil.getPartitions((SimpleGraph<Partition, PartitionBorder>) state);

					for (int p = 0; p < partitions.length; p++) 
					{
						Partition par = partitions[p];
						ArrayList<Node> neighbors = par.getNeighbors();
						for (int i = 0; i < neighbors.size(); i++) 
						{
							if (neighbors.get(i).getContainer().getNumMembers() > 1) 
							{
								if (preventGaps) 
								{
									if (!GraphUtil.isCreatingAGap(neighbors.get(i), basicGraph)) 
									{
										if (allowNodeRemoval) 
										{
											actions.add((Action) new NodeRemovalAction(neighbors.get(i)));
										}
										actions.add((Action) new PartitionChangeAction(par, neighbors.get(i)));
									}

								} else {
									if (allowNodeRemoval) {
										actions.add((Action) new NodeRemovalAction(neighbors.get(i)));
									}
									actions.add((Action) new PartitionChangeAction(par, neighbors.get(i)));
								}
							}

						}
					}
					return actions;
				}

			};
			this.resultFunction = new ResultFunction() {
				public Object result(Object s, Action a) 
				{
					long time = System.currentTimeMillis();
					if (a instanceof PartitionChangeAction) {
						PartitionChangeAction action = ((PartitionChangeAction) a);				
						GraphPartitioningState o = GraphUtil.createHypotheticalGraph((GraphPartitioningState) s, basicGraph, action.getNode(), action.getPartition());
						return o;
					} else if (a instanceof NodeRemovalAction)
					{
						NodeRemovalAction action = ((NodeRemovalAction) a);
						GraphPartitioningState o = GraphUtil.createHypotheticalGraph((GraphPartitioningState) s, basicGraph, action.getNodeToBeRemoved());
						return o;
					}
					return null;
				}
			};
			this.goalTest = new GoalTest()
			{
				public boolean isGoalState(Object state) 
				{
					GraphPartitioningState result =( GraphPartitioningState) state;
					VF2GraphIsomorphismInspector<Partition, PartitionBorder> inspector = 
					new VF2GraphIsomorphismInspector<Partition, PartitionBorder>(result, C);
					if( inspector.isomorphismExists())
					{
						System.out.println("ISOMORPHISM");
						return true;
					}
					else return false;
				}
			};
			//laplacianSpectralHeuristicFunction
			this.heuristicFunction  = new HeuristicFunction() {
				Random r =  new Random();
				int num;
				float sum = 0;
				double min = Double.MAX_VALUE;

				public double h(Object state) {
					GraphPartitioningState graph = (GraphPartitioningState) state;
					double d= GraphUtil.laplacianSpectralError(graph, C);
					if(d < min)
					{
						min = d;
					}
					return d;
				}
				@Override
				public String toString() {
					return "(Laplacian Spectral Error) " + (sum / num);
				}
			};
		}
		
		/***********Getters and Setters*************/
		public SimpleGraph<Node, Border> getBasicGraph() {
			return basicGraph;
		}
		public void setBasicGraph(SimpleGraph<Node, Border> basicGraph) {
			this.basicGraph = basicGraph;
		}
		public GraphPartitioningState getConstraintGraph() {
			return constraintGraph;
		}
		public void setConstraintGraph(GraphPartitioningState constraintGraph) {
			this.constraintGraph = constraintGraph;
		}
		public GoalTest getGoalTest() {
			return goalTest;
		}
		public void setGoalTest(GoalTest goalTest) {
			this.goalTest = goalTest;
		}
		public HeuristicFunction getHeuristicFunction() {
			return heuristicFunction;
		}
		public void setHeuristicFunction(HeuristicFunction heuristicFunction) {
			this.heuristicFunction = heuristicFunction;
		}
		public ResultFunction getResultFunction() {
			return resultFunction;
		}
		public void setResultFunction(ResultFunction resultFunction) {
			this.resultFunction = resultFunction;
		}
		public ActionsFunction getActionsFunction() {
			return actionsFunction;
		}
		public void setActionsFunction(ActionsFunction actionsFunction) {
			this.actionsFunction = actionsFunction;
		}
		public SearchType getSearchType() {
			return searchType;
		}
		public void setSearchType(SearchType searchType) {
			this.searchType = searchType;
		}
		public SearchStrategy getSearchStrategy() {
			return searchStrategy;
		}
		public void setSearchStrategy(SearchStrategy searchStrategy) {
			this.searchStrategy = searchStrategy;
		}
		public boolean isAllowNodeRemoval() {
			return allowNodeRemoval;
		}
		public void setAllowNodeRemoval(boolean allowNodeRemoval) {
			this.allowNodeRemoval = allowNodeRemoval;
		}
		public boolean isPreventGaps() {
			return preventGaps;
		}
		public void setPreventGaps(boolean preventGaps) {
			this.preventGaps = preventGaps;
		}
		public boolean isAllowEarlyGoalTest() {
			return allowEarlyGoalTest;
		}
		public void setAllowEarlyGoalTest(boolean allowEarlyGoalTest) {
			this.allowEarlyGoalTest = allowEarlyGoalTest;
		}
		public PartitioningType getInitialStatePartitioningType() {
			return initialStatePartitioningType;
		}
		public void setInitialStatePartitioningType(PartitioningType initialStatePartitioningType) {
			this.initialStatePartitioningType = initialStatePartitioningType;
		}
		
		
		
	}
