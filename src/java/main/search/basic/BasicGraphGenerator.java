package search.basic;
import java.util.Random;

import org.jgrapht.graph.SimpleGraph;

public interface BasicGraphGenerator 
{
	SimpleGraph<Node,Border> generate(int num,Random rand);
}
