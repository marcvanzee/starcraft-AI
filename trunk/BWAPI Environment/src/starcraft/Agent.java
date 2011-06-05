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
	private Set<Integer> _buildings = new HashSet<Integer>();
	
	/**
	 * Constructor
	 * 
	 * @param agName	Name of the agent, useful to match it with the BDI agent
	 */
	public Agent(String agName) {
		this._agName = agName;
	}
	
	/**
	 * Add a unit to the agent
	 * 
	 * @param unit
	 */
	public void addUnit(int unit) {
		_units.add(unit);
	}
	
	/**
	 * Add a building to the agent
	 * 
	 * @param unit
	 */
	public void addBuilding(int unit) {
		_buildings.add(unit);
	}
	
	/**
	 * Remove a specific unit from the agent
	 * 
	 * @param unit		Id of the unit to remove
	 */
	public void removeUnit(int unit) {
		_units.remove(unit);
	}
	
	/**
	 * Remove a specific building from the agent
	 * Note: buildings are also refered to as units in BWAPI
	 * 
	 * @param unit
	 */
	public void removeBuilding(int unit) {
		_buildings.remove(unit);
	}
	
	/**
	 * Returns the name of the agent
	 * 
	 * @return
	 */
	public String getName() {
		return _agName;
	}
	
	/**
	 * Returns all the units in an arraylist.
	 * 
	 * @return
	 */
	public List<Integer> getUnits() 
	{
		return new ArrayList<Integer>(_units);
	}
	
	/**
	 * Returns a given number of units randomly
	 * 
	 * @param numUnits
	 * @return
	 */
	public List<Integer> getUnits(int numUnits) 
	{
		return new ArrayList<Integer>(_units).subList(0, numUnits);
	}
	
	/**
	 * Return the first building in the list of buildings.
	 * Useful when working with simplified agents that possess only one building.
	 * 
	 * @return building id
	 */
	public int getBuilding() {
		return (Integer)_buildings.toArray()[0];
	}
	
	/**
	 * Returns a random unit
	 * @return
	 */
	public int getRandomUnit() {
		int rand = new Random().nextInt(_units.size());
		Object agentArray[] = _units.toArray();
		Object randomAgent = agentArray[rand];
		
		return (Integer) randomAgent;
	}
	
	/**
	 * Returns the amount of units that this agent controls.
	 * @return
	 */
	public int countUnits() {
		return _units.size();
	}
}
