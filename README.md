# Modified-ICCBot

## 环境准备 ##

1. Python 3+

2. Java 1.8+

## 使用 ##

ICCBot代码中使用了相对路径，需先切换到modified-ICCBot目录下

```
cd modified-ICCBot
```

在run.py中修改应用路径apkFile与输出结果路径resPath后，运行即可

```
python3 run.py
```

## 可选项 ##

在run.py中可修改ICCBot参数，包括限时、深度、所用client等

```

java -jar ICCBot.jar -h

usage: java -jar ICCBot.jar [options] [-path] [-name] [-androidJar] [-outputDir][-client]

 -h                     -h: Show the help information.
 -name <arg>            -name: Set the name of the apk under analysis.
 -path <arg>            -path: Set the path to the apk under analysis.
 -outputDir <arg>       -outputDir: Set the output folder of the apk.
 -client <arg>          -client
                         "CallGraphClient: Output call graph files."
                         "ManifestClient: Output manifest.xml file."
                         "IROutputClient: Output soot IR files."
                         "FragmentClient: Output the fragment loading results."
                         "CTGClient/MainClient: Resolve ICC and generate CTG."
                         "ICCSpecClient:  Report ICC specification for each component."

 -androidJar <arg>      -androidJar: Set the path of android.jar.
 -version <arg>         -version [default:23]: Version of Android SDK.
 -maxPathNumber <arg>   -maxPathNumber [default:10000]: Set the max number of paths.
 -time <arg>            -time [default:90]: Set the max running time (min).

 -noAdapter             -noAdapter: exclude super simple adapter model
 -noAsyncMethod         -noAsyncMethod: exclude async method call edge
 -noCallBackEntry       -noCallBackEntry: exclude the call back methods
 -noDynamicBC           -noDynamicBC: exclude dynamic broadcast receiver matching
 -noFragment            -noFragment: exclude fragment operation model
 -noFunctionExpand      -noFunctionExpand: do not inline function with useful contexts
 -noImplicit            -noImplicit: exclude implict matching
 -noLibCode             -noLibCode: exclude the activities not declared in app's package
 -noPolym               -noPolym: exclude polymorphism methods
 -noStaticField         -noStaticField: exclude string operation model
 -noStringOp            -noStringOp: exclude string operation model
 -noWrapperAPI          -noWrapperAPI: exclude RAICC model
 -onlyDummyMain         -onlyDummyMain: limit the entry scope

 -debug                 -debug: use debug mode.

 -targetClasses         指定所需分析的类名（们），为空则为默认的全局分析
```

## 输入 ##

apk文件

## 输出 ##

输出共分为三个目录:

+ fdlogs
  > Flowdroid运行日志

+ logs
  > ICCBot运行日志

+ output

  + CallGraphInfo
    > 应用原始 call graph
  + ManifestInfo
    > Extracted AndroidManifest file
  + FragmentInfo
    > Generated fragment loading graph
  + CTGResult
    > 生成的component transition graph、**IccModel.txt**等
  + SootIRInfo
    > 插桩ICC重定向语句后的 call graph 和重打包的apk文件

## 代码构成 ##

新版Flowdroid/IccTA : 
modified-ICCBot/lib/soot-infoflow-cmd-jar-with-dependencies.jar

ICCBot <- 新版Flowdroid/IccTA 部分 ：modified-ICCBot/src/main/java/client/cg/

ICCBot <- 间接类ICC的上下文敏感部分 ：modified-ICCBot/src/main/java/client/obj/target/ctg

ICCBot <- Fragment处理部分 ：modified-ICCBot/src/main/java/client/obj/target/fragment

ICCBot -> IccModel文件 -> 新版Flowdroid/IccTA 部分 ：modified-ICCBot/src/main/java/client/soot/GetApiGenClient.java
