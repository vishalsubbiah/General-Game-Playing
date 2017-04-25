package dembois.heuristics;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;

public class GoalProximityHeuristic implements Heuristic {

	@Override
	public int getValue(Role role, MachineState state, StateMachine machine) throws GoalDefinitionException {
		return machine.getGoal(state, role);
	}

}
