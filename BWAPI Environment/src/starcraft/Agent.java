package starcraft;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Agent 
{
	private String _agName;
	private Set<Integer> _units = new HashSet<Integer>();
	
	public Agent(String agName) {
		this._agName = agName;
	}
	
	public void addUnit(int unit) {
		_units.add(unit);
	}
	
	public String getName() {
		return _agName;
	}
	
	public List<Integer> getUnits() 
	{
		return new ArrayList<Integer>(_units);
	}
	
	public List<Integer> getUnits(int numUnits) 
	{
		return new ArrayList<Integer>(_units).subList(0, numUnits);
	}
	
	/**
	 * Returns a random unit
	 * @return
	 */
	public int getRandomUnit() {
		return (Integer) (_units.toArray())[new Random().nextInt(_units.size())];
	}
}
