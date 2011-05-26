

//
//
// 

package starcraft;

import java.util.ArrayList;
import java.util.HashMap;

import eisbot.proxy.BWAPIEventListener;
import eisbot.proxy.JNIBWAPI;
import eisbot.proxy.model.Unit;

public class BWAPICoop extends JNIBWAPI 
{
	// Edit Marc: I have put the officers/agents and the units in the Environment, and created
	// a class called Agent where they are defined more in detail
	/*
	private ArrayList<ArrayList<Unit>> playerUnits = new ArrayList<ArrayList<Unit>>();
	
	
	//private HashMap<Integer, Integer> officers = new HashMap<Integer, Integer>();
	
	// player listss
	//Edit Frank: What is this for? i guessed you mean predefined values instead of blanco.
	public static final int LOC_NE = 0;
	public static final int LOC_NW = 1;
	public static final int LOC_SE = 2;
	public static final int LOC_SW = 3;
	
	/**
	 * Instantiates a BWAPI instance, but does not connect to the bridge. To 
	 * connect, the start method must be invokeed.
	 * 
	 * @param listener - listener for BWAPI callback events.
	 * 
	 */
	public BWAPICoop(BWAPIEventListener listener, int[] officerLocations ) 
	{
		super(listener);

		
		// create officers with their location
		//for( int i = 0; i < officerLocations.length; i++ )
		//{
		//	this.officers.put(i, officerLocations[ i ] );
		//}
	}
	/*
	 * this method can now be called using agent.getUnits
	 *
	public ArrayList<Unit> getMyUnits( int officer  ) 
	{
		return playerUnits.get( officer );
	}*/
}
