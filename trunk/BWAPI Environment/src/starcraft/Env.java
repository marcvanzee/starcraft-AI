package starcraft;

import java.util.logging.Logger;

import apapl.data.APLIdent;
import apapl.data.APLList;
import apapl.data.APLListVar;
import apapl.data.APLNum;
import apapl.data.Term;

public class Env extends apapl.Environment
{
	
	 private Logger _logger = Logger.getLogger("EMarket."+Env.class.getName());
	
	 /* Test method for logging*/
	 public synchronized Term log(String agentName,APLIdent var)
	 {
	    	_logger.info("Performing testAction1" + agentName);
	    	
	    	return wrapBoolean(true);
	 }
	    
	 /* Test method for waiting */
	 public synchronized Term wait( String agentName, APLNum time )
	 {
	    	try {
	    		Thread.sleep( time.toInt() );
	    	} catch( Exception e ) {
	    		
	    	}
	    	return wrapBoolean(true);
	  }
	  
	 /* Test method for starting ExampleAICLient */
	 public synchronized Term start(String agentName)
	 {
		 eisbot.proxy.ExampleAIClient a = new eisbot.proxy.ExampleAIClient();
		 return wrapBoolean(true);
	 }
	 
	 public static  APLListVar wrapBoolean( boolean b )
	 {
		return new APLList(new APLIdent(b ? "true" : "false"));
	 }

}
