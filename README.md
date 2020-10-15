# Update
A more mature formulation can be found in my subsequent paper:
Abuzuraiq, A. M., Ferguson, A., & Pasquier, P. (2019, August). [Taksim: A Constrained Graph Partitioning Framework for Procedural Content Generation](https://abuzreq.netlify.app/pdfs/inproceedings/taksim_2019.pdf). In 2019 IEEE Conference on Games (CoG) (pp. 1-8). IEEE.

The presentaion of Taksim can be seen [Here](https://www.youtube.com/watch?v=Bu3m_7-3Tm4&list=FL-Z_nrHntYILlQn017zopHQ&index=2&t=2331s).
The source code for Taksim can be found [Here](https://github.com/abuzreq/Taksim)

# About
This is the source code of the algorithm described in the paper:
Abuzuraiq, A. M. (2017, August). On using graph partitioning with isomorphism constraint in procedural content generation. In Proceedings of the 12th International Conference on the Foundations of Digital Games (p. 77). ACM.

You can see the presentation [here](https://www.youtube.com/watch?v=Te2ek89EEUs)

As is explained in the presentation, two ways of introducing variations are available. The first is when the Basic Graph is fixed (e.g. a fixed level with its underlying navmesh or a fixed 2D grid graph) and in this case variations are introduced through a stochastic initial partitioning. In the second mode, the Basic Graph can change and in this can a generator for this Basic Graph must be provided (e.g. a Voronoi map generator) a deterministic initial partitioning is done in this case. 

### First Mode:
The class SameBasicGraphStochasticPartitioningExample in java/tests/examples is a good place to start with the first mode.
The class SameBasicGraphStochasticPartitioningActionsVisualizer allows you to use the Right and Left arrow keys to see the effect of each of actions that are found along the path from the initial partitioning to the final partitioning.

### Second Mode:
The class DifferentBasicGraphDeterministicPartitioningExample in java/tests/examples is a good place to start with the second mode.
The class DifferentBasicGraphDeterministicPartitioningActionsVisualizer allows you to use the Right and Left arrow keys to see the effect of each of actions that are found along the path from the initial partitioning to the final partitioning.

### Isomorphism Mapping
As is explained in the presentation, we can assign properties to the nodes in the Constraint Graph. After the result is found, we can find the partitions that were mapped to those nodes and assign them those properties.
Examples illustrating this technique can be found under java/tests/isomorphism_mapping which illustrate the examples mentioned in the presentation.

### Initial State Sensitivity and Random Restarting
The class InitialStateSensitivityRunsAnalysis is an illustration of the sensitivity of the initial state. It also illustrates how a Restart Policy is implemented to solve this problem.

### Notes:
You can use the TestUtils class (under java/tests/util) to read Basic and Constraint graphs from files

# Installation:
This is a Gradle project. You can use an IDE like Eclipse (after having the the Gradle Integration installed)

# Contact
I am interested in making this code accessible and easy to use to game developers and researchers alike, if you faced problems or have any suggestions, feel free to contact me at  abuzreq at gmail dot com
