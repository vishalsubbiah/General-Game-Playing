package dembois.heuristics;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class MobilityHeuristic implements Heuristic {

	private int numSteps;
	public MobilityHeuristic(int numSteps){
		this.numSteps = numSteps;
	}

	@Override
	public int getValue(Role role, MachineState state, StateMachine machine, long timeout) throws MoveDefinitionException, TransitionDefinitionException {
		return getStatesAvailableAfterNSteps(role, state, machine);
	}

	private int getStatesAvailableAfterNSteps(Role role, MachineState state, StateMachine machine) throws MoveDefinitionException, TransitionDefinitionException {
		Queue<MachineState> prevQueue = null;
		Queue<MachineState> nextQueue = new LinkedList<MachineState>();
		nextQueue.add(state);
		for(int i=0;i<this.numSteps;++i){
			prevQueue = nextQueue;
			nextQueue = new LinkedList<MachineState>();
			while(!prevQueue.isEmpty()){
				MachineState curState = prevQueue.poll();
				List<List<Move>> moveSets = machine.getLegalJointMoves(curState);
				for(List<Move> moveSet : moveSets){
					MachineState newState = machine.getNextState(curState, moveSet);
					if(!nextQueue.contains(newState)){
						nextQueue.add(newState);
					}
				}
			}
		}
		int numActions = 0;
		while(!nextQueue.isEmpty()){
			MachineState curState = nextQueue.poll();
			numActions += machine.getLegalMoves(curState, role).size();
		}
		return numActions * 100 / machine.findActions(role).size();
	}
}
