package starcraft.actions;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;


import starcraft.BWAPICoop;

import eisbot.proxy.model.Unit;

/**
 * Class for holding the plans of all individual units.
 * @author Frank
 *
 */
public class PlanBase 
{
	MinimalPlanBase _minimalPlanBase;
	
	//A hashmap of actions, indexed by their id.
	private HashMap<Integer,Action> _actionsPerID;
	
	private int _currentActionId;
	
//	/**
//	 * Constructs a new PlanBase, with empty plans for initial units. 
//	 * @param initialUnits
//	 */
//	public PlanBase(List<Unit> initialUnits)
//	{
//		int[] unitIds = new int[initialUnits.size()];
//		for(int i=0; i<unitIds.length; i++)
//		{
//			unitIds[i] = initialUnits.get(i).getID();
//		}
//		init(unitIds);
//	}
	
	public PlanBase(List<Integer> initialUnitIDs )
	{
		int[] unitIds = new int[initialUnitIDs.size()];
		for(int i=0; i<unitIds.length; i++)
		{
			unitIds[i] = initialUnitIDs.get(i);
		}
		init(unitIds);
	}
	
	/**
	 * Constructs a new PlanBase, with empty plans for initial units. 
	 * @param initialUnits
	 */
	public PlanBase(Unit... initialUnits)
	{	
		int[] unitIds = new int[initialUnits.length];
		for(int i=0; i<unitIds.length; i++)
		{
			unitIds[i] = initialUnits[i].getID();
		}
		init(unitIds);
	}
	
	/**
	 * Initialises a new empty planbase.
	 */
	private void init(int... unitIds)
	{
		_minimalPlanBase = new MinimalPlanBase();
		_actionsPerID = new HashMap<Integer,Action>();
		_currentActionId = 0;
	}
		
	/**
	 * Inserts an action and removes all other actions for units involved.
	 * @param action The Action to perform.
	 * @return The id of the action.
	 */
	public synchronized int insertReplace(Action action)
	{
		int actionId = _currentActionId;
		_currentActionId++;
		
		
		_actionsPerID.put(actionId, action);
		_minimalPlanBase.insertReplace(actionId,action.getInvolvedUnitIds());
	
		return actionId;
	}
	
	/**
	 * Inserts an action before other actions for units involved.
	 * @param action The Action to perform.
	 * @return The id of the action.
	 */
	public synchronized int insertFirst(Action action)
	{
		int actionId = _currentActionId;
		_currentActionId++;
		
		_actionsPerID.put(actionId, action);
		_minimalPlanBase.insertFirst(actionId, action.getInvolvedUnitIds());
		
		return actionId;
	}
	
	/**
	 * Inserts an action as the last action for units involved.
	 * @param action The Action to perform.
	 * @return The id of the action.
	 */
	public synchronized int insertLast(Action action)
	{
		int actionId = _currentActionId;
		_currentActionId++;
		
		_actionsPerID.put(actionId, action);
		_minimalPlanBase.insertLast(actionId, action.getInvolvedUnitIds());
		
		return actionId;
	}
	
	/**
	 * Inserts an action to those units who are idle and involved.
	 * @param action The Action to perform.
	 * @return The id of the action.
	 */
	public synchronized int insertOnlyWhenIdle(Action action)
	{
		int actionId = _currentActionId;
		_currentActionId++;
		
		_actionsPerID.put(actionId, action);
		_minimalPlanBase.insertOnlyWhenIdle(actionId, action.getInvolvedUnitIds());
	
		return actionId;
	}
	
	/**
	 * Executes the actions for the units. 
	 * If an action is finished it is removed from the list of actions for that unit and from the actions list.
	 * An action is executed each updateCycle till it is finished. 
	 */
	public synchronized void executeActions(BWAPICoop bwapi)
	{
		
		Set<Integer> actionIds = _minimalPlanBase.getActionsToExecute();
		List<Integer> finishedActions = new ArrayList<Integer>();
		
		for(int actionId : actionIds)
		{
			Action action = _actionsPerID.get(actionId);
			action.perform(bwapi);
			
			if(action.isFinished(bwapi))
			{
				_actionsPerID.remove(actionId);
				finishedActions.add(actionId);
			}
		}
		//Fit the contents of the finished actions list into an array.
		int[] actionsFinishedArray = new int[finishedActions.size()];
		for(int i=0; i<actionsFinishedArray.length; i++)
			actionsFinishedArray[i] = finishedActions.get(i);		
		
		_minimalPlanBase.actionsFinished(actionsFinishedArray);
	}
	
}
