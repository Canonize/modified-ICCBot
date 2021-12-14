package main.java.client.toolEvaluate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import main.java.Global;
import main.java.MyConfig;
import main.java.analyze.model.labeledOracleModel.LabeledOracleReader;
import main.java.analyze.utils.ConstantUtils;
import main.java.analyze.utils.GraphUtils;
import main.java.analyze.utils.output.FileUtils;
import main.java.client.BaseClient;
import main.java.client.obj.model.atg.ATGModel;
import main.java.client.obj.target.ctg.CTGReaderClient;
import main.java.client.related.gator.GatorATGResultEvaluateClient;
import main.java.client.related.ic3.IC3ResultEvaluateClient;
import main.java.client.related.ic3dial.IC3DIALDroidResultEvaluateClient;

/**
 * Analyzer Class
 * 
 * @author hanada
 * @version 2.0
 */
public class ToolEvaluateClient extends BaseClient {

	@Override
	protected void clientAnalyze() {
		new IC3ResultEvaluateClient().start();

		new IC3DIALDroidResultEvaluateClient().start();

		new GatorATGResultEvaluateClient().start();

		new CTGReaderClient().start();

//		dynamicResultAnalyzer analyzer = new dynamicResultAnalyzer();
//		analyzer.analyze();
//
//		manualResultAnalyzer analyzer2 = new manualResultAnalyzer();
//		analyzer2.analyze();

		LabeledOracleReader reader = new LabeledOracleReader();
		reader.analyze();

		System.out.println("Successfully analyze with ToolEvaluateClient.");

	}

	@Override
	public void clientOutput() {
		String appName = Global.v().getAppModel().getAppName();
		String summary_app_dir = MyConfig.getInstance().getResultFolder() + appName + File.separator;
		FileUtils.createFolder(summary_app_dir + ConstantUtils.ORACLEFOLDETR);
		StringBuilder sb = new StringBuilder(MyConfig.getInstance().getMySwithch().toString());

//		dynamicModelContruction(sb, appName, summary_app_dir);
//		manualModelContruction(sb, appName, summary_app_dir);
//		oracleModelContruction(sb, appName, summary_app_dir);
		
		oracleEvaluate(sb);
		ICCBotEvaluate(sb);
		IC3Evaluate(sb);
		IC3DialEvaluate(sb);
		GatorEvaluate(sb);

//		FilterAndEnhanceEvaluate(sb);

		outputForExcel();

		String content = sb.toString();
		FileUtils.writeText2File(summary_app_dir + ConstantUtils.ORACLEFOLDETR + appName + ConstantUtils.SCORERECORD,
				content, true);
	}

	private void FilterAndEnhanceEvaluate(StringBuilder sb) {
		ATGModel manualModel = Global.v().getiCTGModel().getMannualModel();
		String content = Global.v().getAppModel().getAppName() + "\t";
		content += "filter\t" + manualModel.getFilteredNum() + "\t";
		content += "add\t" + manualModel.getEnhancedNum() + "\t";
		content += "filteredService\t" + manualModel.getFilteredServiceNum() + "\t";
		FileUtils.writeText2File(MyConfig.getInstance().getResultWarpperFolder() + File.separator + "edgeResult.txt", content, true);
		System.out.print(manualModel.getFilteredNum() + "\t");
		System.out.print(manualModel.getEnhancedNum() + "\t");

	}

