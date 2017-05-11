package dembois.montecarlo;

import org.ggp.base.player.gamer.exception.GamePreviewException;
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

import dembois.base.BaseHeuristicGamer;
import dembois.heuristics.MCTSHeuristic;

public class MCTSGamer extends BaseHeuristicGamer {
	private MCTSHeuristic mcts;
	@Override
	public StateMachine getInitialStateMachine() {
		return new CachedStateMachine(new ProverStateMachine());
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		mcts = new MCTSHeuristic(getStateMachine(),1,50);
		long submit_timeout = (long)(timeout - 3*Math.pow(10, 3));
		StateMachine machine = getStateMachine();
		MachineState state = getCurrentState();
		Role role = getRole();
		mcts.getMove(role, state, machine, submit_timeout);
	}

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		long submit_timeout = (long)(timeout - 3*Math.pow(10,3));
		StateMachine machine = getStateMachine();
		MachineState state = getCurrentState();
		Role role = getRole();
		return mcts.getMove(role, state, machine, submit_timeout);
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
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "MCTSPlayer";
	}
}