package main.java.client.obj.target.ctg;

import heros.utilities.JsonArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.java.analyze.model.analyzeModel.UnitNode;
import soot.Unit;
import soot.jimple.Stmt;
import soot.SootMethod;
import soot.jimple.infoflow.android.iccta.Ic3Data.Application.Component.ExitPoint.Uri;
import main.java.Global;
import main.java.MyConfig;
import main.java.analyze.utils.ConstantUtils;
import main.java.analyze.utils.SootUtils;
import main.java.analyze.utils.TypeValueUtil;
import main.java.analyze.utils.output.FileUtils;
import main.java.analyze.utils.output.PrintUtils;
import main.java.client.obj.model.atg.ATGModel;
import main.java.client.obj.model.atg.AtgEdge;
import main.java.client.obj.model.atg.AtgNode;
import main.java.client.obj.model.component.BundleType;
import main.java.client.obj.model.component.ComponentModel;
import main.java.client.obj.model.component.Data;
import main.java.client.obj.model.component.ExtraData;
import main.java.client.obj.model.component.IntentFilterModel;
import main.java.client.obj.model.ctg.IntentSummaryModel;
import main.java.client.statistic.model.StatisticResult;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;

/**
 * output analyze result
 * 
 * @author 79940
 *
 */
public class CTGClientOutput {
	StatisticResult result;

	public CTGClientOutput(StatisticResult result) {
		this.result = result;
	}

	/**
	 * write icc info about a component
	 * 
	 * @param dir
	 * @param file
	 * @param AppModel
	 *            .getInstance()
	 * @throws IOException
	 * @throws DocumentException
	 */
	public void writeComponentModel(String dir, String file) throws IOException, DocumentException {
		Document document = FileUtils.xmlWriterBegin(dir, file, false);
		Element root = document.getRootElement();
		for (String componentName : Global.v().getAppModel().getComponentMap().keySet()) {
			ComponentModel componentInstance = Global.v().getAppModel().getComponentMap().get(componentName);
			Element component = root.addElement("component");
			component.addAttribute("name", componentName);
			component.addAttribute("type", componentInstance.getComponentType());
			if (componentInstance.getExported() != null && componentInstance.getExported().equals("true")) {
				component.addAttribute("exported", "true");
			}
			if (componentInstance.getPermission() != null && componentInstance.getPermission().length() > 0)
				component.addAttribute("permission", componentInstance.getPermission());

			writeManifest(componentInstance, component);
			writeSendNode(componentInstance, component);
			writereceiveNode(componentInstance, component);
		}

		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("UTF-8");
		File f = new File(dir + file);
		XMLWriter writer = new XMLWriter(new FileOutputStream(f), format);
		writer.setEscapeText(true);
		writer.write(document);
		writer.close();
	}

	/**
	 * writeMethodSummaryModel
	 * 
	 * @param entry
	 * 
	 * @param string
	 * @param topo
	 * @param AppModel
	 *            .getInstance()
	 * @throws IOException
	 */
	public void writeMethodSummaryModel(String dir, String file, boolean entryMethod) throws DocumentException,
			IOException {
		Document document = FileUtils.xmlWriterBegin(dir, file, false);
		Element root = document.getRootElement();
		List<Element> eleList = new ArrayList<Element>();
		if (entryMethod) {
			eleList = result.getXmlStatistic().getEntryMethodSummaryEleList();
		} else {
			eleList = result.getXmlStatistic().getAllMethodSummaryEleList();
		}
		for (Element e : eleList) {
			root.add(e.detach());
		}
		FileUtils.xmlWriteEnd(dir, file, document);
	}

	/**
	 * writePathSummaryModel write info about each icc flow
	 * 
	 * @param dir
	 * @param file
	 * @param AppModel
	 *            .getInstance()
	 * @param entryMethod
	 */
	public void writePathSummaryModel(String dir, String file, boolean entryMethod) throws DocumentException,
			IOException {
		Document document = FileUtils.xmlWriterBegin(dir, file, false);
		Element root = document.getRootElement();
		List<Element> eleList;
		if (entryMethod) {
			eleList = result.getXmlStatistic().getEntryPathSummaryEleList();
		} else {
			eleList = result.getXmlStatistic().getAllPathSummaryEleList();
		}

		for (Element e : eleList) {
			root.add(e.detach());
		}
		FileUtils.xmlWriteEnd(dir, file, document);
	}

