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
    
    subprocess.run(['python3','tmptest/runICCBot.py','--apk',app,'--target',entry_class,'--resPath',config.RES_PATH,'--jarFile',config.ICCBot_JAR],cwd=config.ICCBot_PATH,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    # runICCBot.analyzeApk(app,entry_class,config.ICCBOT_RES,config.ICCBot_JAR,sdk)
    e_time = datetime.datetime.now()
    print("============="+app['apk_name']+" finished =============")
    print("cg time: {} seconds".format((e_time-s_time).seconds))

if __name__ == "__main__":
    # 获取CG
    test = pd.read_csv("tmptest/AppEntry.csv").values.tolist()
    p = Pool(4)
    for case in test:
        app = os.path.join(config.APK_BASE_PATH,case[0]+".apk")
        entry_class = case[1]
        p.apply_async(run,args=(app,entry_class))
    p.close()
    p.join()
    print("done")
    
        
        

        