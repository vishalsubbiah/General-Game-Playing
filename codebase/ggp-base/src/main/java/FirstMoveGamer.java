import java.util.List;

import org.ggp.base.apps.player.Player;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.propnet.SamplePropNetStateMachine;

public class FirstMoveGamer extends StateMachineGamer {
	Player p;

	@Override
	public StateMachine getInitialStateMachine() {
		// TODO Auto-generated method stub
		System.out.println("INITIAL");
		return new CachedStateMachine(new SamplePropNetStateMachine());
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub
		System.out.println(getInitialStateMachine().toString());
	}

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub
		System.out.println("0");
		StateMachine machine = getStateMachine();
		System.out.println("1");
		MachineState state = getCurrentState();
		System.out.println(state);
		System.out.println("2");
		Role role = getRole();
		System.out.println("3");
		for(GdlSentence g: state.getContents()) {
			System.out.println("beforeMove: " + g); //this will show whether initial state was computed correctly
			System.out.println();
		}
		List<Move> moves = machine.getLegalMoves(state, role);
		System.out.println(moves.size());
		return moves.get(0);

//		SamplePropNetStateMachine machine = (SamplePropNetStateMachine) getStateMachine();
//		System.out.println("1");
//		MachineState state= getCurrentState();
//		System.out.println("2");
//		Role role = getRole();
//		System.out.println("3");
//		List<Move> moves = machine.getLegalMoves(state, role);
//		System.out.println(moves.size());
//		return moves.get(0);
	}

	@Override
	public void stateMachineStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stateMachineAbort() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "FirstMoveDemBois";
	}

}
