package main.java.client.soot;

import java.io.File;

import main.java.MyConfig;
import main.java.analyze.utils.ConstantUtils;
import main.java.client.BaseClient;
import main.java.client.soot.GetApiGenClient;
import main.java.client.soot.FlowDroidAnalyzer;
import main.java.client.obj.target.ctg.CTGClient;
import main.java.Global;
import soot.PackManager;

/**
 * Analyzer Class
 * 
 * @author hanada
 * @version 2.0
 */
public class GetApiGenClient extends BaseClient {

	@Override
	protected void clientAnalyze() {
		if (!MyConfig.getInstance().isCTGAnalyzeFinish()) {
			new CTGClient().start();
			MyConfig.getInstance().setCTGAnalyzeFinish(true);
		}

		String appname = MyConfig.getInstance().getAppPath() + MyConfig.getInstance().getAppName();

        String out = MyConfig.getInstance().getResultFolder() + Global.v().getAppModel().getAppName() + File.separator
				+ ConstantUtils.SOOTOUTPUT;

        String iccmodelPath = MyConfig.getInstance().getResultFolder() + Global.v().getAppModel().getAppName() + File.separator
				+ ConstantUtils.ICTGFOLDETR + "IccModel.txt";

        FlowDroidAnalyzer flowdroidAnalyzer = new FlowDroidAnalyzer();

        flowdroidAnalyzer.analyseOne(appname,out,iccmodelPath);

		System.out.println("Successfully analyze with GetApiGenClient.");
	}

	@Override
	public void clientOutput() {
		//PackManager.v().writeOutput();
	}

}