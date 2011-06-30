package starcraft.actions;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import starcraft.BWAPICoop;
import eisbot.proxy.model.*;
import eisbot.proxy.types.*;

public class Attack extends AbstractAction 
{
	private List<Integer> _usingUnits;
	private List<Integer> _targetEnemies;
	private Point _targetPosition;
	private boolean _isPerformedOnce;
	
	public Attack(String identifier, boolean isJoint, Collection<Integer> units, int x, int y)
	{
		super(identifier, isJoint);
		init();
		_usingUnits.addAll(units);
		_targetPosition.x = x;
		_targetPosition.y = y;
	}
	
	public Attack(String identifier, boolean isJoint, Collection<Integer> units, Collection<Integer> enemies)
	{
		super(identifier, isJoint);
		init();
		_usingUnits.addAll(units);
		_targetEnemies.addAll(enemies);
	}
	
	private void init()
	{
		_usingUnits = new ArrayList<Integer>();
		_targetEnemies = new ArrayList<Integer>();
		_targetPosition = new Point();
	}
	
	
	
	public void perform(BWAPICoop bwapi)
	{
		//The perform action is called each updateCycle.
		//And is only needed to be given once, hence the _isPerformedOnce.
		
		if(!_isPerformedOnce)
		for( int unit : _usingUnits )
		{
			//If targetEnemies is empty than attack position
			if(_targetEnemies.isEmpty())
			{
				bwapi.attackMove(unit, _targetPosition.x, _targetPosition.y);
			}
			else
			{
				for( int enemy : _targetEnemies )
				{
					bwapi.attackUnit(unit, enemy );
					break;
				}
			}
		}
		
		_isPerformedOnce = true;
	}
	

	public boolean isFinished(BWAPICoop bwapi)
	{
		//If every unit is idle, the aciton is finished. Dunno if this is good behaviour.
		for( int unit : _usingUnits)
		{
			if(!bwapi.getUnit( unit ).isIdle() )
				return false;
		}
		
		return true;
	}
	
	public int[] getInvolvedUnitIds()
	{
		return AbstractAction.toArray(_usingUnits);
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