	/**
	 * writeIntentSummaryModel
	 * 
	 * @param dir
	 * @param file
	 * @param entryMethod
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void writeIntentSummaryModel(String dir, String file, boolean entryMethod) throws DocumentException,
			IOException {
		Document document = FileUtils.xmlWriterBegin(dir, file, false);
		Element root = document.getRootElement();
		List<Element> eleList;
		if (entryMethod) {
			eleList = result.getXmlStatistic().getEntryIntentSummaryEleList();
		} else {
			eleList = result.getXmlStatistic().getAllIntentSummaryEleList();
		}
		for (Element e : eleList) {
			root.add(e.detach());
		}
		FileUtils.xmlWriteEnd(dir, file, document);

	}

     //+++
	public void writeIC3Output(String dir, String file, ATGModel atgModel){
		System.out.println("writeIC3Output-----------------------");
		if(atgModel==null){
			System.out.println("AtgModel NULL-------------");
			return;
		}
		StringBuilder iccModelString= new StringBuilder();

		StringBuilder otherModelString=new StringBuilder();

		StringBuilder idNegativeString=new StringBuilder();

		for (String className: Global.v().getAppModel().getComponentMap().keySet()) {
			iccModelString.append("components {\n");
			iccModelString.append("\tname: \"").append(className).append("\"\n");
			iccModelString.append("}\n");
		}


		// for (String className: Global.v().getAppModel().getComponentMap().keySet()) {
		for (String className: atgModel.getAtgEdges().keySet()) {

			System.out.println("name: "+className);
			iccModelString.append("components {\n");
			iccModelString.append("\tname: \"").append(className).append("\"\n");

			if(!atgModel.getAtgEdges().containsKey(className)) {
				iccModelString.append("}\n");
				continue;
			}
//			System.out.println("name: "+className);

			for (AtgEdge edge : atgModel.getAtgEdges().get(className)) {

				if(edge==null){
					System.out.println("ATGEdge NULL-------------");
					continue;
				}

				if (edge.getIntentSummary() != null){
					UnitNode passNode = null;
					UnitNode contextNode = null;
					// iccModelString.append("\t\tintentSummaryMethod: ").append(edge.getIntentSummary().getMethod()).append("\"\n");
					for(UnitNode curNode:edge.getIntentSummary().getNodes())
						if(curNode != null)
							if (curNode.getType().length() <= 0) 
								contextNode = curNode;								
							else if(curNode.getType().equals("PassOutIntent"))
								passNode = curNode;
					//+++ add new output type: SendIntent2ICC
					if(passNode != null ) {

						// check id is -1
						if(SootUtils.getIdForUnit(passNode.getUnit(),passNode.getMethod())==-1){
							idNegativeString.append("\tname: \"").append(className).append("\"\n");
							// idNegativeString.append("\t\t\tstatement: \"").append(curNode.getUnit()).append("\"\n");
							idNegativeString.append("\t\t\tstatement: \"").append(passNode.getUnit()).append("\"\n");
							idNegativeString.append("\t\t\tclass_name: \"").append(passNode.getMethod().getDeclaringClass()).append("\"\n");
							idNegativeString.append("\t\t\tmethod: \"").append(passNode.getMethod()).append("\"\n\n");
							continue;
						}
						// do not include putExtra method into ICC
						if(passNode.getUnit().toString().contains("\"")){
							continue;
						}

						iccModelString.append("\texit_points {\n\t\tinstruction {\n");
						iccModelString.append("\t\t\tstatement: \"").append(passNode.getUnit()).append("\"\n");

						//+++ use class_name output the actual source class
						if(contextNode != null)
							iccModelString.append("\t\t\tclass_name: \"").append(contextNode.getMethod()).append("#").append(SootUtils.getIdForUnit(contextNode.getUnit(),contextNode.getMethod())).append("\"\n");
						else
							iccModelString.append("\t\t\tclass_name: \"").append(passNode.getMethod()).append("#").append(SootUtils.getIdForUnit(passNode.getUnit(),passNode.getMethod())).append("\"\n");
						// if(passNode != null) {
						// System.out.println(passNode.getMethod().getSubSignature()+"\n");
						// int i = 0;
						// for (Iterator<Unit> iter = passNode.getMethod().retrieveActiveBody().getUnits().snapshotIterator(); iter.hasNext();i++) {
						// 	Stmt stmt = (Stmt) iter.next();
						// 	System.out.println(i + " : "+ stmt+"\n");
						// 	}
						// }
						iccModelString.append("\t\t\tmethod: \"").append(passNode.getMethod()).append("\"\n");
						iccModelString.append("\t\t\tid: ").append(SootUtils.getIdForUnit(passNode.getUnit(),passNode.getMethod())).append("\n");
						iccModelString.append("\t\t}\n");
						iccModelString.append("\t\tintents {\n");
		//				System.out.println("\tintents {");

						// CLASS
						iccModelString.append("\t\t\tattributes {\n\t\t\t\tkind: CLASS\n\t\t\t\tvalue: \"").append(edge.getDestnation().getClassName().replace(".", "/")).append("\"\n");
						iccModelString.append("\t\t\t}\n");

						// PACKAGE
						iccModelString.append("\t\t\tattributes {\n\t\t\t\tkind: PACKAGE\n\t\t\t\tvalue: \"").append(Global.v().getAppModel().getPackageName()).append("\"\n");
						iccModelString.append("\t\t\t}\n");
						iccModelString.append("\t\t}\n\t}\n");
					}
										
				}
//
//					// TODO: Class ???????????????????????????,????????????
//
//
//					// EXTRA?????????????????????????????????
//					if (edge.getIntentSummary().getSetExtrasValueList() != null){
//						System.out.println("\t\tattributes {\n\t\t\tkind: EXTRA\n\t\t\tvalue: "+edge.getIntentSummary().getSetExtrasValueList().toString());
//						System.out.println("\t\t}");
//					}
			}

			iccModelString.append("}\n");

		}


		FileUtils.writeText2File(dir+File.separator+file,iccModelString.toString(),false);
		// FileUtils.writeText2File(dir+File.separator+"otherPointModel.txt",otherModelString.toString(),true);
		FileUtils.writeText2File(dir+File.separator+"idNegitiveStatement.txt",idNegativeString.toString(),false);
		// FileUtils.writeText2File(dir+File.separator+"componentMap.txt",PrintUtils.printMap(Global.v().getAppModel().getComponentMap()),true);
		System.out.println("writeIC3Output-----------------------");
	}


	/**
	 * writeATGModel
	 * 
	 * @param string
	 * @param atg
	 * @param atgEdges
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void writeATGModel(String dir, String file, ATGModel atgModel) throws DocumentException, IOException {
		Document document = FileUtils.xmlWriterBegin(dir, file, false);
		Element root = document.getRootElement();
		for (String className: Global.v().getAppModel().getComponentMap().keySet()) {
			Element source = root.addElement("source");
			source.addAttribute("name", className);
			Set<String> addedEdgeStr = new HashSet<String>();
			if(!atgModel.getAtgEdges().containsKey(className)) continue;
			for (AtgEdge edge : atgModel.getAtgEdges().get(className)) {
				Element desEle = new DefaultElement("destination");
				desEle.addAttribute("name", edge.getDestnation().getName());
				desEle.addAttribute("type", edge.getType().name());
				desEle.addAttribute("method", edge.getMethodSig());
				desEle.addAttribute("InstructionId", edge.getInstructionId() + "");
				if (edge.getIntentSummary() != null) {
					if (edge.getIntentSummary().getSetActionValueList().size() > 0)
						desEle.addAttribute("action",
								PrintUtils.printList(edge.getIntentSummary().getSetActionValueList()));
					if (edge.getIntentSummary().getSetCategoryValueList().size() > 0)
						desEle.addAttribute("category",
								PrintUtils.printList(edge.getIntentSummary().getSetCategoryValueList()));
					if (edge.getIntentSummary().getSetDataValueList().size() > 0)
						desEle.addAttribute("data", PrintUtils.printList(edge.getIntentSummary().getSetDataValueList()));
					if (edge.getIntentSummary().getSetTypeValueList().size() > 0)
						desEle.addAttribute("type", PrintUtils.printList(edge.getIntentSummary().getSetTypeValueList()));
					if (edge.getIntentSummary().getSetExtrasValueList() != null)
						desEle.addAttribute("extras", edge.getIntentSummary().getSetExtrasValueList().toString());
					if (edge.getIntentSummary().getSetFlagsList() != null)
						desEle.addAttribute("flags", PrintUtils.printList(edge.getIntentSummary().getSetFlagsList()));
					// single intent has finish, atg do not has finish
					if (edge.getIntentSummary().isFinishFlag())
						desEle.addAttribute("finish", "true");
				}

				if (!addedEdgeStr.contains(desEle.asXML())) {
					source.add(desEle);
					addedEdgeStr.add(desEle.asXML());
				}
			}
		}
		FileUtils.xmlWriteEnd(dir, file, document);

	}

	public void writeIccLinksConfigFile(String dir, String linkfile, ATGModel aAtgModel) {
		FileUtils.createFolder(dir);
		File f = new File(dir + linkfile);
		BufferedWriter writer = null;
		Set<String> histroy = new HashSet<String>();
		try {
			writer = new BufferedWriter(new FileWriter(f));
			if (aAtgModel == null)
				return;
			for (Entry<String, Set<AtgEdge>> en : aAtgModel.getAtgEdges().entrySet()) {
				Set<AtgEdge> resList = en.getValue();
				for (AtgEdge edge : resList) {
					String edgeStr = "";
					String pkg = Global.v().getAppModel().getPackageName();
					String method = edge.getMethodSig();
					String instuction = edge.getInstructionId() + "-" + edge.getiCCkind();
					String dest = edge.getDestnation().getClassName();
					String instructions = Global.v().getAppModel().getMethod2InstructionMap().get(method);
					if (instructions != null) {
						edgeStr = pkg + ": " + method + " [" + instuction + "] " + dest + " {" + instructions + "}\n";
						if (!histroy.contains(edgeStr)) {
							writer.write(edgeStr);
							histroy.add(edgeStr);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * addElementInICC
	 * 
	 * @param icc
	 * @param key
	 * @param map
	 */
	private void addElementInICC(Element icc, String key, Map<String, Set<IntentSummaryModel>> map) {
		Element ele = icc.addElement(key);
		for (Entry<String, Set<IntentSummaryModel>> en : map.entrySet())
			ele.addAttribute(en.getKey(), en.getValue().size() + "");
	}

