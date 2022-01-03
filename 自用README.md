  # ICCBot打包及运行脚本说明


- 脚本: tmptest/test_runICCBot.py
- 脚本由/scripts/runICCBot.py修改而得，只改变了需要执行的终端命令的参数。
- 使用方式：按需修改入口函数中内容和注释
- 入口函数说明
  
1.改输出目录resPath，jar包名字jarFile

 ```
    resPath = "/home/lw/Auth_Risk_Analysis_tool/Iccbot/icc_result_tmp2_nodebug/"

    jarFile = "ICCBot.jar"
 ```

2.打包
 
 ```
 os.system("mvn -f pom.xml package -q")
 ```

3.并复制jar包到当前文件夹

 ```
    if os.path.exists("target/ICCBot.jar"):
        print("Successfully build! generate jar-with-dependencies in folder target/")
        shutil.copy("target/ICCBot.jar", jarFile)
        print("copy jar to the root directory.")
    else:
        print("Fail to build! Please run \"mvn -f pom.xml package\" to see the detail info.")
 ```
4.分析apk
 ```
    sdk = "lib/"    
    analyzeApk(apkPath, resPath, sdk)
 ```


- 注：
  - <b>ICCBot代码中使用了相对路径，所以只能在ICCBot文件夹下通过以下命令运行脚本:```python tmptest/test_runICCBot.py```
        </b>
  - 输出文件夹(resPath)最好不要建在ICCBot目录下，避免被上传。