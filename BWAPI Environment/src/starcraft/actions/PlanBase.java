package starcraft.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eisbot.proxy.model.Unit;

/**
 * Class for holding the plans of all individual units.
 * @author Frank
 *
 */
public class PlanBase 
{
	//Hashmap for holding the Lists of actions per unit.
	private HashMap<Integer,List<Action>> _actions;
	
	/**
	 * Constructs a new PlanBase
	 */
	public PlanBase()
	{
		init();
	}
	
	/**
	 * Constructs a new PlanBase, with empty plans for initial units. 
	 * @param initialUnits
	 */
	public PlanBase(List<Unit>  initialUnits)
	{		
		init();
		
		for(Unit unit : initialUnits)
		{
			_actions.put(unit.getID(), new ArrayList<Action>());
		}
	}
	
	/**
	 * Initialises a new empty planbase.
	 */
	private void init()
	{
		_actions = new HashMap<Integer,List<Action>>();
	}
		
	
	
	
	/**
	 * Executes the actions for the units. If an action is finished it is removed from the list of actions for that unit.
	 * An action is executed each updateCycle till it is finished.
	 */
	public void executeActions()
	{
		
		for(List<Action> actionsPerUnit : _actions.values())
		{
			//Performs the action
			actionsPerUnit.get(0).perform();			
			//If the action is finished it is removed from the action.
			if(actionsPerUnit.isFinished())
				actionsPerUnit.remove(0);
		}
	}
	
}
