package dembois.heuristics;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;

public interface Heuristic {
	public int getValue(Role role, MachineState state);
}
