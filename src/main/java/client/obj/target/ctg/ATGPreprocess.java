package main.java.client.obj.target.ctg;

import java.util.Map;

import main.java.Analyzer;
import main.java.Global;
import main.java.analyze.utils.RAICCUtils;
import main.java.analyze.utils.SootUtils;
import main.java.client.obj.target.ctg.ATGPreprocess;
import soot.Body;
import soot.PatchingChain;
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
public class ATGPreprocess extends Analyzer {

	@Override
	public void analyze() {
		Map<String, String> method2InstructionMap = Global.v().getAppModel().getMethod2InstructionMap();
		for (SootClass sc : Scene.v().getApplicationClasses()) {
			for (SootMethod sm : sc.getMethods()) {
				Body body = SootUtils.getSootActiveBody(sm);
				if (body == null)
					continue;
				PatchingChain<Unit> units = body.getUnits();
				int i = 0;
				for (Unit u : units) {
					if (CTGAnalyzerHelper.isSendIntent2IccMethod(u) || RAICCUtils.isWrapperMethods(u)) {
						String res = method2InstructionMap.get(sm.getSignature());
						if (res == null) {
							res = i + "";
							method2InstructionMap.put(sm.getSignature(), res);
						} else {
							res += "," + i;
							method2InstructionMap.put(sm.getSignature(), res);
						}
					}
					i++;
				}

			}
		}
		System.out.println("ATGPreprocess finish\n");
	}

}