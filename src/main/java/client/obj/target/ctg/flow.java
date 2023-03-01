package main.java.client.obj.target.ctg;

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

public class flow {
    //设置android的jar包目录
//    public final static String androidPlatformPath = "C:\\Users\\van\\AppData\\Local\\Android\\Sdk\\platforms";
//    public final static String androidPlatformPath = "D:\\android_sdk\\platforms";
    public final static String androidPlatformPath = "/home/flash/singledetect/platforms";
    //设置要分析的APK文件
    public final static String appDirPath = "/home/flash/bin/FlowDroid/FlowDroid/soot-infoflow-android/testAPKs";
    //设置结果输出的路径

    public final static String singleApk="/home/flash/laboratory/findmyphone/flowdroidtest/testspk/国家医保服务平台_cn.hsa.app.apk";
    //    public final static String singleApk="/home/flash/laboratory/findmyphone/flowdroidtest/testspk/新湘事成_com.ccb.fintech.app.productions.hnga.apk";
//    public final static String singleApk="/home/flash/bin/FlowDroid/FlowDroid/soot-infoflow-android/testAPKs/enriched1.apk";

    public static String basePath;
//    public final static String nodePath="/home/flash/singledetect/flowDroid_SPARK_1/nodeResult";
//    public final static String edgePath="/home/flash/singledetect/flowDroid_SPARK_1/edgeResult";
    public final static String nodePath=basePath+"/nodeResult";
    public final static String edgePath=basePath+"/edgeResult";
//    public final static String nodePath="/home/flash/singledetect/flowDroid_hsa";
//    public final static String edgePath="/home/flash/singledetect/flowDroid_hsa";
//    public final static String nodePath="D:\\laboratory\\singledetect\\test\\flowDroid_res\\nodeResult";
//    public final static String edgePath="D:\\laboratory\\singledetect\\test\\flowDroid_res\\edgeResult";
//    public final static String nodePath="D:\\laboratory\\singledetect\\test\\flowDroid_hsa";
//    public final static String edgePath="D:\\laboratory\\singledetect\\test\\flowDroid_hsa";


    //public final static String AnaPath = "F:\\Sample\\malapi";

    public final static String SenapiPath = "";
    static Object ob = new Object();

    private static Map<String,Boolean> visited = new HashMap<String,Boolean>();
    private static  Vector<String> file_vec =  new Vector<String>();
    private static  Vector<String> sen_api =  new Vector<String>();

    private static Map<String,String> node_map = new HashMap<String,String>();
    public static int globalIndex=1;


    public static void getfile(){
        File file = new File(appDirPath);
        File[] tempList = file.listFiles();
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                int lenname =tempList[i].getName().length();
                if(tempList[i].getName().substring(lenname-4).equals(".apk"))
                {
                    file_vec.addElement(tempList[i].getName());
                }

            }
        }
    }

    //apk路径，遍历模式(0只给出边，1给出所有路径)，输出位置
    public static void getapi(String appname,int mode,String AnaPath) {
        SetupApplication app = new SetupApplication(androidPlatformPath,appDirPath+File.separator+appname);
        soot.G.reset();
        //构造调用图，但是不进行数据流分析
        app.constructCallgraph();

        //SootMethod 获取函数调用图
        SootMethod entryPoint = app.getDummyMainMethod();
        CallGraph cg = Scene.v().getCallGraph();

        // System.out.println(apppath.substring(0,apppath.length()-4)+".txt");
        File oFile = new File(AnaPath+File.separator+appname.substring(0,appname.length()-4)+".txt");
        //可视化函数调用图

        switch (mode) {
            case 0:
                visit(cg,entryPoint,oFile,entryPoint.getSignature());
                break;
            case 1:
                visit1(cg,entryPoint,oFile,entryPoint.getSignature());
                break;

            default:
                break;
        }




    }





    public static void mulCgTest(String appname, String curnodepath, String curedgepath, InfoflowConfiguration.CallgraphAlgorithm cgg){
        SetupApplication app = new SetupApplication(androidPlatformPath,appname);
//        for(InfoflowConfiguration.CallgraphAlgorithm cgAlgo: InfoflowConfiguration.CallgraphAlgorithm.values()){
        final InfoflowConfiguration.CallgraphAlgorithm cgAlgo= cgg;
        SootConfigForAndroid sootconf = new SootConfigForAndroid(){
            @Override
            public void setSootOptions(Options options, InfoflowConfiguration config) {
                super.setSootOptions(options, config);
                Options.v().set_process_multiple_dex(true);
                Options.v().set_allow_phantom_refs(true);
                Options.v().set_whole_program(true);
                config.setCallgraphAlgorithm(cgAlgo);
                config.setMaxThreadNum(64);
//                config.getSourceSinkConfig().setLayoutMatchingMode(InfoflowConfiguration.LayoutMatchingMode.MatchAll);
            }
        };
        app.setSootConfig(sootconf);
        soot.G.reset();
        app.constructCallgraph();
        SootMethod entryPoint = app.getDummyMainMethod();
        CallGraph cg = Scene.v().getCallGraph();
        String basenameOfapp=appname.substring(appname.lastIndexOf(File.separator)+1,appname.length()-4);
        File oFile = new File(curedgepath+File.separator+basenameOfapp+"_"+cgAlgo.name()+"_edge.txt");
        visit(cg,entryPoint,oFile,entryPoint.getSignature());

        File nodeFile=new File(curnodepath+File.separator+basenameOfapp+"_"+cgAlgo.name()+"_node.txt");
        Iterator<Map.Entry<String,String>> entries = node_map.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry entry = entries.next();
            writerow(nodeFile,entry.getKey()+"\t"+entry.getValue());
        }


    }




    public static void getapi_gen(String appname,int mode,String curnodepath,String curedgepath,final String outputDir,final String iccModelPath) {
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
//                config.setCallgraphAlgorithm(InfoflowConfiguration.CallgraphAlgorithm.SPARK);

                // test option for same output jimple
//                Options.v().set_keep_line_number(true); // not work
//                Options.v().set_src_prec(6);
//                Options.v().set_keep_offset(false);
                config.setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination); // this works
