import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import dembois.heuristics.GoalProximityHeuristic;
import dembois.heuristics.Heuristic;
import dembois.heuristics.MobilityHeuristic;
import dembois.heuristics.OpponentMobilityHeuristic;
import dembois.heuristics.WeightedHeuristic;

public class WeightedHeuristicFixedDepthPlayer extends BaseDemBoisGamer {

	@Override
	public StateMachine getInitialStateMachine() {
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
		long actual_time=(long) (timeout-4*Math.pow(10,3));
		StateMachine machine = getStateMachine();
		MachineState state= getCurrentState();
		List<Role> roles = machine.getRoles();
		Role role = getRole();
		Map<Heuristic, Double> hMap = new HashMap<>();
		hMap.put(new MobilityHeuristic(0), 0.2);
		hMap.put(new GoalProximityHeuristic(), 0.6);
		hMap.put(new OpponentMobilityHeuristic(0), 0.2);
		if(roles.size()==1){
			return getDLDeliberationMove(role, state, new WeightedHeuristic(hMap), actual_time);
		}
		else{
			return getDepthFirstDLMove(role, state, new WeightedHeuristic(hMap), actual_time);
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
		return "WeightedHeuristicFixedDepthPlayer";
	}

}
