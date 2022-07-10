package main.java.client.obj.checker;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import org.dom4j.DocumentException;

import soot.SootMethod;
import soot.dava.toolkits.base.AST.analysis.Analysis;
import main.java.Analyzer;
import main.java.Global;
import main.java.MyConfig;
import main.java.client.BaseClient;
import main.java.client.cg.CallGraphClient;
import main.java.client.cg.DynamicReceiverCGAnalyzer;
import main.java.client.manifest.ManifestClient;
import main.java.client.obj.ObjectAnalyzer;
import main.java.client.obj.model.component.ComponentModel;
import main.java.client.obj.target.ctg.CTGAnalyzer;
import main.java.client.obj.target.ctg.StaticValueAnalyzer;
import main.java.client.obj.target.fragment.FragmentClient;
import main.java.client.soot.IROutputClient;
import main.java.client.statistic.model.StatisticResult;

public class MisEACheckerClient  extends BaseClient {

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

		
		MisEAAnalysis misEaAnalyzer = new MisEAAnalysis();
		misEaAnalyzer.analyze();
		
		System.out.println("Successfully analyze with MisEACheckerClient.");
	}

	@Override
	public void clientOutput() throws IOException, DocumentException {
		
	}

}
