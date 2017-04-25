package dembois.heuristics;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class OpponentMobilityHeuristic implements Heuristic {

	private MobilityHeuristic heuristic;
	public OpponentMobilityHeuristic(int numSteps){
		this.heuristic = new MobilityHeuristic(numSteps);
	}
	@Override
	public int getValue(Role role, MachineState state, StateMachine machine, long timeout) throws MoveDefinitionException, TransitionDefinitionException {
		List<Role> roles = machine.getRoles();
		int opponentCount = 0;
		int opponentScoreSum = 0;
		for(Role opp : roles){
			if(opp.equals(role)){
				continue;
			}
			opponentCount += 1;
			opponentScoreSum = heuristic.getValue(opp, state, machine, timeout);
		}
		if(opponentCount == 0){
			return 100;
		}
		return 100 - opponentScoreSum / opponentCount;
	}

}