	private static void writereceiveNode(ComponentModel componentInstance, Element component) {
		Element receiver = new DefaultElement("receive");
		Set<String> actions2 = componentInstance.getReceiveModel().getReceivedActionSet();
		Set<String> category2 = componentInstance.getReceiveModel().getReceivedCategorySet();
		Set<String> data2 = componentInstance.getReceiveModel().getReceivedDataSet();
		Set<String> type2 = componentInstance.getReceiveModel().getReceivedTypeSet();
		BundleType extras2 = componentInstance.getReceiveModel().getReceivedExtraData();

		if (actions2.size() > 0)
			receiver.addAttribute("action", PrintUtils.printSet(actions2));
		if (category2.size() > 0)
			receiver.addAttribute("category", PrintUtils.printSet(category2));
		if (data2.size() > 0)
			receiver.addAttribute("data", PrintUtils.printSet(data2));
		if (type2.size() > 0)
			receiver.addAttribute("type", PrintUtils.printSet(type2));
		if (extras2.toString().length() > 0)
			receiver.addAttribute("extras", extras2.toString());
		if (receiver.attributeCount() > 0)
			component.add(receiver);
	}

	private static void writeSendNode(ComponentModel componentInstance, Element component) {
		Element sender = new DefaultElement("sender");
		Set<String> dess = componentInstance.getSendModel().getIccTargetSet();
		Set<String> newDestinationSet = new HashSet<String>();
		if (dess.size() > 0) {
			for (String des : dess) {
				newDestinationSet.add(des);
			}
			sender.addAttribute("destination", PrintUtils.printSet(newDestinationSet));
		}

		Set<String> actions = componentInstance.getSendModel().getSendActionSet();
		Set<String> category = componentInstance.getSendModel().getSendCategorySet();
		Set<String> data = componentInstance.getSendModel().getSendDataSet();
		Set<String> type = componentInstance.getSendModel().getSendTypeSet();
		Set<String> flags = componentInstance.getSendModel().getSendFlagSet();
		BundleType extras = componentInstance.getSendModel().getSendExtraData();

		if (actions.size() > 0)
			sender.addAttribute("action", PrintUtils.printSet(actions));
		if (category.size() > 0)
			sender.addAttribute("category", PrintUtils.printSet(category));
		if (data.size() > 0)
			sender.addAttribute("data", PrintUtils.printSet(data));
		if (type.size() > 0)
			sender.addAttribute("type", PrintUtils.printSet(type));
		if (flags.size() > 0)
			sender.addAttribute("flags", PrintUtils.printSet(flags));
		if (extras.toString().length() > 0)
			sender.addAttribute("extras", extras.toString());

		if (sender.attributeCount() > 0)
			component.add(sender);
	}

