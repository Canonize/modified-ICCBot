import subprocess
import os
import datetime
import pandas as pd
from multiprocessing import Pool

import config
import runICCBot

def run(app,entry_class):
    print("==============={} ：{}===============".format(app,entry_class))
    sdk = "/home/cqt/Auth_Risk_Analysis_tool/modefied-ICCBot/lib/"
    s_time = datetime.datetime.now()
    if len(entry_class) == 0 :
        subprocess.run(['python3','tmptest/runICCBot.py','--apk',app,'--resPath',config.RES_PATH,'--jarFile',config.ICCBot_JAR],cwd=config.ICCBot_PATH,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    else :
        subprocess.run(['python3','tmptest/runICCBot.py','--apk',app,'--target',entry_class,'--resPath',config.RES_PATH,'--jarFile',config.ICCBot_JAR],cwd=config.ICCBot_PATH,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    # runICCBot.analyzeApk(app,entry_class,config.ICCBOT_RES,config.ICCBot_JAR,sdk)
    e_time = datetime.datetime.now()
    print("============="+app+" finished =============")
    print("cg time: {} seconds".format((e_time-s_time).seconds))

if __name__ == "__main__":
    # 获取CG
    apk_name_list = open("tmptest/InTime.csv").readlines()
    apk_name_list = [_.strip() for _ in apk_name_list]
    
    p = Pool(4)
    for case in apk_name_list:
        app = os.path.join(config.APK_BASE_PATH,case.split(',')[0]+".apk")
        entry_class = case.split(',',1)[1]
        p.apply_async(run,args=(app,entry_class))
    p.close()
    p.join()
    print("done")
    
        
        

        