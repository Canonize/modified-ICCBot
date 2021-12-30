import os
import sys
import shutil



def analyzeApk(apkPath, resPath, sdk):

    logDir = resPath+"/logs"
    outputDir = resPath+"/output"
    if(not os.path.exists(logDir)): 
        os.makedirs(logDir) 
    if(not os.path.exists(outputDir)): 
        os.makedirs(outputDir) 
        
    if(os.path.exists(apkPath)): 
        apks = os.listdir(apkPath)
        extraArgs = ""
        for apk in apks:
            if apk[-4:] ==".apk":
                # print("java -jar "+jarFile+"  -path "+ apkPath +" -name "+apk+" -androidJar "+ sdk +"/platforms  "+ extraArgs +" -time 30 -maxPathNumber 100 -client MainClient  -outputDir "+outputDir+" >> "+logDir+"/"+apk[:-4]+".txt")
                # print("java -jar "+jarFile+"  -path "+ apkPath +" -name "+apk+" -androidJar "+ sdk +"/platforms  "+ extraArgs +" -time 30 -maxPathNumber 100 -outputDir "+outputDir+" >> "+logDir+"/"+apk[:-4]+".txt")
                # os.system("java -jar "+jarFile+"  -path "+ apkPath +" -name "+apk+" -androidJar "+ sdk +"/platforms "+ extraArgs +" -time 60 -maxPathNumber 100 -client MainClient -outputDir "+outputDir+" >> "+logDir+"/"+apk[:-4]+".txt")
                try:
                    print("==============={}=============".format(apk))
                    # os.system("open")
                    os.system("java -Xms12g -Xmx24g -jar "+jarFile+"  -path "+ apkPath +" -name "+apk+" -androidJar "+ sdk +"/platforms "+ extraArgs +" -time 360 -maxPathNumber 100 -outputDir "+outputDir+" >> "+logDir+"/"+apk[:-4]+".txt")
                    print("==============={}=============".format(apk))
                except:
                    print("{} run error".format(apk))



if __name__ == '__main__' :
    apkPath = "/home/lw/Auth_Risk_Analysis_tool/apk/test2/"
    # apkPath = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/"

    # resPath = "/home/lw/Auth_Risk_Analysis_tool/Iccbot/icc_result_tmp2_nodebug/"
    resPath = "/home/flash/singledetect/ICCBotOotputResult/otherNodeResult_4"

    jarFile = "S_ICCBot_SaveIdNegative.jar"
    # jarFile = "ICCBot.jar"
    
    os.system("mvn -f pom.xml package -q")
    if os.path.exists("target/ICCBot.jar"):
        print("Successfully build! generate jar-with-dependencies in folder target/")
        shutil.copy("target/ICCBot.jar", jarFile)
        print("copy jar to the root directory.")
    else:
        print("Fail to build! Please run \"mvn -f pom.xml package\" to see the detail info.")
    
    sdk = "lib/"    
    # analyzeApk(apkPath, resPath, sdk)
    
