package starcraft.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class MinimalPlanBase 
{
	private List<HashMap<Integer,Integer>> _planBase;
	private Set<Integer> _units;
	
	public MinimalPlanBase(int... unitIds)
	{
		init();
		for(int unitId : unitIds)
		{
			_units.add(unitId);
		}
	}
	
	private void init()
	{
		_planBase = new ArrayList<HashMap<Integer,Integer>>();
		_units = new HashSet<Integer>();
	}
	
	
	/**
	 * Removes a unitId from the 2darray.
	 * @param unitIds
	 */
	public synchronized void removeUnits(int... unitIds)
	{
		for(int unitId : unitIds)
		{
			_units.remove(unitId);
		}
		for(HashMap<Integer,Integer> map : _planBase)
		{
			for(int unitId : unitIds)
			{
				_units.remove(unitId);
			}
		}
		
		System.out.println("beforeShift");
		System.out.println(this);
		shift(0);
		System.out.println("afterShift");
		System.out.println(this);
	}
	
	/**
	 * inserts the actionID and replaces all other actions for those units with 0, indicating no action.
	 * @param actionId, the actionID of the action to perform
	 * @param unitIds, the units associated with that action.
	 */
	public synchronized void insertReplace(int actionId, int... unitIds)
	{
		boolean firstMap = true;
		for(HashMap<Integer,Integer> map : _planBase)
		{
			
			for(int unitId : unitIds)
			{
				//In the first map replace/add the actionID
				if(firstMap)
				{
					map.put(unitId, actionId);
				}
				// In the rest of the replace/add 0. Indicating no action.
				else
				{
					map.put(unitId, 0);
				}
			}
			firstMap = false;
		}
		System.out.println("beforeShift");
		System.out.println(this);
		shift(0);
		System.out.println("afterShift");
		System.out.println(this);
	}
	
	/**
	 * Inserts the actionID before all other actions of specified units.
	 * @param actionId
	 * @param unitIds
	 */
	public synchronized void insertFirst(int actionId, int... unitIds)
	{
		
		//Initialise the first map.
		
		HashMap<Integer,Integer> first = new HashMap<Integer,Integer>();
		//initialise all existing units with 0.
		for(Integer key : _units)
		{
			first.put(key, 0);
		}
		
		//set specified units 
		for(int unitId : unitIds)
		{
			first.put(unitId, actionId);
		}
		
		//adds the list at the beginning of the map.
		_planBase.add(0, first);
		
		System.out.println("beforeShift");
		System.out.println(this);
		shift(0);
		System.out.println("afterShift");
		System.out.println(this);
	}
	
	/**
	 * Inserts the actionID at the end of the planbase for specified units.
	 * @param actionId
	 * @param unitIds
	 */
	public synchronized void insertLast(int actionId, int... unitIds)
	{
		//Initialise the last map.
		HashMap<Integer,Integer> last = new HashMap<Integer,Integer>();
		//initialise all existing units with 0.
		for(Integer key : _units)
		{
			last.put(key, 0);
		}
		
		//set specified units 
		for(int unitId : unitIds)
		{
			last.put(unitId, actionId);
		}
		
		//adds the list at the beginning of the map.
		_planBase.add(last);
		
		
		System.out.println("beforeShift");
		System.out.println(this);
		shift(_planBase.size()-2);
		System.out.println("afterShift");
		System.out.println(this);
	}
	
	public synchronized void insertOnlyWhenIdle(int actionId, int... unitIds)
	{
		if(_planBase.size() < 1)
		{
			insertFirst(actionId, unitIds);
		}
		else
		{
			HashMap<Integer,Integer> map = _planBase.get(0);
			
			for(int unitId : unitIds)
			{
				if(map.get(unitId)==0)
					map.put(unitId, actionId);
			}
		}
	}
	
	public synchronized Set<Integer> getActionsToExecute()
	{
		Set<Integer> actions = new HashSet<Integer>();
		if(_planBase.size() > 0)
		{
			for(Integer actionId : _planBase.get(0).values())
			{
				actions.add(actionId);
			}
		}
		return actions;
	}
	
	public synchronized void actionsFinished(int... actionIds)
	{
		if(_planBase.size() > 0)
		{
			for(int actionId : actionIds)
			{
				for(Entry<Integer,Integer> entry : _planBase.get(0).entrySet())
				{
					if(entry.getValue() == actionId)
						entry.setValue(0);
				}
			}
		}
		shift(0);
	}
	
	/**
	 * Shifts actions from the row proceeding to the index given, to the the row before it, if possible.
	 * @param the index of the row to begin
	 */
	private void shift(int beginRow)
	{
		//no need to shift if the rows are out of bounds.
		if(((beginRow+1) >= _planBase.size()) || beginRow < 0)
			return;

		//Get the first map.
		HashMap<Integer,Integer> mapFirst = _planBase.get(beginRow);
		//Get the second map.
		HashMap<Integer,Integer> mapSecond = _planBase.get(beginRow+1);
		
		
		HashMap<Integer,Integer> toShiftIfFree = new HashMap<Integer,Integer>();
		Set<Integer> nonFreeActions = new HashSet<Integer>();
		
		//Loop through every unit.
		for(Integer unitId : _units)
		{
			int actionIdFirst = mapFirst.get(unitId);
			int actionIdSecond = mapSecond.get(unitId);
			//If the action is 0 (no action), then this unit can possibly shift to the next action if its free.
			if(actionIdFirst == 0)
			{
				if(actionIdSecond != 0)
					toShiftIfFree.put(unitId, actionIdSecond);
			}
			//Otherwise the next action of this unit, if non-zero, is marked as non free.
			else
			{
				if(actionIdSecond != 0)
					nonFreeActions.add(actionIdSecond);
			}
		}
		
		//Now we shift all actions, who are free.
		for(Entry<Integer,Integer> entry : toShiftIfFree.entrySet())
		{
			int actionId = entry.getValue();
			int unitId = entry.getKey();
			//if the action is free
			if(!nonFreeActions.contains(actionId))
			{
				mapFirst.put(unitId, actionId);
				mapSecond.put(unitId, 0);
			}
		}
		
		//Finally we can remove the secondMap if it contains all zeros.
		boolean allZeros = true;
		for(Integer actionId : mapSecond.values())
		{
			if(actionId != 0)
				allZeros = false;
		}
		
		
		if(allZeros)
		{
			_planBase.remove(beginRow+1);
		}
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder("---- PlanBase ----\n");
		
		for(int unitId : _units)
		{
			sb.append(unitId);
			sb.append('\t');
			for(HashMap<Integer,Integer> map : _planBase)
			{
				sb.append(map.get(unitId));
				sb.append('\t');
			}
			sb.append('\n');
		}
		
		sb.append(" --------------------------------- ");
		return sb.toString();
	}
	
	public static void main(String[] args)
	{
		
		MinimalPlanBase a = new MinimalPlanBase(1,2,3,4,5);
		a.insertFirst(1, 1,2,3);
		//System.out.println(a);
		a.insertFirst(2, 1,4,5);
		//System.out.println(a);
		a.insertFirst(3, 2,3);
		
		System.out.println("action finsiehd 3");
		a.actionsFinished(3);
		System.out.println(a);
		a.insertFirst(4, 1,3,5);
		//
		a.insertLast(5, 4,5);
		//
		a.insertLast(6, 1,3,4);
		//
		a.insertReplace(7, 1,3,5);
		
		a.removeUnits(1);
	}
}
