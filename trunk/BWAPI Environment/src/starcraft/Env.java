package starcraft;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

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
	 
	private JNIAgent _jnibwapi;
	private Thread _clientThread;
	
	//The agent names.
	private ArrayList<String> _agentNames = new ArrayList<String>();
	
	// List of desired actions per officer
	private ArrayList<Action> _actions;
	
	public Env()
	{
		init();
	}
	
	/**
	 * Initializes the environment.
	 */
	private void init()
	{
		// locations of our officers
		int[] locations = {JNIAgent.LOC_NE, JNIAgent.LOC_NW};
	 
		_jnibwapi = new JNIAgent(this, locations );
		_clientThread = new Thread(new JNIBWAPIClient(_jnibwapi));
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
		if (_agentNames.size() < TOTAL_AGENTS) {
			_agentNames.add(agentName);
			if (_agentNames.size() == TOTAL_AGENTS) {
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
	
	public void throwEventToAll(String name)
	{
		//Note the use of a function with 1 meaningless argument, since no arguments are not supported for throwing as event.
		APLFunction event = new APLFunction(name, new APLIdent("true"));
		super.throwEvent(event, (String[])_agentNames.toArray());
	}

	// ---------------------------------------------------------------------------------------
	// ------------------------- BWAPI Event Listener functions.  ----------------------------
	// ---------------------------------------------------------------------------------------
	
	@Override
	public void connected() 
	{
		throwEventToAll("connected");
	}

	@Override
	public void gameStarted() 
	{
		throwEventToAll("gameStarted");		
	}

	@Override
	public void gameUpdate() {
		// TODO Auto-generated method stub
		
		
		for( Action action : _actions )
		{
			action.perform( _jnibwapi );
		}
		
	}

	@Override
	public void gameEnded() 
	{
		throwEventToAll("gameEnded");		
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
	public void unitDiscover(int unitID) {
		// TODO Auto-generated method stub
		
		// For now, just send one unit to enemy
		List<Unit> enemies = new LinkedList<Unit>();
		enemies.add( _jnibwapi.getUnit(unitID) );
		
		for( int i = 0; i < _agentNames.size(); i++ )
		{
			_actions.add( new Attack( _jnibwapi, i, 1, enemies ) );
		}
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
