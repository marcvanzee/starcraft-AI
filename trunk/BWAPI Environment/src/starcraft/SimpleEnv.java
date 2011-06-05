package starcraft;

import eisbot.proxy.types.UnitType.UnitTypes;
import apapl.data.APLFunction;
import apapl.data.APLNum;

public class SimpleEnv extends Env
{	
	public SimpleEnv()
	{
		super();
	}
	
	@Override
	public void gameStarted() {
		// send all agents how many units they have
		for (Agent agent : _agents) {
			APLFunction f = new APLFunction("units", new APLNum(agent.countUnits()));
			throwEvent(f, agent.getName());
		}
		
		// update information
		updateAgents();
		
		super.gameStarted();
	}
	
	@Override
	public void unitDestroy(int unitID) {
		
		if (_bwapi.getUnit(unitID).getTypeID() == UnitTypes.Terran_Marine.ordinal()) {
			System.out.println("a marine died, sad day...");
		}
		System.out.println("JAAAAAA!!!! ");
		// get the agent that holds this unit
		
		Agent ag = getAgent(unitID);
		
		// remove the unit
		ag.removeUnit(unitID);
		
		// inform the agent that the unit has been removed
		throwEvent(new APLFunction("unitDestroyed", new APLNum(0)), ag.getName());
	}
	
	@Override
	public void gameUpdate() 
	{
		System.out.println("update");
		super.gameUpdate();
		
		// update the agents
		updateAgents();
		
		// tell agents that they have to make their next move
		throwEventToAll("nextMove");
	}
	
	public static void main(String arg[]) {
		
	}
	
	public void updateAgents() {
		// TODO: implement this method
	}
}
