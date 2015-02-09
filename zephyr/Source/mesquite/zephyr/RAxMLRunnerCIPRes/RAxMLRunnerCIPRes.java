/* Mesquite.zephyr source code.  Copyright 2007 and onwards D. Maddison and W. Maddison. 

Mesquite.zephyr is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Zephry's web site is http://mesquitezephyr.wikispaces.com

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.zephyr.RAxMLRunnerCIPRes;


import java.awt.*;
import java.io.*;
import java.awt.event.*;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import mesquite.lib.*;
import mesquite.zephyr.CIPResRESTRunner.CIPResRESTRunner;
import mesquite.zephyr.lib.*;

/* TODO:
-b bootstrapRandomNumberSeed  // allow user to set seed

outgroups

 */

public class RAxMLRunnerCIPRes extends RAxMLRunner  implements ActionListener, ItemListener, ExternalProcessRequester  {

	boolean RAxML814orLater = false;

	int randomIntSeed = (int)System.currentTimeMillis();   // convert to int as RAxML doesn't like really big numbers

	boolean showIntermediateTrees = true;


	long  randseed = -1;

	SingleLineTextField MPISetupField;
	javax.swing.JLabel commandLabel;
	SingleLineTextArea commandField;
	Checkbox RAxML814orLaterCheckbox;


