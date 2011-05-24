package starcraft;

import java.util.ArrayList;
import java.util.HashMap;

import eisbot.proxy.BWAPIEventListener;
import eisbot.proxy.JNIBWAPI;
import eisbot.proxy.model.Unit;

public class JNIAgent extends JNIBWAPI {
	private ArrayList<ArrayList<Unit>> playerUnits = new ArrayList<ArrayList<Unit>>();
	
	// player lists
	public static int LOC_NE;
	public static int LOC_NW;
	public static int LOC_SE;
	public static int LOC_SW;
	private HashMap<Integer, Integer> officers = new HashMap<Integer, Integer>();
	
	/**
	 * Instantiates a BWAPI instance, but does not connect to the bridge. To 
	 * connect, the start method must be invokeed.
	 * 
	 * @param listener - listener for BWAPI callback events.
	 */
	public JNIAgent(BWAPIEventListener listener, int[] officerLocations ) {
		super(listener);

		// create officers with their location
		for( int i = 0; i < officerLocations.length; i++ )
		{
			this.officers.put(i, officerLocations[ i ] );
		}
	}
	
	public ArrayList<Unit> getMyUnits( int officer  ) {
		return playerUnits.get( officer );
	}
}
