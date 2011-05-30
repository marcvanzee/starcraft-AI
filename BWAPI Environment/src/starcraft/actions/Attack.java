package starcraft.actions;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import starcraft.BWAPICoop;
import eisbot.proxy.JNIBWAPI;
import eisbot.proxy.model.*;
import eisbot.proxy.types.*;

public class Attack extends Action 
{
	private List<Unit> _usingUnits;
	private List<Unit> _targetEnemies;
	private Point _targetPosition;
	private boolean _isPerformedOnce;
	
	
	
	
	public Attack(List<Unit> units, int x, int y)
	{
		init();
		_usingUnits.addAll(units);
		_targetPosition.x = x;
		_targetPosition.y = y;
	}
	
	public Attack(List<Unit> units, List<Unit> enemies)
	{
		init();
		_usingUnits.addAll(units);
		_targetEnemies.addAll(enemies);
	}
	
	private void init()
	{
		_usingUnits = new ArrayList<Unit>();
		_targetEnemies = new ArrayList<Unit>();
		_targetPosition = new Point();
	}
	
	/**
	 * 
	 * @param units
	 * @param amount
	 * @param withType
	 * @param onlyIdle
	 * @return
	 */
	public static List<Unit> selectUnits(List<Unit> units, int amount, List<UnitType> withType, boolean onlyIdle)
	{
		
		List<Unit> selectedUnits = new ArrayList<Unit>();
		
		for( Unit unit : units )
		{
			if( onlyIdle && !unit.isIdle() )
				continue;
			
			if(withType.size() > 0 && !withType.contains(unit.getTypeID()))
				continue;
			
			selectedUnits.add(unit);
			
			if(amount > 0 && selectedUnits.size() >= amount)
				break;
		}
		
		return selectedUnits;
	}
	
	
	public void perform(BWAPICoop bwapi)
	{
		//The perform action is called each updateCycle.
		//And is only needed to be given once, hence the _isPerformedOnce.
		
		if(!_isPerformedOnce)
		for( Unit unit : _usingUnits )
		{
			//If targetEnemies is empty than attack position
			if(_targetEnemies.isEmpty())
			{
				bwapi.attackMove(unit.getID(), _targetPosition.x, _targetPosition.y);
			}
			else
			{
				for( Unit enemy : _targetEnemies )
				{
					bwapi.attackUnit(unit.getID(), enemy.getID());
					break;
				}
			}
		}
		
		_isPerformedOnce = true;
	}
	

	public boolean isFinished(BWAPICoop bwapi)
	{
		//If every unit is idle, the aciton is finished. Dunno if this is good behaviour.
		for(Unit unit : _usingUnits)
		{
			if(!bwapi.getUnit(unit.getID()).isIdle())
				return false;
		}
		
		return true;
	}
	
	public int[] getInvolvedUnitIds()
	{
		int[] unitIds = new int[_usingUnits.size()];
		for(int i=0; i<unitIds.length; i++)
		{
			unitIds[i] = _usingUnits.get(i).getID();
		}
		return unitIds;
	}
	
	/**
	 * deprecated
	 * @param bwapi
	 * @param myUnits
	 * @param enemies
	
	public void createAttack( JNIBWAPI bwapi, List<Unit> myUnits, List<Unit> enemies )
	{
		for( Unit unit : myUnits )
		{
			for( Unit enemy : enemies )
			{
				actions.add( new String[] {"attackMove", ""+unit.getID(), ""+enemy.getX(), ""+enemy.getY()} );
				break;
			}
		}
	}
	 */
}
