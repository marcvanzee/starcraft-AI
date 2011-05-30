package starcraft;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import starcraft.actions.Attack;
import starcraft.actions.PlanBase;

import eisbot.proxy.BWAPIEventListener;
import eisbot.proxy.JNIBWAPI;
import eisbot.proxy.model.*;

import apapl.data.APLFunction;
import apapl.data.APLIdent;
import apapl.data.APLList;
import apapl.data.APLListVar;
import apapl.data.APLNum;
import apapl.data.Term;

public class Env extends apapl.Environment implements BWAPIEventListener
{
	private static final int TOTAL_AGENTS = 2;
	
	private Logger _logger = Logger.getLogger("Starcraft."+Env.class.getName());
	 
	private BWAPICoop _bwapi;
	private Thread _clientThread;
	
	// *** Edit Marc: arraylist of agents.  *** //
	private List<Agent> _agents;
	// *** Edit: Frank AgentName => PlanBase  mapping *** //
	private HashMap<String,PlanBase> _planBases;
	
	
	public Env()
	{
		init();
		_logger.info("Environment Initialised..");
	}
	
	/**
	 * Initializes the environment.
	 */
	private void init()
	{
		// Edit Marc: by declaring the agents with their units in this class, the BWAPICoop class is not needed anymore
		// Edit Frank: However for future changes, i prefer to work with BWAPICoop class.
		// locations of our agents
		//int[] locations = {BWAPICoop.LOC_NE, BWAPICoop.LOC_NW};
		_planBases = new HashMap<String,PlanBase>();
		_agents = new ArrayList<Agent>();
		
		_bwapi = new BWAPICoop(this);
		_clientThread = new Thread(new BWAPIClient(_bwapi));
	}
	
	/**
	 * Starts the jnibwapi client thread.
	 */
	private void start()
	{
		if(!_clientThread.isAlive())
			_clientThread.start();
	}
		 
	
	public synchronized Term hello(String agentName) throws Exception
	{
		if (_agents.size() < TOTAL_AGENTS) {
			_agents.add(new Agent(agentName));
			if (_agents.size() == TOTAL_AGENTS) {
				start();
			}
		} else {
			throw new Exception("env> ERROR: too many agents try to register");
		}
		
		return wrapBoolean(true);
	}
	
	
	private synchronized Agent getAgent( String agentName )
	{
		for( Agent agent : _agents )
		{
			if( agent.getName().equals( agentName ) )
				return agent;
		}
		
		return null;
	}
	
	
	/**
	 * Select num random units
	 * @param agentName
	 * @param num
	 * @return
	 * @throws Exception
	 */
	public synchronized Term selectRandom( String agentName, int num ) throws Exception
	{
		return wrapBoolean( false );
	}
	
	/**
	 * Select num units closest to position
	 * @param agentName
	 * @param position
	 * @param num
	 * @return
	 * @throws Exception
	 */
	public synchronized Term selectClosestRandom( String agentName, Point position, int num ) throws Exception
	{
		return wrapBoolean( false );
	}
	
	/**
	 * Select num of idle units
	 * @param agentName
	 * @param num
	 * @return
	 * @throws Exception
	 */
	public synchronized Term selectIdle( String agentName, int num ) throws Exception
	{
		return wrapBoolean( false );
	}
	
	/**
	 * Select idle units from a given list
	 * @param agentName
	 * @param units
	 * @return
	 * @throws Exception
	 */
	public synchronized Term selectIdle( String agentName, List<Unit> units ) throws Exception
	{
		return wrapBoolean( false );
	}
	
	/**
	 * Select all the units within a range (position as centerpoint)
	 * @param agentName
	 * @param position
	 * @param distance
	 * @return
	 * @throws Exception
	 */
	public synchronized Term selectRange( String agentName, Point position, int distance ) throws Exception
	{
		return wrapBoolean( false );
	}
	
	/**
	 * Select all the units of this agent
	 * @param agentName
	 * @return
	 * @throws Exception
	 */
	public synchronized Term selectAll( String agentName ) throws Exception
	{
		return wrapBoolean( false );
	}
	
	
//	/**
//	 * Select all the units currently under attack
//	 * @param agentName
//	 * @return
//	 * @throws Exception
//	 */
//	public synchronized Term selectUnderAttack( String agentName ) throws Exception
//	{
//		Agent agent = this.getAgent( agentName );
//		
//		for( Unit unit : agent.getUnits() )
//		{
//			
//		}
//		
//		return wrapBoolean( false );
//	}
	
