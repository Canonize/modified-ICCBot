import os
import sys
import shutil

logDir = "../results/logs"
outputDir = "../results/output"
def analyzeApk(path, sdk):
    if(os.path.exists(path)): 
        apks = os.listdir(path)
        for apk in apks:
            if apk[-4:] =="_ins.apk":
                continue
            if apk[-4:] ==".apk":
                print("java -jar ICCBot.jar  -path "+ path +" -name "+apk+" -androidJar "+ sdk +"/platforms -time 30 -maxPathNumber 100 -client ToolEvaluateClient -outputDir "+outputDir+" >> "+logDir+"/"+apk+".txt")
                os.system("java -jar ICCBot.jar  -path "+ path +" -name "+apk+" -androidJar "+ sdk +"/platforms -time 60 -maxPathNumber 100 -client ToolEvaluateClient -outputDir "+outputDir+" >> "+logDir+"/"+apk+".txt")


if __name__ == '__main__' :
    path = "apk/"
    sdk = "lib/"    
    os.system("mvn -f pom.xml clean package")
    shutil.copy("target/ICCBot-1.0-SNAPSHOT.one-jar.jar", "ICCBot.jar")
    if(not os.path.exists(logDir)): 
        os.makedirs(logDir) 
    analyzeApk(path, sdk)
    
