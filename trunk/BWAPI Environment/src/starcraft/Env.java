package starcraft;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import starcraft.actions.*;
import starcraft.gui.Window;
import apapl.ExternalActionFailedException;
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
	
	//Hashmap of agents with their name as key.
	public HashMap<String, Agent> _agents;
	
	//private HashMap<String,PlanBase> _planBases;
	
	private Logger _logger = Logger.getLogger("Starcraft."+Env.class.getName());
	
	public BWAPICoop _bwapi;
	private Thread _clientThread;
	private CoopEventListener _listener;
	
	final protected Window _window;
	
	// ------------------------------------------ CONSTRUCTORS -------------------------------------
	
	public Env()
	{
		_window = new Window();
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
		_agents = new HashMap<String,Agent>();
		
		_listener = new CoopEventListener(this);
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
		String s[] = new String[_agents.keySet().size()];
		int i=0; 
		
		for(String agentName : _agents.keySet())
		{
			s[i] = agentName;
			i++;
		}
		
		return s;
	}
	
	// ------------------------------------------ PUBLIC 2APL METHODS  -----------------------------
	
	public synchronized Term hello(String agentName) throws ExternalActionFailedException
	{
		if (_agents.size() < TOTAL_AGENTS) 
		{
			_logger.info(agentName + " registered");
			double wta = new Random().nextFloat();
			_agents.put(agentName, new Agent(agentName,_bwapi, wta));
			_window.setWta(agentName, wta);
			if (_agents.size() == TOTAL_AGENTS) {
				start();
		}
		} else {
			throw new ExternalActionFailedException("env> ERROR: too many agents try to register");
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
	public synchronized Term selectUnits( String agentName, int num ) throws ExternalActionFailedException
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
	public synchronized Term selectIdle( String agentName, int num ) throws ExternalActionFailedException
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
	public synchronized Term selectIdle( String agentName, APLList units ) throws ExternalActionFailedException
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
	public synchronized Term selectRange( String agentName, Point position, int radius ) throws ExternalActionFailedException
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
	public synchronized Term selectAll( String agentName ) throws ExternalActionFailedException
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
	public synchronized Term log(String agentName,APLIdent var) throws ExternalActionFailedException
	{
		_logger.info(agentName + ":" + var.toString());
		return wrapBoolean(true);
	}
	   
	/* Test method for waiting */
	public synchronized Term wait( String agentName, APLNum time ) throws ExternalActionFailedException
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
	/*
	public synchronized Term attackUnit( String agentName, String actionIdentifier, APLList unitIDs, APLNum enemyID ) throws Exception
	{
		Unit enemy = new Unit( enemyID.toInt() );		
		LinkedList<Integer> enemies = new LinkedList<Integer>();
		enemies.add( enemy.getID() );
				
		String[] IDs = unwrapStringArray( unitIDs );
		LinkedList<Integer> IDsAsInt = new LinkedList<Integer>();
		/PlanBase planbase = _planBases.get( agentName );
//		LinkedList<Unit> units = new LinkedList<Unit>();
	
		for( String id : IDs  )
		{
//			Unit unit = new Unit( Integer.parseInt( id ) );
//			units.add( unit );
			IDsAsInt.add( Integer.parseInt( id ) );
		}
		
		planbase.insertFirst( new Attack(actionIdentifier, IDsAsInt, enemies) );
		return wrapBoolean(true);
	}
	*/
	
	// kijk frank zie je hoe ik de accolades goed doe?
	// Ja, ik zie het! Goed bezig ;)
	public synchronized Term action(String agentName, APLList args) throws ExternalActionFailedException
	{
		try
		{
		LinkedList<Term> l = args.toLinkedList();
		System.out.println(agentName + " performs action " + l);
		String action = l.get(0).toString();
		
		if (action.equals("defendBuilding") && (l.size() ==  5))
			return defendBuilding(agentName, (APLIdent)l.get(1), (APLIdent)l.get(2), (APLNum)l.get(3), (APLNum)l.get(4)); 
		else if (action.equals("exploreDefensive") && (l.size() ==  3)) 
			return exploreDefensive(agentName, (APLIdent)l.get(1), (APLIdent)l.get(2));
		else if (action.equals("exploreAggressive") && (l.size() ==  3))
			return exploreAggressive(agentName, (APLIdent)l.get(1), (APLIdent)l.get(2));
		else if (action.equals("attack") && (l.size() ==  5))
			return attackPos(agentName, (APLIdent)l.get(1), (APLIdent)l.get(2), (APLNum)l.get(3), (APLNum)l.get(4));
		else if (action.equals("attackUnit") && (l.size() ==  4))
			return attackUnit(agentName, (APLIdent)l.get(1), (APLIdent)l.get(2), (APLNum)l.get(3));
		else if (action.equals("gatherForAttackBuilding") && (l.size() == 5))
			return gatherForAttackBuilding(agentName, (APLIdent) l.get(1), (APLIdent) l.get(2), (APLNum) l.get(3), (APLNum) l.get(4));
		else if (action.equals("attackEnemieBuilding") && (l.size() ==  4))
			return attackUnit(agentName, (APLIdent)l.get(1), (APLIdent)l.get(2), (APLNum)l.get(3));
		else
			return wrapBoolean(false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return wrapBoolean(false);
	}
	
	public synchronized Term defendBuilding(String agentName, APLIdent actionIdentifier, APLIdent actionType, APLNum buildingX, APLNum buildingY) throws ExternalActionFailedException
	{
		try
		{
			System.out.println("In defendBuilding");
			
			boolean isJoint = actionType.toString().toLowerCase().equals("joint") ? true : false;
			
			Agent agent = _agents.get(agentName);
			AbstractAction defendAction = new DefendBuildingAction(actionIdentifier.toString(), isJoint, agent.getUnits(), buildingX.toInt(), buildingY.toInt());
			agent.getPlanBase().insertReplace(defendAction);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return wrapBoolean(true);
	}
	
	public synchronized Term exploreDefensive(String agentName, APLIdent actionIdentifier, APLIdent actionType) throws ExternalActionFailedException
	{
		try
		{
			System.out.println("In exploreDefensive");
			// ActionType does not matter, since for single and joint defendBuilding is the same.
			boolean isJoint = actionType.toString().toLowerCase().equals("joint") ? true : false;
			boolean isDefensive = true;
			Agent agent = _agents.get(agentName);
			Point basePos = agent.getBasePos();
			Point coBasePos = agent.getCoBasePos();
			AbstractAction exploreAction = new ExploreNearestBase(actionIdentifier.toString(), isJoint, isDefensive, agent.getUnits(), basePos.x, basePos.y, coBasePos.x, coBasePos.y);
			agent.getPlanBase().insertReplace(exploreAction);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return wrapBoolean(true);
	}
	
	public synchronized Term exploreAggressive(String agentName, APLIdent actionIdentifier, APLIdent actionType) throws ExternalActionFailedException
	{
		try
		{
			boolean isJoint = actionType.toString().toLowerCase().equals("joint") ? true : false;
			boolean isDefensive = false;
			Agent agent = _agents.get(agentName);
			Point basePos = agent.getBasePos();
			Point coBasePos = agent.getCoBasePos();
			AbstractAction exploreAction = new ExploreNearestBase(actionIdentifier.toString(), isJoint, isDefensive, agent.getUnits(), basePos.x, basePos.y, coBasePos.x, coBasePos.y);
			agent.getPlanBase().insertReplace(exploreAction);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return wrapBoolean(true);
	}
	
	public synchronized Term attackPos( String agentName, APLIdent actionIdentifier, APLIdent actionType, APLNum x, APLNum y) throws ExternalActionFailedException
	{
		try
		{
			boolean isJoint = actionType.toString().toLowerCase().equals("joint") ? true : false;
			Agent agent = _agents.get(agentName);
			AbstractAction attackAction = new Attack(actionIdentifier.toString(), isJoint, agent.getUnits(), x.toInt(), y.toInt());
			agent.getPlanBase().insertReplace(attackAction);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
			
			return wrapBoolean(true);
	}
	
	public synchronized Term attackUnit(String agentName, APLIdent actionIdentifier,  APLIdent actionType, APLNum unitId )
	{
		try
		{
			boolean isJoint = actionType.toString().toLowerCase().equals("joint") ? true : false;
			Agent agent = _agents.get(agentName);
			List<Integer> units = new ArrayList<Integer>();
			units.add(unitId.toInt());
			
			AbstractAction  attackAction  = new Attack(actionIdentifier.toString(), isJoint, agent.getUnits(), units);
			agent.getPlanBase().insertReplace(attackAction);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return wrapBoolean(true);
	}
	
	public synchronized Term gatherForAttackBuilding(String agentName, APLIdent actionIdentifier, APLIdent actionType, APLNum X, APLNum Y)
	{
		try
		{
			boolean isJoint = actionType.toString().toLowerCase().equals("joint") ? true : false;
			Agent agent = _agents.get(agentName);
			
			AbstractAction  gatherAction  = new GatherForAttackBuilding(actionIdentifier.toString(), isJoint, agent.getUnits(), new Point(X.toInt(),Y.toInt()), agent.getCP(), agent.getCoBasePos());
			agent.getPlanBase().insertReplace(gatherAction);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return wrapBoolean(true);
	}
	
	public synchronized Term allocatePriorities( String agentName, APLNum baseX, APLNum baseY, APLNum BaseHP, 
												 APLNum NumUnits, APLList UnitCP, 
												 APLNum NumEnemies, APLList Enemies, 
												 APLList EnemyBases, APLNum WTA ) 
	throws ExternalActionFailedException
	{
		int defendPriority = 0;
		int attackPriority = 0;
		
		// first of all, look if there are enemies around
		if (Enemies.isEmpty()) 
		{
			// is there an enemybase?
			if (EnemyBases.isEmpty()) 
			{
				// there is nothing, so we will defend or attack depending on our WTA
				// both priorities will be very low, so an important event will be able
				// to interrupt it easily
				
				// the WTA has domain [0,1], so these priorities are not able to exceed 0,1
				attackPriority =      (int) Math.round(Math.random() * WTA.toDouble() * 10);
				defendPriority = 10 - attackPriority;
			} 
			else 
			{
				// there is an enemy-base, but no enemy characters: attack it!
				attackPriority = 10;
				defendPriority = 0;
			}
		} 
		else 
		{
			// there are enemies, ohoh, better watch out
			// first see if we are with more units
			if (NumUnits.toInt() > NumEnemies.toInt()/2) 
			{
				// attack the enemy
				attackPriority = 10;
				defendPriority = 0;
			}
			else {
				// we are with less units, shit!
				// RETREAT RETREAT!!!
				defendPriority = 10;
				attackPriority = 0;
			}
		}
		
		APLList ret = new APLList(new APLNum(attackPriority),new APLNum(defendPriority));
		_window.setSubgoal(agentName, attackPriority, defendPriority);
		
		return ret;
	}
	
	public synchronized Term selectedPlan(String agentName, APLNum planId, APLList jointPlan, APLList singlePlan)
		throws ExternalActionFailedException
	{
		_window.selectedPlan(agentName, planId.toInt(), jointPlan.toString(), singlePlan.toString());
		
		return wrapBoolean(true);
	}
	
	public synchronized Term receivedPlan(String agentName)
		throws ExternalActionFailedException
	{
		_window.receivedPlan(agentName);
		
		return wrapBoolean(true);
	}
	
	public synchronized Term newPlan(String agentName, APLList plan)
		throws ExternalActionFailedException
	{
		_window.newPlan(agentName, plan.toString());
		
		return wrapBoolean(true);
	}
	
	public synchronized Term continuePlan(String agentName)
		throws ExternalActionFailedException
	{
		_window.continuePlan(agentName);
		
		return wrapBoolean(true);
	}
	
	// ------------------------------------------ PUBLIC NOT-2APL METHODS ------------------------
	
	/**
	 * Get the agent object using a String
	 * @param agentName
	 * @return
	 */
	public Agent getAgent( String agentName )
	{
		if(_agents.containsKey(agentName))
			return _agents.get(agentName);
		else
			return null;
	}
	
	/**
	 * Get the agent of a specific unit
	 * @param unit
	 * @return
	 */
	public Agent getAgent( Integer unit )
	{
		for( Agent agent : _agents.values() )
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
		System.out.println("Gonna throw "  + agNames[0] + agNames[1]);
		try
		{
			if(agNames.length > 0)
				super.throwEvent(event, agNames);
		}catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Error " + name + " -- " + agNames);
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
	
	/**
	 * Broadcast the teammates to all the agents
	 * @author Roemer Vlasveld
	 */
	public void broadcastTeammates()
	{	
		for( Agent agent : _agents.values() )
		{
			for( Agent agent2 : _agents.values() )
			{
				if( agent.getName() != agent2.getName() ) {
					// send: teamMate(name, baseX, baseY)
					Point basePos = agent2.getBasePos();
					System.out.println(basePos.toString());
					throwEvent( new APLFunction("teamMate", new APLIdent(agent2.getName()), new APLNum(basePos.x), new APLNum(basePos.y)), agent.getName() );
				}
			}
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
