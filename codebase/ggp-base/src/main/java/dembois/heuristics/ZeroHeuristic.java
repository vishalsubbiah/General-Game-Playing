package dembois.heuristics;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;

public class ZeroHeuristic implements Heuristic {

	@Override
	public int getValue(Role role, MachineState state, StateMachine machine, long timeout) {
		return 0;
	}
}
