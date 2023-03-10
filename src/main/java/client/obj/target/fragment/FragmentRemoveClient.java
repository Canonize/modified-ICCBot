package main.java.client.obj.target.fragment;

import java.io.File;
import java.io.IOException;
import java.util.List;

import main.java.Global;
import main.java.MyConfig;
import main.java.analyze.utils.ConstantUtils;
import main.java.analyze.utils.GraphUtils;
import main.java.analyze.utils.SootUtils;
import main.java.analyze.utils.output.FileUtils;
import main.java.client.BaseClient;
import main.java.client.cg.CallGraphClient;
import main.java.client.manifest.ManifestClient;
import main.java.client.obj.ObjectAnalyzer;
import main.java.client.obj.target.ctg.StaticValueAnalyzer;
import main.java.client.soot.SootAnalyzer;
import main.java.client.statistic.model.StatisticResult;

import org.dom4j.DocumentException;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

/**
 * Analyzer Class
 * 
 * @author hanada
 * @version 2.0
 */
public class FragmentRemoveClient extends BaseClient {

	/**
	 * analyze logic for single app
	 * 
	 * @return
	 */
	@Override
	protected void clientAnalyze() {
		result = new StatisticResult();
		if (!MyConfig.getInstance().isSootAnalyzeFinish()) {
			SootAnalyzer sootAnalyzer = new SootAnalyzer();
			sootAnalyzer.analyze();
		}

		String summary_app_dir = MyConfig.getInstance().getResultFolder() + Global.v().getAppModel().getAppName()
				+ File.separator+ConstantUtils.FRAGTMPFOLDETR+ File.separator;
		FileUtils.createFolder(summary_app_dir );
		String content = "";
		for (SootClass sc : Scene.v().getApplicationClasses()) {
			for (SootMethod sMethod : sc.getMethods()) {
				Body body = SootUtils.getSootActiveBody(sMethod);
				if(body==null) continue;
				for(Unit u: body.getUnits()){
					if(u.toString().contains("app.FragmentTransaction remove(")){
						System.out.println(u);
						content += sMethod.getSignature() +"\t"+ u.toString()+"\n";
						
					}
						
				}
			}
		}
		FileUtils.writeText2File(summary_app_dir+"remove.txt",content, false);
		System.out.println("Successfully analyze with FragmentRemoveClient.");
	}

	@Override
	public void clientOutput() throws IOException, DocumentException {
		

	}

}