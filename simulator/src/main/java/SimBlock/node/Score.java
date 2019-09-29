package SimBlock.node;
import SimBlock.simulator.Main;

import java.util.HashMap;
import java.util.Map;

public class Score{
	private Map<Node,Double> scores = new HashMap<Node,Double>();
	private double score = 0;
	public static double para = 0.4;
	private Node worst;

	public static long bft_flag = 0;
	public static double average_bft;
	public static long count;

	private int score_count = 0;
	private double average_score = 0;

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
		return average_score;
	}

	public Node getWorstNode(){
		worst = scores.keySet().iterator().next();
		for(Node i: scores.keySet()){
			if(scores.get(worst)<scores.get(i)){
				worst = i;
			}
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
		

	}
}