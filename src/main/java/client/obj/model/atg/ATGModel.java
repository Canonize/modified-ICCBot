package main.java.client.obj.model.atg;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import main.java.Global;
import main.java.MyConfig;
import main.java.analyze.model.labeledOracleModel.IccTag;
import main.java.analyze.model.labeledOracleModel.IccTagCount;
import main.java.analyze.model.labeledOracleModel.LabeledOracleModel;
import main.java.analyze.utils.SootUtils;
import main.java.analyze.utils.output.FileUtils;
import main.java.client.obj.model.component.ComponentModel;

public class ATGModel {
	private Map<String, Set<AtgEdge>> atgEdges;
	private String ATGFilePath;
	private double connectionScore;
	private double falsenegativeScore;
	private double completenessScore;
	private int connectionSize;
	private int oracleEdgeSize;
	private int fnEdgeSize;
	private int enhancedNum;
	private int filteredNum;
	private int filteredServiceNum;
	private int filteredReceiverNum;
	private boolean exist = true;

	public int getConnectionSize() {
		if (connectionSize > 0)
			return connectionSize;

		Set<String> history = new HashSet<String>();
		for (Set<AtgEdge> edges : atgEdges.values()) {
			for (AtgEdge edge : edges) {
				if (!history.contains(edge.getDescribtion())) {
					history.add(edge.getDescribtion());
					connectionSize += 1;
				}
			}
		}
		return connectionSize;
	}

	public ATGModel() {
		atgEdges = new HashMap<String, Set<AtgEdge>>();
	}

	public void countTagForOracle() {
		ATGModel oracleModel = Global.v().getiCTGModel().getOracleModel();
		IccTagCount counter = new IccTagCount();
		Set<String> oracleEdges = new HashSet<String>();
		for (Entry<String, Set<AtgEdge>> entry : oracleModel.getAtgEdges().entrySet()) {
			for (AtgEdge oracleEdge : entry.getValue()) {
				oracleEdges.add(oracleEdge.getDescribtion());
				modifyTagCounter(counter, oracleEdge.getDescribtion());
			}
		}

		File f = new File(MyConfig.getInstance().getResultWarpperFolder() + File.separator + "tagResult.txt");
		if (!f.exists())
			FileUtils.writeText2File(MyConfig.getInstance().getResultWarpperFolder()+ File.separator + "tagResult.txt", "\t\t" + counter.getTitle() + "\n",true);
		if (isExist())
			FileUtils.writeText2File(MyConfig.getInstance().getResultWarpperFolder() + File.separator + "tagResult.txt", Global.v().getAppModel().getAppName()
					+ "\t" + "oracle       " + "\t" + counter.toSimpleString() + "\n", true);
		else {
			FileUtils.writeText2File(MyConfig.getInstance().getResultWarpperFolder() + File.separator + "tagResult.txt", Global.v().getAppModel().getAppName()
					+ "\t" + "oracle       " + "\n", true);
		}
		return;
	}

