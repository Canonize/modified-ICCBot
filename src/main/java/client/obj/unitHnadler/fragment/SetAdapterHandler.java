package main.java.client.obj.unitHnadler.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import heros.solver.Pair;
import main.java.analyze.model.analyzeModel.ObjectSummaryModel;
import main.java.analyze.model.sootAnalysisModel.Context;
import main.java.analyze.utils.SootUtils;
import main.java.client.obj.model.fragment.FragmentSummaryModel;
import main.java.client.obj.unitHnadler.UnitHandler;
import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.FieldRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraphFactory;
import soot.toolkits.scalar.SimpleLocalDefs;

public class SetAdapterHandler extends UnitHandler {
	Context context;
	ObjectSummaryModel objectModel;

	protected final SootClass scSupportFragment = Scene.v().getSootClassUnsafe("android.support.v4.app.Fragment");
	protected final SootClass scAndroidXFragment = Scene.v().getSootClassUnsafe("androidx.fragment.app.Fragment");

	protected final SootClass scSupportViewPager = Scene.v().getSootClassUnsafe("android.support.v4.view.ViewPager");
	protected final SootClass scAndroidXViewPager = Scene.v().getSootClassUnsafe("androidx.viewpager.widget.ViewPager");

	protected final SootClass scFragmentStatePagerAdapter = Scene.v()
			.getSootClassUnsafe("android.support.v4.app.FragmentStatePagerAdapter");
	protected final SootClass scFragmentPagerAdapter = Scene.v()
			.getSootClassUnsafe("android.support.v4.app.FragmentPagerAdapter");

	protected final SootClass scAndroidXFragmentStatePagerAdapter = Scene.v()
			.getSootClassUnsafe("androidx.fragment.app.FragmentStatePagerAdapter");
	protected final SootClass scAndroidXFragmentPagerAdapter = Scene.v()
			.getSootClassUnsafe("androidx.fragment.app.FragmentPagerAdapter");
	
	protected LoadingCache<SootField, List<Type>> arrayToContentTypes = CacheBuilder.newBuilder()
	.build(new CacheLoader<SootField, List<Type>>() {

		@Override
		public List<Type> load(SootField field) throws Exception {
			// Find all assignments to this field
			List<Type> typeList = new ArrayList<>();
			field.getDeclaringClass().getMethods().stream().filter(m -> m.isConcrete())
					.map(m -> m.retrieveActiveBody()).forEach(b -> {
						// Find all locals that reference the field
						Set<Local> arrayLocals = new HashSet<>();
						for (Unit u : b.getUnits()) {
							if (u instanceof AssignStmt) {
								AssignStmt assignStmt = (AssignStmt) u;
								Value rop = assignStmt.getRightOp();
								Value lop = assignStmt.getLeftOp();
								if (rop instanceof FieldRef && ((FieldRef) rop).getField() == field) {
									arrayLocals.add((Local) lop);
								} else if (lop instanceof FieldRef && ((FieldRef) lop).getField() == field) {
									arrayLocals.add((Local) rop);
								}
							}
						}

						// Find casts
						for (Unit u : b.getUnits()) {
							if (u instanceof AssignStmt) {
								AssignStmt assignStmt = (AssignStmt) u;
								Value rop = assignStmt.getRightOp();
								Value lop = assignStmt.getLeftOp();

								if (rop instanceof CastExpr) {
									CastExpr ce = (CastExpr) rop;
									if (arrayLocals.contains(ce.getOp()))
										arrayLocals.add((Local) lop);
									else if (arrayLocals.contains(lop))
										arrayLocals.add((Local) ce.getOp());
								}
							}
						}

						// Find the assignments to the array locals
						for (Unit u : b.getUnits()) {
							if (u instanceof AssignStmt) {
								AssignStmt assignStmt = (AssignStmt) u;
								Value rop = assignStmt.getRightOp();
								Value lop = assignStmt.getLeftOp();
								if (lop instanceof ArrayRef) {
									ArrayRef arrayRef = (ArrayRef) lop;
									if (arrayLocals.contains(arrayRef.getBase())) {
										Type t = rop.getType();
										if (t instanceof RefType)
											typeList.add(rop.getType());
									}
								}
							}
						}
					});
			return typeList;
		}

	});


	@Override
	public void handleSingleObject(ObjectSummaryModel singleObject) {
		this.objectModel = singleObject;
		this.handleSingleObject(new Context(), singleObject);
	}

