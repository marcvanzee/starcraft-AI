package starcraft;

import eisbot.proxy.JNIBWAPI;

public class JNIBWAPIClient implements Runnable 
{

	
	private JNIBWAPI _jnibwapi;
	
	
	public JNIBWAPIClient(JNIBWAPI jnibwapi)
	{
		_jnibwapi = jnibwapi;
	}
	
	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		_jnibwapi.start();
	}

}
