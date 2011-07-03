package starcraft.actions;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eisbot.proxy.model.Unit;

import starcraft.BWAPICoop;

public class GatherForAttackBuilding extends AbstractAction 
{
	
	private List<Integer> _usingUnits;
	private Point _toAttack;
	private Point _gatherPoint;
	private boolean _isPerformedOnce;
	public static final int DISTANCE_TRESHOLD_DESTINATION = 100;
	
	public GatherForAttackBuilding(String actionIdentifier, boolean isJoint, Collection<Integer> units,Point toAttack, Point myCP,Point coCP)
	{
		super(actionIdentifier, isJoint);
		_usingUnits = new ArrayList<Integer>(units);
		_toAttack = toAttack;
		if(isJoint)
			_gatherPoint = calculateGatherPoint(myCP,coCP);
		else
			_gatherPoint = calculateGatherPoint(myCP);
		
		_isPerformedOnce = false;
	}
	
	@Override
	public int[] getInvolvedUnitIds() 
	{
		return AbstractAction.toArray(_usingUnits);
	}

	@Override
	public void perform(BWAPICoop bwapi) 
	{
		if(!_isPerformedOnce)
		{
			for(Integer unitId : _usingUnits)
			{
				bwapi.attackMove(unitId, _gatherPoint.x, _gatherPoint.y);
			}
			
			_isPerformedOnce = true;
		}

	}
	
	
	@Override
	public boolean isFinished(BWAPICoop bwapi) 
	{
		//If every unit is idle, the action is finished. 
		//Dunno if this is good behaviour.
		int count = 0;
		
		if(_usingUnits.size() == 0)
			return true;
		
		
		for( int unit : _usingUnits)
		{
			
			Unit u = bwapi.getUnit(unit);
			if(u != null)
			{
				Point pos = new Point(u.getX(), u.getY());
			
				if(pos.distance(_gatherPoint) < DISTANCE_TRESHOLD_DESTINATION)
				{
					System.out.println("TARGET REACHED!!" + pos);
					return true;
				}
			}
			if(count > _usingUnits.size() / 2)
			{
				//System.out.println("TARGET NOT REACHED!!" + pos + "  distance: " + pos.distance(_enemyBuildingToExplore));
				return false;
			}
				
			count++;
		}
		
		return false;
	}
	

	private Point calculateGatherPoint(Point myCP)
	{
		return calculateGatherPoint(myCP,myCP);
	}
	
	private Point calculateGatherPoint(Point myCP, Point coCP)
	{
		Point gatherPoint = new Point();
		Point nearestPoint = _toAttack.distance(myCP) <= _toAttack.distance(coCP) ? myCP : coCP;
		
		gatherPoint.x = _toAttack.x +  Math.min(200,(nearestPoint.x - _toAttack.x));
		gatherPoint.y = _toAttack.y + Math.min(200,(nearestPoint.y - _toAttack.y));
		
		return gatherPoint;
	}


}
