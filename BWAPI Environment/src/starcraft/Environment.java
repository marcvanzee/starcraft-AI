package starcraft;

import eisbot.proxy.BWAPIEventListener;
import eisbot.proxy.JNIBWAPI;

public class Environment implements BWAPIEventListener
{
	
	private JNIBWAPIClient _client;
	private JNIBWAPI _jnibwapi;
	
	public Environment()
	{
		_jnibwapi = new JNIBWAPI(this);
		_client = new JNIBWAPIClient(_jnibwapi);
		Thread t = new Thread(_client);
		t.start();
	}
	
	public void testMethod()
	{
		int frameCount = _jnibwapi.getFrameCount();
		System.out.println("Environment: testmethod framecount = " + frameCount);
	}

	@Override
	public void connected() {
		// TODO Auto-generated method stub
		System.out.println("Environment: connected");
	}

	@Override
	public void gameStarted() {
		// TODO Auto-generated method stub
		System.out.println("Environment: game started");
	}

	@Override
	public void gameUpdate() {
		// TODO Auto-generated method stub
	}

	@Override
	public void gameEnded() {
		// TODO Auto-generated method stub
		
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
