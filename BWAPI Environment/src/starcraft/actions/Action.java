package starcraft.actions;

import starcraft.BWAPICoop;

public abstract class Action 
{
	public abstract int[] getInvolvedUnitIds();
	
	public abstract void perform(BWAPICoop bwapi);
	
	public abstract boolean isFinished(BWAPICoop bwapi);
}
