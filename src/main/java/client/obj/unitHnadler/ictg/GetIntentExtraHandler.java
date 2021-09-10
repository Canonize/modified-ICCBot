package main.java.client.obj.unitHnadler.ictg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import main.java.analyze.model.analyzeModel.Attribute;
import main.java.analyze.model.analyzeModel.ObjectSummaryModel;
import main.java.analyze.utils.CollectionUtils;
import main.java.analyze.utils.SootUtils;
import main.java.analyze.utils.ValueObtainer;
import main.java.client.obj.model.component.BundleType;
import main.java.client.obj.model.component.ExtraData;
import main.java.client.obj.model.ictg.IntentSummaryModel;
import main.java.client.obj.target.ictg.ICTGAnalyzerHelper;
import main.java.client.obj.unitHnadler.UnitHandler;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.toolkits.scalar.UnitValueBoxPair;

public class GetIntentExtraHandler extends UnitHandler {
	IntentSummaryModel intentSummary;

	@Override
	public void handleSingleObject(ObjectSummaryModel singleObject) {
		this.intentSummary = (IntentSummaryModel) singleObject;
		this.intentSummary.getDataHandleList().add(unit);
		getExtraAPIAnalyze(unit); // extra
	}

	/**
	 * get key value pair of extra stmts
	 * 
	 * @param uses
	 * @param defs
	 * @param eug
	 * @param eug
	 * @param sm
	 * @param extra_unit_list
	 * @param clsSet
	 */
	void getExtraAPIAnalyze(Unit u) {
		// for each unit contains extras
		String type = ICTGAnalyzerHelper.getTypeOfIntentExtra(u.toString());
		Map<String, List<ExtraData>> param_list;
		param_list = getParamList(u);
		if (param_list == null)
			return;
		if (SootUtils.isBundleExtra(type)) {
			BundleType bundle_type = null;
			try {
				bundle_type = genBundleType(u);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			if (bundle_type == null) {
				param_list = null;
				return;
			}
			bundle_type.setType(type);
			for (Entry<String, List<ExtraData>> en : param_list.entrySet()) {
				for (ExtraData ed : en.getValue())
					ed.setType(bundle_type);
			}
		} else if (SootUtils.isIntentExtra(type)) {
			// TODO

		} else if (SootUtils.isParOrSerExtra(type)) {
			for (Entry<String, List<ExtraData>> en : param_list.entrySet()) {
				for (ExtraData ed : en.getValue()) {
					ed.setType(type);
					ed.setObjName(getObjectNameforReflection(u));
				}
			}
		} else {
			// String type
			for (Entry<String, List<ExtraData>> en : param_list.entrySet()) {
				for (ExtraData ed : en.getValue())
					ed.setType(type);
			}
		}
		// entry class
		// appModel.ops.addReceivedExtraDataItemToMap(methodName, u, className,
		// param_list);
		addExtraValue2Set(methodUnderAnalyze, u, methodSig, param_list, intentSummary.getGetExtrasCandidateList());
	}

	/**
	 * get param list
	 * 
	 * @param u
	 * @return
	 */
	public Map<String, List<ExtraData>> getParamList(Unit u) {
		Value var = null;
		try {
			var = getVarInExtraStmt(u);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		Map<String, List<ExtraData>> param_list = new HashMap<String, List<ExtraData>>();
		try {
			ValueObtainer vo = new ValueObtainer(methodSig, "");
			param_list = new HashMap<String, List<ExtraData>>();
			// same u -->multiple str
			List<ExtraData> eds = new ArrayList<ExtraData>();
			param_list.put(u.toString(), eds);
			if (var != null) {
				List<String> reslist = vo.getValueofVar(var, u, 0).getValues();
				for (String res : reslist) {
					ExtraData ed = new ExtraData();
					ed.setName(res);
					eds.add(ed);
				}
			} else {
				ExtraData ed = new ExtraData();
				eds.add(ed);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (param_list == null || param_list.size() == 0) {
			return null;
		}
		return param_list;
	}

	/**
	 * get object name of the par and ser objs
	 * 
	 * @param u
	 * @return
	 */
	private String getObjectNameforReflection(Unit u) {
		String res = "";
		List<UnitValueBoxPair> use_var_list = SootUtils.getUseOfLocal(methodSig, u);

		for (UnitValueBoxPair vb : use_var_list) {
			if (vb.getUnit().getDefBoxes().size() > 0 && vb.getUnit().getUseBoxes().size() > 0) {
				if (vb.getUnit().getUseBoxes().get(0).getValue() instanceof JCastExpr) {
					JCastExpr cast = (JCastExpr) vb.getUnit().getUseBoxes().get(0).getValue();
					if (cast.getCastType().toString().endsWith("[]"))
						res = cast.getCastType().toString().replace("[]", "");
					else
						res = cast.getCastType().toString();
				}
			}
			// else{
			// System.err.println("no left/right value  "+u.getClass().getName());
			// }
		}
		return res;
	}

	/**
	 * 
	 * @param u
	 * @return
	 * @throws Exception
	 */
	private Value getVarInExtraStmt(Unit u) throws Exception {
		Value res = null;
		if (u instanceof JAssignStmt) {
			JAssignStmt jas = (JAssignStmt) u;
			ValueBox ads = jas.rightBox;
			Value v = ads.getValue();
			if (v instanceof JVirtualInvokeExpr) {
				JVirtualInvokeExpr jvie = (JVirtualInvokeExpr) v;
				if (jvie.getArgCount() == 0)
					res = null;
				else
					res = jvie.getArg(0);
			}
		} else if (u instanceof JVirtualInvokeExpr) {
			JVirtualInvokeExpr jvie = (JVirtualInvokeExpr) u;
			if (jvie.getArgCount() == 0)
				res = null;
			else
				res = jvie.getArg(0);
		} else if (u instanceof JInvokeStmt) {
			JInvokeStmt jis = (JInvokeStmt) u;
			if (jis.getInvokeExpr().getArgCount() == 0)
				res = null;
			else
				res = jis.getInvokeExpr().getArg(0);
		}
		return res;
	}

	/**
	 * gen_bundle_type
	 * 
	 * @param u
	 * @return
	 * @throws Exception
	 */
	public BundleType genBundleType(Unit u) throws Exception {
		// TODO
		BundleType bt = new BundleType();
		List<UnitValueBoxPair> use_var_list = SootUtils.getUseOfLocal(methodSig, u);
		if (use_var_list == null)
			return bt;
		for (int i = 0; i < use_var_list.size(); i++) {
			Unit useUnit = use_var_list.get(i).getUnit();
			if (!ICTGAnalyzerHelper.isGetBundleExtraMethod(useUnit.toString()))
				continue;
			Map<String, List<ExtraData>> param_list = getParamList(useUnit);
			if (param_list == null)
				continue;
			String type = ICTGAnalyzerHelper.getTypeOfGetBundleExtra(useUnit.toString());

			if (!SootUtils.isBundleExtra(type) && !SootUtils.isExtrasExtra(type)) {
				for (Entry<String, List<ExtraData>> en : param_list.entrySet()) {
					for (ExtraData ed : en.getValue()) {
						ed.setType(type);
					}
					bt.put(en.getKey(), en.getValue());
				}
			} else {
				BundleType bundle_type = null;
				try {
					bundle_type = genBundleType(useUnit);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				if (bundle_type == null) {
					param_list = null;
					continue;
				}
				bundle_type.setType(type);
				for (Entry<String, List<ExtraData>> en : param_list.entrySet()) {
					for (ExtraData ed : en.getValue()) {
						ed.setType(bundle_type);
					}
					bt.put(en.getKey(), en.getValue());
				}
			}
		}
		return bt;
	}

	public void addExtraValue2Set(SootMethod sm, Unit u, String act_name, Map<String, List<ExtraData>> param_list,
			BundleType bundleType) {
		String res = "";
		for (Entry<String, List<ExtraData>> en : param_list.entrySet()) {
			bundleType.put(en.getKey(), en.getValue());
			for (ExtraData ed : en.getValue()) {
				res += ed.toString().trim();
			}
		}
		if (res.endsWith(","))
			res = res.substring(0, res.length() - 1);
		// key and value
		Attribute attr = new Attribute(sm.getSignature() + "," + u.toString() + "," + u.hashCode(), "extra", res,
				"equals");
		CollectionUtils.add_attribute_to_map(attr.getId(), attr, appModel.getUnit2Attribute());
	}

}
