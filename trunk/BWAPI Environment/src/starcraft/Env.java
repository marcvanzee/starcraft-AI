package starcraft;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import starcraft.actions.Attack;
import starcraft.actions.PlanBase;
import starcraft.parameters.Grid;
import eisbot.proxy.BWAPIEventListener;
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
	
	/**
	 * Get the agent object using a String
	 * @param agentName
	 * @return
	 */
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
	 * Get the agent of a specific unit
	 * @param unit
	 * @return
	 */
	private Agent getAgent( Integer unit )
	{
		for( Agent agent : _agents )
		{
			_logger.info( "Looking at agent " + agent.getName() + " with agents: " + agent.getUnits() );
			
			if( agent.getUnits().contains( unit ) )
			{
				_logger.info( "Found agent " + agent.getName() + " for unit: " + unit );
				return agent;
			}
		}
		
		_logger.info( "No agent found for " + unit );
		return null;
	}
	
	
	/**
	 * Select num random units
	 * @param agentName
	 * @param num
	 * @return
	 * @throws Exception
	 */
	public synchronized Term selectUnits( String agentName, int num ) throws Exception
	{
		List<Integer> unitIds = getAgent(agentName).getUnits(num);
		
		return intListToAPLList(unitIds);
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
		// receive all units, then filter out the non-idle ones and finally limit number
		// of units to <num>
		List<Integer> unitIds = getAgent(agentName).getUnits();
		
		// filter out non-idle units
		for (int unit : unitIds) {
			if (!_bwapi.getUnit(unit).isIdle())
				unitIds.remove(unit);
		}
		
		// limit number to <num>
		unitIds = unitIds.subList(0, (num>unitIds.size())?unitIds.size():num);

		return intListToAPLList(unitIds);
	}
	
	public APLList intListToAPLList(List<Integer> unitIds) {
		LinkedList<Term> aplIds = new LinkedList<Term>();
		
		for (Integer id : unitIds) {
			aplIds.add(new APLNum(id));
		}
		
		return new APLList(aplIds);
	}
	
	/**
	 * Select idle units from a given list
	 * @param agentName
	 * @param units
	 * @return
	 * @throws Exception
	 */
	public synchronized Term selectIdle( String agentName, APLList units ) throws Exception
	{
		LinkedList<Term> list = units.toLinkedList();

		for (Term unit : list) {
			if (!_bwapi.getUnit(((APLNum)unit).toInt()).isIdle())
				list.remove(unit);
		}
		
		return new APLList(list);
	}
	
	/**
	 * Select all the units within a range (position as centerpoint)
	 * @param agentName
	 * @param position
	 * @param distance
	 * @return
	 * @throws Exception
	 */
	public synchronized Term selectRange( String agentName, Point position, int radius ) throws Exception
	{
		LinkedList<Term> list = new LinkedList<Term>();
		
		LinkedList<Term> result = new LinkedList<Term>();
		
		Agent agent = this.getAgent( agentName );
		
		for( int unitID : agent.getUnits() )
		{
			result.add( new APLNum( unitID ) );
		}
		
		return new APLList(list);
	}
	
	/**
	 * Select all the units of this agent
	 * @param agentName
	 * @return
	 * @throws Exception
	 */
	public synchronized Term selectAll( String agentName ) throws Exception
	{
//		Unit enemy = new Unit( enemyID.toInt() );		
//		LinkedList<Integer> enemies = new LinkedList<Integer>();
//		enemies.add( enemy.getID() );
//				
//		String[] IDs = unwrapStringArray( unitIDs );
//		LinkedList<Integer> IDsAsInt = new LinkedList<Integer>();
//		PlanBase planbase = _planBases.get( agentName );
//		LinkedList<Unit> units = new LinkedList<Unit>();
//	
//		for( String id : IDs  )
//		{
////			Unit unit = new Unit( Integer.parseInt( id ) );
////			units.add( unit );
//			IDsAsInt.add( Integer.parseInt( id ) );
//		}
//		
//		planbase.insertFirst( new Attack(IDsAsInt, enemies) );
//		return wrapBoolean(true);
		List<Integer> list = new ArrayList<Integer>();
		
		list = getAgent(agentName).getUnits();
		
		return intListToAPLList(list);
	}
	
	
	
	public static String[] unwrapStringArray(APLList list)
	{
		String[] array = new String[list.toLinkedList().size()];
		int i=0;
		for(Term t : list.toLinkedList())
		{
			array[i] = t.toString();
			i++;
		}
		return array;
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
	
	private void throwEventToAll( APLFunction event )
    {
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
	 * Select an enemyID unit with own unitIDs
	 * @param agentName
	 * @param unitIDs
	 * @param enemyID
	 * @return
	 * @throws Exception
	 */
	public synchronized Term attackUnit( String agentName, APLList unitIDs, APLNum enemyID ) throws Exception
	{
		Unit enemy = new Unit( enemyID.toInt() );		
		LinkedList<Integer> enemies = new LinkedList<Integer>();
		enemies.add( enemy.getID() );
				
		String[] IDs = unwrapStringArray( unitIDs );
		LinkedList<Integer> IDsAsInt = new LinkedList<Integer>();
		PlanBase planbase = _planBases.get( agentName );
//		LinkedList<Unit> units = new LinkedList<Unit>();
	
		for( String id : IDs  )
		{
//			Unit unit = new Unit( Integer.parseInt( id ) );
//			units.add( unit );
			IDsAsInt.add( Integer.parseInt( id ) );
		}
		
		planbase.insertFirst( new Attack(IDsAsInt, enemies) );
		return wrapBoolean(true);
	}
	
	/**
	 * Returns the squadron that the units is positioned in, based on values in Parameters class
	 * @param unit		The unit under consideration
	 * @return			The squadron, see Parameters class for exact values
	 */
	private int getUnitLocation(Unit unit)
	{
		int x = unit.getTileX();
		int y = unit.getTileY();
		int width = Grid.WIDTH;
		int height = Grid.HEIGHT;
		_logger.info("before _bwapi.getMap)");
		
		
		//See info of method getMap(), returns null if loadMapData hasnt been called!
		if(_bwapi.getMap() != null)
		{	
			width = _bwapi.getMap().getWalkWidth();
			height= _bwapi.getMap().getWalkHeight();
			
		}
		else
		{
			_logger.info("Bwapi.getMap() == null");
		}
		_logger.info("getUnitLoccation " + x + " - " + y + " - " + width + " - " + height);
		
		if (x < width/2) 
		{
			// Marc: we need to check whether y = 0 is bottom or top, but I think it is bottom
			return (y < height/2) ? Grid.LOC_SW : Grid.LOC_NW;
		} else {
			return (y < height/2) ? Grid.LOC_SE : Grid.LOC_SW;
		}
	}
	
	/**
	 * Allocates units to the agents by their location
	 * Method is called by BWAPICoop when the game starts
	 */
	@Override
	public void gameStarted() 
	{
		_bwapi.enableUserInput();
		_logger.info("Game Started");
		
		//Loads the map, use false else starcraft might freeze.
		_bwapi.loadMapData(false);
	
		for (Unit unit : _bwapi.getMyUnits()) 
		{
			_logger.info("In loop");
			
			// if units are left top, allocate them to the first agent
			if (getUnitLocation(unit) == Grid.LOC_NW) 
			{
				_agents.get(0).addUnit(unit.getID());
			} 
			else 
			{
				_agents.get(1).addUnit(unit.getID());
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
		APLFunction event = new APLFunction("enemyUnitDiscovered", new APLNum(unitID));
        throwEventToAll( event );

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
		
		
		
//		Unit unit = new Unit( unitID );
		Agent agent = this.getAgent( unitID );
		_logger.info( "unitDestroy " + unitID + " agent of unit: " + agent.getName() );
		
		APLFunction event = new APLFunction( "ownUnitDestroyed", new APLNum( agent.getUnits().size() ) );
		
		_logger.info( event.toString() );
		
		super.throwEvent(event, agent.getName() );
//		throwEventToAll( event );
	}

	@Override
	public void unitMorph(int unitID) {
		// TODO Auto-generated method stub
		
	}
}
