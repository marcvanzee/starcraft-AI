package starcraft.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import eisbot.proxy.model.Unit;
import eisbot.proxy.types.UnitType;
import starcraft.BWAPICoop;

public abstract class AbstractAction 
{
	private String _identifier;
	private boolean _isJoint;
	
	public AbstractAction(String identifier, boolean isJoint)
	{
		if(identifier == null)
			_identifier = "";
		_identifier = identifier;
		_isJoint = isJoint;
	}
	
	public String getIdentity()
	{
		return _identifier;
	}
	
	public boolean isJoint()
	{
		return _isJoint;
	}
	
	public abstract int[] getInvolvedUnitIds();
	
	public abstract void perform(BWAPICoop bwapi);
	
	public abstract boolean isFinished(BWAPICoop bwapi);

	/**
	 * 
	 * @param units
	 * @param amount
	 * @param withType
	 * @param onlyIdle
	 * @return
	 */
	public static List<Unit> selectUnits(Collection<Unit> units, int amount, List<UnitType> withType, boolean onlyIdle)
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
	
	public static int[] toArray(Collection<Integer> integerCollection)
	{
		int[] array = new int[integerCollection.size()];
		int i=0;
		Iterator<Integer> iterator = integerCollection.iterator();
		while(iterator.hasNext())
		{
			array[i] = iterator.next();
			i++;
		}
		
		return array;
	}
}
