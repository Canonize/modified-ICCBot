package main.java.client.soot;

import java.io.File;

import main.java.Global;
import main.java.MyConfig;
import main.java.analyze.utils.ConstantUtils;

import main.java.client.BaseClient;
import main.java.client.obj.target.ctg.StaticValueAnalyzer;

import soot.SootClass;

import soot.jimple.infoflow.values.IValueProvider;
import soot.jimple.infoflow.values.SimpleConstantValueProvider;

import soot.util.HashMultiMap;
import soot.util.MultiMap;


/**
 * Analyzer Class
 * 
 * @author hanada
 * @version 2.0
 */
public class DrawLayoutClient extends BaseClient {
    IValueProvider valueProvider = new SimpleConstantValueProvider();
	MultiMap<SootClass, Integer> layoutClasses = new HashMultiMap<>();

	@Override
	protected void clientAnalyze() {
		if (!MyConfig.getInstance().isStaitiucValueAnalyzeFinish()) {
			if (MyConfig.getInstance().getMySwithch().isStaticFieldSwitch()) {
				StaticValueAnalyzer staticValueAnalyzer = new StaticValueAnalyzer();
				staticValueAnalyzer.analyze();
				MyConfig.getInstance().setStaitiucValueAnalyzeFinish(true);
			}
		}

		String appname = MyConfig.getInstance().getAppPath() + MyConfig.getInstance().getAppName();

        String out = MyConfig.getInstance().getResultFolder() + Global.v().getAppModel().getAppName() + File.separator
				+ ConstantUtils.SOOTOUTPUT;
		

		LayoutAnalyzer layoutAnalyzer = new LayoutAnalyzer();

		layoutAnalyzer.analyze();

		System.out.println("Successfully analyze with LayoutAnalyzer.");
	}

    
	@Override
	public void clientOutput() {
		//PackManager.v().writeOutput();
	}
    
}