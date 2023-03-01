import os
import sys
import shutil
import datetime
import subprocess

import config

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
        # print(apkPath)
        # os.system("java -Xms12g -Xmx24g -jar "+jarFile+"  -path "+ apkPath +" -name "+apk+" -androidJar "+ sdk +"/platforms "+ extraArgs +" -time 720 -maxPathNumber 100 -outputDir "+outputDir+" >> "+logDir+"/"+apk[:-4]+".txt")
        if len(targetClasses) == 0:
            os.system("java -Xms12g -Xmx24g -Xss3m -Dorg.slf4j.simpleLogger.logFile="+fdLogDir+"/"+apk[:-4]+".txt"+" -jar "+jarFile+"  -path "+ apkPath +" -name "+apk+" -androidJar "+ sdk +"/platforms "+ extraArgs +" -time 720 -maxPathNumber 100 -client GetApiGenClient -outputDir "+outputDir+" >> "+logDir+"/"+apk[:-4]+".txt")
        else:
            os.system("java -Xms12g -Xmx24g -Xss3m -Dorg.slf4j.simpleLogger.logFile="+fdLogDir+"/"+apk[:-4]+".txt"+" -jar "+jarFile+"  -path "+ apkPath +" -name "+apk+" -targetClasses "+targetClasses+" -androidJar "+ sdk +"/platforms "+ extraArgs +" -time 720 -maxPathNumber 100 -client GetApiGenClient -outputDir "+outputDir+" >> "+logDir+"/"+apk[:-4]+".txt")
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

# decompse dex
def decompose_apk(dex_path, smali_path):
    print("====================dex 反编译====================")
    try:
        subprocess.run(["java","-jar",config.APKTOOL_PATH,"d",dex_path,"-o",smali_path],
                                stdout=subprocess.PIPE,universal_newlines=True)
        print("反编译结束")
    except Exception as e:
        print(e)
        
