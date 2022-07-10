package main.java.client.obj.target.ctg;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
//+++
import java.util.ArrayList;
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
import main.java.client.obj.target.ctg.CTGClient;
import main.java.client.obj.target.fragment.FragmentClient;
import main.java.client.soot.IROutputClient;
import main.java.client.statistic.model.StatisticResult;
import main.java.client.toolEvaluate.ToolEvaluateClient;

import main.java.client.obj.target.ctg.flow;

import org.dom4j.DocumentException;

import soot.SootMethod;

/**
 * Analyzer Class
 * 
 * @author hanada
 * @version 2.0
 */
public class CTGClient extends BaseClient {

	
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
		setMySwitch1();
		for (List<SootMethod> topoQueue : Global.v().getAppModel().getTopoMethodQueueSet()) {
			ObjectAnalyzer analyzer = new CTGAnalyzer(topoQueue, result);
			analyzer.analyze();
		}
		//+++ iterate to add new topo method(no rule)
		while(Global.v().getAppModel().getNewTopoMethods().size() != 0) {
			System.out.println("Successfully analyze new: " + Global.v().getAppModel().getNewTopoMethods().size());
			Global.v().getAppModel().getTopoMethodQueue().addAll(Global.v().getAppModel().getNewTopoMethods());
			List<SootMethod> topoQueue = new ArrayList<SootMethod>(Global.v().getAppModel().getNewTopoMethods());
			//topoQueue.addAll(Global.v().getAppModel().getNewTopoMethods());
			Global.v().getAppModel().getNewTopoMethods().clear();
			System.out.println("toposize: " + topoQueue.size());
			System.out.println("analyze new method size: " + Global.v().getAppModel().getTopoMethodQueue().size());
			ObjectAnalyzer analyzer = new CTGAnalyzer(topoQueue, result);
			analyzer.analyze();
		}
//		setMySwitch2();
//		for (List<SootMethod> topoQueue : Global.v().getAppModel().getTopoMethodQueueSet()) {
//			ObjectAnalyzer analyzer = new CTGAnalyzer(topoQueue, result);
//			analyzer.analyze();
//		}

		System.out.println("++++++++here !!!++++");
		System.out.println("Successfully analyze with CTGClient.");