	@Override
	public void handleSingleObject(Context context, ObjectSummaryModel singleObject) {
		this.context = context;
		try {
			analyzeMethodForViewPagers();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleSingleObject(Context oldContextwithRealValue, ObjectSummaryModel singleObject, Unit targetUnit) {
		this.oldContextwithRealValue = oldContextwithRealValue;
		singleObject.getDataHandleList().add(unit);
		this.targetUnit = targetUnit;
		try {
			analyzeMethodForViewPagers();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    /**
	 * Check whether a method registers a FragmentStatePagerAdapter to a ViewPager.
	 * This pattern is very common for tabbed apps.
	 *
	 * @param clazz
	 * @param method
	 *
	 */
	protected void analyzeMethodForViewPagers() {
		// We need at least one fragment base class
		if (scSupportViewPager == null && scAndroidXViewPager == null)
			return;
		// We need at least one class with a method to register a fragment
		if (scFragmentStatePagerAdapter == null && scAndroidXFragmentStatePagerAdapter == null
				&& scFragmentPagerAdapter == null && scAndroidXFragmentPagerAdapter == null)
			return;	

		// get argument
		Value pa = SootUtils.getInvokeExp(unit).getArg(0);
		if (!(pa.getType() instanceof RefType))
			return;
		RefType rt = (RefType) pa.getType();

		// check whether argument is of type FragmentStatePagerAdapter
		if (!safeIsType(pa, scFragmentStatePagerAdapter) && !safeIsType(pa, scAndroidXFragmentStatePagerAdapter)
				&& !safeIsType(pa, scFragmentPagerAdapter) && !safeIsType(pa, scAndroidXFragmentPagerAdapter))
			return;

		// now analyze getItem() to find possible Fragments
		SootMethod getItem = rt.getSootClass().getMethodUnsafe("android.support.v4.app.Fragment getItem(int)");
		if (getItem == null)
			getItem = rt.getSootClass().getMethodUnsafe("androidx.fragment.app.Fragment getItem(int)");
		if (getItem == null || !getItem.isConcrete())
			return;

		Body b = getItem.retrieveActiveBody();
		if (b == null)
			return;

		// iterate and add any returned Fragment classes
		for (Unit getItemUnit : b.getUnits()) {
			if (getItemUnit instanceof ReturnStmt) {
				ReturnStmt rs = (ReturnStmt) getItemUnit;
				Value rv = rs.getOp();
				Type type = rv.getType();
				if (type instanceof RefType) {
					SootClass rtClass = ((RefType) type).getSootClass();
					if (rv instanceof Local && (rtClass.getName().startsWith("android.")
							|| rtClass.getName().startsWith("androidx.")))
						analyzeFragmentCandidates(rs, getItem, (Local) rv);
					else
						checkAndAddFragment(rtClass);
				}
			}
		}
		
	}

	/**
	 * Attempts to find fragments that are not returned immediately, but that
	 * require a more complex backward analysis. This analysis is best-effort, we do
	 * not attempt to solve every possible case.
	 * 
	 * @param s The statement at which the fragment is returned
	 * @param m The method in which the fragment is returned
	 * @param l The local that contains the fragment
	 */
	private void analyzeFragmentCandidates(Stmt s, SootMethod m, Local l) {
		ExceptionalUnitGraph g = ExceptionalUnitGraphFactory.createExceptionalUnitGraph(m.getActiveBody());
		SimpleLocalDefs lds = new SimpleLocalDefs(g);

		List<Pair<Local, Stmt>> toSearch = new ArrayList<>();
		Set<Pair<Local, Stmt>> doneSet = new HashSet<>();
		toSearch.add(new Pair<>(l, s));

		while (!toSearch.isEmpty()) {
			Pair<Local, Stmt> pair = toSearch.remove(0);
			if (doneSet.add(pair)) {
				List<Unit> defs = lds.getDefsOfAt(pair.getO1(), pair.getO2());
				for (Unit def : defs) {
					if (def instanceof AssignStmt) {
						AssignStmt assignStmt = (AssignStmt) def;
						Value rop = assignStmt.getRightOp();
						if (rop instanceof ArrayRef) {
							ArrayRef arrayRef = (ArrayRef) rop;

							// Look for all assignments to the array
							toSearch.add(new Pair<>((Local) arrayRef.getBase(), assignStmt));
						} else if (rop instanceof FieldRef) {
							FieldRef fieldRef = (FieldRef) rop;
							try {
								List<Type> typeList = arrayToContentTypes.get(fieldRef.getField());
								typeList.stream().map(t -> ((RefType) t).getSootClass())
										.forEach(c -> checkAndAddFragment(c));
							} catch (ExecutionException e) {
								System.out.println(String.format("Could not load potential types for field %s"+
										fieldRef.getField().getSignature())+ e);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Checks whether the given value is of the type of the given class
	 * 
	 * @param val   The value to check
	 * @param clazz The class from which to get the type
	 * @return True if the given value is of the type of the given class
	 */
	private boolean safeIsType(Value val, SootClass clazz) {
		return clazz != null && Scene.v().getFastHierarchy().canStoreType(val.getType(), clazz.getType());
	}

	/**
	 * Registers a fragment that belongs to a given component
	 *
	 * @param componentClass The component (usually an activity) to which the
	 *                       fragment belongs
	 * @param fragmentClass  The fragment class
	 */
	protected void checkAndAddFragment(SootClass fragmentClass) {
		((FragmentSummaryModel)objectModel).getSetDestinationList().add(fragmentClass.getName());
		((FragmentSummaryModel)objectModel).getSendFragment2Start().add(unit);
	}
}
