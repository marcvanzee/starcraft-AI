package starcraft.actions;

import starcraft.BWAPICoop;

public abstract class Action 
{
		
	public abstract void perform(BWAPICoop bwapi);
	
	public abstract void isFinished(BWAPICoop bwapi);
}
