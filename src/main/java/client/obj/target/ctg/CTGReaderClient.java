package main.java.client.obj.target.ctg;

import java.io.File;
import java.io.IOException;

import main.java.Global;
import main.java.MyConfig;
import main.java.analyze.utils.ConstantUtils;
import main.java.client.BaseClient;
import main.java.client.obj.model.atg.ATGModel;
import main.java.client.obj.target.ctg.CTGReaderClient;
import main.java.client.related.gator.ATGReader;

import org.dom4j.DocumentException;

/**
 * Analyzer Class
 * 
 * @author hanada
 * @version 2.0
 */
public class CTGReaderClient extends BaseClient {

	/**
	 * analyze logic for single app
	 * 
	 * @return
	 */
	@Override
	protected void clientAnalyze() {
		ATGModel model = Global.v().getiCTGModel().getOptModelwithoutFrag();
		model.setATGFilePath(MyConfig.getInstance().getResultFolder() + Global.v().getAppModel().getAppName()
				+ File.separator + ConstantUtils.ICTGFOLDETR + Global.v().getAppModel().getAppName() + "_"
				+ ConstantUtils.ICTGOPT + ".txt");
		System.out.println(model.getATGFilePath());
		ATGReader reader = new ATGReader(model);
		reader.analyze();
		System.out.println("Successfully analyze with ICTGGraphClient.");
	}

	@Override
	public void clientOutput() throws IOException, DocumentException {
	}
}