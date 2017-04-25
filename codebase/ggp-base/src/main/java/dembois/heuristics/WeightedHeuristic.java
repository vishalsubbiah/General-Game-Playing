package dembois.heuristics;

import java.util.Map;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class WeightedHeuristic implements Heuristic {

	private Map<Heuristic, Double> heuristicMap;
	public WeightedHeuristic(Map<Heuristic, Double> heuristicMap){
		this.heuristicMap = heuristicMap;
	}
	@Override
	public int getValue(Role role, MachineState state, StateMachine machine, long timeout) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		int scoreSum = 0;
		for(Map.Entry<Heuristic, Double> pair : heuristicMap.entrySet()){
			scoreSum += pair.getKey().getValue(role, state, machine, timeout) * pair.getValue();
		}
		return scoreSum;
	}

}
