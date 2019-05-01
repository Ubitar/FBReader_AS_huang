###### 自定义修改过的fbreader，参考 https://github.com/adolfAn/FBReader_AS 项目
- 添加txt目录解析
- 添加菜单界面
- 添加右侧目录和标签栏

使用方法：
1. 导入项目中的fBReader库到你的项目下。
2. 参考app库中的Application类，设置 FBReaderIntents.DEFAULT_PACKAGE 的包名为你的项目的applicationId。
3. 在你的Application类中添加
    FBReaderApplication.init(this);
4. 参考MainActivity类中的 FBReader打开方式.

效果如下：

![](https://github.com/Ubitar/FBReader_AS_huang/blob/master/screenshot/screen1.png)
![](https://github.com/Ubitar/FBReader_AS_huang/blob/master/screenshot/screen2.png)

感谢 https://github.com/adolfAn/FBReader_AS 的作者