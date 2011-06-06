package starcraft;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import eisbot.proxy.model.Unit;

public class Agent 
{
	private String _agName;
	
	/**
	 * HashMap<int,bool>
	 * int: 		unitID
	 * boolean: 	whether this unit is a building
	 */
	private Set<Integer> _units 	= new HashSet<Integer>();
	private Set<Integer> _buildings = new HashSet<Integer>();
	private Point _centerPoint;
	private BWAPICoop _bwapi;
	
	/**
	 * Constructor
	 * 
	 * @param agName	Name of the agent, useful to match it with the BDI agent
	 */
	public Agent(String agName, BWAPICoop _bwapi) {
		this._agName = agName;
		this._bwapi = _bwapi;
	}
	
	/**
	 * Add a unit to the agent
	 * 
	 * @param unit
	 */
	public void addUnit(int unitID) {
		_units.add(unitID);
	}
	
	/**
	 * Add a building to the agent
	 * 
	 * @param unit
	 */
	public void addBuilding(int unitID) {
		_buildings.add(unitID);
	}
	
	/**
	 * Remove a specific unit from the agent, that can be a building or a character
	 * 
	 * @param unit		Id of the unit to remove
	 */
	public void removeElement(int id) {
		_units.remove(id);
		_buildings.remove(id);
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
		
		return (_buildings.size() > 0) ? (Integer)_buildings.toArray()[0] : -1;
	}
	
	/**
	 * Returns all buildings
	 * 
	 * @return
	 */
	public List<Integer> getBuildings() 
	{
		return new ArrayList<Integer>(_buildings);
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
	
	/**
	 * TODO: hier wordt de planbase geinitialiseerd
	 */
	public void initPlanBase() {
		//Setup plan bases for the agents.
		//TEST: insert attack action to position 0.0.
		/*
		for(Agent agent : _agents)
		{
			PlanBase planBase = new PlanBase(agent.getUnits());
			planBase.insertFirst(new Attack(agent.getUnits(), 0, 0));
			_planBases.put(agent.getName(), planBase);
		}*/
	}
	
	public void update() {
		updateCP();
	}
	
	public void updateCP() {
		int avgX=0, avgY=0;
		
		for (int unitID : getUnits()) {
			Unit u = _bwapi.getUnit(unitID);
			avgX += u.getX();
			avgY += u.getY();
		}
		avgX /= countUnits();
		avgY /= countUnits();
		
		_centerPoint = new Point(avgX, avgY);
	}
}
