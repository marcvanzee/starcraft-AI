package starcraft;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import starcraft.actions.Attack;
import starcraft.actions.PlanBase;
import eisbot.proxy.model.*;
import apapl.data.APLFunction;
import apapl.data.APLIdent;
import apapl.data.APLList;
import apapl.data.APLListVar;
import apapl.data.APLNum;
import apapl.data.Term;

public class Env extends apapl.Environment
{	
	// ------------------------------------------ VARIABLE DECLARATIONS ----------------------------
	
	private static final int TOTAL_AGENTS = 2;

	public List<Agent> _agents;
	private HashMap<String,PlanBase> _planBases;
	
	private Logger _logger = Logger.getLogger("Starcraft."+Env.class.getName());
	
	public BWAPICoop _bwapi;
	private Thread _clientThread;
	private CoopEventListener _listener;
	
	// ------------------------------------------ CONSTRUCTORS -------------------------------------
	
	public Env()
	{
		init();
		_logger.info("Environment Initialised..");
	}
	
	// ------------------------------------------ PRIVATE/PROTECTED METHODS ------------------------
	
	/**
	 * Initializes the environment.
	 */
	private void init()
	{
		//int[] locations = {BWAPICoop.LOC_NE, BWAPICoop.LOC_NW};
		_planBases = new HashMap<String,PlanBase>();
		_agents = new ArrayList<Agent>();
		
		_listener = new CoopEventListener(this, _agents);
		_bwapi = new BWAPICoop(_listener);
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
	
	private APLList intListToAPLList(List<Integer> unitIds) {
		LinkedList<Term> aplIds = new LinkedList<Term>();
		
		for (Integer id : unitIds) {
			aplIds.add(new APLNum(id));
		}
		
		return new APLList(aplIds);
	}
	
	private String[] getAgentNames() 
	{
		String s[] = new String[_agents.size()];
		for (int i=0; i<_agents.size();i++) {
			s[i] = _agents.get(i).getName();
		}
		
		return s;
	}
	
	// ------------------------------------------ PUBLIC 2APL METHODS  -----------------------------
	
	public synchronized Term hello(String agentName) throws Exception
	{
		if (_agents.size() < TOTAL_AGENTS) {
			_logger.info(agentName + " registered");
			_agents.add(new Agent(agentName, _bwapi));
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
		
		planbase.insertFirst( new Attack("test", IDsAsInt, enemies) );
		return wrapBoolean(true);
	}
		
	
	/**
	 * Broadcast the teammates to all the agents
	 * @author Roemer Vlasveld
	 */
	public void broadcastTeammates()
	{
		LinkedList<Term> list = new LinkedList<Term>();
		
		for( Agent agent : _agents )
		{
			for( Agent agent2 : _agents )
			{
				if( agent.getName() != agent2.getName() )
					this.throwEvent( new APLFunction("teamMate", new APLIdent(agent2.getName())), agent.getName() );
			}
		}
		
	}
	
	
	
	// ------------------------------------------ PUBLIC NOT-2APL METHODS ------------------------
	
	/**
	 * Get the agent object using a String
	 * @param agentName
	 * @return
	 */
	public Agent getAgent( String agentName )
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
	public Agent getAgent( Integer unit )
	{
		for( Agent agent : _agents )
		{
			//_logger.info( "Looking at agent " + agent.getName() + " with agents: " + agent.getUnits() );
			
			if( agent.getUnits().contains( unit ) || (agent.getBuilding() == unit))
			{
				//_logger.info( "Found agent " + agent.getName() + " for unit: " + unit );
				return agent;
			}
		}
		
		//_logger.info( "No agent found for " + unit );
		return null;
	}
	
	/**
	 * Added a public version of throwEvent so that the eventlistener can use it
	 * @param agName
	 * @param f
	 */
	public void throwEvent(APLFunction f, String agName) 
	{
		try
		{
			super.throwEvent(f, agName);
		}catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void throwEventToAll(String name)
	{
		//Note the use of a function with 1 meaningless argument, since no arguments are not supported for throwing as event.
		APLFunction event = new APLFunction(name, new APLIdent("true"));
		String[] agNames = getAgentNames();
		try
		{
			super.throwEvent(event, agNames);
		}catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void throwEventToAll( APLFunction event )
    {
       String[] agNames = getAgentNames();
       try
       {
           super.throwEvent(event, agNames);
       }catch(Exception e)
       {
    	   e.printStackTrace();
    	   System.exit(-1);
       }
    }
	
	// ------------------------------------------ PUBLIC STATIC NON-2APL METHODS -----------------
	
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
