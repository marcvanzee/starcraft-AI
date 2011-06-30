package starcraft.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import starcraft.BWAPICoop;

public class DefendBuildingAction extends AbstractAction
{

	private int _buildingX = -1;
	private int _buildingY = -1;
	private List<Integer> _units = new ArrayList<Integer>();
	private boolean _isPerformedOnce = false;
	
	public DefendBuildingAction(String identifier, boolean isJoint, Collection<Integer> units, int buildingX, int buildingY) 
	{
		super(identifier, isJoint);	
		_buildingX = buildingX;
		_buildingY = buildingY;
		_units.addAll(units);
	}
	
	
	@Override
	public int[] getInvolvedUnitIds() 
	{
		return AbstractAction.toArray(_units);
	}

	@Override
	public void perform(BWAPICoop bwapi)
	{
		if(!_isPerformedOnce)
		{
			for(Integer unitId : _units)
			{
				bwapi.patrol(unitId, _buildingX, _buildingY);
			}
			_isPerformedOnce = true;
		}
		
		
	}

	@Override
	public boolean isFinished(BWAPICoop bwapi) 
	{
		return false;
	}
	

	
	
}
