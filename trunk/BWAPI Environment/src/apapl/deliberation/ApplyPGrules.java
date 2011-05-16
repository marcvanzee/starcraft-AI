package apapl.deliberation;

import apapl.*;
import apapl.program.*;
import apapl.data.APLFunction;
import apapl.plans.PlanSeq;
import java.util.ArrayList;

/**
 * The deliberation step in which PG-rules are selected and applied. The application of 
 * a PG-rule results in the addition of that plan to the plan base of the module.
 */
public class ApplyPGrules implements DeliberationStep
{
	/**
	 * Selects and applies PG-rules.
	 * 
	 * @return the result of this deliberation step.
	 **/
	public DeliberationResult execute( APLModule module )
	{
	    ApplyPGrulesResult result;
	
		Goalbase goalbase = module.getGoalbase();
		Beliefbase beliefbase = module.getBeliefbase();
		PGrulebase pgrules = module.getPGrulebase();
		Planbase planbase = module.getPlanbase();
			
		ArrayList<PlanSeq> ps = pgrules.generatePlans(goalbase,beliefbase,planbase);
			
	    return( new ApplyPGrulesResult( ps ) );
	}
	
	public String toString()
	{
		return "Apply PG rules";
	}
}
