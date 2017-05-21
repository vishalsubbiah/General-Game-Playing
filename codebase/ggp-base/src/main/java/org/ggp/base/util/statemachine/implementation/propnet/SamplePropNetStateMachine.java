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
            try {
				initialState = getNextState(initialState, null);
				for(GdlSentence g: initialState.getContents()) System.out.println("ALL GDL2: " + g);
			} catch (TransitionDefinitionException e) {
				e.printStackTrace();
			}
            System.out.println("Finished computeInitialState " + initialState.getContents() + "\n\n\n\n\n\n");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private MachineState computeInitialState() {
    	Set<GdlSentence> contents = new HashSet<GdlSentence>();
    	Proposition init = propNet.getInitProposition();
        init.setValue(true);
        contents.add(init.getName());

        System.out.println("computeInitialState: number of outputs for initialProp = " + propNet.getInitProposition().getOutputs().size());
        System.out.println(init.getValue());
        System.out.println("Printing all init inputs");
        for(Component c: init.getInputs()) System.out.println("initINPUT: " + c);
        System.out.println("Printing all init outputs");
        for(Component c: init.getOutputs()) System.out.println("initOUTPUT: " + c);
        System.out.println(init.getType());

    	return new MachineState(contents);
    }

    /*
     * Given the current state and the base propositions, set the name of the base proposition, and set the value.
     */
    private boolean markBases(MachineState state) {
    	Map<GdlSentence, Proposition> baseProps = propNet.getBasePropositions();
    	Set<GdlSentence> statePropositions = state.getContents();

//    	System.out.println("mb1 " + baseProps.size() + " " + baseProps);
//    	for(GdlSentence s: baseProps.keySet()) System.out.println("mb2 " + s);

    	System.out.println("mb activate: " + statePropositions + " " + statePropositions.size());
//    	for(GdlSentence s: statePropositions) System.out.println("mb4 " + s);

    	for(GdlSentence s: statePropositions) {
    		Proposition base = baseProps.get(s);
    		System.out.println("mb before" + base.getName() + " " + base.getValue() + " " + base);
    		base.setValue(true);
    	    System.out.println("mb after" + base.getName() + " " + base.getValue() + " " + base);
    	}

    	System.out.println();
    	return true;
    }

    private boolean markActions(List<GdlSentence> moves) {
    	Map<GdlSentence, Proposition> inputProps = propNet.getInputPropositions();

    	System.out.println("ma activate: " + inputProps + " " + inputProps.size());
    	for(GdlSentence s: moves){
    		Proposition input = inputProps.get(s);
    		System.out.println("ma before" + input.getName() + " " + input.getValue() + " " + input);
    		input.setValue(true);
    		System.out.println("ma after" + input.getName() + " " + input.getValue() + " " + input);
    	}

    	return true;
    }

    private boolean clearPropnet() {
    	System.out.println("CLEARING PROPNET");
    	Map<GdlSentence, Proposition> baseProps = propNet.getBasePropositions();
    	for (Map.Entry<GdlSentence, Proposition> entry : baseProps.entrySet()) {
    		System.out.println(entry.getKey() + " " + entry.getValue() + entry.getValue().getValue());
    	    entry.getValue().setValue(false);
    	    System.out.println(entry.getKey() + " " + entry.getValue() + entry.getValue().getValue());
    	}

//    	Map<GdlSentence, Proposition> inputProps = propNet.getInputPropositions();
//    	for (Map.Entry<GdlSentence, Proposition> entry : inputProps.entrySet()) {
//    	    entry.getValue().setValue(false);
//    	}

//    	propNet.getInitProposition().setValue(false);
    	System.out.println("CLEARED PROPNET");
    	return true;
    }

    private boolean propmarkp(Component c) {
    	System.out.println("inside propmark: " + c);
    	if(c instanceof Transition) {
    		System.out.println("propmarkp: transition" + c + c.getValue());
    		return c.getValue();
    	}

    	if(c instanceof And) {
    		System.out.println("propmarkp: and" + c + c.getValue());
    		return propmarkAND(c);
    	}

    	if(c instanceof Or) {
    		System.out.println("propmarkp: or" + c + c.getValue());
    		return propmarkOR(c);
    	}

    	if(c instanceof Not) {
    		System.out.println("propmarkp: not" + c + c.getValue());
    		return propmarkNOT(c);
    	}

    	if(c instanceof Proposition) {
    		System.out.println("propmarkp: proposition" + c + c.getValue());
    		return c.getValue();
    	}

    	if(c instanceof Constant) {
    		System.out.println("propmarkp: constant" + c + c.getValue());
    		return c.getValue();
    	}

//    	String name = p.getType();
//    	switch(name) {
//    		case "base": case "input":
//    			System.out.println("base/input " + p.getType() + " " + p.getValue());
//    			return p.getValue();
//    		case "view": // FIND OUT WHAT VIEW SHOULD DO IN THIS CASE
//    			System.out.println("view " + p.getType() + " " + p.getValue());
//    			return propmarkp((Proposition)p.getSingleInput());
//    		case "negation":
//    			System.out.println("negation " + p.getType() + " " + p.getValue());
//    			return propmarkNOT(p);
//    		case "conjunction" :
//    			System.out.println("conjunction " + p.getType() + " " + p.getValue());
//    			return propmarkAND(p);
//    		case "disjunction" :
//    			System.out.println("disjunction " + p.getType() + " " + p.getValue());
//    			return propmarkOR(p);
//    	}
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
    	System.out.println("\nInside propLegals");
    	markBases(state);

    	Set<Proposition> legals = propNet.getLegalPropositions().get(role);
    	System.out.println("propLegals: legals.sie() == " + legals.size());

    	Set<Proposition> actions = new HashSet<Proposition>();
    	for(Proposition p: legals) {
    		System.out.println("propLegals " + p + " " + p.getType() + " " + p.getValue());
    		System.out.println("propLegals inputs:  " + p.getInputs());
    		if(propmarkp(p.getSingleInput())) actions.add(p);
    		System.out.println();
    	}

    	if(actions.size() > 0) return actions;
    	else throw new MoveDefinitionException(state, role);
    }

    private MachineState propNextState(MachineState state, List<GdlSentence> moves){ //HERE
    	System.out.println("\nInside propNextState");
    	Set<GdlSentence> nextState = new HashSet<GdlSentence>();
    	if(moves != null){
    		markActions(moves);
    		markBases(state);
    	}

    	Map<GdlSentence, Proposition> bases = propNet.getBasePropositions();
    	for(Proposition base: bases.values()){
    		if(propmarkp(base.getSingleInput())){ nextState.add(base.getName());}
    		System.out.println("propNextState: " + base.getName() + " " + base.getValue() + " " + base.getSingleInput());
    	}

    	System.out.println("propNextState: nextState.size(): == " + nextState.size());
    	for(GdlSentence g: nextState) System.out.println("propNextState gdls:" + g);
    	return new MachineState(nextState);
    }

    private int propReward(MachineState state, Role role) throws GoalDefinitionException{
    	System.out.println("\nInside propReward");
    	markBases(state);
    	Set<Proposition> rewards = propNet.getGoalPropositions().get(role);
    	for(Proposition r: rewards) System.out.println("propReward " + r);


    	for(Proposition reward: rewards) {
    		if(propmarkp(reward.getSingleInput())) {
    			System.out.println("propReward: returning " + reward);
    			return getGoalValue(reward); }
    	}

    	throw new GoalDefinitionException(state, role);
    }

    private boolean propTerminal(MachineState state){
    	System.out.println("\nInside propTerminal");
    	markBases(state);
    	return propmarkp(propNet.getTerminalProposition().getSingleInput());
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
        clearPropnet();
        return moves;
    }

    /**
     * Computes the next state given state and the list of moves.
     */
    @Override
    public MachineState getNextState(MachineState state, List<Move> moves)
            throws TransitionDefinitionException {
        // TODO: Compute the next state.
    	MachineState nextState;
        if(moves != null) nextState = propNextState(state, toDoes(moves));
        else nextState = propNextState(state, null);

        clearPropnet();
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