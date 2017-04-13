import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;

public abstract class BaseDemBoisGamer extends StateMachineGamer{
	protected Move getCompulsiveDeliberationMove(Role role, MachineState state, long timeout){
		//TODO: not implemented yet
		return null;
	}

	protected Move getMinimaxMove(Role role, MachineState state, long timeout){
		//TODO: not implemented yet
		return null;
	}

	protected Move getAlphaBetaMove(Role role, MachineState state, long timeout){
		//TODO: not implemented yet
		return null;
	}
}
