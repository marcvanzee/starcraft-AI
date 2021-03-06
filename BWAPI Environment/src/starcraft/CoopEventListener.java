package starcraft;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import starcraft.actions.AbstractAction;
import starcraft.parameters.Grid;
import apapl.data.APLFunction;
import apapl.data.APLIdent;
import apapl.data.APLList;
import apapl.data.APLNum;
import apapl.data.Term;
import eisbot.proxy.BWAPIEventListener;
import eisbot.proxy.model.Unit;
import eisbot.proxy.types.UnitType.UnitTypes;

public class CoopEventListener implements BWAPIEventListener 
{
	
	// ------------------------------------------ VARIABLE DECLARATIONS ----------------------------
	public static final int UPDATE_STEP = 40;
	private Env _env;
	private volatile boolean _gameStarted = false;
	private volatile int _counter = 0;
	
	Map<String,Integer> finishedJointActions = new HashMap<String,Integer>();
	
	// ------------------------------------------ CONSTRUCTORS -------------------------------------
	
	public CoopEventListener(Env env) {
		_env = env;
	}

	// ------------------------------------------ PRIVATE METHODS ----------------------------------
	
	private void initBWAPI() {
		_env._bwapi.enableUserInput();
		System.out.println("Game Started");
		
		//Loads the map, use false else starcraft might freeze.
		_env._bwapi.loadMapData(false);
		
		//_env._bwapi.setGameSpeed(20);
	}

	/**
	 * TODO: Agents hebben nu de methode initPlanBase(), hier moet de planbase van elke
	 * agent nog geinitialiseerd worden.
	 */
	private void initPlanBases() 
	{
		//Setup plan bases for the agents.
		//TEST: insert attack action to position 0.0.
		
		for(Agent agent : _env._agents.values())
		{
			agent.initPlanBase();
			//PlanBase planBase = new PlanBase(agent.getUnits());
			//planBase.insertFirst(new Attack(agent.getUnits(), 0, 0));
			//_planBases.put(agent.getName(), planBase);
		}
	}
	
	/**
	 * Inform all agents on their initial parameters, namely the number of units, the position
	 * of the base and his character, his willingness to attack.
	 * These parameters only have to be given once.
	 * 
	 * The following event will be sent to every agent: gameStarted([wta, numUnits, [baseX,baseY] ])
	 */
	private void init2APLAgents() 
	{
		APLNum units, wta, baseX, baseY, coBaseX,coBaseY;
		
		for (Agent agent : _env._agents.values()) 
		{
			units = new APLNum(agent.countUnits());
			wta   = new APLNum(agent.getWTA());
			baseX = new APLNum(agent.getBasePos().x);
			baseY = new APLNum(agent.getBasePos().y);
			coBaseX = new APLNum(agent.getCoBasePos().x);
			coBaseY = new APLNum(agent.getCoBasePos().y);
			
			APLFunction f = new APLFunction("gameStarted", wta, units, baseX, baseY, coBaseX,coBaseY);
			throwEvent(f, agent.getName());
		}
	}
	
	
	