	private void outputForExcel() {
		ATGModel dynamicModel = Global.v().getiCTGModel().getDynamicModel();
		ATGModel manualModel = Global.v().getiCTGModel().getMannualModel();
		ATGModel oracleModel = Global.v().getiCTGModel().getOracleModel();

		ATGModel optModel = Global.v().getiCTGModel().getOptModelwithoutFrag();
		ATGModel ic3Model = Global.v().getiC3Model().getIC3AtgModel();
		ATGModel IC3DialModel = Global.v().getiC3DialDroidModel().getIC3AtgModel();
		ATGModel gatorModel = Global.v().getGatorModel().getGatorAtgModel();

		StringBuilder sb = new StringBuilder();
		sb.append(Global.v().getAppModel().getComponentMap().size() + "\t");

//		sb.append(String.format("%.2f", dynamicModel.getCompletenessScore()) + "\t");
//		sb.append(String.format("%.2f", manualModel.getCompletenessScore()) + "\t");
//		sb.append(String.format("%.2f", oracleModel.getCompletenessScore()) + "\t");
//
//		sb.append(String.format("%.2f", dynamicModel.getConnectionScore()) + "\t");
//		sb.append(String.format("%.2f", manualModel.getConnectionScore()) + "\t");
//		sb.append(String.format("%.2f", oracleModel.getConnectionScore()) + "\t");

		sb.append(String.format("%.2f", optModel.getCompletenessScore()) + "\t");
		sb.append(String.format("%.2f", ic3Model.getCompletenessScore()) + "\t");
		sb.append(String.format("%.2f", IC3DialModel.getCompletenessScore()) + "\t");
		sb.append(String.format("%.2f", gatorModel.getCompletenessScore()) + "\t");

		sb.append(String.format("%.2f", optModel.getConnectionScore()) + "\t");
		sb.append(String.format("%.2f", ic3Model.getConnectionScore()) + "\t");
		sb.append(String.format("%.2f", IC3DialModel.getConnectionScore()) + "\t");
		sb.append(String.format("%.2f", gatorModel.getConnectionScore()) + "\t");

		sb.append(optModel.getConnectionSize() + "\t");
		sb.append(ic3Model.getConnectionSize() + "\t");
		sb.append(IC3DialModel.getConnectionSize() + "\t");
		sb.append(gatorModel.getConnectionSize() + "\t");

		sb.append(optModel.getOracleEdgeSize() + "\t");

		sb.append(optModel.getFnEdgeSize() + "\t");
		sb.append(ic3Model.getFnEdgeSize() + "\t");
		sb.append(IC3DialModel.getFnEdgeSize() + "\t");
		sb.append(gatorModel.getFnEdgeSize() + "\t");

		sb.append(String.format("%.2f", optModel.getFalsenegativeScore()) + "\t");
		sb.append(String.format("%.2f", ic3Model.getFalsenegativeScore()) + "\t");
		sb.append(String.format("%.2f", IC3DialModel.getFalsenegativeScore()) + "\t");
		sb.append(String.format("%.2f", gatorModel.getFalsenegativeScore()));

		System.out.println(sb.toString());
		FileUtils.writeText2File(MyConfig.getInstance().getResultWarpperFolder() + File.separator + "oracleResult.txt", Global.v().getAppModel().getAppName()
				+ "\t" + sb.toString() + "\n", true);
	}

	/**
	 * construct model use dynamic log information
	 * 
	 * @param sb
	 * @param appName
	 * @param summary_app_dir
	 */
	private void dynamicModelContruction(StringBuilder sb, String appName, String summary_app_dir) {
		System.out.println();
		ATGModel dynamicModel = Global.v().getiCTGModel().getDynamicModel();

		ToolEvaluateClientOutput outer = new ToolEvaluateClientOutput();
		String dotname = appName + "_" + ConstantUtils.ICTGDYNAMIC;
		outer.writeDotFile(summary_app_dir + ConstantUtils.ORACLEFOLDETR, dotname, dynamicModel, false);
		GraphUtils.generateDotFile(summary_app_dir + ConstantUtils.ORACLEFOLDETR + dotname, "pdf");
		initStringBuilderComplete("dynamic", sb);
		dynamicModel.evaluateCompleteness("dynamic oracle", sb);

	}

	/**
	 * construct model use manual information
	 * 
	 * @param sb
	 * @param appName
	 * @param summary_app_dir
	 */
	private void manualModelContruction(StringBuilder sb, String appName, String summary_app_dir) {
		System.out.println();
		ATGModel manualModel = Global.v().getiCTGModel().getMannualModel();

		ToolEvaluateClientOutput outer = new ToolEvaluateClientOutput();
		String dotname2 = appName + "_" + ConstantUtils.ICTGDMANUAL;
		outer.writeDotFile(summary_app_dir + ConstantUtils.ORACLEFOLDETR, dotname2, manualModel, false);
		GraphUtils.generateDotFile(summary_app_dir + ConstantUtils.ORACLEFOLDETR + dotname2, "pdf");
		initStringBuilderComplete("manual", sb);
		manualModel.evaluateCompleteness("manual oracle", sb);
	}

