package dembois.heuristics;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public interface Heuristic {
	public int getValue(Role role, MachineState state, StateMachine machine, long timeout) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException;
}
