package SimBlock.node;
import SimBlock.simulator.Main;

import java.util.HashMap;
import java.util.Map;

public class Score{
	private Map<Node,Double> scores = new HashMap<Node,Double>();
	private double score = 0;
	public static double para = 0.001;
	private Node worst;

	public static long bft_flag = 0;
	public static double average_bft;
	public static long count;

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
		int i = 0;	
		double average_score = 0;
		
		for(double val : scores.values()){
			average_score = average_score + val;
			i++;
		}
		return average_score/i;
	}

	public Node getWorstNode(){
		worst = scores.keySet().iterator().next();
		for(Node i: scores.keySet()){
			// System.out.println(scores.get(i));
			if(scores.get(worst)<scores.get(i)){
				worst = i;
			}
		}
		// System.out.println("\n" + scores.get(worst) + "\n");
		scores.remove(worst);
		return worst;
	}

	public void addScore(Node from, long t_inv, long t_block){
		if(scores.get(from) == null){
			score = (t_inv-t_block);
			scores.put(from, score);
			// System.out.println("first");
		}
		else{
			score = scores.get(from);
			scores.remove(from);
			score = (1-para)*(score)+para*(t_inv-t_block);
			scores.put(from, score);
			// System.out.println("more than once");
		}
	}
	// public long getAverageScore(){
	// 	for(Node score; scores)
	// }
}