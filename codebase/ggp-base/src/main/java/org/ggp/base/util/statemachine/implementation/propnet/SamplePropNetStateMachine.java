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
import org.ggp.base.util.propnet.architecture.components.Proposition;
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

    /**
     * Initializes the PropNetStateMachine. You should compute the topological
     * ordering here. Additionally you may compute the initial state here, at
     * your discretion.
     */
    @Override
    public void initialize(List<Gdl> description) {
        try {
            propNet = OptimizingPropNetFactory.create(description);
            for(Proposition p: propNet.getPropositions()) System.out.println("ALL PROPS: " + p);
            roles = propNet.getRoles();
            ordering = getOrdering();
            initialState = computeInitialState();
            for(GdlSentence g: initialState.getContents()) System.out.println("ALL GDL: " + g);
            clearPropnet();
            try {
				initialState = getNextState(initialState, null);
				for(GdlSentence g: initialState.getContents()) System.out.println("ALL GDL2: " + g);
	            roles = propNet.getRoles();
			} catch (TransitionDefinitionException e) {
				e.printStackTrace();
			}
            System.out.println("Finished computeInitialState " + initialState.getContents() + "\n");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private MachineState computeInitialState() {
    	Set<GdlSentence> contents = new HashSet<GdlSentence>();
        Proposition INIT = propNet.getInitProposition();
        System.out.println("computeInitialState: number of outputs for initialProp = " + propNet.getInitProposition().getOutputs().size() );
        INIT.setValue(true);
//        for(Component c: propNet.getInitProposition().getOutputs()) {
//        	System.out.println("computeInitialState: " + c.toString());
//        	Proposition p = (Proposition) c;
//        	p.setValue(true);
//        	contents.add(p.getName());
//        }

        System.out.println(INIT.getValue());
        for(Component s: INIT.getInputs()) System.out.println("initINPUT: " + s);
        for(Component s: INIT.getOutputs()) System.out.println("initOUTPUT: " + s);
        System.out.println(INIT.getType());

        contents.add(INIT.getName());
    	return new MachineState(contents);
    }

    /*
     * Given the current state and the base propositions, set the name of the base proposition, and set the value.
     */
    private boolean markBases(MachineState state) {
    	Map<GdlSentence, Proposition> baseProps = propNet.getBasePropositions();
    	Set<GdlSentence> statePropositions = state.getContents();

    	System.out.println("mb1 " + baseProps.size() + " " + baseProps);
    	for(GdlSentence s: baseProps.keySet()) System.out.println("mb2 " + s);

    	System.out.println("mb3 " + statePropositions + " " + statePropositions.size());
    	for(GdlSentence s: statePropositions) System.out.println("mb4 " + s);
//    	for(Component c: propNet.getInitProposition().getOutputs()) System.out.println("initcomponent " + c);

    	for (Map.Entry<GdlSentence, Proposition> entry : baseProps.entrySet()) {
    		Proposition base = entry.getValue();
    	    base.setValue(statePropositions.contains(entry.getKey()));
    	    System.out.println("mb5 " + base.getName() + " " + statePropositions.contains(base.getName()) + " " + base);
    	}

    	System.out.println();
    	return true;
    }

    private boolean markActions(List<GdlSentence> moves) {
    	Map<GdlSentence, Proposition> inputProps = propNet.getInputPropositions();

    	for(GdlSentence s: moves){
    		Proposition input = inputProps.get(s);
    		input.setValue(true);
    		System.out.println("markActions: " + input);
    	}

    	return true;
    }

    // DONT FORGET TO USE THIS
    private boolean clearPropnet() {
    	Map<GdlSentence, Proposition> baseProps = propNet.getBasePropositions();
    	for (Map.Entry<GdlSentence, Proposition> entry : baseProps.entrySet()) {
    	    entry.getValue().setValue(false);
    	}

    	Map<GdlSentence, Proposition> inputProps = propNet.getInputPropositions();
    	for (Map.Entry<GdlSentence, Proposition> entry : inputProps.entrySet()) {
    	    entry.getValue().setValue(false);
    	}

//    	propNet.getInitProposition().setValue(false);

    	return true;
    }

    private boolean propmarkp(Proposition p) {
    	String name = p.getType();
    	switch(name) {
    		case "base": case "input":
    			System.out.println("base/input " + p.getType() + " " + p.getValue());
    			return p.getValue();
    		case "view": // FIND OUT WHAT VIEW SHOULD DO IN THIS CASE
    			System.out.println("view " + p.getType() + " " + p.getValue());
    			return propmarkp((Proposition)p.getSingleInput());
    		case "negation":
    			System.out.println("negation " + p.getType() + " " + p.getValue());
    			return propmarkNOT(p);
    		case "conjunction" :
    			System.out.println("conjunction " + p.getType() + " " + p.getValue());
    			return propmarkAND(p);
    		case "disjunction" :
    			System.out.println("disjunction " + p.getType() + " " + p.getValue());
    			return propmarkOR(p);
    	}
    	return false;
    }

    private boolean propmarkNOT(Proposition p){
    	return !propmarkp((Proposition)p.getSingleInput());
    }

    private boolean propmarkAND(Proposition p) {
    	Set<Component> sources = p.getInputs();
    	for(Component c: sources) {
    		Proposition sourceProp = (Proposition) c;
    		if(!propmarkp(sourceProp)) return false;
    	}
    	return true;
    }

    private boolean propmarkOR(Proposition p) {
    	Set<Component> sources = p.getInputs();
    	for(Component c: sources) {
    		Proposition sourceProp = (Proposition) c;
    		if(propmarkp(sourceProp)) return true;
    	}
    	return false;
    }


    private Set<Proposition> propLegals(MachineState state, Role role) throws MoveDefinitionException{
    	markBases(state);
    	Set<Proposition> legals = propNet.getLegalPropositions().get(role);
    	System.out.println("propLegals " + legals.size());
    	for(Proposition p: legals) {
    		System.out.println("propLegals " + p + " " + p.getType() + " " + p.getValue());
    		System.out.println(p.getInputs() + "\n");
    	}


    	Set<Proposition> actions = new HashSet<Proposition>();
    	for(Proposition p: legals) {
    		if(propmarkp(p)) actions.add(p);
    	}

    	clearPropnet();
    	if(actions.size() > 0) return actions;
    	else throw new MoveDefinitionException(state, role);
    }

    private MachineState propNextState(MachineState state, List<GdlSentence> moves){
    	if(moves != null) { //handles the initial state, where no move has been made
    		markActions(moves);
    		markBases(state);
    	}

    	Map<GdlSentence, Proposition> bases = propNet.getBasePropositions();
    	Set<GdlSentence> nextContents = new HashSet<GdlSentence>();
    	for(Proposition base: bases.values()){
    		if(propmarkp(base.getSingleInput())) nextContents.add(base.getName()); //might need to use base.source.source instead
    		System.out.println("propNextState: " + base.getName() + " " + base.getValue());
    	}
    	//not propagating correctly


//    	clearPropnet();
    	System.out.println("propNextState: nextContents.size(): == " + nextContents.size());
    	for(GdlSentence g: nextContents) System.out.println("propNextState gdls:" + g);
    	return new MachineState(nextContents);
    }

    private int propReward(MachineState state, Role role) throws GoalDefinitionException{
    	markBases(state);
    	Set<Proposition> rewards = propNet.getGoalPropositions().get(role);

    	for(Proposition reward: rewards) {
    		if(propmarkp(reward)) { return getGoalValue(reward); }
    	}

//    	clearPropnet();
    	throw new GoalDefinitionException(state, role);
    }

    private boolean propTerminal(MachineState state){
    	markBases(state);
//    	clearPropnet();
    	return propmarkp(propNet.getTerminalProposition());
    }

    /**
     * Computes if the state is terminal. Should return the value
     * of the terminal proposition for the state.
     */
    @Override
    public boolean isTerminal(MachineState state) {
        // TODO: Compute whether the MachineState is terminal.
        return propTerminal(state);
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
        // TODO: Compute the goal for role in state.
    	return propReward(state, role);
    }

    /**
     * Returns the initial state. The initial state can be computed
     * by only setting the truth value of the INIT proposition to true,
     * and then computing the resulting state.
     */
    @Override
    public MachineState getInitialState() {
        // TODO: Compute the initial state.
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
        System.out.println("before get move from proposition");
        for(Proposition p: propLegals(state, role)) { moves.add(getMoveFromProposition(p)); }
        System.out.println(moves.size());
        return moves;
    }

    /**
     * Computes the next state given state and the list of moves.
     */
    @Override
    public MachineState getNextState(MachineState state, List<Move> moves)
            throws TransitionDefinitionException {
        // TODO: Compute the next state.
        if(moves != null) return propNextState(state, toDoes(moves));
        else return propNextState(state, null);
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
    public MachineState getStateFromBase()
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