//                Options.v().set_keep_offset(false); // not work
                Options.v().set_no_bodies_for_excluded(true);
                Options.v().set_src_prec(6);
                Options.v().set_keep_offset(false);
                Options.v().set_keep_line_number(true);
                Options.v().set_throw_analysis(3);
                Options.v().set_ignore_resolution_errors(true);
//                Options.v().setPhaseOption("jb", "use-original-names:true");
                Main.v().autoSetOptions();


                if(!iccModelPath.equals("0")) {
                    InfoflowAndroidConfiguration ifffss = (InfoflowAndroidConfiguration) config;
                    ifffss.getIccConfig().setIccModel(iccModelPath);
                }

//                Options.v().set_output_format(Options.output_format_class);
                Options.v().set_output_format(Options.output_format_jimple);
//                Options.v().set_output_dir("/home/flash/singledetect/sootOO");
                Options.v().set_output_dir(outputDir+File.separator+basenameOfapp);
                Options.v().set_no_writeout_body_releasing(true);
            }
        };
        app.setSootConfig(sootconf);

        soot.G.reset();

//        Scanner input = new Scanner(System.in);
//        String val=input.next();
//        System.out.println(val);

        //构造调用图，但是不进行数据流分析
        app.constructCallgraph();


//        val=input.next();
//        System.out.println(val);

        //SootMethod 获取函数调用图
        SootMethod entryPoint = app.getDummyMainMethod();
        CallGraph cg = Scene.v().getCallGraph();


        PackManager.v().writeOutput();


//        Scene.v().get

//        val=input.next();
//        System.out.println(val);

        // System.out.println(apppath.substring(0,apppath.length()-4)+".txt");
//        String basenameOfapp=appname.substring(appname.lastIndexOf("\\")+1,appname.length()-4);

//        System.out.println(basenameOfapp);
//        return;
        File oFile = new File(curedgepath+File.separator+basenameOfapp+"_edge.txt");
        //可视化函数调用图

        switch (mode) {
            case 0:
                visit(cg,entryPoint,oFile,entryPoint.getSignature());
                break;
            case 1:
                visit1(cg,entryPoint,oFile,entryPoint.getSignature());
                break;
            case 2:
                visit_Broad(cg,entryPoint,oFile,entryPoint.getSignature());
                break;

            default:
                break;
        }

        File nodeFile=new File(curnodepath+File.separator+basenameOfapp+"_node.txt");
        Iterator<Map.Entry<String,String>> entries = node_map.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry entry = entries.next();
            writerow(nodeFile,entry.getKey()+"\t"+entry.getValue());
        }
    }

    public static void outputSootFunc(String appname, final String outputDir) {
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
//                config.setCallgraphAlgorithm(InfoflowConfiguration.CallgraphAlgorithm.SPARK);

                // test option for same output jimple
//                Options.v().set_keep_line_number(true); // not work
//                Options.v().set_src_prec(6);
//                Options.v().set_keep_offset(false);
                config.setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination); // this works