	private void distributeUnits() 
	{
		String agentName1 = "officer1";
		String agentName2 = "officer2";
	
		ArrayList<Unit> myUnits = _env._bwapi.getMyUnits();
		
		for (Unit unit : myUnits) 
		{
			
			String thisAgent = unit.getX() < 1000 ? agentName1 : agentName2;
			String otherAgent = unit.getX() < 1000 ? agentName2 : agentName1;
			
			
			//_logger.info("In loop");
			
			// <Marc> TODO: distribution is not correct, something goes wrong with the
			// coordinate system. Moreover, buildings are also counted as units! weird....
			// Temporarily (ugly) fix:
			if (unit.getTypeID() == UnitTypes.Terran_Marine.ordinal()) 
			{
				_env._agents.get(thisAgent).addUnit(unit.getID());
			} 
			else if (unit.getTypeID() == UnitTypes.Terran_Supply_Depot.ordinal()) 
			{	
				_env._agents.get(thisAgent).addBuilding(unit.getID(), unit.getX(), unit.getY());
				_env._agents.get(otherAgent).setCoBase(unit.getX(), unit.getY());
			}
			
			// this needs to be checked
			/*
			// if units are left top, allocate them to the first agent
			if (getUnitLocation(unit) == Grid.LOC_NW) 
			{
				_logger.info("unit at (" + unit.getX() + "," + unit.getY() + ") allocated to " + _env._agents.get(0).getName());
				_env._agents.ge
				t(0).addUnit(unit.getID());
			} 
			else 
			{
				_logger.info("unit at (" + unit.getX() + "," + unit.getY() + ") allocated to " + _env._agents.get(1).getName());
				_env._agents.get(1).addUnit(unit.getID());
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
	private int getUnitLocation(Unit unit)
	{
		int x = unit.getTileX();
		int y = unit.getTileY();
		int width = Grid.WIDTH;
		int height = Grid.HEIGHT;
		//_logger.info("before _bwapi.getMap)");
		
		
		//See info of method getMap(), returns null if loadMapData hasnt been called!
		if(_env._bwapi.getMap() != null)
		{	
			width = _env._bwapi.getMap().getWalkWidth();
			height= _env._bwapi.getMap().getWalkHeight();
			
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
	
	/**
	 * Agents update at every game tick, and will send the information regarding this update
	 * to the 2APL agents. The Agents class has access to the BWAPI interface, so is able
	 * to update itself. In this methods we only need to send the information to the 2APL agents.
	 * 
	 * we are sending the following APLFunction: gameUpdate(ownCP, baseHP, numEnemyUnits, [enemyCP1, enemyCP2, ...])
	 * 
	 * there are more center points of enemies possible, which depends on their location
	 */
	private void updateAgents()
	{
		
		int countEnemies=0,countBuildings=0;
		LinkedList<Term> enemyBuildings = new LinkedList<Term>();
		LinkedList<Term> enemyUnits = new LinkedList<Term>();
		APLNum baseHP, numEnemies;
		APLList unitCP;
		APLList enemyU, enemyB;

		for (Unit enemy : _env._bwapi.getEnemyUnits()) 
		{
			if (enemy.getTypeID() == UnitTypes.Terran_Marine.ordinal()) 
			{
				countEnemies++;
				enemyUnits.add(new APLList(new APLNum(enemy.getX()), new APLNum(enemy.getY()), new APLNum(enemy.getHitPoints())));
			} else if (enemy.getTypeID() == UnitTypes.Terran_Supply_Depot.ordinal()) 
			{
				countBuildings++;
				enemyBuildings.add(new APLList(new APLNum(enemy.getID()), new APLNum(enemy.getHitPoints()), new APLNum(enemy.getX()), new APLNum(enemy.getY())));;
			}
		}

		enemyU = new APLList(enemyUnits);
		enemyB = new APLList(enemyBuildings);
		
		for (Agent agent : _env._agents.values()) 
		{
			String agentName = agent.getName();
			Set<AbstractAction> finishedActions = agent.update();
			
			for(AbstractAction action : finishedActions)
			{
				if(action.isJoint())
				{
					if(finishedJointActions.containsKey(action.getIdentity()))
					{
						Integer newValue = finishedJointActions.get(action.getIdentity());
						finishedJointActions.put(action.getIdentity(), newValue);
					}
					else
					{
						finishedJointActions.put(action.getIdentity(), 1);
					}
					
				}
			}
			throwFinishedSingleActionsEvents(finishedActions, agentName);
			
			
			Point cp = agent.getCP();
			//unitCP = new APLFunction("unitCP", new APLNum((cp != null) ? cp.x : -1), new APLNum((cp != null) ? cp.y : -1));
			unitCP = new APLList(new APLNum(cp.x) ,new APLNum(cp.y));
			baseHP = new APLNum(agent.getBaseHP());
			numEnemies = new APLNum(countEnemies);

			APLFunction f = new APLFunction("gameUpdate", unitCP, baseHP, numEnemies, enemyU, enemyB);
			
			throwEvent(f, agent.getName());
		}
		
		List<String> keysToRemove = new ArrayList<String>();
		for(Entry<String,Integer> entry :  finishedJointActions.entrySet())
		{
			if(entry.getValue() ==  _env._agents.size())
			{
				keysToRemove.add(entry.getKey());
				//joint action finished.
				throwFinishedJointActionsEvents(entry.getKey());
			}
		}
		
		for(String key : keysToRemove)
		{
			finishedJointActions.remove(key);
		}
		
	}
	
	public void throwFinishedJointActionsEvents(String actionIdentifier)
	{
		APLIdent actionId = new APLIdent(actionIdentifier);
		APLFunction function = new APLFunction("actionPerformed", actionId);
		throwEventToAll(function);
	}
	
	public void throwFinishedSingleActionsEvents(Set<AbstractAction> finishedActions, String agentName)
	{
		for(AbstractAction action : finishedActions)
		{
			if(!action.isJoint())
			{
				System.out.println("gonna throwing actionPerfomed: " + action.getIdentity());
				APLIdent actionId = new APLIdent(action.getIdentity());
				APLFunction function = new APLFunction("actionPerformed", actionId);
				System.out.println("gonna throwing actionPerfomed: " + actionId.toString() + "--" + function.toString());
				throwEvent(function, agentName);
			}
		}
		
	}
	
	// INTERFACE METHODS FOR ENVIRONMENT
	
	private void throwEvent(APLFunction f, String agName) {
		_env.throwEvent(f, agName);
	}
	
	private void throwEventToAll(String s) {
		_env.throwEventToAll(s);
	}
	
	private void throwEventToAll(APLFunction f) {
		_env.throwEventToAll(f);
	}
	
	private Agent getAgent(int unitID) {
		return _env.getAgent(unitID);
	}
	
	// ------------------------------------------ PUBLIC BWAPI METHODS -----------------------------

	/**
	 * Allocates units to the agents by their location
	 * Method is called by BWAPICoop when the game starts
	 */
	@Override
	public void gameStarted() 
	{
		initBWAPI();
		
		distributeUnits();

		// make sure all 2APL agents know the basic facts
		init2APLAgents();
		
		//Init plan Bases should occur after all units are assigned!
		initPlanBases();

		// update information
		//updateAgents();
		
		

		System.out.println("gameStarted in Coop");
		_gameStarted = true;
		
		_env.broadcastTeammates();
	}
	
	@Override
	public void gameUpdate()
	{	
		if (_gameStarted) 
		{
			if (_counter == UPDATE_STEP) 
			{
				try
				{
					updateAgents();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
					_counter=0;
			} 
			else 
			{
				_counter++;
			}
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
		System.out.println("unit discovered: " + unitID);
		
		try{
		APLFunction event = new APLFunction("enemyUnitDiscovered", new APLNum(unitID));
        throwEventToAll( event );
        System.out.println("unit discovered: " + event.toString());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
		}
   
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
	public void unitDestroy(int unitID) 
	{
		// unit can be both a character or a building. doesn't matter for the agent, he deletes both
		Agent ag = getAgent(unitID);
		ag.removeElement(unitID);

		// inform the agent that the unit has been removed
		throwEvent(new APLFunction("unitDestroyed", new APLNum(0)), ag.getName());
		
		System.out.println("unit destroyed!");
	}

	@Override
	public void unitMorph(int unitID) {
		// TODO Auto-generated method stub
		
	}
}
