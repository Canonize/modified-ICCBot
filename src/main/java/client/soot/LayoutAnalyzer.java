package main.java.client.soot;

import main.java.Analyzer;
import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.java.Analyzer;
import main.java.Global;
import main.java.MyConfig;
import main.java.analyze.utils.ConstantUtils;
import main.java.analyze.utils.SootUtils;
import main.java.analyze.utils.ValueObtainer;
import main.java.analyze.utils.output.FileUtils;
import main.java.client.soot.SootAnalyzer;
import main.java.client.BaseClient;
import main.java.client.obj.target.ctg.StaticValueAnalyzer;
import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.FieldRef;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowConfiguration.CallgraphAlgorithm;
import soot.jimple.infoflow.InfoflowConfiguration.CodeEliminationMode;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.callbacks.AndroidCallbackDefinition;
import soot.jimple.infoflow.android.resources.ARSCFileParser;
import soot.jimple.infoflow.android.resources.LayoutFileParser;
import soot.jimple.infoflow.android.resources.ARSCFileParser.AbstractResource;
import soot.jimple.infoflow.android.resources.ARSCFileParser.StringResource;
import soot.jimple.infoflow.android.resources.controls.AndroidLayoutControl;
import soot.jimple.infoflow.util.SootMethodRepresentationParser;
import soot.jimple.infoflow.util.SystemClassHandler;
import soot.jimple.infoflow.values.IValueProvider;
import soot.jimple.infoflow.values.SimpleConstantValueProvider;
import soot.options.Options;
import soot.tagkit.IntegerConstantValueTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraphFactory;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
//+++
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class LayoutAnalyzer extends Analyzer {
    
    IValueProvider valueProvider = new SimpleConstantValueProvider();
    MultiMap<SootClass, Integer> layoutClasses = new HashMultiMap<>();
    
	@Override
	public void analyze() {
        try {
            drawLayoutText();
            System.exit(0);
        } catch (XMLStreamException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
	 * if dynamically setText() for a layout, add an attribute to its xml
	 */
	public void drawLayoutText() throws XMLStreamException, IOException {
		if (MyConfig.getInstance().getMySwithch().drawLayoutTextSwitch()) {

			findClassLayoutMappings();

			System.out.println("Mapping: " + layoutClasses);
			
			ARSCFileParser resParser = new ARSCFileParser();
			try {
				resParser.parse(appModel.getAppPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			appModel.setResParser(resParser);
			LayoutFileParser layoutParser = new LayoutFileParser(appModel.getPackageName(), resParser);
			layoutParser.parseLayoutFileDirect(appModel.getAppPath());

			for (SootClass sc : Scene.v().getApplicationClasses()) {
				MultiMap<SootField, Integer> layoutId = new HashMultiMap<>();
				Map<SootField, String> layoutText = new HashMap<>();

				for (SootMethod sMethod : sc.getMethods()) {
					if (!sMethod.isConcrete())
						continue;
					Body body = sMethod.retrieveActiveBody();
					Iterator<Unit> unitsIterator = body.getUnits().snapshotIterator();
					System.out.println("Method: " + sMethod);
					while(unitsIterator.hasNext()) {  
						//将Unit强制转换为Stmt,Stmt为Jimple里每条语句的表示   
						Unit unit = unitsIterator.next();
						Stmt stmt=(Stmt)unit;
						
						System.out.println("#: " + stmt);
						
						if (stmt instanceof AssignStmt) {
							AssignStmt assign = (AssignStmt) stmt;
							
							if (assign.getLeftOp() instanceof FieldRef && assign.getRightOp() instanceof Local) {
								SootField field = ((FieldRef) assign.getLeftOp()).getField();
								if(field.getType().toString().startsWith("android.widget")) {
									System.out.println("#lop: " + stmt);
									
									List<Unit> defChain = SootUtils.getDefOfLocal(sMethod.getSignature(), assign.getRightOp(), unit);
									System.out.println("@defchain: " + defChain);
									Integer id = findIdDef(sMethod, defChain.get(0));
									if(defChain != null && id != null) 
										layoutId.put(field, id);
								
								}
								
							}

							if (assign.getRightOp() instanceof FieldRef && assign.getLeftOp() instanceof Local) {
								SootField field = ((FieldRef) assign.getRightOp()).getField();
								if(field != null && field.getType().toString().startsWith("android.widget")) {
									System.out.println("#rop: " + stmt);
									
									List<UnitValueBoxPair> useChain = SootUtils.getUseOfLocal(sMethod.getSignature(), unit);
									System.out.println("@usechain: " + useChain);
									String text = findTextUse(sMethod, useChain.get(0).getUnit());
									if(useChain != null && text != null)
										layoutText.put(field, text);
									
								
								}
								
							}
								
						}
							
					}

				}
				System.out.println("Class: " + sc);
				System.out.println("IDMap: " + layoutId);
				System.out.println("TextMap: " + layoutText);

					
				for(SootField layout : layoutText.keySet()) {
					String text = layoutText.get(layout);
					if(text == null || layout == null)
							continue;
					for(Integer intValue : layoutId.get(layout)) {
						if(intValue == null)
							continue;
						Set<Integer> classIds = layoutClasses.get(sc);
						for (Integer classId : classIds) {
							AbstractResource resource = resParser.findResource(classId);
							AbstractResource resourceId = resParser.findResource(intValue);
							String type = layout.getType().toString();
							String tag = type.substring(type.lastIndexOf('.') + 1, type.length());
							System.out.println("@tag: " + tag);
							System.out.println("@resourceId: " + resourceId.getResourceName());

							if (resource instanceof StringResource) {
								String layoutFileName = ((StringResource) resource).getValue();
								
								String fileName = "/home/cqt/Auth_Risk_Analysis_tool/output/decomposed/" + Global.v().getAppModel().getAppName() + File.separator
								+ layoutFileName;
								System.out.println("FilePath: " + fileName);
								
								try {
									rewriteXML(fileName, tag, resourceId.getResourceName(), text);
								} catch (TransformerException | ParserConfigurationException | SAXException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								
							}
						}
						
					}
				}
					
			}
		}
    } 

	public void rewriteXML(String file, String tagName, String id, String text) throws TransformerException, ParserConfigurationException, SAXException, IOException {
		// Load XML from file
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
		if(!FileUtils.isFileExist(file))
			return;
		Document document = domBuilder.parse(file);

		// Modify DOM tree (simple version)
		NodeList rowNodes = document.getElementsByTagName(tagName);
		for (int i = 0; i < rowNodes.getLength(); i++) {

			// Add text content
			Node androidId = rowNodes.item(i).getAttributes().getNamedItem("android:id");
    		if(androidId!= null && androidId.getNodeValue().contains(id)) {
				
				Element element = (Element) rowNodes.item(i);
				element.setAttribute("android:text", text);
				System.out.println("Success insert: " + id);
			}

    		
		}

		// Save XML to file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(document),
							new StreamResult(file));
	}

    Iterator<MethodOrMethodContext> rmIterator;
	/**
	 * Finds the mappings between classes and their respective layout files
	 */
	private void findClassLayoutMappings() {
		for (SootClass sc : Scene.v().getApplicationClasses()) {
			for (SootMethod sm : sc.getMethods()) {

				if (!sm.isConcrete())
					continue;
				if (SystemClassHandler.v().isClassInSystemPackage(sm.getDeclaringClass().getName()))
					continue;
				RefType fragmentType = RefType.v("android.app.Fragment");
				for (Unit u : sm.retrieveActiveBody().getUnits()) {
					if (u instanceof Stmt) {
						Stmt stmt = (Stmt) u;
						if (stmt.containsInvokeExpr()) {
							InvokeExpr inv = stmt.getInvokeExpr();
							if (invokesSetContentView(inv)) { // check
																// also
																// for
																// inflate
																// to
																// look
																// for
																// the
																// fragments
								for (Value val : inv.getArgs()) {
									Integer intValue = valueProvider.getValue(sm, stmt, val, Integer.class);
									if (intValue != null) {
										this.layoutClasses.put(sm.getDeclaringClass(), intValue);
									}

								}
							}
							if (invokesInflate(inv)) {
								Integer intValue = valueProvider.getValue(sm, stmt, inv.getArg(0), Integer.class);
								if (intValue != null) {
									this.layoutClasses.put(sm.getDeclaringClass(), intValue);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Checks whether this invocation calls Android's Activity.setContentView method
	 *
	 * @param inv The invocaton to check
	 * @return True if this invocation calls setContentView, otherwise false
	 */
	public boolean invokesSetContentView(InvokeExpr inv) {
		String methodName = SootMethodRepresentationParser.v()
				.getMethodNameFromSubSignature(inv.getMethodRef().getSubSignature().getString());
		if (!methodName.equals("setContentView"))
			return false;

		// In some cases, the bytecode points the invocation to the current
		// class even though it does not implement setContentView, instead
		// of using the superclass signature
		SootClass curClass = inv.getMethod().getDeclaringClass();
		while (curClass != null) {
			final String curClassName = curClass.getName();
			if (curClassName.equals("android.app.Activity")
					|| curClassName.equals("android.support.v7.app.ActionBarActivity")
					|| curClassName.equals("android.support.v7.app.AppCompatActivity")
					|| curClassName.equals("androidx.appcompat.app.AppCompatActivity"))
				return true;
			// As long as the class is subclass of android.app.Activity,
			// it can be sure that the setContentView method is what we expected.
			// Following 2 statements make the overriding of method
			// setContentView ignored.
			// if (curClass.declaresMethod("void setContentView(int)"))
			// return false;
			curClass = curClass.hasSuperclass() ? curClass.getSuperclass() : null;
		}
		return false;
	}

	/**
	 * Checks whether this invocation calls Android's LayoutInflater.inflate method
	 *
	 * @param inv The invocaton to check
	 * @return True if this invocation calls inflate, otherwise false
	 */
	public boolean invokesInflate(InvokeExpr inv) {
		String methodName = SootMethodRepresentationParser.v()
				.getMethodNameFromSubSignature(inv.getMethodRef().getSubSignature().getString());
		if (!methodName.equals("inflate"))
			return false;

		// In some cases, the bytecode points the invocation to the current
		// class even though it does not implement setContentView, instead
		// of using the superclass signature
		SootClass curClass = inv.getMethod().getDeclaringClass();
		while (curClass != null) {
			final String curClassName = curClass.getName();
			if (curClassName.equals("android.app.Fragment") || curClassName.equals("android.view.LayoutInflater"))
				return true;
			curClass = curClass.hasSuperclass() ? curClass.getSuperclass() : null;
		}
		return false;
	}

	

	public Integer findIdDef(SootMethod sootMethod, Unit unit) {

		String methodSig = sootMethod.getSignature();
		
		Stmt stmt = (Stmt)unit;
		if(stmt.containsInvokeExpr()) {
			InvokeExpr invokeExpr = stmt.getInvokeExpr();

			if(invokesInflate(invokeExpr) || invokeExpr.getMethodRef().getSignature().contains("android.view.View findViewById(int)")) {
				Integer id = valueProvider.getValue(sootMethod, stmt, invokeExpr.getArg(0), Integer.class);

				System.out.println("%invoke: " + stmt);
				System.out.println("%arg: " + id);
				return id;
			}
		}
		else if (stmt instanceof AssignStmt) {
			
			System.out.println("%assign: " + stmt);
			AssignStmt assign = (AssignStmt) stmt;
			Value rop = assign.getRightOp();
			List<Unit> dc = SootUtils.getDefOfLocal(methodSig, rop, unit);

			if (rop instanceof CastExpr) {
				CastExpr ce = (CastExpr) rop;
				dc = SootUtils.getDefOfLocal(methodSig, (Local)ce.getOp(), unit);
			}
			
			System.out.println("@dc: " + dc);
			
			if(!dc.isEmpty())
				return findIdDef(sootMethod, dc.get(0));
			
		}
		
		return null;
	}

	public String findTextUse(SootMethod sootMethod, Unit unit) {

		String methodSig = sootMethod.getSignature();
		
		Stmt stmt = (Stmt)unit;
		if(stmt.containsInvokeExpr()) {
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			String string = invokeExpr.getMethod().getSignature();

			//对SetText进行检测
			if(string.startsWith("<android.widget.TextView: void setText(java.lang.CharSequence)>")) {

				Value value = invokeExpr.getArg(0);

				ValueObtainer vo = new ValueObtainer(sootMethod.getSignature(), "");
				List<String> text = vo.getValueofVar(value, stmt, 0).getValues();

				System.out.println("%invoke: " + stmt);
				System.out.println("%text: " + text);
				if(text.contains("登录"))
					return "登录";
				else if(!text.isEmpty())
					return text.get(0);
			}
		}
		else if (stmt instanceof AssignStmt) {
			
			System.out.println("%assign: " + stmt);

			List<UnitValueBoxPair> dc = SootUtils.getUseOfLocal(methodSig, unit);
			
			System.out.println("@dc: " + dc);
			if(!dc.isEmpty())
				return findTextUse(sootMethod, dc.get(0).getUnit());
			
		}
		
		return null;
		
	}

    
}