	/**
	 * merge dynamic and manual oracle to oracleModel
	 * 
	 * @param sb
	 * @param appName
	 * @param summary_app_dir
	 */
	private void oracleModelContruction(StringBuilder sb, String appName, String summary_app_dir) {
		System.out.println();
		ATGModel dynamicModel = Global.v().getiCTGModel().getDynamicModel();
		ATGModel manualModel = Global.v().getiCTGModel().getMannualModel();
		ATGModel oracleModel = Global.v().getiCTGModel().getOracleModel();

		ToolEvaluateClientOutput outer = new ToolEvaluateClientOutput();
		ATGModel.mergeNodels2newOne(dynamicModel, manualModel, oracleModel);
		String dotname3 = appName + "_" + ConstantUtils.ICTGORACLE;
		outer.writeDotFile(summary_app_dir + ConstantUtils.ORACLEFOLDETR, dotname3, oracleModel, false);
		GraphUtils.generateDotFile(summary_app_dir + ConstantUtils.ORACLEFOLDETR + dotname3, "pdf");
		initStringBuilderComplete("oracle", sb);
		writeTagedOracleFile(summary_app_dir, appName);
		oracleModel.evaluateCompleteness("whole oracle", sb);
	}

	private void oracleEvaluate(StringBuilder sb) {
		System.out.println();
//		ATGModel dynamicModel = Global.v().getiCTGModel().getDynamicModel();
//		ATGModel manualModel = Global.v().getiCTGModel().getMannualModel();
		ATGModel oracleModel = Global.v().getiCTGModel().getOracleModel();

//		initStringBuilderConnection("oracleModel", sb);
//		dynamicModel.evaluateConnectivity("dynamicModel", sb);
//		manualModel.evaluateConnectivity("manualModel", sb);
//		oracleModel.evaluateConnectivity("oracleModel", sb);
		oracleModel.countTagForOracle();
		System.out.println();

	}

	/**
	 * Evaluate the completeness/connection rate/false nagetive of ICCBot
	 * 
	 * @param sb
	 */
	private void ICCBotEvaluate(StringBuilder sb) {
		System.out.println();
		ATGModel oracleModel = Global.v().getiCTGModel().getOracleModel();
		ATGModel optModel = Global.v().getiCTGModel().getOptModelwithoutFrag();
		if(optModel.isExist()){
			initStringBuilderComplete("ICCBot      ", sb);
			optModel.evaluateCompleteness("ICCBot      ", sb);
	
			initStringBuilderConnection("ICCBot      ", sb);
			optModel.evaluateConnectivity("ICCBot      ", sb);
	
			if (oracleModel.getAtgEdges().size() > 0) {
				initStringBuilderFN("ICCBot      ", sb);
				optModel.evaluateFalseNegative("ICCBot      ", oracleModel, sb);
			}else{
				String hint = "The results of labeled oracle doesn't exist, for false negative evaluation, please add files into folder /labeledOracle\n";
				sb.append(hint);
				System.out.println(hint);
			}
			System.out.println();
		}else{
			String hint = "The results for tool IC3 doesn't exist, for false negative evaluation, please run the MainClient first.\n";
			sb.append(hint);
			System.out.println(hint);
		}
	}

	/**
	 * Evaluate the completeness/connection rate/false nagetive of IC3
	 * 
	 * @param sb
	 */
	private void IC3Evaluate(StringBuilder sb) {
		ATGModel oracleModel = Global.v().getiCTGModel().getOracleModel();
		ATGModel ic3Model = Global.v().getiC3Model().getIC3AtgModel();
		if(ic3Model.isExist()){
			initStringBuilderComplete("IC3         ", sb);
			ic3Model.evaluateCompleteness("IC3         ", sb);
	
			initStringBuilderConnection("IC3         ", sb);
			ic3Model.evaluateConnectivity("IC3         ", sb);
	
			if (oracleModel.getAtgEdges().size() > 0) {
				initStringBuilderFN("IC3         ", sb);
				ic3Model.evaluateFalseNegative("IC3         ", oracleModel, sb);
			}else{
				String hint = "The results of labeled oracle doesn't exist, for false negative evaluation, please add files into folder /labeledOracle\n";
				sb.append(hint);
				System.out.println(hint);
			}
			System.out.println();
		}else{
			String hint = "The results for tool IC3 doesn't exist, please add files into folder /relatedTools/IC3\n";
			sb.append(hint);
			System.out.println(hint);
		}
	}

