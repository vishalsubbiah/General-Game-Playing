package dembois.heuristics;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class MonteCarloHeuristic implements Heuristic {

	private int charges;
	public MonteCarloHeuristic(int charges){
		this.charges = charges;
	}

	@Override
	public int getValue(Role role, MachineState state, StateMachine machine, long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		int total = 0;
		int chargesDropped = 0;
	    for (int i=0; i< this.charges && System.currentTimeMillis() < timeout; i++){
	    	int goal = machine.getGoal(machine.performDepthCharge(state, new int[1]), role);
	    	total += goal;
	    	chargesDropped += 1;
	    }
	    if (chargesDropped == 0){
	    	return 0;
	    }
	    return total/chargesDropped;
	}
}
