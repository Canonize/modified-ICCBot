package main.java.client.soot;

import soot.*;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.config.SootConfigForAndroid;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import main.java.MyConfig;

public class FlowDroidAnalyzer {
    //设置android的jar包目录
    // public final static String androidPlatformPath = "/home/flash/singledetect/platforms";
    public final static String androidPlatformPath = "lib/platforms";

    private static Map<String,Boolean> visited = new HashMap<String,Boolean>();

    private static Map<String,String> node_map = new HashMap<String,String>();
    public static int globalIndex=1;

    public void getapi_gen(String appname,String curnodepath,String curedgepath,final String outputDir,final String iccModelPath) {
        SetupApplication app = new SetupApplication(androidPlatformPath,appname);
        final String basenameOfapp=appname.substring(appname.lastIndexOf(File.separator)+1,appname.length()-4);

        SootConfigForAndroid sootconf = new SootConfigForAndroid(){
            @Override
            public void setSootOptions(Options options, InfoflowConfiguration config) {
                super.setSootOptions(options, config);
                Options.v().set_process_multiple_dex(true);
                Options.v().set_allow_phantom_refs(true);
                Options.v().set_whole_program(true);
                config.setCallgraphAlgorithm(InfoflowConfiguration.CallgraphAlgorithm.CHA);
                // config.setExcludeSootLibraryClasses(false);
			    // config.setIgnoreFlowsInSystemPackages(false);
                config.setTaintAnalysisEnabled(false);
//                config.setCallgraphAlgorithm(InfoflowConfiguration.CallgraphAlgorithm.SPARK);

                if(!iccModelPath.equals("0")) {
                    InfoflowAndroidConfiguration ifffss = (InfoflowAndroidConfiguration) config;
                    ifffss.getIccConfig().setIccModel(iccModelPath);
                }

                Options.v().set_output_format(Options.output_format_dex); // 14
                // set_android_api_version 22 to support multi dex output
                Options.v().set_android_api_version(22);
//                Options.v().set_output_format(Options.output_format_jimple);
//                Options.v().set_output_dir("/home/flash/singledetect/sootOO");
                // Options.v().set_output_dir(outputDir+File.separator+basenameOfapp);
                Options.v().set_output_dir(outputDir);
                Options.v().set_no_writeout_body_releasing(true);
            }
        };
        app.setSootConfig(sootconf);

        soot.G.reset();

        //构造调用图，但是不进行数据流分析
        app.getConfig().setTargetClasses(MyConfig.getInstance().getTargetClasses());
        System.out.println("TargetClasses : " + MyConfig.getInstance().getTargetClasses());
        app.getConfig().setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination);
        app.constructCallgraph();

        //SootMethod 获取函数调用图
        SootMethod entryPoint = app.getDummyMainMethod();
        CallGraph cg = Scene.v().getCallGraph();     

        //可视化函数调用图

        System.out.println("Writing cg.txt..."+cg.size());
        System.out.println("Writing to"+curedgepath);
        File oFile = new File(curedgepath+File.separator+basenameOfapp+"_edge.txt");

        try {
            if (!oFile.exists())
                oFile.createNewFile();
            FileWriter fw = new FileWriter(oFile,true);
            PrintWriter pw = new PrintWriter(fw);

            visit(cg,entryPoint,pw,entryPoint.getSignature(),0);

            pw.flush();
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Writing Exception!");
            e.printStackTrace(System.out);
        }

        File nodeFile=new File(curnodepath+File.separator+basenameOfapp+"_node.txt");

        Iterator<Map.Entry<String,String>> entries = node_map.entrySet().iterator();
        try {
            if (!nodeFile.exists())
                nodeFile.createNewFile();
            FileWriter fw = new FileWriter(nodeFile,true);
            PrintWriter pw = new PrintWriter(fw);
            while(entries.hasNext()){
                Map.Entry entry = entries.next();
                pw.println(entry.getKey()+"\t"+entry.getValue());
            }
            pw.flush();
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Writing Exception!");
            e.printStackTrace(System.out);
        }
        Scene.v().removeClass(Scene.v().getSootClass("dummyMainClass"));
        //app.removeSimulatedCodeElements();
        System.out.println("Writing dex output...");
        PackManager.v().writeOutput();
        
    }

    private void visit(CallGraph cg,SootMethod m,PrintWriter oFile,String pString,int curDepth){
        //在soot中，函数的signature就是由该函数的类名，函数名，参数类型，以及返回值类型组成的字符串
        String identifier = m.getSignature();

        // 判断该点是否已被访问,是的话直接return
//        if(visited.get(identifier)) return;

        //记录处理过该点
        visited.put(identifier, true);

        if (!node_map.containsKey(identifier)){
            node_map.put(identifier,String.valueOf(globalIndex));
            globalIndex=globalIndex+1;
        }
        //以函数的signature为label在图中添加该节点
        //获取该函数调用的函数
        Iterator<MethodOrMethodContext> ctargets = new Targets(cg.edgesOutOf(m));
        if(ctargets != null){
            while(ctargets.hasNext())
            {
                SootMethod c = (SootMethod) ctargets.next();
                if(c == null){
                    System.out.println("c is null");
                }
                //将被调用的函数加入图中

                //添加一条指向该被调函数的边
                //pString = pString+"-->"+ c.getSignature();
                String identifier_child=c.getSignature();
                if (!node_map.containsKey(identifier_child)){
                    node_map.put(identifier_child,String.valueOf(globalIndex));
                    globalIndex=globalIndex+1;
                }
                identifier_child=node_map.get(identifier_child);
                oFile.println(node_map.get(identifier)+"\t"+ identifier_child);


                if(curDepth<5000) {
                    if (!visited.containsKey(c.getSignature())) {
                        //递归
                        try {
                            visit(cg, c, oFile, pString, curDepth + 1);
                        }
                        catch (Exception e){
                            System.out.println("curDepth: "+curDepth);
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                }
                //writerow(oFile,pString);
            }
        }
    }

    // private void writerow (File ofile, String s) {
    //     FileWriter fw = null;
    //     try {
    //         fw = new FileWriter(ofile,true);
    //     } catch (IOException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //     }
    //     PrintWriter pw = new PrintWriter(fw);
    //     pw.println(s);
    //     pw.flush();
    //     try {
    //         fw.flush();
    //         pw.close();
    //         fw.close();
    //     } catch (Exception e) {
    //         // TODO: handle exception
    //     }

    // }

    public void analyseOne(String apkpath,String sootOutputDir,String iccModelPath){
        getapi_gen(apkpath,sootOutputDir,sootOutputDir,sootOutputDir,iccModelPath);
    }

}