	/*.................................................................................................................*/
	 public String getExternalProcessRunnerModuleName(){
			return "#mesquite.zephyr.CIPResRESTRunner.CIPResRESTRunner";
	 }
	/*.................................................................................................................*/
	 public Class getExternalProcessRunnerClass(){
			return CIPResRESTRunner.class;
	 }


	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		temp.addLine("setExternalProcessRunner", externalProcRunner);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Hires the ExternalProcessRunner", "[name of module]", commandName, "setExternalProcessRunner")) {
			ExternalProcessRunner temp = (ExternalProcessRunner)replaceEmployee(ExternalProcessRunner.class, arguments, "External Process Runner", externalProcRunner);
			if (temp != null) {
				externalProcRunner = temp;
				parametersChanged();
			}
			externalProcRunner.setProcessRequester(this);
			return externalProcRunner;
		} else
			return super.doCommand(commandName, arguments, checker);
	}	

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("RAxML814orLater".equalsIgnoreCase(tag))
			RAxML814orLater = MesquiteBoolean.fromTrueFalseString(content);

		
		super.processSingleXMLPreference(tag, content);

		preferencesSet = true;
	}
	
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "RAxML814orLater", RAxML814orLater);  
		
		buffer.append(super.preparePreferencesForXML());

		preferencesSet = true;
		return buffer.toString();
	}

	/*.................................................................................................................*/
	public String getTestedProgramVersions(){
		return "8.0.0 and 8.1.4";
	}
	/*.................................................................................................................*/
	public void addRunnerOptions(ExtensibleDialog dialog) {
		dialog.addLabel("CIPRes Options");
		RAxML814orLaterCheckbox = dialog.addCheckBox("RAxML version 8.1.4 or later", RAxML814orLater);
		dialog.addLabelSmallText("This version of Zephyr tested on the following RAxML version(s): " + getTestedProgramVersions());
	}
	/*.................................................................................................................*/
	public void processRunnerOptions() {
		RAxML814orLater = RAxML814orLaterCheckbox.getState();
	}
	/*.................................................................................................................*/
	public  void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("composeRAxMLCommand")) {

			MultipartEntityBuilder arguments = MultipartEntityBuilder.create();
			getArguments(arguments, "fileName", proteinModelField.getText(), dnaModelField.getText(), otherOptionsField.getText(), bootStrapRepsField.getValue(), bootstrapSeed, numRunsField.getValue(), outgroupTaxSetString, null, false);
			String command = externalProcRunner.getExecutableCommand() + arguments.toString();
			commandLabel.setText("This command will be used by CIPRes to run RAxML:");
			commandField.setText(command);
		}
		else	if (e.getActionCommand().equalsIgnoreCase("clearCommand")) {
			commandField.setText("");
			commandLabel.setText("");
		}
	}
	/*.................................................................................................................*/
	public void setRAxMLSeed(long seed){
		this.randseed = seed;
	}
	
	static final int DATAFILENUMBER = 0;

	public void prepareRunnerObject(Object obj){
		if (obj instanceof MultipartEntityBuilder) {
			MultipartEntityBuilder builder = (MultipartEntityBuilder)obj;
			final File file = new File(externalProcRunner.getInputFilePath(DATAFILENUMBER));
			FileBody fb = new FileBody(file);
			builder.addPart("input.infile_", fb);  
			Debugg.println("*** preparing runner object");
		}
	}

	/*.................................................................................................................*/
	void getArguments(MultipartEntityBuilder builder, String fileName, String LOCproteinModel, String LOCdnaModel, String LOCotherOptions, int LOCbootstrapreps, int LOCbootstrapSeed, int LOCnumRuns, String LOCoutgroupTaxSetString, String LOCMultipleModelFile, boolean preflight){
		if (builder==null)
			return;
	/*	
		if (preflight)
			arguments += " -n preflight.out "; 
		else
			arguments += " -s " + fileName + " -n file.out "; 
		*/
		
		if (isProtein) {
			if (StringUtil.blank(LOCproteinModel))
				builder.addTextBody("vparam.protein_opts_", "PROTGAMMAJTT");
			else
				builder.addTextBody("vparam.protein_opts_", LOCproteinModel);
		}
		else if (StringUtil.blank(LOCdnaModel))
			builder.addTextBody("vparam.dna_gtrcat_", "GTRGAMMA");
		else
			builder.addTextBody("vparam.dna_gtrcat_","GTRGAMMA");
	//	builder.addTextBody("vparam.dna_gtrcat_",LOCdnaModel);

		/*
		if (StringUtil.notEmpty(LOCMultipleModelFile))
			arguments += " -q " + ShellScriptUtil.protectForShellScript(LOCMultipleModelFile);

		if (!StringUtil.blank(LOCotherOptions)) 
			arguments += " " + LOCotherOptions;

*/
		
		builder.addTextBody("vparam.provide_parsimony_seed_","1");
		builder.addTextBody("vparam.parsimony_seed_val_",""+randomIntSeed);

		
		if (bootstrapOrJackknife() && LOCbootstrapreps>0) {
			builder.addTextBody("vparam.bootstrap_",""+LOCbootstrapreps);
			builder.addTextBody("vparam.mulparambootstrap_seed_",""+LOCbootstrapSeed);
		}
		else {
			builder.addTextBody("vparam.specify_runs_","1");
			builder.addTextBody("vparam.altrun_number_",""+LOCnumRuns);
			//if (RAxML814orLater)
			//	arguments += " --mesquite";
		}

		TaxaSelectionSet outgroupSet =null;
		if (!StringUtil.blank(LOCoutgroupTaxSetString)) {
			outgroupSet = (TaxaSelectionSet) taxa.getSpecsSet(LOCoutgroupTaxSetString,TaxaSelectionSet.class);
			if (outgroupSet!=null) 
				builder.addTextBody("vparam.outgroup_",outgroupSet.getStringList(",", true));
				arguments += " -o " + outgroupSet.getStringList(",", true);
		}
		

	}	
	


	static final int OUT_LOGFILE=0;
	static final int OUT_TREEFILE=1;
	static final int OUT_SUMMARYFILE=2;
	/*.................................................................................................................*/
	public String[] getLogFileNames(){
		String treeFileName;
		String logFileName;
		if (bootstrapOrJackknife())
			treeFileName = "RAxML_bootstrap.result";
		else 
			treeFileName = "RAxML_result.result";
		logFileName = "RAxML_info.result";
		if (!bootstrapOrJackknife() && numRuns>1) {
			treeFileName+=".RUN.";
			logFileName+=".RUN.";
		}
		return new String[]{logFileName, treeFileName, "RAxML_info.result"};
	}

	/*.................................................................................................................*/
	public String getPreflightLogFileNames(){
		return "RAxML_log.file.out";	
	}

	
	
	TaxaSelectionSet outgroupSet;
	
	/*.................................................................................................................*/
	public boolean preFlightSuccessful(String preflightCommand){
		return runPreflightCommand(preflightCommand);
	}

	String arguments;
	/*.................................................................................................................*/
	public Object getProgramArguments(String dataFileName, boolean isPreflight) {
		MultipartEntityBuilder arguments = MultipartEntityBuilder.create();

		if (!isPreflight) {
			getArguments(arguments, dataFileName, proteinModel, dnaModel, otherOptions, bootstrapreps, bootstrapSeed, numRuns, outgroupTaxSetString, multipleModelFileName, false);
			logln("RAxML arguments: \n" + arguments.toString() + "\n");
		} else {
			getArguments(arguments, dataFileName, proteinModel, dnaModel, otherOptions, bootstrapreps, bootstrapSeed, numRuns, outgroupTaxSetString, multipleModelFileName, true);
		}
		return arguments;

	}



	public String getExecutableName() {
		return "RAXMLHPC2_TGB";
	}


	/*.................................................................................................................*/


	public boolean singleTreeFromResampling() {
		return false;
	}


	public Class getDutyClass() {
		return RAxMLRunnerCIPRes.class;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return -100;  
	}

	public String getName() {
		return "RAxML CIPRes Runner";
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}


	public void runFailed(String message) {
		// TODO Auto-generated method stub

	}

	public void runFinished(String message) {
		// TODO Auto-generated method stub

	}

	public String getProgramName() {
		return "RAxML";
	}




}