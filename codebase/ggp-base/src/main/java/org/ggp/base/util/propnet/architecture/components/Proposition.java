package org.ggp.base.util.propnet.architecture.components;

import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.Component;

/**
 * The Proposition class is designed to represent named latches.
 */
@SuppressWarnings("serial")
public class Proposition extends Component
{
	/** The name of the Proposition. */
	private GdlSentence name;
	/** The value of the Proposition. */
	private boolean value;
	/** The type of the Proposition. */
	private String type = "";

	/**
	 * Creates a new Proposition with name <tt>name</tt>.
	 *
	 * @param name
	 * The name of the Proposition.
	 */
	public Proposition(GdlSentence name)
	{
		this.name = name;
		this.value = false;
	}

	/**
	 * Getter method.
	 *
	 * @return The name of the Proposition.
	 */
	public GdlSentence getName()
	{
		return name;
	}

    /**
     * Setter method.
     *
     * This should only be rarely used; the name of a proposition
     * is usually constant over its entire lifetime.
     *
     * @return The name of the Proposition.
     */
    public void setName(GdlSentence newName)
    {
        name = newName;
    }

	/**
	 * Returns the current value of the Proposition.
	 *
	 * @see org.ggp.base.util.propnet.architecture.Component#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return value;
	}

	/**
	 * Setter method.
	 *
	 * @param value
	 *            The new value of the Proposition.
	 */
	public void setValue(boolean value)
	{
		this.value = value;
	}

    /**
     * Setter method, used only for base and input propositions.
     *
     * This should only be rarely used; the type of a proposition
     * is usually constant over its entire lifetime.
     *
     * @param value
     * 				The type of the Proposition.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Getter method, used to return the type of a proposition.
     *
     * @return The type of the Proposition.
     */
    public String getType(){
    	if(!type.equals("")) return type;
    	if((Component)this instanceof Not) return "negation";
    	if((Component)this instanceof And) return "conjunction";
    	if((Component)this instanceof Or) return "disjunction";
    	return "view";
    }

	/**
	 * @see org.ggp.base.util.propnet.architecture.Component#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("circle", value ? "red" : "white", name.toString());
	}
}