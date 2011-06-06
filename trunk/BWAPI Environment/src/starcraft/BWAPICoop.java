package starcraft;

import eisbot.proxy.BWAPIEventListener;
import eisbot.proxy.JNIBWAPI;

public class BWAPICoop extends JNIBWAPI
{	
	/**
	 * Instantiates a BWAPI instance, but does not connect to the bridge. To 
	 * connect, the start method must be invokeed.
	 * 
	 * @param listener - listener for BWAPI callback events.
	 * 
	 */
	public BWAPICoop(BWAPIEventListener listener) 
	{
		super(listener);
	}
}
