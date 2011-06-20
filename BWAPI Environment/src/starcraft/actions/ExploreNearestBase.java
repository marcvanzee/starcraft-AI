package starcraft.actions;

import java.awt.Point;
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
	private int _coBuidlingId;
	private Point _enemyBuildingToExplore;
	
	private boolean _isPerformedOnce;
	private boolean _hasReachedSpot;
	
	public static int DISTANCE_TRESHOLD = 10;
	
	public ExploreNearestBase(String identifier,  Collection<Integer> units, int myBuildingId, int coBuildingId)
	{
		super(identifier);
		_usingUnits.addAll(units);
		_myBuildingId = myBuildingId;
		_coBuidlingId = coBuildingId;
		this._isPerformedOnce = false;
		this._hasReachedSpot = false;
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
		Point myBuilding = new Point();
		myBuilding.x = bwapi.getUnit(_myBuildingId).getX();
		myBuilding.y = bwapi.getUnit(_myBuildingId).getY();
		Point coBuilding = new Point();
		coBuilding.x = bwapi.getUnit(_coBuidlingId).getX();
		coBuilding.y = bwapi.getUnit(_coBuidlingId).getY();
		
		//Get nearest Enemy Starting location
		Point nearestEnemyPos = null;
		double smallestEnemyDistance = Double.MAX_VALUE;
		
		for(BaseLocation b : bwapi.getMap().getBaseLocations())
		{
			System.out.println("Found baselocation: " + b.getX() + " " + b.getY());
			
			if(b.isStartLocation())
			{
				Point startLocation = new Point(b.getX(),b.getY());
				if(startLocation.distance(coBuilding) > DISTANCE_TRESHOLD)
				{
					double distance = startLocation.distance(myBuilding);
					if(distance < smallestEnemyDistance)
					{
						smallestEnemyDistance = distance;
						nearestEnemyPos = startLocation;
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
			
			for(Integer unitID : _usingUnits)
			{
				bwapi.move(unitID, _enemyBuildingToExplore.x, _enemyBuildingToExplore.y);
			}
			_isPerformedOnce = true;
		}
	}


	public boolean isFinished(BWAPICoop bwapi)
	{
		//If every unit is idle, the aciton is finished. 
		//Dunno if this is good behaviour.
		for( int unit : _usingUnits)
		{
			if(!bwapi.getUnit( unit ).isIdle() )
				return false;
		}
		
		return true;
	}

}