	/**
	 * Evaluate the completeness/connection rate/false nagetive of IC3-dialdroid
	 * 
	 * @param sb
	 */
	private void IC3DialEvaluate(StringBuilder sb) {
		ATGModel oracleModel = Global.v().getiCTGModel().getOracleModel();
		ATGModel ic3Model = Global.v().getiC3DialDroidModel().getIC3AtgModel();
		if(ic3Model.isExist()){
			initStringBuilderComplete("IC3Dial     ", sb);
			ic3Model.evaluateCompleteness("IC3Dial     ", sb);
	
			initStringBuilderConnection("IC3Dial     ", sb);
			ic3Model.evaluateConnectivity("IC3Dial     ", sb);
	
			if (oracleModel.getAtgEdges().size() > 0) {
				initStringBuilderFN("IC3Dial     ", sb);
				ic3Model.evaluateFalseNegative("IC3Dial     ", oracleModel, sb);
			}else{
				String hint = "The results of labeled oracle doesn't exist, for false negative evaluation, please add files into folder /labeledOracle\n";
				sb.append(hint);
				System.out.println(hint);
			}
			System.out.println();
		}else{
			String hint = "The results for tool IC3-DIALDroid doesn't exist, please add files into folder /relatedTools/IC3-Dial\n";
			sb.append(hint);
			System.out.println(hint);
		}
	}

	/**
	 * Evaluate the completeness/connection rate/false nagetive of Gator
	 * 
	 * @param sb
	 */
	private void GatorEvaluate(StringBuilder sb) {
		ATGModel oracleModel = Global.v().getiCTGModel().getOracleModel();
		ATGModel GatorModel = Global.v().getGatorModel().getGatorAtgModel();
		if(GatorModel.isExist()){
			initStringBuilderComplete("Gator         ", sb);
			GatorModel.evaluateCompleteness("Gator         ", sb);
	
			initStringBuilderConnection("Gator         ", sb);
			GatorModel.evaluateConnectivity("Gator         ", sb);
	
			if (oracleModel.getAtgEdges().size() > 0) {
				initStringBuilderFN("Gator         ", sb);
				GatorModel.evaluateFalseNegative("Gator         ", oracleModel, sb);
			}else{
				String hint = "The results of labeled oracle doesn't exist, please add files into folder /labeledOracle\n";
				sb.append(hint);
				System.out.println(hint);
			}
			System.out.println();
		}else{
			String hint = "The results for tool Gator doesn't exist, please add files into folder /relatedTools/Gator\n";
			sb.append(hint);
			System.out.println(hint);
		}
	}

	/**
	 * inital sb for output
	 * 
	 * @param start
	 * @param sb
	 */
	private void initStringBuilderComplete(String start, StringBuilder sb) {
		sb.append("\n" + start + " completeness: \n");
		sb.append("allNode\t");
		sb.append("mainReachable\t");
		sb.append("mainScore\t");
		sb.append("exportReachable\t");
		sb.append("expoortScore\t");
		sb.append("nonSeperateNode\t");
		sb.append("nonSeperateNodeScore\t");
		sb.append("completenessScore\n");
	}

	/**
	 * inital sb for output
	 * 
	 * @param start
	 * @param sb
	 */
	private void initStringBuilderConnection(String start, StringBuilder sb) {
		sb.append("\n" + start + " connectivity: \n");
		sb.append("actualEdges\t");
		sb.append("maxEdges\t");
		sb.append("connectivityScore\n");
	}

	/**
	 * inital sb for output
	 * 
	 * @param start
	 * @param sb
	 */
	private void initStringBuilderFN(String start, StringBuilder sb) {
		sb.append("\n" + start + " false negative: \n");
		sb.append("falseNegativeNum\t");
		sb.append("oracleEdgeNum\t");
		sb.append("FalseNegativeScore\n");
	}

