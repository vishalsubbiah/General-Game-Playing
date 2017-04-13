import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;

public abstract class BaseDemBoisGamer extends StateMachineGamer{
	protected Move getCompulsiveDeliberationMove(Role role, MachineState state, long timeout){
		//TODO: not implemented yet
		return null;
	}

	private int getDeliberationMaxScore(Role role, MachineState state){
		//TODO: not implemented yet
		return -1;
	}

	protected Move getMinimaxMove(Role role, MachineState state, long timeout){
		//TODO: not implemented yet
		return null;
	}

	private int getMMMinScore(Role role, MachineState state){
		//TODO: not implemented yet
		return -1;
	}

	private int getMMMaxScore(Role role, MachineState state){
		//TODO: not implemented yet
		return -1;
	}

	protected Move getAlphaBetaMove(Role role, MachineState state, long timeout){
		//TODO: not implemented yet
		return null;
	}

	private int getABMinScore(Role role, MachineState state){
		//TODO: not implemented yet
		return -1;
	}

	private int getABMaxScore(Role role, MachineState state){
		//TODO: not implemented yet
		return -1;
	}
}
