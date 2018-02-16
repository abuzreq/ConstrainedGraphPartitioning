# About
This is the source code of the algorithm described in the paper:
"On Using Graph Partitioning with Isomorphism Constraint in Procedural Content Generation"
presented at PCG Workshop 2017 part of FDG 2017

You can see the presentation at:
https://www.youtube.com/watch?v=Te2ek89EEUs


As is explained in the presentation, two ways of introducing variations are available. The first is when the Basic Graph is fixed (e.g. a fixed level with its underlying navmesh or a fixed 2D grid graph) and in this case variations are introduced through a stochastic initial partitioning. In the second mode, the Basic Graph can change and in this can a generator for this Basic Graph must be provided (e.g. a Voronoi map generator) a determinisitc intial partitioning is done in this case. 

### First Mode:
The class SameBasicGraphStochasticPartitioningExample in java/tests/examples is a good place to start with the first mode.
The class SameBasicGraphStochasticPartitioningActionsVisualizer allows you to use the Right and Left arrow keys to see the effect of each of actions that are found along the path from the initial partitioning to the final partitioning.

### Second Mode:
The class DifferentBasicGraphDeterministicPartitioningExample in java/tests/examples is a good place to start with the second mode.
The class DifferentBasicGraphDeterministicPartitioningActionsVisualizer allows you to use the Right and Left arrow keys to see the effect of each of actions that are found along the path from the initial partitioning to the final partitioning.

### Isomorphism Mapping
As is explained in the presentation, we can assign properties to the nodes in the Constraint Graph. After the result is found, we can find the partitions that were mapped to those nodes and assign them those properties.
Examples illustrating this technique can be found under java/tests/isomorphism_mapping which illustrate the examples mentioned in the presentation.

### Initial State Sensetivity and Random Restarting
The class InitialStateSensitivityRunsAnalysis is an illustration of the sensetivity of the initial state. It also illustrated how a Restart Policy is implemented to solve this problem.

### Notes:
You can use the TestUtils class (under java/tests/util) to read Basic and Constraint graphs from files

# Installation:
This is a Gradle project. You use an IDE like Eclipse (after having the the Gradle Integration installed)
