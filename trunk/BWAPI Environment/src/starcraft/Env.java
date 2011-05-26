package starcraft;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

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
	 
	private JNIBWAPI _bwapi;
	private Thread _clientThread;
		
	// *** Edit Marc: zullen we officers gewoon agents noemen? dat is denk ik duidelijker *** //
	// List of desired actions per agent
	private ArrayList<Action> _actions;
	
	// *** Edit Marc: arraylist of agents.  *** //
	private ArrayList<Agent> _agents = new ArrayList<Agent>();
	
	public Env()
	{
		init();
	}
	
	/**
	 * Initializes the environment.
	 */
	private void init()
	{
		// Edit Marc: by declaring the agents with their units in this class, the BWAPICoop class is not needed anymore
		
		// locations of our agents
		//int[] locations = {BWAPICoop.LOC_NE, BWAPICoop.LOC_NW};
	 
		//_bwapi = new BWAPICoop(this, locations );
		
		_bwapi = new JNIBWAPI(this);
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
	private int getUnitLocation(Unit unit) {
		int x = unit.getX();
		int y = unit.getY();
		int width = _bwapi.getMap().getWidth();
		int height= _bwapi.getMap().getHeight();
		
		if (x < width/2) {
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
		for (Unit unit : _bwapi.getMyUnits()) {
			// if units are left top, allocate them to the first agent
			if (getUnitLocation(unit) == Parameters.LOC_NW) {
				_agents.get(0).addUnit(unit);
			} else {
				_agents.get(1).addUnit(unit);
			}
		}
		throwEventToAll("gameStarted");		
	}
	
	@Override
	public void gameUpdate() 
	{
		for( Action action : _actions )
		{
			action.perform( _bwapi );
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
		// For now, just send one unit to enemy
		List<Unit> enemies = new LinkedList<Unit>();
		enemies.add( _bwapi.getUnit(unitID) );
		
		for( int i = 0; i < _agents.size(); i++ )
		{
			// *** MARC *** dit geeft een error, en ik snap eigenlijk de "action" opzet nog niet helemaal
		//	_actions.add( new Attack( _bwapi, i, 1, enemies ) );
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
	public void unitDestroy(int unitID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unitMorph(int unitID) {
		// TODO Auto-generated method stub
		
	}
}