//                Options.v().set_keep_offset(false); // not work
                Options.v().set_no_bodies_for_excluded(true);
                Options.v().set_src_prec(6);
                Options.v().set_keep_offset(false);
                Options.v().set_keep_line_number(true);
                Options.v().set_throw_analysis(3);
                Options.v().set_ignore_resolution_errors(true);
//                Options.v().setPhaseOption("jb", "use-original-names:true");
                Main.v().autoSetOptions();


//                Options.v().set_output_format(Options.output_format_class);
                Options.v().set_output_format(Options.output_format_jimple);
//                Options.v().set_output_dir("/home/flash/singledetect/sootOO");
                Options.v().set_output_dir(outputDir+File.separator+basenameOfapp);
                Options.v().set_no_writeout_body_releasing(true);
            }
        };
        app.setSootConfig(sootconf);
        PackManager.v().writeOutput();

    }


    private static void visit_Broad(CallGraph cg,SootMethod m,File oFile,String pString) {
//        Queue<SootMethod> toCheckList = new LinkedList<SootMethod>();
//        String identifier = m.getSignature();
//        visited.put(identifier, true);
//        toCheckList.offer(m);
//
//        if (!node_map.containsKey(identifier)){
//            node_map.put(identifier,String.valueOf(globalIndex));
//            globalIndex=globalIndex+1;
//        }
//
//        while(!toCheckList.isEmpty()){
//            SootMethod c=toCheckList.poll();
//            Iterator<MethodOrMethodContext> ctargets = new Targets(cg.edgesOutOf(m));
//
//        }

        for (Iterator<Edge> it = cg.iterator(); it.hasNext(); ) {
            Edge curEdge = it.next();
            SootMethod src=curEdge.src();
            String srcModi=src.getSignature();
            SootMethod dst=curEdge.tgt();
            String dstModi=dst.getSignature();

            if (!node_map.containsKey(srcModi)){
                node_map.put(srcModi,String.valueOf(globalIndex));
                globalIndex=globalIndex+1;
            }

            if (!node_map.containsKey(dstModi)){
                node_map.put(dstModi,String.valueOf(globalIndex));
                globalIndex=globalIndex+1;
            }

            writerow(oFile,node_map.get(srcModi)+"\t"+ node_map.get(dstModi));
        }
    }



    private static void visit(CallGraph cg,SootMethod m,File oFile,String pString){
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
                writerow(oFile,node_map.get(identifier)+"\t"+ identifier_child);

                if(!visited.containsKey(c.getSignature())){
                    //递归
                    visit(cg,c,oFile,pString);
                }
                //writerow(oFile,pString);
            }
        }
    }


    private static void visit1(CallGraph cg,SootMethod m,File oFile,String pString){
        //在soot中，函数的signature就是由该函数的类名，函数名，参数类型，以及返回值类型组成的字符串
        String identifier = m.getSignature();
        //记录是否已经处理过该点
        visited.put(identifier, true);
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
                pString = pString+"-->"+ c.getSignature();
                //writerow(oFile,delpar(identifier)+"-->"+ delpar(c.getSignature()));
                if(!visited.containsKey(c.getSignature())){
                    //递归
                    visit1(cg,c,oFile,pString);
                }
                writerow(oFile,pString);
            }
        }
    }


    private static void visit2(CallGraph cg,SootMethod m,File oFile,String pString){
        //在soot中，函数的signature就是由该函数的类名，函数名，参数类型，以及返回值类型组成的字符串
        String identifier = m.getSignature();
        //记录是否已经处理过该点
        visited.put(identifier, true);
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

                if(!visited.containsKey(c.getSignature())){
                    //递归
                    writerow(oFile,delpar(c.getSignature()));
                    visit2(cg,c,oFile,pString);
                }
            }
        }
    }




    private static void writerow (File ofile, String s) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(ofile,true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println(s);
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (Exception e) {
            // TODO: handle exception
        }

    }


    public static boolean judgesen(String api)
    {
        return true;
    }
    public static String delpar(String s)
    {
        int x = s.indexOf('(');
        int y = s.indexOf(')');
        String b = s.substring(0,x+1);
        String e = s.substring(y,s.length());
        s=b+e;
        x = s.indexOf(':');
        String r = s.substring(x+1,s.length()-1);
        return r;
    }

    public static String cutMethod(String s)
    {
        int x = s.indexOf(':');
        String e = s.substring(x+1,s.length()-1);
        return e;
    }

    public static void analyseOne(String apkpath,String curnodePath,String curedgePath,String sootOutputDir,String iccModelPath){
        getapi_gen(apkpath,0,curnodePath,curedgePath,sootOutputDir,iccModelPath);
    }


    public static void outputSoot(String apkpath,String outputSootPath){

    }



