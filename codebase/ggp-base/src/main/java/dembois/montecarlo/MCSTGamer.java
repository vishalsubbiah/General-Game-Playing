package dembois.montecarlo;

import java.util.List;

import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import dembois.base.BaseHeuristicGamer;
import dembois.heuristics.MonteCarloHeuristic;

public class MCSTGamer extends BaseHeuristicGamer {

	@Override
	public StateMachine getInitialStateMachine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub

	}

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		long heuristic_begin_time=(long) (System.currentTimeMillis() + (timeout - System.currentTimeMillis())/10);
		long submit_timeout = (long)(timeout - 3*Math.pow(10,3));
		StateMachine machine = getStateMachine();
		MachineState state= getCurrentState();
		List<Role> roles = machine.getRoles();
		Role role = getRole();
		if(roles.size()==1){
			return getDLDeliberationMove(role, state, new MonteCarloHeuristic(4), heuristic_begin_time, submit_timeout);
		}
		else{
			return getDLAlphaBetaMove(role, state, new MonteCarloHeuristic(4), heuristic_begin_time, submit_timeout);
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
		// TODO Auto-generated method stub
		return "MCSTPlayer";
	}

}