	private static void writeManifest(ComponentModel componentInstance, Element component) {
		Element manifest = new DefaultElement("manifest");

		for (int i = 0; i < componentInstance.getIntentFilters().size(); i++) {
			IntentFilterModel ifd = componentInstance.getIntentFilters().get(i);
			if (ifd.getAction_list().size() == 0)
				continue;
			Element intent_filter = manifest.addElement("intent_filter");
			if (ifd.getAction_list().size() > 0)
				intent_filter.addAttribute("action", PrintUtils.printSet(ifd.getAction_list()));

			if (ifd.getCategory_list().size() > 0)
				intent_filter.addAttribute("category", PrintUtils.printSet(ifd.getCategory_list()));

			for (Data data : ifd.getData_list()) {
				if (data.getMime_type().length() > 0)
					intent_filter.addAttribute("mimetype", data.getMime_type());

				if (data.getScheme().length() > 0)
					intent_filter.addAttribute("scheme", data.getScheme());

				if (data.getHost().length() > 0)
					intent_filter.addAttribute("host", data.getHost());

				if (data.getPort().length() > 0)
					intent_filter.addAttribute("port", data.getPort());

				if (data.getPath().length() > 0)
					intent_filter.addAttribute("path", data.getPath());
			}
		}
		if (manifest.content().size() > 0)
			component.add(manifest);
	}

