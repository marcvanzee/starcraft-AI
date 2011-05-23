package starcraft;

import java.util.ArrayList;
import java.util.List;

import eisbot.proxy.JNIBWAPI;

public abstract class Action {

	public abstract void perform( JNIBWAPI bwapi );
	
}
