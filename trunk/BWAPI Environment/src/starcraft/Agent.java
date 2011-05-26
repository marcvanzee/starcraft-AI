package starcraft;

import java.util.HashSet;

import eisbot.proxy.model.Unit;

public class Agent {
	private String _agName;
	private HashSet<Unit> _units = new HashSet<Unit>();
	
	public Agent(String agName) {
		this._agName = agName;
	}
	
	public void addUnit(Unit unit) {
		_units.add(unit);
	}
	
	public String getName() {
		return _agName;
	}
	
	public HashSet<Unit> getUnits() {
		return _units;
	}
}
