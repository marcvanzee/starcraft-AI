package starcraft;

import eisbot.proxy.JNIBWAPI;

public class BWAPIClient implements Runnable 
{
	private JNIBWAPI _bwapi;
	
	
	public BWAPIClient(JNIBWAPI bwapi)
	{
		_bwapi = bwapi;
	}
	
	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		_bwapi.start();
	}

}
