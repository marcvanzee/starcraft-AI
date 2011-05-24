package starcraft;

import java.util.ArrayList;
import java.util.List;

import eisbot.proxy.JNIBWAPI;

public abstract class Action {

	protected ArrayList<String[]> actions;
	
	public void perform( JNIBWAPI bwapi ) {
		// TODO Auto-generated method stub
		
		for( String[] action : actions )
		{
			if( action[0].equals( "attackMove" ) )
			{
				bwapi.attackMove( Integer.parseInt( action[1] ), Integer.parseInt(action[2]), Integer.parseInt( action[3] ) );
			}
			actions.remove( action );
		}
		
	}
	
}
