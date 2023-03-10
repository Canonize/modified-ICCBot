package main.java;

import main.java.MyConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * config information for current run
 * 
 * @author 79940
 *
 */
public class MyConfig {

	private MyConfig() {
	}

	private boolean isJimple = true;
	private boolean testGeneration = false;
	private boolean writeSootOutput = false;
	//+++
	private List<String> targetClasses = new ArrayList<String>();
	private String androidVersion;
	private String resultFolder;
	private String resultWarpperFolder;
	private String srcFolder;
	private String appName;
	private String appPath;
	private String client;
	private String gatorClient;
	private int timeLimit;
	private int maxPathNumber;
	private String androidJar;
	private boolean stopFlag = false;

	private boolean isSootAnalyzeFinish;
	private boolean isManifestClientFinish;
	private boolean isFragemenClientFinish;
	private boolean isCallGraphClientFinish;
	private boolean isCTGClientFinish;
	private boolean isLayoutAnalyzeFinish;
	private boolean isStaitiucValueAnalyzeFinish;
	private boolean isOracleConstructionClientFinish;
	private Switch mySwithch = new Switch();

	private static class SingletonInstance {
		private static final MyConfig INSTANCE = new MyConfig();
	}

	public static MyConfig getInstance() {
		return SingletonInstance.INSTANCE;
	}

	/**
	 * @return the mySwithch
	 */
	public Switch getMySwithch() {
		return mySwithch;
	}

	/**
	 * @param mySwithch
	 *            the mySwithch to set
	 */
	public void setMySwithch(Switch mySwithch) {
		this.mySwithch = mySwithch;
	}

	public boolean isJimple() {
		return isJimple;
	}

	public void setJimple(boolean isJimple) {
		this.isJimple = isJimple;
	}

	public boolean isTestGeneration() {
		return testGeneration;
	}

	public void setTestGeneration(boolean testGeneration) {
		this.testGeneration = testGeneration;
	}

	public boolean isWriteSootOutput() {
		return writeSootOutput;
	}

	public void setWriteSootOutput(boolean writeSootOutput) {
		this.writeSootOutput = writeSootOutput;
	}

	public String getAndroidVersion() {
		return androidVersion;
	}

	public void setAndroidVersion(String androidVersion) {
		this.androidVersion = androidVersion;
	}

	public String getResultFolder() {
		return resultFolder;
	}

	public void setResultFolder(String resultFolder) {
		this.resultFolder = resultFolder;
	}

	public String getSrcFolder() {
		return srcFolder;
	}

