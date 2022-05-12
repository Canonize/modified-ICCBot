import os
import sys
import shutil



def analyzeApk(apkFile, targetClass, resPath, sdk):

    logDir = resPath+"/logs"
    outputDir = resPath+"/output"
    if(not os.path.exists(logDir)): 
        os.makedirs(logDir) 
    if(not os.path.exists(outputDir)): 
        os.makedirs(outputDir) 
    try:
        apk = os.path.basename(apkFile)
        apkPath = os.path.dirname(apkFile)
        extraArgs = ""
        print("==============={}=============".format(apk))
        # print(apkPath)
        # os.system("java -Xms12g -Xmx24g -jar "+jarFile+"  -path "+ apkPath +" -name "+apk+" -androidJar "+ sdk +"/platforms "+ extraArgs +" -time 720 -maxPathNumber 100 -outputDir "+outputDir+" >> "+logDir+"/"+apk[:-4]+".txt")
        os.system("java -Xms12g -Xmx24g -jar "+jarFile+"  -path "+ apkPath +" -name "+apk+" -targetClass "+targetClass+" -androidJar "+ sdk +"/platforms "+ extraArgs +" -time 720 -maxPathNumber 100 -outputDir "+outputDir+" >> "+logDir+"/"+apk[:-4]+".txt")
        print("==============={}=============".format(apk))
    except:
        print("{} run error".format(apk))



if __name__ == '__main__' :
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/com.ctid.open.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/com.epoint.mobileframe.wssb.qinghai.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/com.ccb.fintech.app.productions.hnga.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/cn.hsa.app.apk"
    apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/com.hanweb.android.zhejiang.activity.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/com.jd.jrapp_6.1.90_410.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/com.guangdong.gov.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/cn.xuexi.android.apk"
    # apkFile = "/home/cqt/Auth_Risk_Analysis_tool/modefied-ICCBot/apk/ICCBotBench.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/SMSLogin.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/TwoHandlerTest.apk"
    targetClass = "com.alibaba.zjzwfw.account.ZWLoginActivityV3"
    # resPath = "/home/flash/singledetect/ICCBotOotputResult/otherNodeResult_4"
    # resPath = "/home/lw/Auth_Risk_Analysis_tool/Iccbot/icc_result_SaveIdNegative/"
    # resPath = "/home/flash/singledetect/ICCBotOotputResult/guangdongsootIR"
    resPath = "/home/cqt/Auth_Risk_Analysis_tool/ICCBot_result"


    # jarFile = "S_ICCBot_SaveIdNegative.jar"
    # jarFile = "C_ICCBot_ChangeSoot.jar"
    jarFile = "M_ICCBot_Merge.jar"

    
    os.system("mvn -f pom.xml package -q")
    if os.path.exists("target/ICCBot.jar"):
        print("Successfully build! generate jar-with-dependencies in folder target/")
        shutil.copy("target/ICCBot.jar", jarFile)
        print("copy jar to the root directory.")
    else:
        print("Fail to build! Please run \"mvn -f pom.xml package\" to see the detail info.")
    
    sdk = "lib/"    
    analyzeApk(apkFile,targetClass,resPath, sdk)
    