		// test outputCG
		//flow.analyseOne("/home/lw/Auth_Risk_Analysis_tool/apk/unpack/com.hanweb.android.zhejiang.activity.apk","/home/lw/Auth_Risk_Analysis_tool/test_auto/by_Iccbot/flowdroid_icc_output_CHA"+"/nodeResult","/home/lw/Auth_Risk_Analysis_tool/test_auto/by_Iccbot/flowdroid_icc_output_CHA"+"/edgeResult","/home/lw/Auth_Risk_Analysis_tool/test_auto/by_Iccbot/tmp_soot_output","/home/lw/Auth_Risk_Analysis_tool/Iccbot/icc_result_OutputComp/output/com.hanweb.android.zhejiang.activity/CTGResult/IccModel.txt");

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
//		if (!MyConfig.getInstance().isOracleConstructionClientFinish()) {
//			new ToolEvaluateClient().start();
//		}
	}

	private void outputCTGInfo() throws IOException, DocumentException {
		String summary_app_dir = MyConfig.getInstance().getResultFolder() + Global.v().getAppModel().getAppName()
				+ File.separator;
		FileUtils.createFolder(summary_app_dir + ConstantUtils.CGFOLDETR);
		FileUtils.createFolder(summary_app_dir + ConstantUtils.FRAGFOLDETR);
		FileUtils.createFolder(summary_app_dir + ConstantUtils.CGFOLDETR);

		CTGClientOutput outer = new CTGClientOutput(this.result);

		String ictgFolder = summary_app_dir + ConstantUtils.ICTGFOLDETR;
		String fragFolder = summary_app_dir + ConstantUtils.FRAGFOLDETR;
		/** Component **/
		outer.writeComponentModel(ictgFolder, ConstantUtils.COMPONENTMODEL);
		/** Method **/
		outer.writeMethodSummaryModel(ictgFolder, ConstantUtils.SINGLEMETHOD_ENTRY,true);
//		outer.writeMethodSummaryModel(ictgFolder, ConstantUtils.SINGLEMETHOD_ALL, false);
		if(MyConfig.getInstance().getMySwithch().isFragmentSwitch()){
			outer.appendInfo(ictgFolder, fragFolder, ConstantUtils.SINGLEMETHOD_ENTRY);
//			outer.appendInfo(ictgFolder, fragFolder, ConstantUtils.SINGLEMETHOD_ALL);
		}
		
		/** Path **/
		outer.writePathSummaryModel(ictgFolder, ConstantUtils.SINGLEPATH_ENTRY, true);
		outer.writePathSummaryModel(ictgFolder, ConstantUtils.SINGLEPATH_ALL, false);
		if(MyConfig.getInstance().getMySwithch().isFragmentSwitch()){
			outer.appendInfo(ictgFolder, fragFolder, ConstantUtils.SINGLEPATH_ENTRY);
//			outer.appendInfo(ictgFolder, fragFolder, ConstantUtils.SINGLEPATH_ALL);
		}
		
		/** Intent **/
		outer.writeIntentSummaryModel(ictgFolder, ConstantUtils.SINGLEOBJECT_ENTRY,true);
//		outer.writeIntentSummaryModel(ictgFolder, ConstantUtils.SINGLEOBJECT_ALL, false);
		if(MyConfig.getInstance().getMySwithch().isFragmentSwitch()){
			outer.appendInfo(ictgFolder, fragFolder, ConstantUtils.SINGLEOBJECT_ENTRY);
//			outer.appendInfo(ictgFolder, fragFolder, ConstantUtils.SINGLEOBJECT_ALL);
		}
		
		
		/** ICTG **/
		// merge frage and component
		ATGModel.mergeNodels2newOne(Global.v().getiCTGModel().getOptModel(), Global.v().getFragmentModel()
				.getAtgModel(), Global.v().getiCTGModel().getOptModel());
		ATGModel ictgMergedModel = Global.v().getiCTGModel().getOptModel();
		// ictgMergedModel
		outer.writeATGModel(ictgFolder, ConstantUtils.ICTGMERGE + ".xml",
				ictgMergedModel);
		String dotname = Global.v().getAppModel().getAppName() + "_" + ConstantUtils.ICTGMERGE;
		outer.writeDotFile(ictgFolder, dotname, ictgMergedModel, true);
		if (ictgMergedModel.getConnectionSize() < 1800)
			GraphUtils.generateDotFile(ictgFolder + dotname, "pdf");

		Global.v().getiCTGModel().setOptModelwithoutFrag(getIctgOptModel());
		ATGModel ictgOptModel = Global.v().getiCTGModel().getOptModelwithoutFrag();
		// ictgOptModel
		outer.writeATGModel(ictgFolder, ConstantUtils.ICTGOPT + ".xml", ictgOptModel);
		String txtName = Global.v().getAppModel().getAppName() + "_" + ConstantUtils.ICTGOPT + ".txt";
		outer.writeAtgModeTxtFile(ictgFolder, txtName, ictgOptModel, false);
		String dotname2 = Global.v().getAppModel().getAppName() + "_" + ConstantUtils.ICTGOPT;
		outer.writeDotFile(ictgFolder, dotname2, ictgOptModel, false);
		if (ictgMergedModel.getConnectionSize() < 1800)
			GraphUtils.generateDotFile(ictgFolder + dotname2, "pdf");

		//+++
		// outer.writeIC3Output(ictgFolder,"IccModel.txt", ictgOptModel);
		outer.writeIC3Output(ictgFolder,"IccModel.txt", Global.v().getiCTGModel().getOptModel());

		// outer.writeIccLinksConfigFile(summary_app_dir +
		// ConstantUtils.ICTGFOLDETR, ConstantUtils.LINKFILE, ictgOptModel);
		
	}

	
	private ATGModel getIctgOptModel() {
		ATGModel ictgOptModel = new ATGModel();
		ATGModel mergedIctgModel = Global.v().getiCTGModel().getOptModel();
		Map<String, Set<String>> desfrag2StratcomMap = new HashMap<String, Set<String>>();
		// System.out.println(PrintUtils.printMap(mergedIctgModel.getAtgEdges()));
		// handle comp2fragment and frag2fragment --> comp2fragment
		for (Entry<String, Set<AtgEdge>> entry : mergedIctgModel.getAtgEdges().entrySet()) {
			for (AtgEdge edge : entry.getValue()) {
				if (edge.getType().equals(AtgType.Act2Frag) || edge.getType().equals(AtgType.NonAct2Frag)) {
					String souComp = edge.getSource().getName();
					String desFrag = edge.getDestnation().getName();
					if (!desfrag2StratcomMap.containsKey(desFrag))
						desfrag2StratcomMap.put(desFrag, new HashSet<String>());
					desfrag2StratcomMap.get(desFrag).add(souComp);
				}
			}
		}
		while (true) { // fix point
			boolean changed = false;
			for (Entry<String, Set<AtgEdge>> entry : mergedIctgModel.getAtgEdges().entrySet()) {
				for (AtgEdge edge : entry.getValue()) {
					if (edge.getType().equals(AtgType.Frag2Frag)) {
						String souFrag = edge.getSource().getName();
						String desFrag = edge.getDestnation().getName();
						if (desfrag2StratcomMap.containsKey(souFrag)) {
							if (!desfrag2StratcomMap.containsKey(desFrag)) {
								desfrag2StratcomMap.put(desFrag, new HashSet<String>());
							}
							for (String com : desfrag2StratcomMap.get(souFrag)) {
								if (!desfrag2StratcomMap.get(desFrag).contains(com)) {
									desfrag2StratcomMap.get(desFrag).add(com);
									changed = true;
								}
							}
						}
					}
				}
			}
			if (changed == false)
				break;
		}

		for (Entry<String, Set<AtgEdge>> entry : mergedIctgModel.getAtgEdges().entrySet()) {
			for (AtgEdge edge : entry.getValue()) {
				// if(edge.getSource().getClassName().contains("ItemlistFragment"))
				// System.out.println(edge);
				switch (edge.getType()) {
				case Act2Act:
					ictgOptModel.addAtgEdges(entry.getKey(), edge);
					break;
				case NonAct2Act:
					ictgOptModel.addAtgEdges(entry.getKey(), edge);
					break;
				case Act2NonAct:
					ictgOptModel.addAtgEdges(entry.getKey(), edge);
					break;
				case NonAct2NonAct:
					ictgOptModel.addAtgEdges(entry.getKey(), edge);
					break;
				case Frag2Act:
					if (desfrag2StratcomMap.containsKey(edge.getSource().getClassName())) {
						for (String startCom : desfrag2StratcomMap.get(edge.getSource().getClassName())) {
							AtgEdge edgeCopy = new AtgEdge(edge);
							edgeCopy.setSource(new AtgNode(startCom));
							ictgOptModel.addAtgEdges(startCom, edgeCopy);
						}
					}
					break;
				case Frag2NonAct:
					if (desfrag2StratcomMap.containsKey(edge.getSource().getClassName())) {
						for (String startCom : desfrag2StratcomMap.get(edge.getSource().getClassName())) {
							AtgEdge edgeCopy = new AtgEdge(edge);
							edgeCopy.setSource(new AtgNode(startCom));
							ictgOptModel.addAtgEdges(startCom, edgeCopy);
						}
					}
					break;
				default:
					break;
				}
			}
		}

		return ictgOptModel;
	}

	protected void setMySwitch() {
		// TODO Auto-generated method stub
		
	}
}