	/**
	 * write instruMethods
	 * 
	 * @param dir
	 * @param file
	 * @param AppModel
	 *            .getInstance()
	 */
	public static void writeInstr(String dir, String file) {
		FileUtils.createFolder(dir);
		FileUtils.createFile(dir + file);
		File f = new File(dir + file);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(f));
			for (SootMethod me : Global.v().getAppModel().getAllMethods())
				writer.write(me.getSignature() + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * write Dot File
	 * 
	 * @param dir
	 * @param file
	 * @param map
	 * @param b
	 * @param AppModel
	 *            .getInstance()
	 */
	public void writeDotFile(String dir, String file, ATGModel atgModel, boolean drawFragNode) {
		Set<String> histroy = new HashSet<String>();
		File f = new File(dir + file + ".dot");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(f));
			if (atgModel.getAtgEdges() == null)
				return;
			writer.write("digraph G {\n");
			String aColor = "red";
			String sColor = "royalblue";
			String rColor = "peru";
			String pColor = "violet";
			String fColor = "teal";
			String bgColor = "mintcream";

			String subgraphString = "subgraph cluster_legend{ \nbgcolor=" + bgColor + ";\n";
			subgraphString += "_Activity[color = " + aColor + "];\n";
			subgraphString += "_Service[color = " + sColor + "];\n";
			subgraphString += "_Provider[color = " + pColor + "];\n";
			subgraphString += "_Reciever[color = " + rColor + "];\n";
			subgraphString += "_Fragment[color = " + fColor + "];\n";
			subgraphString += "_Main[style=filled, fillcolor=orange, color = " + bgColor + "];\n";
			subgraphString += "_Exported[style=filled, fillcolor=lightpink, color = " + bgColor + "];\n";
			writer.write(subgraphString
					+ "_Main -> _Exported -> _Activity -> _Service -> _Reciever -> _Provider -> _Fragment; "
					+ "\n}\n");
			
			String mainAct = Global.v().getAppModel().getMainActivity();
			String mainActNode =mainAct.split("\\.")[mainAct.split("\\.").length - 1];
			mainActNode = AtgNode.getClassName(mainActNode);
//			writer.write("_Main->"+mainActNode+"[ltail=cluster_component lhead=cluster_legend];\n");
			
			writer.write("subgraph cluster_component{ \n"); 
			
			// for (String component : Global.v().getAppModel().getActivityMap().keySet()) {
			// 	writeWithColor(writer, component, aColor);
			// }
			// for (String component : Global.v().getAppModel().getServiceMap().keySet()) {
			// 	writeWithColor(writer, component, sColor);
			// }
			// for (String component : Global.v().getAppModel().getRecieverMap().keySet()) {
			// 	writeWithColor(writer, component, rColor);
			// }
			// for (String component :
			// Global.v().getAppModel().getProviderMap().keySet()) {
			// writeWithColor(writer, component, pColor);
			// }
			if (drawFragNode)
				for (String component : Global.v().getAppModel().getFragmentClasses()) {
					writeWithColor(writer, component, fColor);
				}
			for (Entry<String, Set<AtgEdge>> en : atgModel.getAtgEdges().entrySet()) {
				Set<AtgEdge> resList = en.getValue();
				for (AtgEdge edge : resList) {
					String classTypeS = SootUtils.getTypeofClassName(SootUtils.getNameofClass(edge.getSource()
							.getName()));
					String classTypeE = SootUtils.getTypeofClassName(SootUtils.getNameofClass(edge.getDestnation()
							.getName()));

					if (classTypeS.equals("other") || classTypeE.equals("other"))
						continue;
					if (classTypeS.equals("provider") || classTypeE.equals("provider"))
						continue;

					String s = edge.getSource().getClassName();
					s = s.split("\\.")[s.split("\\.").length - 1].replace("\"", "").replace("\'", "");

					String e = edge.getDestnation().getClassName();
					e = e.split("\\.")[e.split("\\.").length - 1].replace("\"", "").replace("\'", "");

					String endString = ";\n";
					String edgeStr = s + "->" + e + endString;

					if (!histroy.contains(edgeStr)) {
						switch(SootUtils.getTypeofClassName(en.getKey())) {
							case "activity": 
								writeWithColor(writer, en.getKey(), aColor);
								break;
							case "service": 
								writeWithColor(writer, en.getKey(), sColor);
								break;
							case "receiver": 
								writeWithColor(writer, en.getKey(), rColor);
								break;
							default :
								writeWithColor(writer, en.getKey(), aColor);
						}
						writer.write(edgeStr);
						histroy.add(edgeStr);
					}
				}
			}
			writer.write("}\n}\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void writeWithColor(BufferedWriter writer, String component, String color) throws IOException {
		String res = component.split("\\.")[component.split("\\.").length - 1];
		res = AtgNode.getClassName(res);
		if (component.equals(Global.v().getAppModel().getMainActivity()))
			writer.write(res + "[style=filled, fillcolor=orange, color = " + color + "];\n");
		else if (Global.v().getAppModel().getExportedComponentMap().containsKey(component))
			writer.write(res + "[style=filled, fillcolor=pink, color = " + color + "];\n");
		else
			writer.write(res + "[color = " + color + "];\n");
	}

	/**
	 * for oracle calculate
	 * 
	 * @param dir
	 * @param fn
	 * @param atgModel
	 * @param b
	 */
	public void writeAtgModeTxtFile(String dir, String fn, ATGModel atgModel, boolean b) {

		List<String> edges = new ArrayList<String>();
		for (Entry<String, Set<AtgEdge>> en : atgModel.getAtgEdges().entrySet()) {
			Set<AtgEdge> resList = en.getValue();
			for (AtgEdge edge : resList) {
				String s = SootUtils.getNameofClass(edge.getSource().getName());
				String e = SootUtils.getNameofClass(edge.getDestnation().getName());
				String edgeStr = s + " -> " + e;
				if (!edges.contains(edge))
					edges.add(edgeStr);
			}
		}
		FileUtils.writeList2File(dir, fn, edges);
	}

	public void appendInfo(String originDir, String newDir, String file) {
		try {
			Document document = FileUtils.xmlWriterBegin(originDir,file, true);
			Element originRoot = document.getRootElement();
			SAXReader reader = new SAXReader();
			Element newRoot = reader.read(newDir+file).getRootElement();
			Iterator<?> iterator = newRoot.elementIterator();
			while (iterator.hasNext()) {
				Element ele = (Element) iterator.next();
				originRoot.add(ele.detach());
			}
			FileUtils.xmlWriteEnd(originDir,file, document);
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * generate json file for an component
	 * @param component
	 */
	public void writeComponentModelJson(String dir, String file) {
		JSONObject rootElement = new JSONObject(new LinkedHashMap());
		rootElement.put("package", new String(Global.v().getAppModel().getPackageName()));
		rootElement.put("version", ConstantUtils.VERSION);
		
		
		List<Object> componentList = new ArrayList<Object>();
		for (String className : Global.v().getAppModel().getComponentMap().keySet()) {
			ComponentModel component = Global.v().getAppModel().getComponentMap().get(className);
	        Map<String, Object> componenetMap = new LinkedHashMap<>();
	        componentList.add(componenetMap);
	        componenetMap.put("className", className);
	        putAttributeValue2componenetMap(componenetMap, component );
	        putAttributeSeed2componenetMap(componenetMap, component);
		}
		
		rootElement.put("components", componentList);
		ValueFilter filter = new ValueFilter() {
			@Override
			public Object process(Object object, String name, Object value) {
				if(value instanceof Collection){
					if(((Collection) value).size()==0){
						value = null;
					}
				}
				if(value instanceof String){
					if(((String) value).length()==0){
						value = null;
					}
				}
				return value;
			}
		};
		String jsonString = JSON.toJSONString(rootElement, filter, SerializerFeature.PrettyFormat, 
				SerializerFeature.DisableCircularReferenceDetect);
        FileUtils.writeText2File(dir+ file+".json", jsonString, false);
        FileUtils.writeText2File(dir+ file+"_typeValue.json", TypeValueUtil.getTypevalueJsonString(), false);
    
	}
	
	/**
	 * putAttributeValue2componenetMap
	 * @param componenetMap
	 * @param component
	 * @param attri
	 */
	private void putAttributeValue2componenetMap(Map<String, Object> componenetMap, ComponentModel component) {
		Map<String, Object> attriMap = new LinkedHashMap<String, Object>();
		putAttributeMap2componenetMap(attriMap, component, "actions");
        putAttributeMap2componenetMap(attriMap, component, "categories");
        putAttributeMap2componenetMap(attriMap, component, "datas");
        putAttributeMap2componenetMap(attriMap, component, "types");
        putAttributeMap2componenetMap(attriMap, component, "extras");
		putToMapIfNotAbsent("fullValueSet", attriMap, componenetMap);
	}
	
	/**
	 * putAttributeSeed2componenetMap
	 * @param componenetMap
	 * @param component
	 * @param attri
	 */
	private void putAttributeSeed2componenetMap(Map<String, Object> componenetMap, ComponentModel component) {
		Map<String, Object> attriMap = new LinkedHashMap<String, Object>();
		
		Object manifestJson = JSONArray.toJSON(component.getIntentFilters());
		putToMapIfNotAbsent("manifest", manifestJson, attriMap);
		Object sendJson = JSONArray.toJSON(component.getReceiveModel().getIntentObjsbyICCMsg());
		putToMapIfNotAbsent("sendIntent", sendJson, attriMap);
		Object reciveJson = JSONArray.toJSON(component.getReceiveModel().getIntentObjsbySpec());
		putToMapIfNotAbsent("recvIntent", reciveJson, attriMap);
		
		Set<Serializable> mixModels = new HashSet<Serializable>();
		Set<Serializable> history = new HashSet<Serializable>();
		for(IntentFilterModel filter: component.getIntentFilters()){
			mixModels.add(filter);
			history.add(JSON.toJSONString(filter)); 
		}
		for(IntentSummaryModel sendIntentSummaryModel:component.getReceiveModel().getIntentObjsbyICCMsg()){
			if(!history.contains(JSON.toJSONString(sendIntentSummaryModel)))
				history.add(sendIntentSummaryModel);
			mixModels.add(sendIntentSummaryModel);
		}
		for(IntentSummaryModel recvIntentSummaryModel:component.getReceiveModel().getIntentObjsbySpec()){
			if(!history.contains(JSON.toJSONString(recvIntentSummaryModel)))
				history.add(recvIntentSummaryModel);
			mixModels.add(recvIntentSummaryModel);
		}
		Object mixJson = JSONArray.toJSON(mixModels);
//		putToMapIfNotAbsent("mixIntent", mixJson, attriMap);
//		putToMapIfNotAbsent("initSeeds", attriMap, componenetMap);
	}


	@SuppressWarnings("unchecked")
	/**
	 * 
	 * @param componenetMap
	 * @param component
	 * @param attri
	 */
	private void putAttributeMap2componenetMap(Map<String, Object> componenetMap, ComponentModel component, String attri) {
		Map<String, Object> attriMap = new LinkedHashMap<String, Object>();
		
		if(attri.equals("datas")){
			putAttributeMap2componenetMap(attriMap, component, "ports");
			putAttributeMap2componenetMap(attriMap, component, "paths");
			putAttributeMap2componenetMap(attriMap, component, "schemes");
			putAttributeMap2componenetMap(attriMap, component, "hosts");
		}
		else if(attri.equals("extras")){
			Set<ExtraData> sendRes = (Set<ExtraData>)getSentIntentAttri(component,attri);
			Set<ExtraData> receivetRes = (Set<ExtraData>)getReceivedIntentAttri(component,attri);
			putToMapIfNotAbsent("sendIntent", JSONArray.toJSON(sendRes), attriMap);
			putToMapIfNotAbsent("recvIntent", JSONArray.toJSON(receivetRes), attriMap);
			
			Set<ExtraData> mixtRes = new HashSet<ExtraData>();
			if(sendRes != null) ExtraData.merge( mixtRes, sendRes);
			if(receivetRes != null) 
				ExtraData.merge( mixtRes, receivetRes);
//			putToMapIfNotAbsent("mixIntent",JSONArray.toJSON(mixtRes), attriMap);

		}else{
			Set<String> manifestRes = getManifestAttri(component,attri);
			Set<String> sendRes = (Set<String>)getSentIntentAttri(component,attri);
			Set<String> receivetRes = (Set<String>)getReceivedIntentAttri(component,attri);
			putToMapIfNotAbsent("manifest", JSONArray.toJSON(manifestRes), attriMap);
			putToMapIfNotAbsent("sendIntent", JSONArray.toJSON(sendRes), attriMap);
			putToMapIfNotAbsent("recvIntent", JSONArray.toJSON(receivetRes), attriMap);
			
			Set<String> mixtRes = new HashSet<String>();
			if(manifestRes != null) mixtRes.addAll(manifestRes);
			if(sendRes != null) mixtRes.addAll(sendRes);
			if(receivetRes != null) mixtRes.addAll(receivetRes);
//			putToMapIfNotAbsent("mixIntent", JSONArray.toJSON(mixtRes), attriMap);
		}
		putToMapIfNotAbsent(attri, attriMap, componenetMap);
	}


	private Set<String> getManifestAttri(ComponentModel component, String attri) {
		Set<String> res = new HashSet<String>();
		for(IntentFilterModel ifModel: component.getIntentFilters()){
			switch (attri) {
				case "actions":
					addAllToSetIfNotNull(ifModel.getAction_list(),res);
					break;
				case "categories":
					addAllToSetIfNotNull(ifModel.getCategory_list(),res);
					break;
				case "ports":
					for(Data data: ifModel.getData_list())
						addToSetIfNotNull(data.getPort(),res);
					break;
				case "paths":
					for(Data data: ifModel.getData_list())
						addToSetIfNotNull(data.getPath(),res);
					break;
				case "schemes":
					for(Data data: ifModel.getData_list())
						addToSetIfNotNull(data.getScheme(),res);
					break;
				case "hosts":
					for(Data data: ifModel.getData_list())
						addToSetIfNotNull(data.getHost(),res);
					break;
				case "types":
					for(Data data: ifModel.getData_list())
						addToSetIfNotNull(data.getMime_type(),res);
					break;
				default:
					break;
			}
		}
		if(res.size()==0) return null;
		return res;
	}
	

	/**
	 * the Intent in the callee
	 * @param component
	 * @param attri
	 * @return
	 */
	private Set getReceivedIntentAttri(ComponentModel component, String attri) {
		Set res = new HashSet<Object>();
		String dataReg = "(\\w*)(://)?(\\w*):?(\\w*)/?(\\w*)";
		Pattern pattern = Pattern.compile(dataReg);
		for(IntentSummaryModel model: component.getReceiveModel().getIntentObjsbySpec()){
			switch (attri) {
				case "actions":
					addAllToSetIfNotNull(model.getGetActionCandidateList(),res);
					break;
				case "categories":
					addAllToSetIfNotNull(model.getGetCategoryCandidateList(),res);
					break;
				// <scheme>://<host>:<port>/[<path>|<pathPrefix>|<pathPattern>]
				case "schemes":
					for(String data: model.getGetDataCandidateList()){
						Matcher matcher = pattern.matcher(data);
						matcher.find();
						if(matcher.matches() && matcher.group(1)!=null && matcher.group(1).length()>0){
							addToSetIfNotNull(matcher.group(1),res); 
						}
					}
					break;
				case "hosts":
					for(String data: model.getGetDataCandidateList()){
						Matcher matcher = pattern.matcher(data);
						matcher.find();
						if(matcher.matches() && matcher.group(3)!=null && matcher.group(3).length()>0){
							addToSetIfNotNull(matcher.group(3),res); 
						}
					}
					break;
				case "ports":
					for(String data: model.getGetDataCandidateList()){
						Matcher matcher = pattern.matcher(data);
						matcher.find();
						if(matcher.matches() && matcher.group(4)!=null  && matcher.group(4).length()>0){
							addToSetIfNotNull(matcher.group(4),res); 
						}
					}
					break;
				case "paths":
					for(String data: model.getGetDataCandidateList()){
						Matcher matcher = pattern.matcher(data);
						matcher.find();
						if(matcher.matches() && matcher.group(5)!=null  && matcher.group(5).length()>0){
							addToSetIfNotNull(matcher.group(5),res); 
						}
					}
					break;
				case "types":
					addAllToSetIfNotNull(model.getGetTypeCandidateList(),res);
					break;
				case "extras":
					BundleType extras = model.getGetExtrasCandidateList();
					for(List<ExtraData> eds: extras.obtainBundle().values()){
						for(ExtraData ed: eds){
							addToSetIfNotNull(ed,res);
						}
					}
					break;
				default:
					break;
			}
		}
		if(res.size()==0) return null;
		return res;
	}
	/**
	 * the Intent form the caller
	 * @param component
	 * @param attri
	 * @return
	 */
	private Set getSentIntentAttri(ComponentModel component, String attri) {
		Set res = new HashSet<Object>();
		String dataReg = "(\\w*)(://)?(\\w*):?(\\w*)/?(\\w*)";
		Pattern pattern = Pattern.compile(dataReg);
		for(IntentSummaryModel model: component.getReceiveModel().getIntentObjsbyICCMsg()){
			switch (attri) {
				case "actions":
					addAllToSetIfNotNull(model.getSetActionValueList(),res);
					break;
				case "categories":
					addAllToSetIfNotNull(model.getSetCategoryValueList(),res);
					break;
					// <scheme>://<host>:<port>/[<path>|<pathPrefix>|<pathPattern>]
				case "schemes":
					for(String data: model.getSetDataValueList()){
						Matcher matcher = pattern.matcher(data);
						matcher.find();
						if(matcher.matches() && matcher.group(1)!=null && matcher.group(1).length()>0){
							addToSetIfNotNull(matcher.group(1),res);
						}
					}
						
					break;
				case "hosts":
					for(String data: model.getSetDataValueList()){
						Matcher matcher = pattern.matcher(data);
						matcher.find();
						if(matcher.matches() && matcher.group(3)!=null && matcher.group(3).length()>0){
							addToSetIfNotNull(matcher.group(3),res); 
						}
					}
					break;
				case "ports":
					for(String data: model.getSetDataValueList()){
						Matcher matcher = pattern.matcher(data);
						matcher.find();
						if(matcher.matches() && matcher.group(4)!=null  && matcher.group(4).length()>0){
							addToSetIfNotNull(matcher.group(4),res);
						}
					}
					break;
				case "paths":
					for(String data: model.getSetDataValueList()){
						Matcher matcher = pattern.matcher(data);
						matcher.find();
						if(matcher.matches() && matcher.group(5)!=null  && matcher.group(5).length()>0){
							addToSetIfNotNull(matcher.group(5),res);
						}
					}
					break;
				case "types":
					addAllToSetIfNotNull(model.getSetTypeValueList(),res);
					break;
				case "extras":
					BundleType extras = model.getSetExtrasValueList();
					for(List<ExtraData> eds: extras.obtainBundle().values()){
						for(ExtraData ed: eds){
							addToSetIfNotNull(ed,res);
						}
					}
				default:
					break;
			}
		}
		if(res.size()==0) return null;
		return res;
	}
	
	/**
	 * put key value into map if value is not null
	 * @param key
	 * @param value
	 * @param map
	 */
	private void putToMapIfNotAbsent(String key, Object value, Map<String, Object> map) {
		if(value instanceof Collection){
			if(((Collection) value).size()>0)
				map.put(key, value);
		}else if(value instanceof Map){
			if(((Map) value).size()>0)
				map.put(key, value);
		}
		else if(value!=null){
			map.put(key, value);
		}
	}
	/**
	 * merge two sets if value is not null
	 * @param newSet
	 * @param oldSet
	 */
	private void addAllToSetIfNotNull(Collection newSet, Collection oldSet) {
		if(newSet.size()>0)
			oldSet.addAll(newSet);
	}
	/**
	 * put value into set if value is not null
	 * @param port
	 * @param res
	 */
	private boolean addToSetIfNotNull(Object value, Set res) {
		// TODO Auto-generated method stub
		if(value!=null){
			res.add(value);
			return true;
		}
		return false;
	}
}
