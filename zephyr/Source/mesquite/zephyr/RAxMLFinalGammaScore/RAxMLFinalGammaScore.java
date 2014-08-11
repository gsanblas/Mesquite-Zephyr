package mesquite.zephyr.RAxMLFinalGammaScore;


import mesquite.io.lib.IOUtil;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.zephyr.RAxMLRunner.*;


public class RAxMLFinalGammaScore extends NumberForTree {

    /* ................................................................................................................. */

    public boolean startJob(String arguments, Object condition, boolean hiredByName) {
        return true;
    }

	/*.................................................................................................................*/
  	 public boolean isPrerelease(){
  	 	return false;
  	 }

    /* ................................................................................................................. */
      public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
        if (result == null || tree == null)
            return;
	   	clearResultAndLastResult(result);
       if (tree instanceof Attachable){
        	Object obj = ((Attachable)tree).getAttachment(IOUtil.RAXMLFINALSCORENAME);
        	if (obj == null){
        			if (resultString != null)
        				resultString.setValue("No RAxML Final Gamma-based score is associated with this tree.  To obtain a score, use as tree source \"RAxML Trees\".");
        			return;
        	}
        	if (obj instanceof MesquiteDouble)
        			result.setValue(((MesquiteDouble)obj).getValue());
			else if (obj instanceof MesquiteNumber)
				result.setValue((MesquiteNumber)obj);
        }
       
        if (resultString != null) {
            resultString.setValue("RAxML Final Gamma-based score : " + result.toString());
        }
		saveLastResult(result);
		saveLastResultString(resultString);
      }

  	/*.................................................................................................................*/
   	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
   	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
   	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
      	public int getVersionOfFirstRelease(){
      		return -100;  
      	}
  /* ................................................................................................................. */
    /** Explains what the module does. */

    public String getExplanation() {
        return "Supplies final gamma-based - ln L score from RAxML";
    }

    /* ................................................................................................................. */
    /** Name of module */
    public String getName() {
        return "RAxML Final Gamma-based Score";
    }
}