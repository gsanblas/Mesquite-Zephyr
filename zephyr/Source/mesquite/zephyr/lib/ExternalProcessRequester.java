package mesquite.zephyr.lib;

import mesquite.lib.*;

public interface ExternalProcessRequester {
	
	public void runFilesAvailable(boolean[] filesAvailable);
	
	public void runFailed(String message);
	
	public void runFinished(String message);
	
	public String nameOfProgram();
	
}