	/**
	 * evaluate the false negetive info compare with an oracle
	 * 
	 * @param tag
	 * @param sb
	 *            dev.ukanth.ufirewall.MainActivity -->
	 *            dev.ukanth.ufirewall.service.FirewallService
	 * @return dev.ukanth.ufirewall.MainActivity -->
	 *         ev.ukanth.ufirewall.service.FirewallService
	 */
	public double evaluateFalseNegative(String tag, ATGModel oracle, StringBuilder sb) {
		IccTagCount counter = new IccTagCount();
		Set<String> myEdges = new HashSet<String>();
		for (Entry<String, Set<AtgEdge>> entry : atgEdges.entrySet()) {
			for (AtgEdge myEdge : entry.getValue()) {
				myEdges.add(myEdge.getDescribtion());
			}
		}
		Set<String> oracleEdges = new HashSet<String>();
		Set<String> fnSet = new HashSet<String>();
		for (Entry<String, Set<AtgEdge>> entry : oracle.getAtgEdges().entrySet()) {
			for (AtgEdge oracleEdge : entry.getValue()) {
				if (!oracleEdges.contains(oracleEdge.getDescribtion())) {
					oracleEdges.add(oracleEdge.getDescribtion());
				}
				if (!myEdges.contains(oracleEdge.getDescribtion()) && !fnSet.contains(oracleEdge.getDescribtion())) {
					fnSet.add(oracleEdge.getDescribtion());
					modifyTagCounter(counter, oracleEdge.getDescribtion());
				} else {

				}
			}
		}
		for (String fn : fnSet) {
			System.out.println("false negative: "+fn);
		}
		oracleEdgeSize = oracleEdges.size();
		fnEdgeSize = fnSet.size();
		falsenegativeScore = 1.0 * fnEdgeSize / oracleEdgeSize;
		sb.append(fnEdgeSize + "\t");
		sb.append(oracleEdgeSize + "\t");
		sb.append(String.format("%.2f", falsenegativeScore) + "\n");

		System.out.println(tag + " false negative score details: " + fnEdgeSize + " / " + oracleEdgeSize);
		System.out.println(tag + " false negative score fn = " + falsenegativeScore);

		System.out.println(counter);
		File f = new File(MyConfig.getInstance().getResultWarpperFolder()  + File.separator + "tagResult.txt");
		if (!f.exists())
			FileUtils.writeText2File(MyConfig.getInstance().getResultWarpperFolder() + File.separator + "tagResult.txt", "\t\t" + counter.getTitle() + "\n",
					true);
		if (isExist())
			FileUtils.writeText2File(MyConfig.getInstance().getResultWarpperFolder() + File.separator + "tagResult.txt", Global.v().getAppModel()
					.getAppName()
					+ "\t" + tag + "\t" + counter.toSimpleString() + "\n", true);
		else {
			FileUtils.writeText2File(MyConfig.getInstance().getResultWarpperFolder() + File.separator + "tagResult.txt", Global.v().getAppModel()
					.getAppName()
					+ "\t" + tag + "\n", true);
		}
		return falsenegativeScore;
	}

