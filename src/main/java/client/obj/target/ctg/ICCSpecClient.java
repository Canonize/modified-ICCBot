package main.java.client.obj.target.ctg;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import main.java.Global;
import main.java.MyConfig;
import main.java.SummaryLevel;
import main.java.analyze.utils.ConstantUtils;
import main.java.analyze.utils.GraphUtils;
import main.java.analyze.utils.output.FileUtils;
import main.java.client.BaseClient;
import main.java.client.cg.CallGraphClient;
import main.java.client.cg.DynamicReceiverCGAnalyzer;
import main.java.client.manifest.ManifestClient;
import main.java.client.obj.ObjectAnalyzer;
import main.java.client.obj.model.atg.ATGModel;
import main.java.client.obj.model.atg.AtgEdge;
import main.java.client.obj.model.atg.AtgNode;
import main.java.client.obj.model.atg.AtgType;
import main.java.client.obj.target.ctg.ICCSpecClient;
import main.java.client.obj.target.fragment.FragmentClient;
import main.java.client.soot.IROutputClient;
import main.java.client.statistic.model.StatisticResult;
import main.java.client.toolEvaluate.ToolEvaluateClient;

import org.dom4j.DocumentException;

import soot.SootMethod;

/**
 * Analyzer Class
 * 
 * @author hanada
 * @version 2.0
 */
public class ICCSpecClient extends BaseClient {

	
	/**
	 * analyze CTG for single app
	 */
	@Override
	protected void clientAnalyze() {
		result = new StatisticResult();
		
		if (!MyConfig.getInstance().isManifestAnalyzeFinish()) {
			new ManifestClient().start();
			MyConfig.getInstance().setManifestAnalyzeFinish(true);
		}
		if (MyConfig.getInstance().isWriteSootOutput()) {
			new IROutputClient().start();
		}
		if (!MyConfig.getInstance().isCallGraphAnalyzeFinish()) {
			new CallGraphClient().start();
			MyConfig.getInstance().setCallGraphAnalyzeFinish(true);
		}
		if (!MyConfig.getInstance().isStaitiucValueAnalyzeFinish()) {
			if (MyConfig.getInstance().getMySwithch().isStaticFieldSwitch()) {
				StaticValueAnalyzer staticValueAnalyzer = new StaticValueAnalyzer();
				staticValueAnalyzer.analyze();
				MyConfig.getInstance().setStaitiucValueAnalyzeFinish(true);
			}
		}
		if (MyConfig.getInstance().getMySwithch().isDynamicBCSwitch()) {
			DynamicReceiverCGAnalyzer dynamicIntentFilterAnalyzer = new DynamicReceiverCGAnalyzer();
			dynamicIntentFilterAnalyzer.analyze();
		}

		if (MyConfig.getInstance().getMySwithch().isFragmentSwitch()) {
			if (!MyConfig.getInstance().isFragementAnalyzeFinish()) {
				new FragmentClient().start();
				MyConfig.getInstance().setFragementAnalyzeFinish(true);
			}
		}
		System.out.println("Analyzing ICC sending.");
		setMySwitch1();
		for (List<SootMethod> topoQueue : Global.v().getAppModel().getTopoMethodQueueSet()) {
			ObjectAnalyzer analyzer = new CTGAnalyzer(topoQueue, result);
			analyzer.analyze();
		}
		System.out.println("Analyzing ICC receiving.");
		setMySwitch2();
		for (List<SootMethod> topoQueue : Global.v().getAppModel().getTopoMethodQueueSet()) {
			ObjectAnalyzer analyzer = new CTGAnalyzer(topoQueue, result);
			analyzer.analyze();
		}
		System.out.println("Successfully analyze with ICCSpecClient.");
	}

	protected void setMySwitch1() {
		MyConfig.getInstance().getMySwithch().setSetAttributeStrategy(true);
		MyConfig.getInstance().getMySwithch().setGetAttributeStrategy(false);
		MyConfig.getInstance().getMySwithch().setSummaryStrategy(SummaryLevel.object);
	}

	protected void setMySwitch2() {
		MyConfig.getInstance().getMySwithch().setSetAttributeStrategy(false);
		MyConfig.getInstance().getMySwithch().setGetAttributeStrategy(true);
		MyConfig.getInstance().getMySwithch().setSummaryStrategy(SummaryLevel.path);

	}
	@Override
	public void clientOutput() throws IOException, DocumentException {
		outputCTGInfo();
	}

	private void outputCTGInfo() throws IOException, DocumentException {
		String summary_app_dir = MyConfig.getInstance().getResultFolder() + Global.v().getAppModel().getAppName()
				+ File.separator;
		CTGClientOutput outer = new CTGClientOutput(this.result);
		
		String ictgFolder = summary_app_dir + ConstantUtils.ICTGSPEC;
		FileUtils.createFolder(ictgFolder); 
		outer.writeComponentModelJson(ictgFolder, ConstantUtils.COMPONENTMODELJSON);
	}

	

	protected void setMySwitch() {
		// TODO Auto-generated method stub
		
	}
}