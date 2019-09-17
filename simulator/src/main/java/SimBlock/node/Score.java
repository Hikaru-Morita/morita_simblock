package SimBlock.node;

import java.util.HashMap;
import java.util.Map;

public class Score{
	private Map<Node,Long> scores = new HashMap<Node,Long>();
	private Node from; 
	private long t_inv;
	private long t_block;
	private long score;
	private long average_score;

	public 	Map<Node,Long> getScores(){return scores;}
	public long getAverageScore(){return average_score;}

	public void updateScore(Node from, long t_block){
		scores.put(from,t_block);
	}

	// public long getAverageScore(){
	// 	for(Node score; scores)
	// }
}