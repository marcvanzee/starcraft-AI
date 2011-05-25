package starcraft;

public class BWAPIClient implements Runnable 
{
	private BWAPICoop _bwapi;
	
	
	public BWAPIClient(BWAPICoop bwapi)
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
