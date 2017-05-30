import java.util.List;

import org.ggp.base.apps.player.Player;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
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
		System.out.println("\n\n\n\nGET_INITIAL_STATE_MACHINE");
		return new CachedStateMachine(new SamplePropNetStateMachine());
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub
		}

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub

		System.out.println("\n\n\n\nSTATE_MACHINE_SELECT_MOVE");
		StateMachine machine = getStateMachine();
		MachineState state = getCurrentState();
		Role role = getRole();

//		for(GdlSentence g: state.getContents()) {
//			System.out.println("STATE_MACHINE_SELECT_MOVE state gdl: " + g);
//		}

		List<Move> moves = machine.getLegalMoves(state, role);
//		try { Thread.sleep(10000); } catch(InterruptedException ex) { }
		System.out.println("STATE_MACHINE_SELECT_MOVE: MOVES FOUND == " + moves.size() + moves);
		System.out.println("STATE_MACHINE_SELECT_MOVE: MOVE CHOSEN == " + moves.get(0));
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
