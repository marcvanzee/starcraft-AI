package starcraft;

import java.util.LinkedList;
import java.util.List;
import eisbot.proxy.JNIBWAPI;
import eisbot.proxy.model.*;
import eisbot.proxy.types.*;

public class Attack extends Action {
	
	/**
	 * 
	 * @param bwapi
	 * @param num
	 * @param enemies
	 */
	public Attack(JNIAgent bwapi, int officer,  int num, List<Unit> enemies )
	{
		this(bwapi, officer, num, enemies, true );
	}
	
	/**
	 * 
	 * @param bwapi
	 * @param num
	 * @param enemies
	 * @param onlyIdle
	 */
	public Attack(JNIAgent bwapi,  int officer, int num, List<Unit> enemies, boolean onlyIdle )
	{
		this( bwapi, officer, num, enemies, new LinkedList<UnitType>(), onlyIdle );
	}
	
	/**
	 * 
	 * @param bwapi
	 * @param num
	 * @param enemies
	 * @param withType
	 * @param onlyIdle
	 */
	public Attack(JNIAgent bwapi, int officer, int num, List<Unit> enemies, List<UnitType> withType, boolean onlyIdle )
	{
		List<Unit> usingUnits = new LinkedList<Unit>();
		for( Unit unit : bwapi.getMyUnits(officer) )
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
				actions.add( new String[] {"attackMove", ""+unit.getID(), ""+enemy.getX(), ""+enemy.getY()} );
				break;
			}
		}
	}
	

}
