package starcraft;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import starcraft.actions.Action;
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
	public static final int UPDATE_FREQ = 5;
	private Env _env;
	private volatile boolean _gameStarted = false;
	private volatile int _counter = 0;
	
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
		APLNum units, wta;
		Unit u;
		APLList base;
		
		for (Agent agent : _env._agents.values()) 
		{
			units = new APLNum(agent.countUnits());
			wta   = new APLNum(agent.getWTA());
			u     = _env._bwapi.getUnit(agent.getBuilding());
			base  = new APLList(new APLNum(u.getX()), new APLNum(u.getY()));
			
			APLFunction f = new APLFunction("gameStarted", wta, units, base);
			throwEvent(f, agent.getName());
		}
	}
	
	private void distributeUnits() 
	{
		
		ArrayList<Unit> myUnits = _env._bwapi.getMyUnits();
		
		for (Unit unit : myUnits) 
		{
			//_logger.info("In loop");
			
			// <Marc> TODO: distribution is not correct, something goes wrong with the
			// coordinate system. Moreover, buildings are also counted as units! weird....
			// Temporarily (ugly) fix:
			if (unit.getTypeID() == UnitTypes.Terran_Marine.ordinal()) 
			{
				_env._agents.get
						(
								(unit.getX() < 1000) ?
								"officer1" : "officer2"
						)
						.addUnit(unit.getID());
			} else if (unit.getTypeID() == UnitTypes.Terran_Supply_Depot.ordinal()) 
			{
				_env._agents.get
				(
						(unit.getX() < 1000) ?
						"officer1" : "officer2"
				)
				.addBuilding(unit.getID());
			}
			
			// this needs to be checked
			/*
			// if units are left top, allocate them to the first agent
			if (getUnitLocation(unit) == Grid.LOC_NW) 
			{
				_logger.info("unit at (" + unit.getX() + "," + unit.getY() + ") allocated to " + _env._agents.get(0).getName());
				_env._agents.get(0).addUnit(unit.getID());
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
		
		for (Unit enemy : _env._bwapi.getEnemyUnits()) 
		{
			if (enemy.getTypeID() == UnitTypes.Terran_Marine.ordinal()) 
			{
				countEnemies++;
				enemyUnits.add(new APLFunction("unit", new APLNum(enemy.getX()), new APLNum(enemy.getY()), new APLNum(enemy.getHitPoints())));
			} else if (enemy.getTypeID() == UnitTypes.Terran_Supply_Depot.ordinal()) 
			{
				countBuildings++;
				enemyBuildings.add(new APLFunction("building", new APLNum(enemy.getX()), new APLNum(enemy.getY()), new APLNum(enemy.getHitPoints())));;
			}
		}
		
		for (Agent agent : _env._agents.values()) 
		{
			
		
			String agentName = agent.getName();
			Set<Action> finishedActions = agent.update();
		
			throwFinishedActionsEvents(finishedActions, agentName);
	
			Point cp = agent.getCP();
			//unitCP = new APLFunction("unitCP", new APLNum((cp != null) ? cp.x : -1), new APLNum((cp != null) ? cp.y : -1));
			unitCP = new APLList(new APLNum(cp.x) ,new APLNum(cp.y));
			baseHP = new APLNum(agent.getBaseHP());
			numEnemies = new APLNum(countEnemies);
			
			//gameUpdate([unitCPx,unitCPy],HP,numEnemies,[[enemy1x,enemy1y,enemy1HP],[enemy2x,enemy2y,enemy2HP],...],[[enemyBuilding1x,enemyBuilding1y,enemyBuildingHP],[enemyBuilding2x,enemyBuilding2y,enemyBuildingHP],...]
		
			APLFunction f = new APLFunction("gameUpdate", unitCP, baseHP, numEnemies, new APLList(enemyUnits), new APLList(enemyBuildings));
			
			//System.out.println("Throwing gameUpdate event(" +  f.toString());
			throwEvent(f, agent.getName());
		}
	}
	
	
	public void throwFinishedActionsEvents(Set<Action> finishedActions, String agentName)
	{
		for(Action action : finishedActions)
		{
			APLIdent actionId = new APLIdent(action.getIdentity());
			APLFunction function = new APLFunction("actionPerformed", actionId);
			//throwEvent(function, agentName);
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
		
		_gameStarted = true;
	}
	
	@Override
	public void gameUpdate()
	{
		
		if (_gameStarted) 
		{
			if (_counter == UPDATE_FREQ) 
			{
				updateAgents();
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
		APLFunction event = new APLFunction("enemyUnitDiscovered", new APLNum(unitID));
        throwEventToAll( event );
	}
	
	@Override
	public void connected() 
	{
		throwEventToAll("connected");
		_env.broadcastTeammates();
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