	private void modifyTagCounter(IccTagCount counter, String key) {
		LabeledOracleModel oracleModel = Global.v().getLabeledOracleModel();
		IccTag iccTag = oracleModel.getLabeledOracle().get(key);
		if (iccTag == null)
			return;
		if (iccTag.isLifeCycle())
			counter.isLifeCycle += 1;
		if (iccTag.isStaticCallBack())
			counter.isStaticCallBack += 1;
		if (iccTag.isDynamicCallBack())
			counter.isDynamicCallBack += 1;
		if (iccTag.isImpliciyCallBack())
			counter.isImpliciyCallBack += 1;
		if (iccTag.isLifeCycleOnly())
			counter.isLifeCycleOnly += 1;
		if (iccTag.isCallBackInclude())
			counter.isCallBackInclude += 1;

		if (iccTag.isNormalSendICC())
			counter.isNormalSendICC += 1;
		if (iccTag.isWarpperSendICC())
			counter.isWarpperSendICC += 1;

		if (iccTag.isExplicit())
			counter.isExplicit += 1;
		if (iccTag.isImplicit())
			counter.isImplicit += 1;

		if (iccTag.isActivity())
			counter.isActivity += 1;
		if (iccTag.isService())
			counter.isService += 1;
		if (iccTag.isBroadCast())
			counter.isBroadCast += 1;
		if (iccTag.isDynamicBroadCast())
			counter.isDynamicBroadCast += 1;
		if (iccTag.isNonActivity())
			counter.isNonActivity += 1;

		if (iccTag.isFragment())
			counter.isFragment += 1;
		if (iccTag.isAdapter())
			counter.isAdapter += 1;
		if (iccTag.isWidget())
			counter.isWidget += 1;
		if (iccTag.isOtherClass())
			counter.isOtherClass += 1;
		if (iccTag.isNonComponent())
			counter.isNonComponent += 1;

		if (iccTag.isLibraryInvocation())
			counter.isLibraryInvocation += 1;
		if (iccTag.isAsyncInvocation())
			counter.isAsyncInvocation += 1;
		if (iccTag.isPolymorphic())
			counter.isPolymorphic += 1;

		if (iccTag.isStaticVal())
			counter.isStaticVal += 1;
		if (iccTag.isStringOp())
			counter.isStringOp += 1;

		if (iccTag.isFlowSensitive())
			counter.isFlowSensitive += 1;
		if (iccTag.isPathSensitive())
			counter.isPathSensitive += 1;
		if (iccTag.isContextSensitive())
			counter.isContextSensitive += 1;
		if (iccTag.isFieldSensitive())
			counter.isFieldSensitive += 1;
		if (iccTag.isObjectSensitive())
			counter.isObjectSensitive += 1;

		if (iccTag.isStaticCallBackonly())
			counter.isStaticCallBackonly += 1;
		if (iccTag.isDynamicCallBackonly())
			counter.isDynamicCallBackonly += 1;
		if (iccTag.isImplicitCallBackonly())
			counter.isImplicitCallBackonly += 1;

		if (iccTag.isWarrperonly())
			counter.isWarrperonly += 1;
		if (iccTag.isImplicitICConly())
			counter.isImplicitICConly += 1;

		if (iccTag.isWarrperonly())
			counter.isWarrperonly += 1;
		if (iccTag.isImplicitICConly())
			counter.isImplicitICConly += 1;

		if (iccTag.isNonComponentonly())
			counter.isNonComponentonly += 1;
		if (iccTag.isFragmentonly())
			counter.isFragmentonly += 1;
		if (iccTag.isAdapteronly())
			counter.isAdapteronly += 1;
		if (iccTag.isWidgetonly())
			counter.isWidgetonly += 1;
		if (iccTag.isOtherClassonly())
			counter.isOtherClassonly += 1;
		if (iccTag.isContextSensionly())
			counter.isContextSensionly += 1;
		if (iccTag.isLibonly())
			counter.isLibonly += 1;
		if (iccTag.isNonActonly())
			counter.isNonActonly += 1;
		if (iccTag.isAsynconly())
			counter.isAsynconly += 1;
		if (iccTag.isPolymonly())
			counter.isPolymonly += 1;
	}

	/**
	 * evaluate whether an atg model is complete enough to be taken as oracle
	 * 
	 * @param tag
	 * @param sb
	 * @return
	 */
	public double evaluateCompleteness(String tag, StringBuilder sb) {
		Set<String> initSet = new HashSet<String>();
		for (ComponentModel component : Global.v().getAppModel().getComponentMap().values()) {
			if (component.is_mainAct()) {
				initSet.add(component.getComponetName());
			}
		}
		Set<String> initSet2 = new HashSet<String>();
		for (ComponentModel component : Global.v().getAppModel().getComponentMap().values()) {
			if (component.is_exported()) {
				initSet2.add(component.getComponetName());
			}
		}
		// evaluate connection, main2other, export2other
		sb.append(Global.v().getAppModel().getComponentMap().size() + "\t");
		double mainScore = getSocreFromEntrySet(initSet, sb);
		sb.append(String.format("%.2f", mainScore) + "\t");
		double expoortScore = getSocreFromEntrySet(initSet2, sb);
		sb.append(String.format("%.2f", expoortScore) + "\t");
		double nonSeperateNodeScore = getScoreOfNonSeperateNode(sb);
		sb.append(String.format("%.2f", nonSeperateNodeScore) + "\t");

		completenessScore = mainScore / 2 + expoortScore / 4 + nonSeperateNodeScore / 4;
		sb.append(String.format("%.2f", completenessScore) + "\n");

		System.out.println(tag + " completeness score details: " + mainScore + " * 1/2 + " + expoortScore + " * 1/4 + "
				+ nonSeperateNodeScore + " * 1/4 ");
		System.out.println(tag + " completeness score t = " + completenessScore);

		return completenessScore;
	}