//    public static void traverseApk(String apkDir){
//        File file=new File(apkDir);
//        File[] fs=file.listFiles();
//        for (File f : fs) {
//            String curApkName= f.getAbsolutePath();
//            System.out.println(curApkName);
//            analyseOne(curApkName);
//        }
//    }

//    public static void traverseApk_multiThread(String apkDir){
//        File file=new File(apkDir);
//        File[] fs=file.listFiles();
//        for (File f : fs) {
//            String curApkName= f.getAbsolutePath();
//            System.out.println(curApkName);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    analyseOne(curApkName);
//                }
//            }).start();
//        }
//    }


//    public static void mulCgTest(String apkPath){
//        mulCgTest(apkPath,nodePath,edgePath);
//    }

    public static void mulCgTest_algo(String apkPath,int algo){
        InfoflowConfiguration.CallgraphAlgorithm cgg=InfoflowConfiguration.CallgraphAlgorithm.values()[algo];
        System.out.println(cgg.name());
        String testnodePath="/home/flash/singledetect/flowDroid_testResult";
        String testedgePath="/home/flash/singledetect/flowDroid_testResult";
        mulCgTest(apkPath,testnodePath,testedgePath, cgg);
    }


    public static void main(String[] args){
        // System.out.print(delpar("<org.json.JSONObject: java.lang.String getString(java.lang.String)>"));

//        analyseOne(singleApk);
//        analyseOne("D:\\laboratory\\singledetect\\case\\unpack_app\\cn.xuexi.android.apk");

//        traverseApk("/home/flash/singledetect/unpack_app");
        if(args.length<2){
            System.out.println("必须指定一个处理的apk文件");
            return;
        }

//        if(args.length<1){
//            System.out.println("必须指定一个算法");
//            return;
//        }

//        mulCgTest_algo("/home/flash/singledetect/unpack_app/cn.hsa.app.apk",4);

        String curbasePath=args[1];

        String sootOutputDir="/home/flash/singledetect/jimpleOutput/";


        String iccmodelPath="0";

        if(args.length==3) iccmodelPath=args[2];

        if(curbasePath.equals("#OutputSoot")){
            String outputSootPath=args[2];
            outputSootFunc(args[0],outputSootPath);
            return;
        }


        analyseOne(args[0],curbasePath+"/nodeResult",curbasePath+"/edgeResult",sootOutputDir,iccmodelPath);
//        mulCgTest(args[0]);
//        mulCgTest("D:\\laboratory\\singledetect\\case\\unpack_app\\cn.hsa.app.apk");

//        for(int i=0;i<7;i++)    System.out.println(InfoflowConfiguration.CallgraphAlgorithm.values()[i].name());


//        traverseApk_multiThread("/home/flash/singledetect/unpack_app");
//        traverseApk("D:\\laboratory\\singledetect\\case\\unpack_app");

//        File LOG = new File("/home/flash/laboratory/findmyphone/flowdroidtest/testresult/log.txt");
//        getfile();
////		  for(int i=0;i<file_vec.size();i++)
////		  {
////			  System.out.println(file_vec.elementAt(i));
////		  }
//        for(int i=0;i<file_vec.size();i++)
//        {
//            try {
//                getapi(file_vec.elementAt(i),0,"/home/flash/laboratory/findmyphone/flowdroidtest/testresult2");
//
//            } catch (Exception e) {
//                // TODO: handle exception
//                writerow(LOG, file_vec.elementAt(i));
//            }
//
//            System.out.println(i);
//            //提取api序列
//        }



    }


}