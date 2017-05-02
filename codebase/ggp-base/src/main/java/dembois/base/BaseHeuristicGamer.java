package dembois.base;
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

import dembois.heuristics.Heuristic;

public abstract class BaseHeuristicGamer extends StateMachineGamer{

	protected Move getDLDeliberationMove(Role role, MachineState state, Heuristic heuristic, long heuristicBeginTime, long timeout) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
		Move result = null;
		Map<Move, List<MachineState>> moves_to_states = getStateMachine().getNextStates(state, role);

		int maxScore = 0;
		long depth=(long) (System.currentTimeMillis());
		for(Move m: moves_to_states.keySet()){
			MachineState s = moves_to_states.get(m).get(0); //assuming this is single player, each move will only return one state
			int value = getDLDeliberationMaxScore(role, s,depth, heuristic, heuristicBeginTime, timeout);
			if(value == 100) {return m;}
			if(value > maxScore) {
				result = m;
				maxScore = value;
			}
		}
		return result;
	}

	private int getDLDeliberationMaxScore(Role role, MachineState state, long depth, Heuristic heuristic, long heuristicBeginTime, long timeout) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
		if(getStateMachine().isTerminal(state)){
			return getStateMachine().getGoal(state, role);
		}
		int score = 0;
		if(depth>timeout){
			return heuristic.getValue(role,state,getStateMachine(),timeout);
		}
		depth=(long) (System.currentTimeMillis());
		Map<Move, List<MachineState>> moves_to_states = getStateMachine().getNextStates(state, role);
		for(Move m: moves_to_states.keySet()){
			MachineState s = moves_to_states.get(m).get(0);
			int currentScore = getDLDeliberationMaxScore(role, s,depth, heuristic, heuristicBeginTime, timeout);
			if(currentScore > score) score = currentScore;
		}
		return score;
	}

	protected Move getDLAlphaBetaMove(Role role, MachineState state, Heuristic heuristic, long heuristicBeginTime, long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		StateMachine machine = getStateMachine();
		List<Move> actions = machine.getLegalMoves(state, role);
		Move action = actions.get(0);
		if (actions.size() == 1){
			return action;
		}
		int score=0;
		int alpha=0;
		int beta=100;
		long depth = (long) (System.currentTimeMillis());
		for(int i=0;i<actions.size();i++)
		{
			int result= this.getDLAlphaBetaMinScore(role, actions.get(i), state, alpha, beta, depth, heuristic, heuristicBeginTime, timeout);
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

	private int getDLAlphaBetaMinScore(Role role, Move action, MachineState state, int alpha, int beta, long depth, Heuristic heuristic, long heuristicBeginTime, long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		StateMachine machine = getStateMachine();
		List<List<Move>> moveSets = machine.getLegalJointMoves(state, role, action);
		for(List<Move> moveSet : moveSets){
			MachineState newState = machine.getNextState(state, moveSet);
			int newScore = this.getDLAlphaBetaMaxScore(role, newState, alpha, beta, depth, heuristic, heuristicBeginTime, timeout);
			if(newScore==0){
				return 0;
			}
			beta = Math.min(beta, newScore);
			if(beta<=alpha){
				return alpha;
			}
		}
		return beta;
	}

	private int getDLAlphaBetaMaxScore(Role role, MachineState state, int alpha, int beta, long depth, Heuristic heuristic, long heuristicBeginTime, long timeout) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
		StateMachine machine = getStateMachine();
		if(machine.isTerminal(state))
		{
			return machine.getGoal(state, role);
		}
		if(depth>=heuristicBeginTime){
			return heuristic.getValue(role,state,machine,timeout);
		}
		depth=(long) (System.currentTimeMillis()) ;
		List<Move> actions = machine.getLegalMoves(state, role);
		for(int i=0;i<actions.size();i++){
			int result=this.getDLAlphaBetaMinScore(role, actions.get(i), state, alpha, beta, depth, heuristic, heuristicBeginTime, timeout);
			alpha=Math.max(alpha,result);
			if(alpha>=beta){
				return beta;
			}
		}
		return alpha;
	}

}