	/**
	 * less edge, more effective avoid two many false positive edges lead to
	 * higher completeness
	 * 
	 * @return
	 */
	public double evaluateConnectivity(String tag, StringBuilder sb) {
		int comNum = Global.v().getAppModel().getComponentMap().size();
		int wholeSize = comNum;
		if (wholeSize == 0)
			return 0;
		connectionScore = 2.0 * getConnectionSize() / wholeSize;
		sb.append(getConnectionSize() + "\t");
		sb.append(wholeSize + "\t");
		sb.append(String.format("%.2f", connectionScore) + "\n");

		System.out.println(tag + " connectivity score details: " + getConnectionSize() + " / " + wholeSize);
		System.out.println(tag + " connectivity score t = " + connectionScore);
		return connectionScore;
	}

	public double getScoreOfNonSeperateNode(StringBuilder sb) {
		double score = 0.0;
		Set<String> initSet = new HashSet<String>();
		for (Entry<String, Set<AtgEdge>> entry : atgEdges.entrySet()) {
			for (AtgEdge myEdge : entry.getValue()) {
				if (Global.v().getAppModel().getComponentMap().keySet().contains(myEdge.getSource().getClassName()))
					initSet.add(myEdge.getSource().getClassName());
				if (Global.v().getAppModel().getComponentMap().keySet().contains(myEdge.getDestnation().getClassName()))
					initSet.add(myEdge.getDestnation().getClassName());
			}
		}
		score = 1.0 * initSet.size() / Global.v().getAppModel().getComponentMap().size();
		sb.append(initSet.size() + "\t");
		return score;
	}

	private double getSocreFromEntrySet(Set<String> reachable, StringBuilder sb) {
		double score = 0.0;
		int wholeSize = Global.v().getAppModel().getComponentMap().size();
		if (wholeSize == 0)
			return score;
		int sizeBefore = 0, sizeAfter = sizeBefore - 1;
		while (sizeBefore != sizeAfter) {
			sizeBefore = reachable.size();
			for (String exported : new HashSet<String>(reachable)) {
				if (atgEdges.containsKey(exported)) {
					for (AtgEdge edge : atgEdges.get(exported)) {
						if (!reachable.contains(edge.getDestnation().getName())) {
							reachable.add(edge.getDestnation().getName());
						}
					}
				}
			}
			sizeAfter = reachable.size();
		}

		score = 1.0 * sizeAfter / wholeSize;
		sb.append(sizeAfter + "\t");
		return score;
	}

	public Map<String, Set<AtgEdge>> getAtgEdges() {
		return atgEdges;
	}

	public void setAtgEdges(Map<String, Set<AtgEdge>> atgEdges) {
		this.atgEdges = atgEdges;
	}

	/**
	 * ATG Construction
	 * 
	 * @param source
	 * @param destination
	 */
	public boolean addAtgEdges(String source, AtgEdge edge) {
		source = SootUtils.getNameofClass(source);
		if (atgEdges.get(source) == null) {
			atgEdges.put(source, new HashSet<AtgEdge>());
		}
		Set<AtgEdge> edges = atgEdges.get(source);
		if (!edges.contains(edge)) {
			edges.add(edge);
			return true;
		}
		return false;
	}

	public static void mergeNodels2newOne(ATGModel m1, ATGModel m2, ATGModel m3) {
		for (Entry<String, Set<AtgEdge>> entry : m1.getAtgEdges().entrySet())
			for (AtgEdge edge : entry.getValue())
				m3.addAtgEdges(entry.getKey(), edge);

		for (Entry<String, Set<AtgEdge>> entry : m2.getAtgEdges().entrySet())
			for (AtgEdge edge : entry.getValue())
				m3.addAtgEdges(entry.getKey(), edge);

	}

