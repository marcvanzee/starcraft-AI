package starcraft.actions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import starcraft.Action;
import starcraft.BWAPICoop;
import eisbot.proxy.JNIBWAPI;
import eisbot.proxy.model.*;
import eisbot.proxy.types.*;

public class Attack extends Action 
{
	private List<Unit> _usingUnits;
	private List<Unit> _targetEnemies;
	private boolean _isPerformedOnce;
	
	/**
	 * 
	 * @param num
	 * @param enemies
	 */
	public Attack(List<Unit> units, int officer,  int num, List<Unit> enemies )
	{
		this(units, officer, num, enemies, true );
	}
	
	/**
	 * 
	 * @param num
	 * @param enemies
	 * @param onlyIdle
	 */
	public Attack(List<Unit> units, int officer, int num, List<Unit> enemies, boolean onlyIdle )
	{
		this(units, officer, num, enemies, new LinkedList<UnitType>(), onlyIdle );
	}
	
	/**
	 * 
	 * @param bwapi
	 * @param num
	 * @param enemies
	 * @param withType
	 * @param onlyIdle
	 */
	public Attack(List<Unit> units, int officer, int num, List<Unit> enemies, List<UnitType> withType, boolean onlyIdle )
	{
		init();
		_targetEnemies.addAll(enemies);
		
		for( Unit unit : units )
		{
			if( onlyIdle && !unit.isIdle() )
				continue;
			
			if( withType.size() > 0 && !withType.contains( unit.getTypeID()) )
				continue;
			
			_usingUnits.add( unit );
			
			if(num > 0 && _usingUnits.size() >= num)
				break;
		}
		
		
		
	}


	private void init()
	{
		_usingUnits = new ArrayList<Unit>();
		_targetEnemies = new ArrayList<Unit>();
	}
	

	public void perform(BWAPICoop bwapi)
	{
		for( Unit unit : _usingUnits )
		{
			for( Unit enemy : _targetEnemies )
			{
				bwapi.attackUnit(unit.getID(), enemy.getID());
				break;
			}
		}
		_isPerformedOnce = true;
	}
	

	public boolean isFinished(BWAPICoop bwapi)
	{
		for(Unit unit : _usingUnits)
		{
			if(!bwapi.getUnit(unit.getID()).isIdle())
				return false;
		}
		
		return true;
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
