package starcraft.actions;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import eisbot.proxy.model.BaseLocation;
import eisbot.proxy.model.Unit;

import FIPA.DateTime;

import starcraft.BWAPICoop;

public class ExploreNearestBase extends Action 
{
	private List<Integer> _usingUnits;
	private int _myBuildingId;
	private Point _myBuildingPos;
	private int _coBuidlingId;
	private Point _coBuildingPos;
	
	private Point _enemyBuildingToExplore;
	
	private boolean _isPerformedOnce;
	private boolean _hasReachedSpot;
	
	public static int DISTANCE_TRESHOLD_BUIlDING = 1000;
	public static int DISTANCE_TRESHOLD_DESTINATION = 100; 
	
	public ExploreNearestBase(String identifier,  Collection<Integer> units, int myBuildingId, int coBuildingId)
	{
		super(identifier);
		init();
		_usingUnits.addAll(units);
		_myBuildingId = myBuildingId;
		_coBuidlingId = coBuildingId;
		this._isPerformedOnce = false;
		this._hasReachedSpot = false;
	}
	
	public ExploreNearestBase(String identifier, Collection<Integer> units, int myBuildingX, int myBuildingY, int coBuildingX, int coBuildingY)
	{
		super(identifier);
		init();
		_usingUnits.addAll(units);
		
		_myBuildingPos = new Point(myBuildingX,myBuildingY);
		_coBuildingPos = new Point(coBuildingX,coBuildingY);
		
		this._isPerformedOnce = false;
		this._hasReachedSpot = false;
	}
	
	private void init()
	{
		_usingUnits = new ArrayList<Integer>();
	}
	
	@Override
	public int[] getInvolvedUnitIds() 
	{
		int[] unitIds = new int[_usingUnits.size()];
		for(int i=0; i<unitIds.length; i++)
		{
			unitIds[i] = _usingUnits.get(i);
		}
		return unitIds;
	}

	private Point determineNearestEnemy(BWAPICoop bwapi)
	{
		
		//get building positions.
		
		if(_myBuildingPos == null)
		{
			_myBuildingPos = new Point();
			Unit mybuilding = bwapi.getUnit(_myBuildingId);
			if(mybuilding != null)
			{
				_myBuildingPos.x = mybuilding.getX();
				_myBuildingPos.y = mybuilding.getY();
			}
		}
		if(_coBuildingPos == null)
		{
			_coBuildingPos = new Point();
			Unit otherBuilding = bwapi.getUnit(_coBuidlingId);
			if(otherBuilding != null)
			{
				_coBuildingPos.x = otherBuilding.getX();
				_coBuildingPos.y = otherBuilding.getY();
			}
		}
		
		//Get nearest Enemy Starting corner
		Point nearestEnemyPos = null;
		double smallestEnemyDistance = Double.MAX_VALUE;
		
		int width = bwapi.getMap().getWalkWidth() * 8;
		int height = bwapi.getMap().getWalkHeight() * 8;
		
		
		
		List<Point> corners = new ArrayList<Point>();
		corners.add(new Point(50,50));
		corners.add(new Point(50,height-50));
		corners.add(new Point(width-50,50));
		corners.add(new Point(width-50, height-50));
		
		
		for(Point corner  : corners)
		{
			
			if(corner.distance(_myBuildingPos) > DISTANCE_TRESHOLD_BUIlDING)
			{
				if(corner.distance(_coBuildingPos) > DISTANCE_TRESHOLD_BUIlDING)
				{
					double distance = corner.distance(_myBuildingPos);
					if(distance < smallestEnemyDistance)
					{
						smallestEnemyDistance = distance;
						nearestEnemyPos = corner;
					}
				}
					
			}
		}
		return nearestEnemyPos;
	}
	
	@Override
	public void perform(BWAPICoop bwapi) 
	{
		if(!_isPerformedOnce)
		{
			_enemyBuildingToExplore = determineNearestEnemy(bwapi);			
			
			System.out.println("Position to explore: " + _enemyBuildingToExplore + " units: " + _usingUnits.size() + " my:" + _myBuildingPos + " otyer" + _coBuildingPos);
			if(_enemyBuildingToExplore != null)
			for(Integer unitID : _usingUnits)
			{
				bwapi.move(unitID, _enemyBuildingToExplore.x, _enemyBuildingToExplore.y);
			}
			_isPerformedOnce = true;
					
		}
	}

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
			
				if(pos.distance(_enemyBuildingToExplore) < DISTANCE_TRESHOLD_DESTINATION)
				{
					System.out.println("TARGET REACHED!!" + pos);
					return true;
				}
			}
			if(count > 2)
			{
				//System.out.println("TARGET NOT REACHED!!" + pos + "  distance: " + pos.distance(_enemyBuildingToExplore));
				return false;
			}
				
			count++;
		}
		
		return false;
	}

}
