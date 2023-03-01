import json
import os
import shutil
import subprocess

#修改以下三个路径：iccbot工具的路径， apk所在目录， 输出目录
ICCBot_PATH = "/mnt/ssd_2T/cqt/modefied-ICCBot"
# APK_BASE_PATH = "/home/lw/Auth_Risk_Analysis_tool/apk/2FA"
# RES_PATH = "/home/lw/Auth_Risk_Analysis_tool/mobile_authentication_analysis_tool/test_0807"

# APK_BASE_PATH = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi2"
# APK_BASE_PATH = "/mnt/iscsi/cqt/appInfo/2FA"
APK_BASE_PATH = "/mnt/ssd_2T/cqt/xiaomi/top_1364-1513"
# APK_BASE_PATH = "/mnt/iscsi/cqt/appInfo/top150-300"

# RES_PATH = "/home/lw/Auth_Risk_Analysis_tool/mobile_authentication_analysis_tool/test_xiaomi2"
# RES_PATH = "/home/lw/Auth_Risk_Analysis_tool/mobile_authentication_analysis_tool/test_0811"

# APK_BASE_PATH = "/mnt/iscsi/lw/202206_xiaomi"
# RES_PATH = "/mnt/iscsi/cqt/2FA_result"
RES_PATH = "/mnt/ssd_2T/cqt/xiaomi/ICCBot_result_1364-1513"
# RES_PATH = "/mnt/iscsi/cqt/top150-300_result"



APKTOOL_PATH = "/mnt/ssd_2T/cqt/mobile_authentication_analysis_tool/lib/apktool_2.6.0.jar"
BAKSMALI_JAR = "/mnt/ssd_2T/cqt/mobile_authentication_analysis_tool/lib/baksmali-2.5.2.jar"
ICCBot_JAR = "/mnt/ssd_2T/cqt/modefied-ICCBot/target/ICCBot.jar"
# ICCBot_JAR = "/home/cqt/Auth_Risk_Analysis_tool/modefied-ICCBot/target/ICCBot.jar"


OUTPUT = ""
FD_RES = ""
ICCBOT_RES = ""
FD_CG = ""
FD_CLASS = ""
FD_LOG = ""
FD_CSV = ""
FD_SMALI = ""
FD_TMP = ""
APK_INFO = ""
APK_RES = ""
APK_DECOMPOSED = ""
APK_LOG = ""

LOGIN_KEY = [ 
    ["登录","登陆"],
    [0,"登录成功"]
]
FORGETPWD_KEY = [
    ["忘记密码"],
    [1,"textPassword","0x81"]
]
# #TODO：结束点关键词
# EID_KEY = [
#     ["电子证件"],
#     ["xxx"]
# ]

PHONE_KEY = [
    ["修改手机号"],
    [1,"新手机号"]
]

#认证方式
UNP_FEATURE_1 = ["用户名","密码"]
UNP_FEATURE_2 = ["textPassword","0x81"]
SMS_FEATURE = ["手机号","验证码"]
FR_FEATURE = ["人脸识别"]


def repack_iccbot():
    global ICCBot_PATH
    iccbot_jar = os.path.join(ICCBot_PATH,"target","ICCBot.jar")
    # delete target jar
    if os.path.exists(iccbot_jar):
        subprocess.run(['rm','-rf',iccbot_jar])
    if os.path.exists(ICCBot_JAR):
        subprocess.run(['rm','-rf',ICCBot_JAR])
    # repack
    subprocess.run(['mvn','-f','pom.xml','package','-q'],cwd=ICCBot_PATH)
    if os.path.exists(iccbot_jar):
        print("Successfully build! generate jar-with-dependencies in folder target/")
        shutil.copy(iccbot_jar, ICCBot_JAR)
        print("copy jar to the root directory.")
    else:
        print("Fail to build! Please run \"mvn -f pom.xml package\" to see the detail info.")


def mk_dir(dir_name):
    if not os.path.exists(dir_name):
        os.makedirs(dir_name)


def init_path():
    global OUTPUT,FD_RES,ICCBOT_RES
    global FD_CG,FD_CLASS,FD_DECOMPOSED,FD_LOG,FD_CSV,FD_SMALI,FD_TMP
    global APK_INFO,APK_RES,APK_DECOMPOSED,APK_LOG

    OUTPUT = os.path.join(RES_PATH,"output")
    FD_RES = os.path.join(RES_PATH,"flowdroid_res")
    ICCBOT_RES = os.path.join(RES_PATH,"ICCBot_result")

    # FD_CG = os.path.join(FD_RES,'cg')
    # FD_CLASS = os.path.join(FD_RES,"class")
    FD_LOG = os.path.join(ICCBOT_RES,'fdlog')
    FD_CSV = os.path.join(FD_RES,"csv")
    FD_SMALI = os.path.join(FD_RES,"smali")
    FD_TMP = os.path.join(FD_RES,"tmp")
    
    APK_INFO = os.path.join(OUTPUT,"apk_info")
    APK_RES = os.path.join(OUTPUT,"apk_res")
    APK_DECOMPOSED = os.path.join(OUTPUT,"decomposed")
    APK_LOG = os.path.join(OUTPUT,"log")

    # mk_dir(FD_CG)
    # mk_dir(FD_CLASS)
    # mk_dir(FD_LOG)
    mk_dir(FD_CSV)
    mk_dir(FD_SMALI)
    mk_dir(FD_TMP)
    mk_dir(APK_INFO)
    mk_dir(APK_RES)
    mk_dir(APK_DECOMPOSED)
    mk_dir(APK_LOG)



def save_as_json(tmp_dict,tmp_dir,file_name):
    if not os.path.exists(tmp_dir):
        os.makedirs(tmp_dir)
    with open(os.path.join(tmp_dir,file_name),"w")as f:
        json.dump(json.loads(json.dumps(tmp_dict)),f,ensure_ascii=False)


def load_json(path):
    with open(path,'r')as f:
        content = json.load(fp=f)
    return content


def del_file(file_path):
    subprocess.run(['rm','-rf',file_path])


def del_all_info_for_cur_apk(app):

    # flowdroid
    del_file(app['node_file'])
    del_file(app['edge_file'])
    del_file(app['merge_file'])
    del_file(app['class'] )
    del_file(app['node_csv'])
    del_file(app['edge_csv'])
    del_file(app['fd_log'])
    del_file(app['smali'])
    del_file(FD_TMP)

    # iccbot reulst
    del_file(os.path.join(ICCBOT_RES,'logs',app['apk_name']+'.txt'))
    del_file(os.path.join(ICCBOT_RES,'output',app['apk_name']))

    # output
    del_file(app['apk_res'])
    del_file(app['apk_info'])
    del_file(app['apktool_res'])
    del_file(app['log'])



