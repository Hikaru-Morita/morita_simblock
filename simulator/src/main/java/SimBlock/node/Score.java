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

	public static long bft_flag = 0;
	public static double average_bft;
	public static long count;

	public static long score_flag = 0;
	public static int score_count = 0;
	public static double average_score = 0;


	Score(Node node){
		this_node = node;
	}

	public static void addBFT(long bft){
		if(bft_flag == 0){
			average_bft = bft;
			bft_flag = 1;
		}else if(bft_flag == 1){
			average_bft = average_bft + bft;
		}
		count = count + 1;
	}

	public static double getAverageBFT(){
		return average_bft/count;
	}

	public 	Map<Node,Double> getScores(){return scores;}
	public double getScore(Node node){return scores.get(node);}
	public double getAverageScore(){
		return average_score/score_count;
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

	public void addScore(Node from, long t_inv, long t_block){
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

		score_count++;
		if(score_flag == 0){
			average_score = score;
			score_flag = 1;
		}else if(score_flag == 1){
			average_score = average_score + score;
		}
	}
}