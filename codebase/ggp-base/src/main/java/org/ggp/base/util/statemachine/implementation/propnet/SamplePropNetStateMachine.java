package org.ggp.base.util.statemachine.implementation.propnet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.Component;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.architecture.components.And;
import org.ggp.base.util.propnet.architecture.components.Constant;
import org.ggp.base.util.propnet.architecture.components.Not;
import org.ggp.base.util.propnet.architecture.components.Or;
import org.ggp.base.util.propnet.architecture.components.Proposition;
import org.ggp.base.util.propnet.architecture.components.Transition;
import org.ggp.base.util.propnet.factory.OptimizingPropNetFactory;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.query.ProverQueryBuilder;


@SuppressWarnings("unused")
public class SamplePropNetStateMachine extends StateMachine {
    /** The underlying proposition network  */
    private PropNet propNet;
    /** The topological ordering of the propositions */
    private List<Proposition> ordering;
    /** The player roles */
    private List<Role> roles;
    /** The initial state, saved upon initialization*/
    private MachineState initialState;

    private final boolean debugComputeInitialState = false;
    private final boolean debugMarking = false;
    private final boolean debugPropmark = false;
    private final boolean debugComputation = false;


    /**
     * Initializes the PropNetStateMachine. You should compute the topological
     * ordering here. Additionally you may compute the initial state here, at
     * your discretion.
     */
    @Override
    public void initialize(List<Gdl> description) {
        try {
            propNet = OptimizingPropNetFactory.create(description);
            roles = propNet.getRoles();
            ordering = getOrdering();
            computeInitialState();
            System.out.println("propnet built");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void computeInitialState() {
    	Set<GdlSentence> contents = new HashSet<GdlSentence>();
    	Proposition init = propNet.getInitProposition();
        init.setValue(true);
        contents.add(init.getName());
        if(debugComputeInitialState){
        	for(Proposition p: propNet.getPropositions()) System.out.println("ALL PROPS: " + p);
        	System.out.println("computeInitialState: number of outputs for initialProp = " + propNet.getInitProposition().getOutputs().size());
        	System.out.println("computeInitialState: value of init = " + init.getValue());
        	System.out.println("computeInitialState: printing all init inputs");
        	for(Component c: init.getInputs()) System.out.println("\tINPUT: " + c);
        	System.out.println("computeInitialState: printing all init outputs");
        	for(Component c: init.getOutputs()) System.out.println("\tOUTPUT: " + c);
        }

        initialState = new MachineState(contents);
        if(debugComputeInitialState) {
        	System.out.println("computeInitialState: printing the gdl of the state with only the init proposition");
        	for(GdlSentence g: initialState.getContents()) System.out.println("\t" + g);
        }

        try {
			initialState = getNextState(initialState, null);
		} catch (TransitionDefinitionException e) {
			e.printStackTrace();
		}
		if(debugComputeInitialState) {
			System.out.println("computeInitialState: printing the gdl of the state after propagating from only init");
			for(GdlSentence g: initialState.getContents()) System.out.println("\t" + g);
		}

		// Important to set initial proposition to false here, our initial state has already propagated once after init was set to true.
        init.setValue(false);
    }

    /*
     * Given the current state and the base propositions, set the name of the base proposition, and set the value.
     */
    private boolean markBases(MachineState state) {
    	Map<GdlSentence, Proposition> basesMap = propNet.getBasePropositions();
    	Set<GdlSentence> currentState = state.getContents();

    	if(debugMarking) System.out.println("MB basesMap: \r\tsize:" + basesMap.size() + "\r\tcontents:" + basesMap);
    	for(GdlSentence s: currentState) {
    		Proposition base = basesMap.get(s);
    		base.setValue(true);
    		if(debugMarking) System.out.println("MB marked true: " + basesMap.get(s).getValue() + " " + basesMap.get(s).getName() + " " + base);
    	}

//    	for(GdlSentence g: propNet.getBasePropositions().keySet()){
//    		System.out.println(g);
//    		propNet.getBasePropositions().get(g).setValue(true);
//    	}

    	if(debugMarking) System.out.println();
    	return true;
    }

    private boolean markActions(List<GdlSentence> moves) {
    	Map<GdlSentence, Proposition> inputsMap = propNet.getInputPropositions();

    	if(debugMarking) System.out.println("MA inputsMap: \r\tsize:" + inputsMap.size() + "\r\tcontents:" + inputsMap);
    	for(GdlSentence s: moves){
    		Proposition input = inputsMap.get(s);
    		input.setValue(true);
    		if(debugMarking) System.out.println("MA marked true: " + inputsMap.get(s).getValue() + " " + inputsMap.get(s).getName() + " " + input);
    	}

    	if(debugMarking) System.out.println();
    	return true;
    }

    private boolean clearPropnet() {
    	if(debugMarking) System.out.println("START CLEAR");

    	Map<GdlSentence, Proposition> baseProps = propNet.getBasePropositions();
    	for (Map.Entry<GdlSentence, Proposition> entry : baseProps.entrySet()) {
    		if(debugMarking) System.out.println(entry.getKey() + " " + entry.getValue() + entry.getValue().getValue());
    	    entry.getValue().setValue(false);
    	    if(debugMarking) System.out.println(entry.getKey() + " " + entry.getValue() + entry.getValue().getValue());
    	}

    	Map<GdlSentence, Proposition> inputProps = propNet.getInputPropositions();
    	for (Map.Entry<GdlSentence, Proposition> entry : inputProps.entrySet()) {
    		if(debugMarking) System.out.println(entry.getKey() + " " + entry.getValue() + entry.getValue().getValue());
    	    entry.getValue().setValue(false);
    	    if(debugMarking) System.out.println(entry.getKey() + " " + entry.getValue() + entry.getValue().getValue());
    	}

    	if(debugMarking) System.out.println("END CLEAR");
    	return true;
    }

    private boolean propmarkp(Component c) {
    	if(c instanceof Transition) {
    		if(debugPropmark) System.out.println("propmarkp: transition" + c + c.getValue());
//    		return propmarkp(c.getSingleInput());
    		return c.getValue();
    	}

    	if(c instanceof And) {
    		if(debugPropmark) System.out.println("propmarkp: and" + c + c.getValue());
    		return propmarkAND(c);
    	}

    	if(c instanceof Or) {
    		if(debugPropmark)System.out.println("propmarkp: or" + c + c.getValue());
    		return propmarkOR(c);
    	}

    	if(c instanceof Not) {
    		if(debugPropmark)System.out.println("propmarkp: not" + c + c.getValue());
    		return propmarkNOT(c);
    	}
    	// work here next distinguishing input, base (base and input treated the same), and anon
    	if(c instanceof Proposition) {
    		if(propNet.getBasePropositions().containsValue(c) || propNet.getInputPropositions().containsValue(c) || c.getInputs().size() == 0) return c.getValue();
    		else return propmarkp(c.getSingleInput());
//    		if(debugPropmark) System.out.println("propmarkp: proposition" + c + c.getValue());
////    		if(debugPropmark) System.out.println(((Proposition) c).getName().toString());
//    		if(((Proposition) c).getName().toString().equals("anon")) {
//    			if(debugPropmark)System.out.println("ANON: " + c.getValue() + " " + c);
//    			return propmarkp(c.getSingleInput());
//    		} else return c.getValue();
    	}

    	if(c instanceof Constant) {
    		if(debugPropmark) System.out.println("propmarkp: constant" + c + c.getValue());
    		return c.getValue();
    	}

    	// This might not be necessary, have it here in case I was not catching all cases of components
    	System.out.println("ERROR IN PROPMARK FOR: " + c);
    	return false;
    }

    private boolean propmarkNOT(Component c){
    	return !propmarkp(c.getSingleInput());
    }

    private boolean propmarkAND(Component c) {
    	Set<Component> sources = c.getInputs();
    	for(Component s: sources) {
    		if(!propmarkp(s)) return false;
    	}
    	return true;
    }

    private boolean propmarkOR(Component c) {
    	Set<Component> sources = c.getInputs();
    	for(Component s: sources) {
    		if(propmarkp(s)) return true;
    	}
    	return false;
    }

    private Set<Proposition> propLegals(MachineState state, Role role) throws MoveDefinitionException{
    	if(debugComputation) System.out.println("\nPROP_LEGALS()");
    	markBases(state);

    	Set<Proposition> legalProps = propNet.getLegalPropositions().get(role);
    	if(debugComputation) {
    		System.out.println("\tPrinting all (" + legalProps.size()  + ") legal propositions: ");
        	for(Proposition p: legalProps) System.out.println("\t\t value = " + p.getValue() + "| contents = " + p);
    	}

    	Set<Proposition> actions = new HashSet<Proposition>();
    	for(Proposition p: legalProps) {
    		if(propmarkp(p.getSingleInput())) actions.add(p);
    	}
    	if(debugComputation) {
    		System.out.println("\tPrinting all (" + actions.size()  + ") choosable actions: ");
        	for(Proposition p: actions) System.out.println("\t\t value = " + p.getValue() + "| contents = " + p);
    	}

    	clearPropnet();
    	if(actions.size() > 0) return actions;
    	else throw new MoveDefinitionException(state, role);
    }

    private MachineState propNextState(MachineState state, List<Move> moves){
    	if(debugComputation) System.out.println("\nPROP_NEXT_STATE()");
    	if(moves != null){
    		markActions(toDoes(moves));
    	    markBases(state);
    	}

    	Map<GdlSentence, Proposition> baseProps = propNet.getBasePropositions();
    	if(debugComputation) {
    		System.out.println("\tPrinting all base propositions: ");
    		for(GdlSentence s: baseProps.keySet()) System.out.println("\t\tbase: value = "+ baseProps.get(s).getValue() + " | " + s + " " + baseProps.get(s));
    		System.out.println("\tPrinting contents of current state: ");
    		for(GdlSentence s: state.getContents()) System.out.println("\t\tstate gdl: " + s + " " + baseProps.get(s));
    	}

    	Set<GdlSentence> nextState = new HashSet<GdlSentence>();
    	for(Proposition base: baseProps.values()){
    		if(propmarkp(base.getSingleInput().getSingleInput())){
    			nextState.add(base.getName());
    			if(debugComputation) System.out.println("\tadded to next state: " + base.getName() + " " + base.getValue() + " " + base.getSingleInput());
    		} else {
    			if(debugComputation) System.out.println("\tdid not add: " + base.getName() + " " + base.getValue() + " " + base.getSingleInput());
    		}
    	}
//    	for(Proposition base: baseProps.values()){
//    		if(propmarkp(base.getSingleInput())){
//    			nextState.add(base.getName());
//    			if(debugComputation) System.out.println("\tadded to next state: " + base.getName() + " " + base.getValue() + " " + base.getSingleInput());
//    		}
//    	}


    	if(debugComputation) {
    		System.out.println("\tPrinting all (" + nextState.size()  + ") gdl in next state: ");
        	for(GdlSentence g: nextState) System.out.println("\t\tnextstateGDL: " + g);
    	}
    	clearPropnet();
    	return new MachineState(nextState);
    }

    private int propReward(MachineState state, Role role) throws GoalDefinitionException{
//    	System.out.println("\nInside propReward");
    	markBases(state);
    	Set<Proposition> rewards = propNet.getGoalPropositions().get(role);
//    	for(Proposition r: rewards) System.out.println("propReward " + r);

    	for(Proposition reward: rewards) {
    		if(propmarkp((Component)reward)) {
    			clearPropnet();
//    			System.out.println("propReward: returning " + reward);
    			return getGoalValue(reward); }
    	}

    	throw new GoalDefinitionException(state, role);
    }

    private boolean propTerminal(MachineState state){
//    	System.out.println("\nPROP_TERMINAL()");
    	markBases(state);
    	return propmarkp((Component)propNet.getTerminalProposition());
    }

    /**
     * Computes if the state is terminal. Should return the value
     * of the terminal proposition for the state.
     */
    @Override
    public boolean isTerminal(MachineState state) {
        boolean result = propTerminal(state);
        clearPropnet();
        return result;
    }

    /**
     * Computes the goal for a role in the current state.
     * Should return the value of the goal proposition that
     * is true for that role. If there is not exactly one goal
     * proposition true for that role, then you should throw a
     * GoalDefinitionException because the goal is ill-defined.
     */
    @Override
    public int getGoal(MachineState state, Role role)
            throws GoalDefinitionException {
    	return propReward(state, role);
    }

    /**
     * Returns the initial state. The initial state can be computed
     * by only setting the truth value of the INIT proposition to true,
     * and then computing the resulting state.
     */
    @Override
    public MachineState getInitialState() {
    	return initialState;
    }

    /**
     * Computes all possible actions for role.
     */
    @Override
    public List<Move> findActions(Role role)
            throws MoveDefinitionException {
        // TODO: Compute legal moves.
        return null;
    }

    /**
     * Computes the legal moves for role in state.
     */
    @Override
    public List<Move> getLegalMoves(MachineState state, Role role)
            throws MoveDefinitionException {
        List<Move> moves = new ArrayList<Move>();
        for(Proposition p: propLegals(state, role)) { moves.add(getMoveFromProposition(p)); }
        return moves;
    }

    /**
     * Computes the next state given state and the list of moves.
     */
    @Override
    public MachineState getNextState(MachineState state, List<Move> moves)
            throws TransitionDefinitionException {
    	MachineState nextState = propNextState(state, moves);
        return nextState;
    }

    /**
     * This should compute the topological ordering of propositions.
     * Each component is either a proposition, logical gate, or transition.
     * Logical gates and transitions only have propositions as inputs.
     *
     * The base propositions and input propositions should always be exempt
     * from this ordering.
     *
     * The base propositions values are set from the MachineState that
     * operations are performed on and the input propositions are set from
     * the Moves that operations are performed on as well (if any).
     *
     * @return The order in which the truth values of propositions need to be set.
     */
    public List<Proposition> getOrdering()
    {
        // List to contain the topological ordering.
        List<Proposition> order = new LinkedList<Proposition>();

        // All of the components in the PropNet
        List<Component> components = new ArrayList<Component>(propNet.getComponents());

        // All of the propositions in the PropNet.
        List<Proposition> propositions = new ArrayList<Proposition>(propNet.getPropositions());

        // TODO: Compute the topological ordering.

        return order;
    }

    /* Already implemented for you */
    @Override
    public List<Role> getRoles() {
        return roles;
    }

    /* Helper methods */

    /**
     * The Input propositions are indexed by (does ?player ?action).
     *
     * This translates a list of Moves (backed by a sentence that is simply ?action)
     * into GdlSentences that can be used to get Propositions from inputPropositions.
     * and accordingly set their values etc.  This is a naive implementation when coupled with
     * setting input values, feel free to change this for a more efficient implementation.
     *
     * @param moves
     * @return
     */
    private List<GdlSentence> toDoes(List<Move> moves)
    {
        List<GdlSentence> doeses = new ArrayList<GdlSentence>(moves.size());
        Map<Role, Integer> roleIndices = getRoleIndices();

        for (int i = 0; i < roles.size(); i++)
        {
            int index = roleIndices.get(roles.get(i));
            doeses.add(ProverQueryBuilder.toDoes(roles.get(i), moves.get(index)));
        }
        return doeses;
    }

    /**
     * Takes in a Legal Proposition and returns the appropriate corresponding Move
     * @param p
     * @return a PropNetMove
     */
    public static Move getMoveFromProposition(Proposition p)
    {
        return new Move(p.getName().get(1));
    }

    /**
     * Helper method for parsing the value of a goal proposition
     * @param goalProposition
     * @return the integer value of the goal proposition
     */
    private int getGoalValue(Proposition goalProposition)
    {
        GdlRelation relation = (GdlRelation) goalProposition.getName();
        GdlConstant constant = (GdlConstant) relation.get(1);
        return Integer.parseInt(constant.toString());
    }

    /**
     * A Naive implementation that computes a PropNetMachineState
     * from the true BasePropositions.  This is correct but slower than more advanced implementations
     * You need not use this method!
     * @return PropNetMachineState
     */
    public MachineState FromBase()
    {
        Set<GdlSentence> contents = new HashSet<GdlSentence>();
        for (Proposition p : propNet.getBasePropositions().values())
        {
            p.setValue(p.getSingleInput().getValue());
            if (p.getValue())
            {
                contents.add(p.getName());
            }

        }
        return new MachineState(contents);
    }
}