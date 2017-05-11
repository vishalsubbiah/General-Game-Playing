package MonteCarloMultithreaded;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

public class MonteCarloSearchMultithreaded extends StateMachineGamer {

	@Override
	public StateMachine getInitialStateMachine() {
		// TODO Auto-generated method stub
		return new CachedStateMachine(new ProverStateMachine());
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub
	}

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub
		long actual_time=(long) (timeout-10*Math.pow(10,3));
		StateMachine machine = getStateMachine();
		MachineState state= getCurrentState();
		List<Role> roles = machine.getRoles();
		Role role = getRole();
		if(roles.size()==1){
			return DeliberationMove(role, state, actual_time);
		}
		else{
			return ABMove(role, state, actual_time);
		}

	}

	@Override
	public void stateMachineStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stateMachineAbort() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return "MonteCarloSearchPlayer";
	}

	protected Move DeliberationMove(Role role, MachineState state, long timeout) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
		Move result = null;
		Map<Move, List<MachineState>> moves_to_states = getStateMachine().getNextStates(state, role);
		if(getStateMachine().getLegalMoves(state, role).size()==1){
			return getStateMachine().getLegalMoves(state, role).get(0);
		}
		int maxScore = 0;
		long depth=(long) (System.currentTimeMillis());
		for(Move m: moves_to_states.keySet()){
			MachineState s = moves_to_states.get(m).get(0); //assuming this is single player, each move will only return one state
			int value = Onemaxscore(role, s,depth, timeout);
			if(value == 100) {return m;}
			if(value > maxScore) {
				result = m;
				maxScore = value;
			}
		}
		System.out.println("12");
		return result;
	}

	private int Onemaxscore(Role role, MachineState state,long depth, long timeout) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
		if(getStateMachine().isTerminal(state)){
			return getStateMachine().getGoal(state, role);
		}
		int score = 0;
		if(depth>timeout){
			return MCHeuristic(role, state, 4);
		}
		Map<Move, List<MachineState>> moves_to_states = getStateMachine().getNextStates(state, role);
		for(Move m: moves_to_states.keySet()){
			depth=(long) (System.currentTimeMillis());
			MachineState s = moves_to_states.get(m).get(0);
			int currentScore = Onemaxscore(role, s, depth, timeout);
			if(currentScore > score) score = currentScore;
		}
		return score;
	}

	private int MCHeuristic(Role role, MachineState state, int count) throws GoalDefinitionException, TransitionDefinitionException, MoveDefinitionException{
		int total = 0;
	     for (int i=0; i<count; i++)
	         {total = total + depthcharge(role,state);}
	     return total/count;
	}

	private int depthcharge(Role role, MachineState state) throws GoalDefinitionException, TransitionDefinitionException, MoveDefinitionException{
		StateMachine machine = getStateMachine();
		if(machine.isTerminal(state)){
			return machine.getGoal(state, role);
		}
		List<Role> roles=machine.getRoles();
		int No_roles=roles.size();
		List<Move> moves = new ArrayList<>();
		for(int i=0;i<No_roles;i++){
	          moves.add(getStateMachine().getRandomMove(state, roles.get(i))); //concurrency will happen here
	    }
	    MachineState newstate = getStateMachine().getNextState(state, moves);
	    return depthcharge(role,newstate);
	}

	protected Move ABMove(Role role, MachineState state, long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		//long actual_time=timeout-3;
		if(getStateMachine().getLegalMoves(state, role).size()==1){
			return getStateMachine().getLegalMoves(state, role).get(0);
		}
		StateMachine machine = getStateMachine();
		List<Move> actions = machine.getLegalMoves(state, role);
		Move action = actions.get(0);
		int score=0;
		int alpha=0;
		int beta=100;

		for(int i=0;i<actions.size();i++)
		{
			long depth = (long) (System.currentTimeMillis());
			int result= this.ABMinScore(role, actions.get(i), state, alpha, beta, depth, timeout);
			if(result==100){
				return actions.get(i);
			}

			if(result>score){
				score=result;
				action=actions.get(i);
			}
		}
		System.out.println("12");
		return action;
	}

	private int ABMinScore(Role role, Move action, MachineState state, int alpha, int beta, long depth, long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		StateMachine machine = getStateMachine();
		List<List<Move>> moveSets = machine.getLegalJointMoves(state, role, action);
		for(List<Move> moveSet : moveSets){
			depth = (long) (System.currentTimeMillis());
			MachineState newState = machine.getNextState(state, moveSet);
			int newScore = this.ABMaxScore(role, newState, alpha, beta, depth, timeout);
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

	private int ABMaxScore(Role role, MachineState state, int alpha, int beta, long depth, long timeout) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
		StateMachine machine = getStateMachine();
		if(machine.isTerminal(state))
		{
			return machine.getGoal(state, role);
		}
		if(depth>=timeout){

			return MCHeuristic(role, state, 4);
		}
		List<Move> actions = machine.getLegalMoves(state, role);
		for(int i=0;i<actions.size();i++){
			depth=(long) (System.currentTimeMillis());
			int result=this.ABMinScore(role, actions.get(i), state, alpha, beta, depth, timeout);
			alpha=Math.max(alpha,result);
			if(alpha>=beta){
				return beta;
			}
		}
		return alpha;
	}

}
