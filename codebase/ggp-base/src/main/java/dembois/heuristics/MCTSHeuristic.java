package dembois.heuristics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class MCTSHeuristic implements Heuristic {

	private Map<Role, Role> nextPlayerMap;
	private Map<Role, Role> prevPlayerMap;
	private Map<MachineState, Node> agentNodes;
	private int charges;
	private int C;
	public MCTSHeuristic(StateMachine machine, int charges, int C){
		initPlayerMaps(machine);
		agentNodes = new HashMap<MachineState, Node>();
		this.charges = charges;
		this.C = C;
	}

	@Override
	public int getValue(Role role, MachineState state, StateMachine machine, long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		Node root;
		if(agentNodes.containsKey(state)){
			System.out.println("Node found!");
			root = agentNodes.get(state);
		}else{
			System.out.println("Node not found.");
			root = new Node(null, prevPlayer(role), state, null);
		}
		while(System.currentTimeMillis() < timeout){
			mcts(root, role, machine, timeout);
		}
		return getBestValue(root, role);
	}

	public Move getMove(Role role, MachineState state, StateMachine machine, long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		Node root;
		if(agentNodes.containsKey(state)){
			System.out.println("Node found!");
			root = agentNodes.get(state);
		}else{
			System.out.println("Node not found.");
			root = new Node(null, prevPlayer(role), state, null);
		}
		while(System.currentTimeMillis() < timeout){
			mcts(root, role, machine, timeout);
		}
		return getBestMove(root, role);
	}

	private Move getBestMove(Node node, Role role){
		int best = 0;
		Move move = null;
		for(Node child : node.getChildren()){
			Move curMove = child.getMove();
			double curVal = child.getValue() / Math.max(1, child.getVisits());
			System.out.println(curMove.toString()+", "+child.getVisits()+", "+curVal+"; "+(int)(uct(child)));
			if(curVal > best){
				best = (int) (curVal);
				move = curMove;
			}
		}
		return move;
	}

	private int getBestValue(Node node, Role role){
		int best = 0;
		Move move = null;
		for(Node child : node.getChildren()){
			if(child.getValue()/child.getVisits() > best){
				best = (int) (child.getValue()/child.getVisits());
				move = child.getMove();
			}
		}
		return best;
	}

	private void initPlayerMaps(StateMachine machine){
		List<Role> roles = machine.findRoles();
		nextPlayerMap = new HashMap<Role, Role>();
		prevPlayerMap = new HashMap<Role, Role>();
		for(int i=1;i<roles.size()-1;i++){
			nextPlayerMap.put(roles.get(i), roles.get(i+1));
			prevPlayerMap.put(roles.get(i), roles.get(i-1));
		}
		prevPlayerMap.put(roles.get(0), roles.get(roles.size()-1));
		if(roles.size() > 1){
			prevPlayerMap.put(roles.get(roles.size()-1), roles.get(roles.size()-2));
			nextPlayerMap.put(roles.get(0), roles.get(1));
		}
		nextPlayerMap.put(roles.get(roles.size()-1), roles.get(0));
	}

	private Role prevPlayer(Role role){
		return prevPlayerMap.get(role);
	}

	private Role nextPlayer(Role role){
		return nextPlayerMap.get(role);
	}

	private void mcts(Node node, Role agentRole, StateMachine machine, long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		Node selectedNode = node;
		while((selectedNode.getVisits() > 0 && !machine.isTerminal(selectedNode.getState())) && System.currentTimeMillis() < timeout){
			selectedNode = select(selectedNode, machine);
		}
		Map<Role,Integer> result;
		if(machine.isTerminal(selectedNode.getState())){
			result = simulate(selectedNode, machine);
			backpropagate(selectedNode, result);
			return;
		}
		if(selectedNode.getVisits() == 0){
			expand(selectedNode, agentRole, machine);
			result = simulate(selectedNode, machine);
			backpropagate(selectedNode, result);
			return;
		}
	}

	private Node select(Node node, StateMachine machine) throws MoveDefinitionException, TransitionDefinitionException{
		if(node.getVisits() == 0){
			return node;
		}
		for(Node child : node.getChildren()){
			if(child.getVisits() == 0){
				return child;
			}
		}
		int score = -1;
		Node result = node;
		for(Node child : node.getChildren()){
			int newScore = (int)uct(child);
			if(newScore >= score){
				score = newScore;
				result = child;
			}
		}
		return result;
	}

	private List<Move> moveMapToList(Map<Role,Move> map, StateMachine machine){
		List<Role> roles = machine.getRoles();
		List<Move> moves = new ArrayList<Move>();
		for(Role role : roles){
			moves.add(map.get(role));
		}
		return moves;
	}

	private void expand(Node node, Role agentRole, StateMachine machine) throws TransitionDefinitionException, MoveDefinitionException{
		Role roleForChildren = nextPlayer(node.getPlayer());
		List<Move> moveList = machine.getLegalMoves(node.getState(), roleForChildren);
		boolean nextIsAgent = nextPlayer(roleForChildren).equals(agentRole);
		for(Move move : moveList){
			Node child = new Node(node, roleForChildren, node.getState(), move);
			if(nextIsAgent){
				MachineState successor = machine.getNextState(child.getState(), moveMapToList(child.getMoveMap(), machine));
				child.resetState(successor);
				agentNodes.put(successor, child);
			}
			node.addChild(child);
		}
	}

	private Map<Role,Integer> simulate(Node node, StateMachine machine) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
		MachineState state = node.getState();
		List<Role> roles = machine.getRoles();
		if(!machine.isTerminal(state) && !node.isMoveMapEmpty()){
			Map<Role, Move> moveMap = node.getMoveMap();
			Random r = new Random();
			for(Role role : roles){
				if(!moveMap.containsKey(role)){
					List<Move> moves = machine.getLegalMoves(state, role);
					moveMap.put(role, moves.get(r.nextInt(moves.size())));
				}
			}
			state = machine.getNextState(state, moveMapToList(moveMap, machine));
		}
		Map<Role, Integer> goalMap = new HashMap<Role, Integer>();
		for(Role r : roles){
			goalMap.put(r, 0);
		}
		for(int c=0;c<charges;c++){
			List<Integer> goals;
			if(machine.isTerminal(state)){
				goals = machine.getGoals(state);
			}else{
				goals = machine.getGoals(machine.performDepthCharge(state, new int[1]));
			}
			for(int i=0;i<roles.size();i++){
				goalMap.put(roles.get(i), goalMap.get(roles.get(i)) + goals.get(i)/charges);
			}
		}
		return goalMap;
	}

	private void backpropagate(Node node, Map<Role,Integer> goalMap){
		node.visit();
		node.addValue((double)goalMap.get(node.getPlayer()));
		if(node.getParent() != null){
			backpropagate(node.getParent(), goalMap);
		}
	}

	private double uct(Node node){
		return node.getValue()/Math.max(1, node.getVisits()) + this.C * Math.sqrt(Math.log(node.getParent().getVisits()) / Math.max(node.getVisits(), 1));
	}

	private static class Node{
		private Map<Role,Move> moveMap;
		private List<Node> children;
		private int visits;
		private double utility;
		private MachineState state;
		private Node parent;
		private Role player;
		private Move move;
		public Node(Node parent, Role player, MachineState state, Move move){
			this.children = new ArrayList<Node>();
			this.visits = 0;
			this.utility = 0;
			this.state = state;
			this.parent = parent;
			this.player = player;
			this.move = move;
			if(parent != null){
				this.moveMap = parent.getMoveMap();
			}else{
				this.moveMap = new HashMap<Role,Move>();
			}
			if(move != null){
				this.moveMap.put(player,move);
			}
		}

		public Move getMove(){
			return move;
		}

		public boolean isMoveMapEmpty(){
			return moveMap.isEmpty();
		}

		public Map<Role,Move> getMoveMap(){
			Map<Role,Move> newMap = new HashMap<Role,Move>();
			for(Map.Entry<Role,Move> entry : moveMap.entrySet()){
				newMap.put(entry.getKey(), entry.getValue());
			}
			return newMap;
		}

		public Role getPlayer(){
			return player;
		}

		public MachineState getState(){
			return state;
		}

		public void resetState(MachineState state){
			this.state = state;
			this.moveMap = new HashMap<Role,Move>();
		}

		public Node getParent(){
			return parent;
		}

		public double getValue(){
			return utility;
		}

		public void addValue(double value){
			utility += value;
		}

		public void visit(){
			visits += 1;
		}

		public int getVisits(){
			return visits;
		}

		public void addChild(Node newChild){
			children.add(newChild);
		}

		public List<Node> getChildren(){
			return children;
		}
	}
}