if __name__ == '__main__' :
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/com.ctid.open.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi2/ctrip.android.view.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/com.epoint.mobileframe.wssb.qinghai.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/com.ccb.fintech.app.productions.hnga.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/cn.hsa.app.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/com.hanweb.android.zhejiang.activity.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/com.jd.jrapp_6.1.90_410.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/unpack/com.guangdong.gov.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/test2/cn.xuexi.android.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi2/com.huajiao.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi2/com.digitalgd.dgyss.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi2/com.MobileTicket.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi2/com.Qunar.apk"
    # apkFile = "/home/cqt/Auth_Risk_Analysis_tool/modefied-ICCBot/apk/a2dp.Vol_133.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi2/com.wudaokou.hippo.apk"
    apkFile = "/mnt/ssd_2T/cqt/top_105/com.instagram.android.apk"
    # apkFile = "/mnt/iscsi/cqt/appInfo/topApp/com.smile.gifmaker.apk"
    # apkFile = "/home/cqt/Auth_Risk_Analysis_tool/appInfo/topApp/com.achievo.vipshop.apk"
    # apkFile = "/home/cqt/Auth_Risk_Analysis_tool/modefied-ICCBot/apk/ICCBotBench.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/2FA/SMSLogin.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi2/me.ele.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/TwoHandlerTest.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi/com.doweidu.android.haoshiqi.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi/bubei.tingshu.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi/com.taobao.idlefish.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi/com.chaozh.iReaderFree15.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi2/com.duowan.kiwi.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi2/com.huajiao.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/2FA/Simple2FA_1.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/2FA/complex2FA_1.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi2/com.autonavi.minimap.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi2/com.huajiao.apk"
    # apkFile = "/home/lw/Auth_Risk_Analysis_tool/apk/xiaomi2/com.zhihu.android.apk"
    # apkFiles = os.listdir("/home/lw/Auth_Risk_Analysis_tool/apk/icc_apk")
    # targetClasses = "com.ctid.open.activity.LoginActivity"
    # targetClasses = "ctrip.android.login.view.thirdlogin.binder.ToThirdAccountBindActivity,ctrip.android.login.view.thirdlogin.binder.ToThirdSimBindActivity,ctrip.android.login.view.thirdlogin.binder.ToThirdOtherBindActivity"
    # targetClasses = "com.ccb.fintech.app.productions.hnga.ui.user.login.LoginActivity"
    # targetClasses = "com.example.handlertest.MainActivity"
    # targetClasses = "com.alibaba.zjzwfw.account.ZWLoginActivityV3"
    # targetClasses = "com.example.smslogin.SMSLoginActivity"
    # targetClasses = "com.example.complexp2fa.LoginActivity"
    # targetClasses = "com.autonavi.map.activity.NewMapActivity"
    # targetClasses = "com.huajiao.user.NewSmsLoginActivity"
    # targetClasses = "com.ali.user.mobile.login.ui.UserLoginActivity"
    # targetClasses = "com.zhihu.android.app.ui.activity.HostActivity"
    # targetClasses = "com.taobao.fleamarket.setting.activity.SettingsActivity"
    # targetClasses = "com.zhangyue.iReader.ui.activity.AppLockActivity,com.zhangyue.iReader.account.Login.ui.AuthorActivity"
    # targetClasses = "com.duowan.kiwi.loginui.impl.gamesdk.GameSdkLoginActivity,com.duowan.kiwi.teenager.impl.activity.TeenagerLockActivity,com.duowan.kiwi.debug.DebugSoftwareSetting,com.duowan.kiwi.userinfo.UserInfoActivity,com.duowan.kiwi.scan.impl.CaptureActivity"
    # targetClasses = "com.huajiao.user.RegisterActivity,com.huajiao.me.accountswitch.AccountSwitchActivity,com.huajiao.user.ModifyPwdActivity,com.huajiao.user.MobileCertActivity,com.huajiao.sharelink.ShareLinkTipsDialog,com.huajiao.user.SetPwdActivity,com.huajiao.user.SmsLoginActivity,com.huajiao.user.ForgetPwdActivity,com.huajiao.user.ReceiveSmsCodeActivity,com.huajiao.user.ValidateCodeActivity,com.huajiao.user.LoginAndRegisterActivity,com.huajiao.user.NewSmsLoginActivity,com.huajiao.user.LoginActivity"
    # targetClasses = "com.example.simple2fa_4.LoginActivity"
    # targetClasses = "cn.hsa.app.login.ui.LoginActivity"
    # targetClasses = "com.alibaba.android.user.login.SignUpWithPwdActivity"
    # targetClasses = "com.bjsidic.bjt.login.LoginActivity"
    # targetClasses = "com.huajiao.user.LoginAndRegisterActivity"
    # targetClasses = "com.digitalgd.auth.ui.DGAuthEntranceActivity"
    # targetClasses = "com.tencent.connect.auth.DialogC8175a"
    # targetClasses = "com.sina.weibo.sdk.web.WebActivity"
    targetClasses = "com.facebook.login.LoginClient:Result,com.google.android.gms.auth.api.identity.BeginSignInResult,com.google.android.gms.auth.api.signin.GoogleSignInOptions,com.facebook.common.classmarkers.LiteLoginMessageSent,com.google.android.gms.auth.api.signin.SignInAccount,com.google.android.gms.auth.api.identity.BeginSignInRequest:GoogleIdTokenRequestOptions,com.google.android.gms.auth.api.identity.BeginSignInRequest:PasswordRequestOptions,com.google.android.gms.auth.api.identity.BeginSignInRequest,com.google.android.gms.auth.api.identity.SignInCredential,com.facebook.login.LoginClient:Request"
    # targetClasses = "np0.d,c39.d,mc9.m,lib.h0,com.yxcorp.login.userlogin.fragment.b,com.yxcorp.login.userlogin.fragment.c,d78.j,hy8.e"
    # targetClasses = "com.achievo.vipshop.usercenter.fragment.LoginFragment,com.achievo.vipshop.usercenter.fragment.LastLoginFragment"
    # targetClasses = "com.mqunar.atom.vacation.localman.activity.LocalmanSubmitOrderActivity"
    # targetClasses = "a2dp.Vol.main"
    # targetClasses = "com.iccbot.withFrag.Frag1WithFrag"
    # targetClasses = "com.ali.user.mobile.login.ui.UserLoginActivity"
    # targetClasses = "bubei.tingshu.listen.account.ui.activity.BindAccountOneKeyLastLoginActivity,bubei.tingshu.listen.account.ui.activity.AccountSecurityAuthActivity,bubei.tingshu.listen.account.ui.activity.ThirdSubscribeAccountActivity,bubei.tingshu.listen.account.ui.activity.VerifyCodeLoginActivity,bubei.tingshu.listen.account.ui.activity.LoginActivity,bubei.tingshu.listen.account.ui.activity.FindPasswordActivity,bubei.tingshu.listen.account.ui.activity.NewVerifyCodeLoginActivity,bubei.tingshu.listen.account.ui.activity.FindPasswordInputActivity,bubei.tingshu.listen.setting.ui.activity.SettingActivity,bubei.tingshu.listen.youngmode.ui.YoungModePwdActivity,bubei.tingshu.listen.account.ui.activity.ThirdLoginBindPhoneActivity,bubei.tingshu.listen.account.ui.activity.OneKeyLoginActivity"
    # targetClasses = "com.doweidu.android.haoshiqi.newversion.activity.BindWeChatActivity,com.doweidu.android.haoshiqi.user.OneclickLoginActivity,com.doweidu.android.haoshiqi.about.SettingActivity,com.doweidu.android.haoshiqi.user.LoginBackActivity,com.doweidu.android.haoshiqi.user.LoginQuickActivity"
    # resPath = "/home/flash/singledetect/ICCBotOotputResult/otherNodeResult_4"
    # resPath = "/home/lw/Auth_Risk_Analysis_tool/Iccbot/icc_result_SaveIdNegative/"
    # resPath = "/home/flash/singledetect/ICCBotOotputResult/guangdongsootIR"
    # resPath = "/home/cqt/Auth_Risk_Analysis_tool/ICCBot_result"
    # resPath = "/home/cqt/Auth_Risk_Analysis_tool/select_result"
    resPath = "/mnt/ssd_2T/cqt/result_105/ICCBot_result"
    # resPath = "/home/lw/Auth_Risk_Analysis_tool/Iccbot/2FA_test"


    # jarFile = "S_ICCBot_SaveIdNegative.jar"
    # jarFile = "C_ICCBot_ChangeSoot.jar"
    jarFile = "M_ICCBot_Merge.jar"
    apk_name = os.path.basename(apkFile)[:-4]
    merge_icc_output = os.path.join(resPath,"output",apk_name,"SootIRInfo")

    node_file = os.path.join(merge_icc_output,apk_name+'_node.txt')
    edge_file = os.path.join(merge_icc_output,apk_name+'_edge.txt')
    dex_file = os.path.join(merge_icc_output,apk_name+'.apk')
    smali_dir = os.path.join(merge_icc_output,apk_name)
    
    subprocess.run(args= 'rm -rf '+merge_icc_output,shell=True)

    s_time = datetime.datetime.now()

    os.system("mvn -f pom.xml package -q")
    if os.path.exists("target/ICCBot.jar"):
        print("Successfully build! generate jar-with-dependencies in folder target/")
        shutil.copy("target/ICCBot.jar", jarFile)
        print("copy jar to the root directory.")
    else:
        print("Fail to build! Please run \"mvn -f pom.xml package\" to see the detail info.")
    
    sdk = "lib/"    
    analyzeApk(apkFile,targetClasses,resPath, sdk)

    merge_node_edge(merge_icc_output,apk_name,merge_icc_output)

    # decompose_apk(dex_file,smali_dir)
    
    e_time = datetime.datetime.now()
    print(str((e_time-s_time).seconds)+" seconds")

    print("============="+apk_name+" finished =============")
    