	/**
	 * write oracle.txt
	 * 
	 * @param summary_app_dir
	 * @param appName
	 */
	private void writeTagedOracleFile(String summary_app_dir, String appName) {
		String manualOracle = summary_app_dir + ConstantUtils.ORACLEFOLDETR + appName + ConstantUtils.ORACLEMANU;
		String dynaOracle = summary_app_dir + ConstantUtils.ORACLEFOLDETR + appName + ConstantUtils.ORACLEDYNA;

		List<String> resList = new ArrayList<String>();
		for (String s : FileUtils.getListFromFile(manualOracle)) {
			if (s.length() > 2 && !resList.contains(s.substring(2)))
				resList.add(s.substring(2));
		}
		for (String s : FileUtils.getListFromFile(dynaOracle)) {
			if (!resList.contains(s))
				resList.add(s);
		}
		try {
			writeOracleModel(summary_app_dir + ConstantUtils.ORACLEFOLDETR, appName + ConstantUtils.ORACLETEXT, resList);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * writeIntentSummaryModel
	 * 
	 * @param dir
	 * @param file
	 * @param resList
	 * @param entryMethod
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void writeOracleModel(String dir, String file, List<String> resList) throws DocumentException, IOException {
		Document document = FileUtils.xmlWriterBegin(dir, file, false);
		Element root = document.getRootElement();
		List<Element> eleList = new ArrayList<Element>();
		for (String line : resList) {
			if (line.startsWith("##") || !line.contains(" --> "))
				continue;
			Element e = new DefaultElement("OracleEdge");
			eleList.add(e);
			e.addAttribute("source", line.split(" --> ")[0]);
			e.addAttribute("destination", line.split(" --> ")[1]);
			e.addAttribute("method", "xxx");
			Element correctness = e.addElement("correctness");
			correctness.addAttribute("correctness", "true");
			correctness.addAttribute("incorrectType", "No.");
			correctness.addAttribute("incorrectReason", "");
			Element nodes = e.addElement("nodes");
			Element node = nodes.addElement("node");
			node.addAttribute("method", "");
			node.addAttribute("type", "");
			node.addAttribute("unit", "");
			Element tags = e.addElement("tags");
			Element entryMethod = tags.addElement("entryMethod");
			entryMethod.addAttribute("isLifeCycle", "");
			entryMethod.addAttribute("isNormalCallBack", "");
			entryMethod.addAttribute("isStaticCallBack", "");
			entryMethod.addAttribute("isOtherComplexCallBack", "");

			Element exitMethod = tags.addElement("exitMethod");
			exitMethod.addAttribute("isNormalSendICC", "");
			exitMethod.addAttribute("isWarpperSendICC", "");

			Element intentMatch = tags.addElement("intentMatch");
			intentMatch.addAttribute("isExplicit", "");
			intentMatch.addAttribute("isImplicit", "");

			Element analyzeScope = tags.addElement("analyzeScope");
			Element componentScope = analyzeScope.addElement("componentScope");
			componentScope.addAttribute("isActivity", "");
			componentScope.addAttribute("isService", "");
			componentScope.addAttribute("isBroadCast", "");
			componentScope.addAttribute("isDynamicBroadCast", "");

			Element nonComponentScope = analyzeScope.addElement("nonComponentScope");
			nonComponentScope.addAttribute("isFragment", "");
			nonComponentScope.addAttribute("isAdapter", "");
			nonComponentScope.addAttribute("isOtherClass", "");

			Element methodScope = analyzeScope.addElement("methodScope");
			methodScope.addAttribute("isLibraryInvocation", "");
			methodScope.addAttribute("isMultipleInvocation", "");
			methodScope.addAttribute("isBasicInvocation", "");
			methodScope.addAttribute("isAsyncInvocation", "");
			methodScope.addAttribute("isListenerInvocation", "");

			Element objectcope = analyzeScope.addElement("objectScope");
			objectcope.addAttribute("isStaticVal", "");
			objectcope.addAttribute("isStringOp", "");
			objectcope.addAttribute("isPolymorphic", "");

			Element sensitivity = analyzeScope.addElement("sensitivityScope");
			sensitivity.addAttribute("flow", "");
			sensitivity.addAttribute("path", "");
			sensitivity.addAttribute("context", "");
			sensitivity.addAttribute("object", "");
			sensitivity.addAttribute("field", "");
		}
		for (Element e : eleList) {
			root.add(e);
		}
		FileUtils.xmlWriteEnd(dir, file, document);

	}

}