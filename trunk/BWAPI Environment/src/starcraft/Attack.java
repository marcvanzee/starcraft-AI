package starcraft;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import eisbot.proxy.BWAPIEventListener;
import eisbot.proxy.ExampleAIClient;
import eisbot.proxy.JNIBWAPI;
import eisbot.proxy.model.*;
import eisbot.proxy.types.*;

public class Attack extends Action {

	private ArrayList<String[]> actions;
	
	/**
	 * 
	 * @param bwapi
	 * @param num
	 * @param enemies
	 */
	public Attack(JNIBWAPI bwapi,  int num, List<Unit> enemies )
	{
		this(bwapi, num, enemies, true );
	}
	
	/**
	 * 
	 * @param bwapi
	 * @param num
	 * @param enemies
	 * @param onlyIdle
	 */
	public Attack(JNIBWAPI bwapi,  int num, List<Unit> enemies, boolean onlyIdle )
	{
		this( bwapi, num, enemies, new LinkedList<UnitType>(), onlyIdle );
	}
	
	/**
	 * 
	 * @param bwapi
	 * @param num
	 * @param enemies
	 * @param withType
	 * @param onlyIdle
	 */
	public Attack(JNIBWAPI bwapi, int num, List<Unit> enemies, List<UnitType> withType, boolean onlyIdle )
	{
		List<Unit> usingUnits = new LinkedList<Unit>();
		for( Unit unit : bwapi.getMyUnits() )
		{
			if( onlyIdle && !unit.isIdle() )
				continue;
			
			if( withType.size() > 0 && !withType.contains( unit.getTypeID()) )
				continue;
			
			usingUnits.add( unit );
			
			if( num > 0 && usingUnits.size() == num )
				break;
		}
		
		this.createAttack(bwapi, usingUnits, enemies );
	}


	/**
	 * 
	 * @param bwapi
	 * @param myUnits
	 * @param enemies
	 */
	public void createAttack( JNIBWAPI bwapi, List<Unit> myUnits, List<Unit> enemies )
	{
		for( Unit unit : myUnits )
		{
			for( Unit enemy : enemies )
			{
				//actions.add( "bwapi.attackMove( " + unit.getID() + ", " + enemy.getX() + ", " + enemy.getY() + " );" );

				actions.add( new String[] {"attackMove", ""+unit.getID(), ""+enemy.getX(), ""+enemy.getY()} );
				break;
			}
		}
	}

	@Override
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
