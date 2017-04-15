import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;

public abstract class BaseDemBoisGamer extends StateMachineGamer{
	protected Move getCompulsiveDeliberationMove(StateMachine machine, Role role, MachineState state, long timeout){
		//TODO: not implemented yet
		return null;
	}

	private int getDeliberationMaxScore(StateMachine machine, Role role, MachineState state){
		//TODO: not implemented yet
		return -1;
	}

	protected Move getMinimaxMove(StateMachine machine, Role role, MachineState state, long timeout){
		//TODO: not implemented yet
		return null;
	}

	private int getMMMinScore(StateMachine machine, Role role, MachineState state){
		//TODO: not implemented yet
		return -1;
	}

	private int getMMMaxScore(StateMachine machine, Role role, MachineState state){
		//TODO: not implemented yet
		return -1;
	}

	protected Move getAlphaBetaMove(StateMachine machine, Role role, MachineState state, long timeout){
		//TODO: not implemented yet
		return null;
	}

	private int getABMinScore(StateMachine machine, Role role, MachineState state, int alpha, int beta){
		//TODO: not implemented yet
		return -1;
	}

	private int getABMaxScore(StateMachine machine, Role role, MachineState state, int alpha, int beta){
		//TODO: not implemented yet
		return -1;
	}
}
