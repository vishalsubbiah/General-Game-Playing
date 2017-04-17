import org.ggp.base.apps.player.Player;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

public class CompulsiveDeliberationGamer extends BaseDemBoisGamer {

	Player p;
	@Override
	public StateMachine getInitialStateMachine() {
		// TODO Auto-generated method stub
		return new CachedStateMachine(new ProverStateMachine());
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
		StateMachine machine = getStateMachine();
		MachineState state = getCurrentState();
		Role role = getRole();
		List<Move> moves = machine.getLegalMoves(state, role);
		return compulsiveDeliberation();
	}

	private Move compulsiveDeliberation(){
		Move result = null;

		try {
			Map<Move, List<MachineState>> moves_to_states = getStateMachine().getNextStates(getCurrentState(), getRole());

			int maxScore = 0;
			for(Move m: moves_to_states.keySet()){
				MachineState s = moves_to_states.get(m).get(0); //assuming this is single player, each move will only return one state
				if(computeScore(s) == 100) return m;
				if(computeScore(s) > maxScore) result = m;
			}


		} catch (MoveDefinitionException | TransitionDefinitionException e){
			System.out.println("Error: Call to machine.getNextStates() threw " + e.getMessage());
		}

		return result;
	}


	private int computeScore(MachineState currentState) {
		if(getStateMachine().isTerminal(currentState)){
			try {
				return getStateMachine().getGoal(currentState, getRole());
			} catch (GoalDefinitionException e) { return 0; }
		}

		int score = 0;
		try{
			Map<Move, List<MachineState>> moves_to_states = getStateMachine().getNextStates(currentState, getRole());
			for(Move m: moves_to_states.keySet()){
				MachineState s = moves_to_states.get(m).get(0);
				int currentScore = computeScore(s);
				if(currentScore > score) score = currentScore;
			}

		} catch (MoveDefinitionException | TransitionDefinitionException e){
			System.out.println("Error: Call to machine.getNextStates() threw " + e.getMessage());
		}

		return score;
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
		return "CompulsiveDemBois";
	}

}
