import os
import sys
import shutil
import datetime
import subprocess
import argparse

def analyzeApk(apkFile, targetClasses, resPath, jarFile, sdk):

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
        # print(apkPath)
        # os.system("java -Xms12g -Xmx24g -Xss3m -Dorg.slf4j.simpleLogger.logFile="+fdLogDir+"/"+apk[:-4]+".txt"+" -jar "+jarFile+"  -path "+ apkPath +" -name "+apk+" -targetClasses "+targetClasses+" -androidJar "+ sdk +"/platforms "+ extraArgs +" -time 720 -maxPathNumber 100 -outputDir "+outputDir+" >> "+logDir+"/"+apk[:-4]+".txt")
        os.system("java -Xms12g -Xmx24g -Xss3m -Dorg.slf4j.simpleLogger.logFile="+fdLogDir+"/"+apk[:-4]+".txt"+" -jar "+jarFile+"  -path "+ apkPath +" -name "+apk+" -targetClasses "+targetClasses+" -noLibCode "+" -androidJar "+ sdk +"/platforms "+ extraArgs +" -time 360 -maxPathNumber 100 -client GetApiGenClient -outputDir "+outputDir+" >> "+logDir+"/"+apk[:-4]+".txt")
        print("==============={}=============".format(apk))
    except:
        print("{} run error".format(apk))

def read_resfile(cur_file_name):
    with open(cur_file_name,'r') as f:
        b=f.read().split('\n')[:-1]
    if '_node.' in cur_file_name:
        node_map=dict()
        for row in b:
            row=row.split('\t')
            node_map[row[1]]=row[0]
        return node_map
    else:
        edge_map=dict()
        for row in b:
            row=row.split('\t')
            if row[0] not in edge_map:
                edge_map[row[0]]=list()
            edge_map[row[0]].append(row[1])
        return edge_map


def extend_edge(node_map,edge_map):
    newedge_map=dict()
    for i in edge_map.keys():
        newedge_map[node_map[i]]=set()
        for edge in edge_map[i]:
                newedge_map[node_map[i]].add(node_map[edge])
    return newedge_map


def merge_node_edge(flowdroid_output_path,apk_name,merge_output):
    if not os.path.exists(merge_output):
        os.makedirs(merge_output)
    node_map=read_resfile(os.path.join(flowdroid_output_path,apk_name+'_node.txt'))
    edge_map=read_resfile(os.path.join(flowdroid_output_path,apk_name+'_edge.txt'))
    newedge_map=extend_edge(node_map,edge_map)
    with open(os.path.join(merge_output,apk_name+".txt"),'w')as f:
                for k,v in newedge_map.items():
                    for i in v:
                        f.write(k+" -> " +i+"\n")

if __name__ == '__main__' :
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/2FA/SMSLogin.apk"
    # targetClasses = "com.example.smslogin.SMSLoginActivity"
    # resPath = "/home/lw/Auth_Risk_Analysis_tool/Iccbot/a_yyb_result"
    parser = argparse.ArgumentParser()
    parser.add_argument('--apk',type=str)
    parser.add_argument('--target',type=str)
    parser.add_argument('--resPath',type=str)
    parser.add_argument('--jarFile',type=str)
    args = parser.parse_args()

    sdk = "lib/" 
    apk_name = os.path.basename(args.apk)[:-4]
    merge_icc_output = os.path.join(args.resPath,"output",apk_name,"SootIRInfo")
    node_file = os.path.join(merge_icc_output,apk_name+'_node.txt')
    edge_file = os.path.join(merge_icc_output,apk_name+'_edge.txt')
    subprocess.run(args= 'rm -rf '+node_file,shell=True)
    subprocess.run(args= 'rm -rf '+edge_file,shell=True)
    
    s_time = datetime.datetime.now()   
    analyzeApk(args.apk,args.target,args.resPath,args.jarFile, sdk)
    merge_node_edge(merge_icc_output,apk_name,merge_icc_output)
    e_time = datetime.datetime.now()
    print(str((e_time-s_time).seconds)+" seconds")

    print("============="+apk_name+" finished =============")
    
