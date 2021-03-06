package starcraft;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import eisbot.proxy.model.Unit;
import starcraft.actions.*;

public class Agent 
{
	private String _agName;
	
	private Set<Integer> _units	= new HashSet<Integer>();
	private int _base, _baseHP;
	private Point _centerPoint;
	private BWAPICoop _bwapi;
//	private int _WTA;				// willingness to attack, the aggressiveness of the agent, which determines his character domain [0,10]
	private double _WTA;
	private PlanBase _planbase;
	private Point _basePos = new Point();
	private Point _coBasePos = new Point();
	/**
	 * Constructor
	 * 
	 * @param agName	Name of the agent, useful to match it with the BDI agent
	 */
	public Agent(String agName, BWAPICoop _bwapi, double wta) 
	{
		this._agName = agName;
		this._bwapi = _bwapi;
		this._WTA = wta; // nextInt(11);
	}
	
	/**
	 * Add a unit to the agent
	 * 
	 * @param unit
	 */
	public void addUnit(int unitID) 
	{
		
		_units.add(unitID);
	}
	
	/**
	 * Add a building to the agent
	 * 
	 * @param unit
	 */
	public void addBuilding(int unitID, int X, int Y) 
	{
		_base = unitID;
		_basePos = new Point(X,Y);
	}
	
	public void setCoBase(int X, int Y)
	{
		_coBasePos = new Point(X,Y);
	}
	
	/**
	 * Remove a specific unit from the agent, that can be a building or a character
	 * 
	 * @param unit		Id of the unit to remove
	 */
	public void removeElement(int id) {
		_units.remove(id);
		//Why is base set to -1 Frank? 
		_base = -1;
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
	 * Returns a given number of units randomly??
	 * How is it random? Frank
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
		
		return _base;
	}
	
	/**
	 * Returns a random unit
	 * @return
	 */
	public int getRandomUnit() 
	{
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
	public void initPlanBase() 
	{
		_planbase = new PlanBase(_units);
		//TEST: insert attack action to position 0.0.
		//_planbase.insertFirst(new Attack("id", _units, 0,0));
		
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
	
	/**
	 * The following information needs to be updated:
	 * - center point of units
	 * - the health points of the base
	 */
	public Set<AbstractAction> update() 
	{
		
		//First execute actions in planbase.
		Set<AbstractAction> finishedActions = _planbase.executeActions(_bwapi);
		
		
		//then update the belief base. Dont know if order matters.
		updateCP();

		updateBaseHP();
		
		return finishedActions;
	}
	
	private void updateCP() 
	{
		int avgX=0, avgY=0;
		int numUnitsRetrieved = 0;
		
		for (int unitID : _units) 
		{
			Unit u = null;
			try
			{
				u = _bwapi.getUnit(unitID);
				numUnitsRetrieved++;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			if(u!=null)
			{
				avgX += u.getX();
				avgY += u.getY();
			}
			
		}
		int totalUnits = countUnits();
		if(totalUnits > 0)
		{
			avgX /= countUnits();
			avgY /= countUnits();
		}
		_centerPoint = new Point(avgX, avgY);
	}
	
	private void updateBaseHP() 
	{
		int hp = -1;
		
		Unit building = _bwapi.getUnit(_base);
		if(building != null)
			hp = building.getHitPoints();
		
		if(hp != -1)
			_baseHP = hp;
	}

	public Point getCP() {
		if(_centerPoint == null)
			_centerPoint = new Point(0,0);
		
		return _centerPoint;
	}
	
	public Point getBasePos()
	{
		return _basePos;
	}
	
	public Point getCoBasePos()
	{
		return _coBasePos;
	}
	
	public int getBaseHP() 
	{
		return _baseHP;
	}

	
	
	public double getWTA() {
		return _WTA;
	}
	
	public PlanBase getPlanBase()
	{
		return _planbase;
	}
}
