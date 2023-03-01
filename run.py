# coding:UTF-8
import os
import sys
import shutil
import datetime
import subprocess


def analyzeApk(apkFile, targetClasses, resPath, sdk):

    logDir = resPath+"/logs"
    outputDir = resPath+"/output"
    fdLogDir = resPath+"/fdlogs"
    if(not os.path.exists(logDir)): 
        os.makedirs(logDir) 
    if(not os.path.exists(outputDir)): 
        os.makedirs(outputDir) 
    if(not os.path.exists(fdLogDir)): 
        os.makedirs(fdLogDir) 
    try:
        apk = os.path.basename(apkFile)
        apkPath = os.path.dirname(apkFile)
        extraArgs = ""
        print("==============={}=============".format(apk))
        # 修改ICCBot参数，限时、深度、所用client等
        if len(targetClasses) == 0:
            os.system("java -Xms12g -Xmx24g -Xss3m -Dorg.slf4j.simpleLogger.logFile="+fdLogDir+"/"+apk[:-4]+".txt"+" -jar "+jarFile+"  -path "+ apkPath +" -name "+apk+" -androidJar "+ sdk +"/platforms "+ extraArgs +" -time 60 -maxPathNumber 100 -client GetApiGenClient -outputDir "+outputDir+" >> "+logDir+"/"+apk[:-4]+".txt")
        else:
            os.system("java -Xms12g -Xmx24g -Xss3m -Dorg.slf4j.simpleLogger.logFile="+fdLogDir+"/"+apk[:-4]+".txt"+" -jar "+jarFile+"  -path "+ apkPath +" -name "+apk+" -targetClasses "+targetClasses+" -androidJar "+ sdk +"/platforms "+ extraArgs +" -time 60 -maxPathNumber 100 -client GetApiGenClient -cgAnalyzeGroup -outputDir "+outputDir+" >> "+logDir+"/"+apk[:-4]+".txt")
        print("==============={}=============".format(apk))
    except:
        print("{} run error".format(apk))


        
if __name__ == '__main__' :
    
    # 需分析的应用路径
    apkFile = "./apk/a2dp.Vol_133.apk"
    # 存放结果路径
    resPath = "./ICCBot_result"
    # andorid platforms路径
    sdk = "lib/"
    
    targetClasses = ""

    jarFile = "M_ICCBot_Merge.jar"
    apk_name = os.path.basename(apkFile)[:-4]

    s_time = datetime.datetime.now()

    os.system("mvn -f pom.xml package -q")
    if os.path.exists("target/ICCBot.jar"):
        print("Successfully build! generate jar-with-dependencies in folder target/")
        shutil.copy("target/ICCBot.jar", jarFile)
        print("copy jar to the root directory.")
    else:
        print("Fail to build! Please run \"mvn -f pom.xml package\" to see the detail info.")
       
    analyzeApk(apkFile,targetClasses,resPath, sdk)
    
    e_time = datetime.datetime.now()
    print(str((e_time-s_time).seconds)+" seconds")

    print("============="+apk_name+" finished =============")
    
