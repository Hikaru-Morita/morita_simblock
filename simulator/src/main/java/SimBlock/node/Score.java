 
package SimBlock.node;
import SimBlock.simulator.Main;

import java.util.HashMap;
import java.util.Map;

public class Score{
	private Map<Node,Double> scores = new HashMap<Node,Double>();
	private double score = 0;
	public static double para = 0.6;
	private Node worst;
	private Node this_node;

	private static long score_count = 0;
	private static double average_score = 0;

	Score(Node node){
		this_node = node;
	}

	public static double getAverageScore(){return average_score/score_count;}
	public Map<Node,Double> getScores(){return scores;}
	public double getScore(Node node){return scores.get(node);}

	public void addScore(Node from, long t_inv, long t_block){

		// calculate score
		if(scores.get(from) == null){
			score = (t_inv-t_block);
			scores.put(from, score);
		}
		else{
			score = scores.get(from);
			scores.remove(from);
			score = (1-para)*(score)+para*(t_inv-t_block);
			scores.put(from, score);
		}

		// calcuate average of all neighbor nodes score 
		score_count++;
		average_score = average_score + score;
	}

	public Node getWorstNode(){
		if(scores.keySet() != null){
			for(Node i: scores.keySet()){
				worst = i;
				break;
			}
			for(Node i: scores.keySet()){
				if(scores.get(worst)<scores.get(i)){
					worst = i;
				}
			}
		}else{
			System.out.println("getWorstNode: error     " + worst);
			worst = this_node;
		}
		return worst;
	}

	public Node getWorstNodeWithRemove(){
		worst = scores.keySet().iterator().next();
		for(Node i: scores.keySet()){
			if(scores.get(worst)<scores.get(i)){
				worst = i;
			}
		}
		scores.remove(worst);
		return worst;
	}

}