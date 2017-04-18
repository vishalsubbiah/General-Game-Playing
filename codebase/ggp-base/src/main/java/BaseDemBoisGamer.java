import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public abstract class BaseDemBoisGamer extends StateMachineGamer{
	protected Move getCompulsiveDeliberationMove(Role role, MachineState state, long timeout) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
		Move result = null;
		Map<Move, List<MachineState>> moves_to_states = getStateMachine().getNextStates(state, role);

		int maxScore = 0;
		for(Move m: moves_to_states.keySet()){
			MachineState s = moves_to_states.get(m).get(0); //assuming this is single player, each move will only return one state
			if(getDeliberationMaxScore(role, s) == 100) {return m;}
			if(getDeliberationMaxScore(role, s) > maxScore) {result = m;}
		}
			return result;
	}

	private int getDeliberationMaxScore(Role role, MachineState state) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
		if(getStateMachine().isTerminal(state)){
			return getStateMachine().getGoal(state, role);
		}
		int score = 0;
		Map<Move, List<MachineState>> moves_to_states = getStateMachine().getNextStates(state, role);
		for(Move m: moves_to_states.keySet()){
			MachineState s = moves_to_states.get(m).get(0);
			int currentScore = getDeliberationMaxScore(role, s);
			if(currentScore > score) score = currentScore;
		}
		return score;
	}

	protected Move getMinimaxMove(Role role, MachineState state, long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		StateMachine machine = getStateMachine();
		List<Move> actions = machine.getLegalMoves(state, role);
		Move action = actions.get(0);
		int score = 0;
		for(int i=0;i<actions.size();i++){
			int newscore = this.getMMMinScore(role, actions.get(i), state);
			if(newscore > score){
				score = newscore;
				action = actions.get(i);
			}
		}
		return action;
	}

	private int getMMMinScore(Role role, Move action, MachineState state) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		StateMachine machine = getStateMachine();
		List<List<Move>> moveSets = this.getPossibleResponseMovesets(role, action, state);
		int score = 100;
		for(List<Move> moveSet : moveSets){
			MachineState newState = machine.getNextState(state, moveSet);
			int newScore = this.getMMMaxScore(role, newState);
			if(newScore < score){
				score = newScore;
			}
		}
		return score;
	}

	private List<List<Move>> getPossibleResponseMovesets(Role role, Move action, MachineState state) throws MoveDefinitionException{
		StateMachine machine = getStateMachine();
		Map<Role, List<Move>> legalMoveMap = new HashMap<Role, List<Move>>();
		List<Role> players = machine.getRoles();
		int numMovesets = 1;
		for(Role player : players){
			if(player == role){
				List<Move> legalMoves = new ArrayList<Move>();
				legalMoves.add(action);
				legalMoveMap.put(player, legalMoves);
				continue;
			}
			List<Move> legalMoves = machine.getLegalMoves(state, player);
			numMovesets *= legalMoves.size();
			legalMoveMap.put(player, legalMoves);
		}
		List<List<Move>> moveSets = new ArrayList<List<Move>>();
		for(int i=0;i<numMovesets;i++){
			moveSets.add(new ArrayList<Move>());
		}
		int scopeSize = numMovesets;
		for(Role player : players){
			if(player == role){
				for(List<Move> moveSet : moveSets){
					moveSet.add(action);
				}
				continue;
			}
			List<Move> legalMoves = legalMoveMap.get(player);
			int numMoves = legalMoves.size();
			for(int tiling=0;tiling<numMovesets/scopeSize;tiling++){
				for(int i=0;i<numMoves;i++){
					Move move = legalMoves.get(i);
					for(int t=0;t<scopeSize/numMoves;t++){
						moveSets.get(t + i*(scopeSize/numMoves) + tiling*scopeSize).add(move);
					}
				}
			}
			scopeSize = scopeSize/numMoves;
		}
		return moveSets;
	}

	private int getMMMaxScore(Role role, MachineState state) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
		StateMachine machine = getStateMachine();
		if(machine.isTerminal(state)){
			return machine.getGoal(state, role);
		}
		List<Move> actions = machine.getLegalMoves(state, role);
		int score = 0;
		for(int i=0;i<actions.size();i++){
			int newscore = this.getMMMinScore(role, actions.get(i), state);
			if(newscore > score){
				score = newscore;
			}
		}
		return score;
	}

	protected Move getAlphaBetaMove(Role role, MachineState state, long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		StateMachine machine = getStateMachine();
		List<Move> actions = machine.getLegalMoves(state, role);
		Move action = actions.get(0);
		int score=0;
		int alpha=0;
		int beta=100;
		for(int i=0;i<actions.size();i++)
		{
			//int alpha=0;
			//int beta=100;
			int result= this.getABMinScore(role, actions.get(i), state, alpha, beta);
			if(result==100){
				return actions.get(i);
			}
			if(result>score){
				score=result;
				action=actions.get(i);
			}
		}
		return action;
	}

	private int getABMinScore(Role role, Move action, MachineState state, int alpha, int beta) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		StateMachine machine = getStateMachine();
		List<List<Move>> moveSets = this.getPossibleResponseMovesets(role, action, state);
		for(List<Move> moveSet : moveSets){
			MachineState newState = machine.getNextState(state, moveSet);
			int newScore = this.getABMaxScore(role, newState, alpha, beta);
			beta = Math.min(beta, newScore);
			if(beta<=alpha){
				return alpha;
			}
		}
		return beta;
	}

	private int getABMaxScore(Role role, MachineState state, int alpha, int beta) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
		StateMachine machine = getStateMachine();
		if(machine.isTerminal(state))
		{
			return machine.getGoal(state, role);
		}
		List<Move> actions = machine.getLegalMoves(state, role);
		for(int i=0;i<actions.size();i++){
			int result=this.getABMinScore(role, actions.get(i), state, alpha, beta);
			alpha=Math.max(alpha,result);
			if(alpha>=beta){
				return beta;
			}
		}
		return alpha;
	}
}