	/* Test method for logging*/
	public synchronized Term log(String agentName,APLIdent var)
	{
		_logger.info(agentName + ":" + var.toString());
		return wrapBoolean(true);
	}
	   
	/* Test method for waiting */
	public synchronized Term wait( String agentName, APLNum time )
	{
		try {
			Thread.sleep( time.toInt() );
		} catch( Exception e ) {		
		}
		return wrapBoolean(true);
	}
	
	public static APLListVar wrapBoolean( boolean b )
	{
		return new APLList(new APLIdent(b ? "true" : "false"));
	}
	
	public static void main(String[] args)
	{
		new Env();
	}
	
	private void throwEventToAll(String name)
	{
		//Note the use of a function with 1 meaningless argument, since no arguments are not supported for throwing as event.
		APLFunction event = new APLFunction(name, new APLIdent("true"));
		String[] agNames = getAgentNames();
		super.throwEvent(event, agNames);
	}
	
	private String[] getAgentNames() {
		String s[] = new String[_agents.size()];
		for (int i=0; i<_agents.size();i++) {
			s[i] = _agents.get(i).getName();
		}
		
		return s;
	}
	
	/**
	 * Returns the squadron that the units is positioned in, based on values in Parameters class
	 * @param unit		The unit under consideration
	 * @return			The squadron, see Parameters class for exact values
	 */
	private int getUnitLocation(Unit unit)
	{
		int x = unit.getX();
		int y = unit.getY();
		int width = 96;
		int height = 96;
		_logger.info("before _bwapi.getMap)");
		
		
		//See info of method getMap(), returns null if loadMapData hasnt been called!
		if(_bwapi.getMap() != null)
		{	
			width = _bwapi.getMap().getWidth();
			height= _bwapi.getMap().getHeight();
			
		}
		else
		{
			_logger.info("Bwapi.getMap() == null");
		}
		_logger.info("getUnitLoccation " + x + " - " + y + " - " + width + " - " + height);
		
		if (x < width/2) 
		{
			// Marc: we need to check whether y = 0 is bottom or top, but I think it is bottom
			return (y < height/2) ? Parameters.LOC_SW : Parameters.LOC_NW;
		} else {
			return (y < height/2) ? Parameters.LOC_SE : Parameters.LOC_SW;
		}
	}
	
	/**
	 * Allocates units to the agents by their location
	 * Method is called by BWAPICoop when the game starts
	 */
	@Override
	public void gameStarted() 
	{
		_logger.info("Begin of game Started");
		
		//Loads the map, use false else starcraft might freeze.
		_bwapi.loadMapData(false);
	
		
		for (Unit unit : _bwapi.getMyUnits()) 
		{
			_logger.info("In loop");
			
			// if units are left top, allocate them to the first agent
			if (getUnitLocation(unit) == Parameters.LOC_NW) 
			{
				_agents.get(0).addUnit(unit);
			} 
			else 
			{
				_agents.get(1).addUnit(unit);
			}
		}
		
		_logger.info("Units distributed");
		
		//Setup plan bases for the agents.
		//TEST: insert attack action to position 0.0.
		for(Agent agent : _agents)
		{
			PlanBase planBase = new PlanBase(agent.getUnits());
			planBase.insertFirst(new Attack(agent.getUnits(), 0, 0));
			_planBases.put(agent.getName(), planBase);
		}
		
		_logger.info("Plan Bases setup");
		
		throwEventToAll("gameStarted");		
		_logger.info("even throwed");
	}
	
	@Override
	public void gameUpdate() 
	{
		for(PlanBase planBase : _planBases.values())
		{
			planBase.executeActions(_bwapi);
		}
	}

	@Override
	public void gameEnded() 
	{
		throwEventToAll("gameEnded");		
	}

	@Override
	public void unitDiscover(int unitID) 
	{	
		
		/*
	
		List<Unit> enemies = new LinkedList<Unit>();
		enemies.add( _bwapi.getUnit(unitID) );
		
		for(Agent agent: _agents)
		{
			PlanBase planBase = _planBases.get(agent.getName());
			planBase.insertFirst(new Attack(agent.getUnits(), enemies));
		}
		*/
	}
	
	@Override
	public void connected() 
	{
		throwEventToAll("connected");
	}
	
	@Override
	public void keyPressed(int keyCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void matchEnded(boolean winner) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playerLeft(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nukeDetect(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nukeDetect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unitEvade(int unitID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unitShow(int unitID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unitHide(int unitID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unitCreate(int unitID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unitDestroy(int unitID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unitMorph(int unitID) {
		// TODO Auto-generated method stub
		
	}
}