	/**
	 * @return the aTGFilePath
	 */
	public String getATGFilePath() {
		return ATGFilePath;
	}

	/**
	 * @param aTGFilePath
	 *            the aTGFilePath to set
	 */
	public void setATGFilePath(String aTGFilePath) {
		ATGFilePath = aTGFilePath;
	}

	/**
	 * @return the connectionScore
	 */
	public double getConnectionScore() {
		if (isExist())
			return connectionScore;
		else
			return -1;
	}

	/**
	 * @param connectionScore
	 *            the connectionScore to set
	 */
	public void setConnectionScore(double connectionScore) {
		this.connectionScore = connectionScore;
	}

	/**
	 * @return the falsenegativeScore
	 */
	public double getFalsenegativeScore() {
		if (isExist())
			return falsenegativeScore;
		else
			return -1;
	}

	/**
	 * @param falsenegativeScore
	 *            the falsenegativeScore to set
	 */
	public void setFalsenegativeScore(double falsenegativeScore) {
		this.falsenegativeScore = falsenegativeScore;
	}

	/**
	 * @return the completenessScore
	 */
	public double getCompletenessScore() {
		if (isExist())
			return completenessScore;
		else
			return -1;
	}

	/**
	 * @param completenessScore
	 *            the completenessScore to set
	 */
	public void setCompletenessScore(double completenessScore) {
		this.completenessScore = completenessScore;
	}

	/**
	 * @return the oracleEdgeSize
	 */
	public int getOracleEdgeSize() {
		return oracleEdgeSize;
	}

	/**
	 * @param oracleEdgeSize
	 *            the oracleEdgeSize to set
	 */
	public void setOracleEdgeSize(int oracleEdgeSize) {
		this.oracleEdgeSize = oracleEdgeSize;
	}

	/**
	 * @return the fnEdgeSize
	 */
	public int getFnEdgeSize() {
		if (isExist())
			return fnEdgeSize;
		else
			return -1;
	}

	/**
	 * @param fnEdgeSize
	 *            the fnEdgeSize to set
	 */
	public void setFnEdgeSize(int fnEdgeSize) {
		this.fnEdgeSize = fnEdgeSize;
	}

	/**
	 * @return the enhancedNum
	 */
	public int getEnhancedNum() {
		return enhancedNum;
	}

	/**
	 * @param enhancedNum
	 *            the enhancedNum to set
	 */
	public void setEnhancedNum(int enhancedNum) {
		this.enhancedNum = enhancedNum;
	}

	/**
	 * @return the filteredNum
	 */
	public int getFilteredNum() {
		return filteredNum;
	}

	/**
	 * @param filteredNum
	 *            the filteredNum to set
	 */
	public void setFilteredNum(int filteredNum) {
		this.filteredNum = filteredNum;
	}

	/**
	 * @return the exist
	 */
	public boolean isExist() {
		return exist;
	}

	/**
	 * @param exist
	 *            the exist to set
	 */
	public void setExist(boolean exist) {
		this.exist = exist;
	}

	/**
	 * @return the filteredServiceNum
	 */
	public int getFilteredServiceNum() {
		return filteredServiceNum;
	}

	/**
	 * @param filteredServiceNum
	 *            the filteredServiceNum to set
	 */
	public void setFilteredServiceNum(int filteredServiceNum) {
		this.filteredServiceNum = filteredServiceNum;
	}

	/**
	 * @return the filteredReceiverNum
	 */
	public int getFilteredReceiverNum() {
		return filteredReceiverNum;
	}

	/**
	 * @param filteredReceiverNum
	 *            the filteredReceiverNum to set
	 */
	public void setFilteredReceiverNum(int filteredReceiverNum) {
		this.filteredReceiverNum = filteredReceiverNum;
	}

}
