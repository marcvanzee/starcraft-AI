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
import eisbot.proxy.types.UnitType.UnitTypes;
import apapl.data.APLFunction;
import apapl.data.APLIdent;
import apapl.data.APLList;
import apapl.data.APLListVar;
import apapl.data.APLNum;
import apapl.data.Term;

public class Env extends apapl.Environment implements BWAPIEventListener
{
	/**
	 * Marc // EDIT #1
	 * organized methods in the following order:
	 * 
	 * variable declarations
	 * constructors
	 * private/protected methods
	 * public methods - calls by the 2apl agents
	 * public methods - calls by BWAPI
	 * other public methods (static)
	 * 
	 * Marc // EDIT #2
	 * - changed all "private" fields to "protected" - allows extending this class
	 * - Removed all "synchronized" from the private methods. They will only be called
	 *   by this class, which does not need synchronization with itself.
	 */
	
	// ------------------------------------------ VARIABLE DECLARATIONS ----------------------------
	
	protected static final int TOTAL_AGENTS = 2;

	protected List<Agent> _agents;
	protected HashMap<String,PlanBase> _planBases;
	
	protected Logger _logger = Logger.getLogger("Starcraft."+Env.class.getName());
	 
	protected BWAPICoop _bwapi;
	protected Thread _clientThread;
	
	// ------------------------------------------ CONSTRUCTORS ----------------------------
	
	public Env()
	{
		init();
		_logger.info("Environment Initialised..");
	}
	
	// ------------------------------------------ PRIVATE/PROTECTED METHODS ----------------------------
	
	/**
	 * Initializes the environment.
	 */
	private void init()
	{
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
	
	/**
	 * Get the agent object using a String
	 * @param agentName
	 * @return
	 */
	protected Agent getAgent( String agentName )
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
	protected Agent getAgent( Integer unit )
	{
		for( Agent agent : _agents )
		{
			//_logger.info( "Looking at agent " + agent.getName() + " with agents: " + agent.getUnits() );
			
			if( agent.getUnits().contains( unit ) )
			{
				//_logger.info( "Found agent " + agent.getName() + " for unit: " + unit );
				return agent;
			}
		}
		
		//_logger.info( "No agent found for " + unit );
		return null;
	}
	
	protected void distributeUnits() {
		for (Unit unit : _bwapi.getMyUnits()) 
		{
			//_logger.info("In loop");
			
			// <Marc> TODO: distribution is not correct, something goes wrong with the
			// coordinate system. Moreover, buildings are also counted as units! weird....
			// Temporarily (ugly) fix:
			if (unit.getTypeID() == UnitTypes.Terran_Marine.ordinal()) {
				_agents.get
						(
								(unit.getX() < 1000) ?
								0 : 1
						)
						.addUnit(unit.getID());
			} else if (unit.getTypeID() == UnitTypes.Terran_Supply_Depot.ordinal()) {
				_agents.get
				(
						(unit.getX() < 1000) ?
						0 : 1
				)
				.addBuilding(unit.getID());
			}
			
			// this needs to be checked
			/*
			// if units are left top, allocate them to the first agent
			if (getUnitLocation(unit) == Grid.LOC_NW) 
			{
				_logger.info("unit at (" + unit.getX() + "," + unit.getY() + ") allocated to " + _agents.get(0).getName());
				_agents.get(0).addUnit(unit.getID());
			} 
			else 
			{
				_logger.info("unit at (" + unit.getX() + "," + unit.getY() + ") allocated to " + _agents.get(1).getName());
				_agents.get(1).addUnit(unit.getID());
			}
			*/
		}
		
		//_logger.info("Units distributed");
	}
	
	/**
	 * Returns the squadron that the units is positioned in, based on values in Parameters class
	 * @param unit		The unit under consideration
	 * @return			The squadron, see Parameters class for exact values
	 */
	protected int getUnitLocation(Unit unit)
	{
		int x = unit.getTileX();
		int y = unit.getTileY();
		int width = Grid.WIDTH;
		int height = Grid.HEIGHT;
		//_logger.info("before _bwapi.getMap)");
		
		
		//See info of method getMap(), returns null if loadMapData hasnt been called!
		if(_bwapi.getMap() != null)
		{	
			width = _bwapi.getMap().getWalkWidth();
			height= _bwapi.getMap().getWalkHeight();
			
		}
		else
		{
			//_logger.info("Bwapi.getMap() == null");
		}
		//F_logger.info("getUnitLoccation " + x + " - " + y + " - " + width + " - " + height);
		
		if (x < width/2) 
		{
			// Marc: we need to check whether y = 0 is bottom or top, but I think it is bottom
			return (y < height/2) ? Grid.LOC_SW : Grid.LOC_NW;
		} else {
			return (y < height/2) ? Grid.LOC_SE : Grid.LOC_SW;
		}
	}
	
	protected void throwEventToAll(String name)
	{
		//Note the use of a function with 1 meaningless argument, since no arguments are not supported for throwing as event.
		APLFunction event = new APLFunction(name, new APLIdent("true"));
		String[] agNames = getAgentNames();
		super.throwEvent(event, agNames);
	}
	
	protected void throwEventToAll( APLFunction event )
    {
            String[] agNames = getAgentNames();
            super.throwEvent(event, agNames);
    }
	
	protected String[] getAgentNames() 
	{
		String s[] = new String[_agents.size()];
		for (int i=0; i<_agents.size();i++) {
			s[i] = _agents.get(i).getName();
		}
		
		return s;
	}
	
	// ------------------------------------------ PUBLIC 2APL METHODS  ----------------------------
	
	public synchronized Term hello(String agentName) throws Exception
	{
		if (_agents.size() < TOTAL_AGENTS) {
			_logger.info(agentName + " registered");
			_agents.add(new Agent(agentName));
			if (_agents.size() == TOTAL_AGENTS) {
				start();
			}
		} else {
			throw new Exception("env> ERROR: too many agents try to register");
		}
		_logger.info("returning " + agentName);
		
		return wrapBoolean(true);
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
	
	public synchronized APLList intListToAPLList(List<Integer> unitIds) {
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
	
	
	// ------------------------------------------ PUBLIC BWAPI METHODS ----------------------------
	
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
	
		distributeUnits();
		
		//Setup plan bases for the agents.
		//TEST: insert attack action to position 0.0.
		for(Agent agent : _agents)
		{
			PlanBase planBase = new PlanBase(agent.getUnits());
			planBase.insertFirst(new Attack(agent.getUnits(), 0, 0));
			_planBases.put(agent.getName(), planBase);
		}
		
		_logger.info("Plan Bases setup");
	}
	
	@Override
	public void gameUpdate() 
	{
		System.out.println("gameupdate");
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

	// ------------------------------------------ PUBLIC STATIC METHODS ----------------------------
	
	public static APLListVar wrapBoolean( boolean b )
	{
		return new APLList(new APLIdent(b ? "true" : "false"));
	}
	
	public static void main(String[] args)
	{
		new Env();
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
}
