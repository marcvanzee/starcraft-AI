package starcraft;

import java.util.List;

import starcraft.parameters.Grid;
import apapl.data.APLFunction;
import apapl.data.APLList;
import apapl.data.APLNum;
import eisbot.proxy.BWAPIEventListener;
import eisbot.proxy.model.Unit;
import eisbot.proxy.types.UnitType.UnitTypes;

public class CoopEventListener implements BWAPIEventListener {
	
	// ------------------------------------------ VARIABLE DECLARATIONS ----------------------------
	
	private BWAPICoop _bwapi;
	private Env _env;
	private List<Agent> _agents;

	// ------------------------------------------ CONSTRUCTORS -------------------------------------
	
	public CoopEventListener(Env env, List<Agent> agents) {
		_env = env;
		_agents = agents;
	}

	// ------------------------------------------ PRIVATE METHODS ----------------------------------
	
	private void initBWAPI() {
		_bwapi.enableUserInput();
		System.out.println("Game Started");
		
		//Loads the map, use false else starcraft might freeze.
		_bwapi.loadMapData(false);
	}

	/**
	 * TODO: Agents hebben nu de methode initPlanBase(), hier moet de planbase van elke
	 * agent nog geinitialiseerd worden.
	 */
	private void initPlanBases() {
		//Setup plan bases for the agents.
		//TEST: insert attack action to position 0.0.
		
		for(Agent agent : _agents)
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
	private void init2APLAgents() {
		APLNum units, wta;
		Unit u;
		APLList base;
		
		for (Agent agent : _agents) {
			units = new APLNum(agent.countUnits());
			wta   = new APLNum(agent.getWTA());
			u     = _bwapi.getUnit(agent.getBuilding());
			base  = new APLList(new APLNum(u.getX()), new APLNum(u.getY()));
			
			APLFunction f = new APLFunction("gameStarted", wta, units, base);
			throwEvent(f, agent.getName());
		}
	}
	
	private void distributeUnits() {
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
	private int getUnitLocation(Unit unit)
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
	
	/**
	 * Agents update at every game tick, and will send the information regarding this update
	 * to the 2APL agents. The Agents class has access to the BWAPI interface, so is able
	 * to update itself. In this methods we only need to send the information to the 2APL agents.
	 * 
	 * we are sending the following APLFunction: gameUpdate(ownCP, baseHP, enemyCP) 
	 */
	private void updateAgents() {
		for (Agent ag : _agents) {
			ag.update();
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
	public void gameStarted() {
		initBWAPI();
		
		distributeUnits();
		//initPlanBases();
		
		// make sure all 2APL agents know the basic facts
		init2APLAgents();
		
		// update information
		updateAgents();
		
		_bwapi.enableUserInput();
		System.out.println("ja");
	}
	
	@Override
	public void gameUpdate()
	{
		updateAgents();
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

	
	public void setClient(BWAPICoop bwapi) {
		_bwapi = bwapi;
	}
}
