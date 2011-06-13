package starcraft.actions;

import starcraft.BWAPICoop;

public abstract class Action 
{
	private String _identifier;
	
	public Action(String identifier)
	{
		_identifier = identifier;
	}
	
	public String getIdentity()
	{
		return _identifier;
	}
	
	public abstract int[] getInvolvedUnitIds();
	
	public abstract void perform(BWAPICoop bwapi);
	
	public abstract boolean isFinished(BWAPICoop bwapi);
}
