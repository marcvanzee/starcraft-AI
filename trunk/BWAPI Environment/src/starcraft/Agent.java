package starcraft;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eisbot.proxy.model.Unit;

public class Agent 
{
	private String _agName;
	private Set<Unit> _units = new HashSet<Unit>();
	
	public Agent(String agName) {
		this._agName = agName;
	}
	
	public void addUnit(Unit unit) {
		_units.add(unit);
	}
	
	public String getName() {
		return _agName;
	}
	
	public List<Unit> getUnits() 
	{
		List<Unit> units = new ArrayList<Unit>(_units);
		return units;
	}
}