	public void setSrcFolder(String srcFolder) {
		this.srcFolder = srcFolder;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppPath() {
		return appPath;
	}

	public void setAppPath(String appPath) {
		this.appPath = appPath;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public int getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}

	/**
	 * @return the androidJar
	 */
	public String getAndroidJar() {
		return androidJar;
	}

	/**
	 * @param androidJar
	 *            the androidJar to set
	 */
	public void setAndroidJar(String androidJar) {
		this.androidJar = androidJar;
	}

	/**
	 * @return the maxPathNumber
	 */
	public int getMaxPathNumber() {
		return maxPathNumber;
	}

	/**
	 * @param maxPathNumber
	 *            the maxPathNumber to set
	 */
	public void setMaxPathNumber(int maxPathNumber) {
		this.maxPathNumber = maxPathNumber;
	}

	/**
	 * @return the isManifestAnalyzeFinish
	 */
	public boolean isManifestAnalyzeFinish() {
		return isManifestClientFinish;
	}

	/**
	 * @param isManifestAnalyzeFinish
	 *            the isManifestAnalyzeFinish to set
	 */
	public void setManifestAnalyzeFinish(boolean isManifestAnalyzeFinish) {
		this.isManifestClientFinish = isManifestAnalyzeFinish;
	}

	/**
	 * @return the isFragementAnalyzeFinish
	 */
	public boolean isFragementAnalyzeFinish() {
		return isFragemenClientFinish;
	}

	/**
	 * @param isFragementAnalyzeFinish
	 *            the isFragementAnalyzeFinish to set
	 */
	public void setFragementAnalyzeFinish(boolean isFragementAnalyzeFinish) {
		this.isFragemenClientFinish = isFragementAnalyzeFinish;
	}

	/**
	 * @return the isCallGraphAnalyzeFinish
	 */
	public boolean isCallGraphAnalyzeFinish() {
		return isCallGraphClientFinish;
	}

	/**
	 * @param isCallGraphAnalyzeFinish
	 *            the isCallGraphAnalyzeFinish to set
	 */
	public void setCallGraphAnalyzeFinish(boolean isCallGraphAnalyzeFinish) {
		this.isCallGraphClientFinish = isCallGraphAnalyzeFinish;
	}

	/**
	 * @return the isCTGAnalyzeFinish
	 */
	public boolean isCTGAnalyzeFinish() {
		return isCTGClientFinish;
	}

	/**
	 * @param isCTGAnalyzeFinish
	 *            the isCTGAnalyzeFinish to set
	 */
	public void setCTGAnalyzeFinish(boolean isCTGAnalyzeFinish) {
		this.isCTGClientFinish = isCTGAnalyzeFinish;
	}

	/**
	 * @return the isLayoutAnalyzeFinish
	 */
	public boolean isLayoutAnalyzeFinish() {
		return isLayoutAnalyzeFinish;
	}

	/**
	 * @param isLayoutAnalyzeFinish
	 *            the isLayoutAnalyzeFinish to set
	 */
	public void setLayoutAnalyzeFinish(boolean isLayoutAnalyzeFinish) {
		this.isLayoutAnalyzeFinish = isLayoutAnalyzeFinish;
	}

	/**
	 * @return the isOracleConstructionClientFinish
	 */
	public boolean isOracleConstructionClientFinish() {
		return isOracleConstructionClientFinish;
	}

	/**
	 * @param isOracleConstructionClientFinish
	 *            the isOracleConstructionClientFinish to set
	 */
	public void setOracleConstructionClientFinish(boolean isOracleConstructionClientFinish) {
		this.isOracleConstructionClientFinish = isOracleConstructionClientFinish;
	}

	/**
	 * @return the isStaitiucValueAnalyzeFinish
	 */
	public boolean isStaitiucValueAnalyzeFinish() {
		return isStaitiucValueAnalyzeFinish;
	}

	/**
	 * @param isStaitiucValueAnalyzeFinish
	 *            the isStaitiucValueAnalyzeFinish to set
	 */
	public void setStaitiucValueAnalyzeFinish(boolean isStaitiucValueAnalyzeFinish) {
		this.isStaitiucValueAnalyzeFinish = isStaitiucValueAnalyzeFinish;
	}

	/**
	 * @return the isSootAnalyzeFinish
	 */
	public boolean isSootAnalyzeFinish() {
		return isSootAnalyzeFinish;
	}

	/**
	 * @param isSootAnalyzeFinish
	 *            the isSootAnalyzeFinish to set
	 */
	public void setSootAnalyzeFinish(boolean isSootAnalyzeFinish) {
		this.isSootAnalyzeFinish = isSootAnalyzeFinish;
	}

	/**
	 * @return the stopFlag
	 */
	public boolean isStopFlag() {
		return stopFlag;
	}

	/**
	 * @param stopFlag
	 *            the stopFlag to set
	 */
	public void setStopFlag(boolean stopFlag) {
		this.stopFlag = stopFlag;
	}

	/**
	 * @return the resultWarpperFolder
	 */
	public String getResultWarpperFolder() {
		return resultWarpperFolder;
	}

	/**
	 * @param resultWarpperFolder the resultWarpperFolder to set
	 */
	public void setResultWarpperFolder(String resultWarpperFolder) {
		this.resultWarpperFolder = resultWarpperFolder;
	}

	/**
	 * @return the gatorClient
	 */
	public String getGatorClient() {
		return gatorClient;
	}

	/**
	 * @param gatorClient the gatorClient to set
	 */
	public void setGatorClient(String gatorClient) {
		this.gatorClient = gatorClient;
	}
	//+++
	/**
	* Gets the target Class on which the data flow analysis shall be conducted
	* 
	* @return The target Class on which the data flow analysis shall be
	*         conducted
	*/
	public List<String> getTargetClasses() {
			return targetClasses;
		}

	/**
	* Sets the target Class on which the data flow analysis shall be conducted
	* 
	* @param targetClass The target Class on which the data flow analysis
	*                      shall be conducted
	*/
	public void setTargetClasses(List<String> targetClasses) {
		this.targetClasses = targetClasses;
	}
}