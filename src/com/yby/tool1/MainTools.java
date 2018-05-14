package com.yby.tool1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class MainTools {

	public static String[] attrChannel;  //String[cp,uc,4399...]
	public static String channelStr;     // String  cp,uc,4399...
	private static String gameApkName;    //ProXEma-production.apk
	private static String configPath;     //D:\packages\xzyx\configuration
	private static String sourcesPath;   //D:\packages\xzyx\sources

	public static void main(String[] args) { 

		System.out.println("Package tools is starting... ***===================================***");

		System.out.println("1.step: decoding ------------------------------");
		// callDosLine1();
		deCodeApk();

		System.out.println("2.step:  python copy------------------------------");
		// 反编译后 先进行python的复制替换操作，再修改manifest文件
		changeDecodeFile();
		
		// 在python已经替换、覆盖好后，再将source中的value值补充添加到输出目录
		System.out.println("3.step: combine <<value>> file!------------------------------");
		CommonChannel commonChannel = new CommonChannel();
		commonChannel.combineValueFile(channelStr, gameApkName, configPath);

		System.out.println("4.step: fix manifest ------------------------------");
		commonChannel.fixed(channelStr, gameApkName, configPath);

		System.out.println("5.step: apktool build ------------------------------");
		// 回编
		apktoolBuild();
		
		System.out.println("6.step: add sign  ------------------------------");
		// 签名apk
		generateSignApk();
		
		
		//___________________________________________________just for test__________________________________________________________________________________________________________
		//System.out.println("3.step: combine ids.xml & public.xml------------------------------");
		//CommonChannel commonChannel = new CommonChannel();
		//commonChannel.combineIds("bs", "ProXEma-production_new.apk", "D:\\packages\\xzyx\\configuration");
	}

	private static void generateSignApk() {
		for (int i = 0; i < attrChannel.length; i++) {
			try {
				
				String signApkPath=configPath.replace("configuration", attrChannel[i])+"\\"+gameApkName+"\\dist"+"\\"+gameApkName;
				String dosLine = "java -jar " + configPath+"\\signapk.jar "+configPath+"\\pem.x509.pem "+configPath+"\\pk8.pk8 "+signApkPath+".apk "+signApkPath+"_"+attrChannel[i]+".apk";
				CommonTool.Log(dosLine);
				Process process = Runtime.getRuntime().exec(dosLine);
				CommonTool.outputDosMes(process);		
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	private static void apktoolBuild() {
		String commPath = configPath.replace("\\configuration", "");
		try {
			for (int i = 0; i < attrChannel.length; i++) {
				
				String buildApkPath = commPath + "\\" + attrChannel[i].trim();
				String dosLine = "java -jar D:\\apktool\\apktool.jar b " + buildApkPath+"\\"+gameApkName;
				CommonTool.Log(dosLine);
				Process process = Runtime.getRuntime().exec(dosLine);
				CommonTool.outputDosMes(process);
				
				/*new Thread(new Runnable(){
					@Override
					public void run() {
						try {
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).run();*/
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void changeDecodeFile() {
		try {
			// String dosLine = "python
			// D:\\packages\\cydzz\\configuration\\packaging.py app-debug
			// "+gameApkName+" "+channelStr;
			String dosLine = "python " + configPath + "\\packaging.py app-debug " + gameApkName + " " + channelStr;
			CommonTool.Log(dosLine);
			Process process = Runtime.getRuntime().exec(dosLine);
			CommonTool.outputDosMes(process);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void deCodeApk() {

		try {

			System.out.print("Please input decoded gameApks path(absolute path):");
			String gameApkPath = CommonTool.valueInput();

			sourcesPath = gameApkPath.substring(0, gameApkPath.lastIndexOf("\\"));

			configPath = sourcesPath.replace("sources", "configuration");

			gameApkName = gameApkPath.replace(sourcesPath + "\\", "").replace(".apk", "");

			System.out.print("Please input channel'name which need packaged:");
			// 得到全局的渠道名及数组
			channelStr = CommonTool.valueInput().trim();
			attrChannel = channelStr.split(",");

			// 开始反编译
			String dosLineGame = "java -jar D:\\apktool\\apktool.jar d " + gameApkPath + " -o " + sourcesPath + "\\"
					+ gameApkName;
			CommonTool.Log(dosLineGame);
			Process processGame = Runtime.getRuntime().exec(dosLineGame);
			CommonTool.outputDosMes(processGame);

			for (int i = 0; i < attrChannel.length; i++) {
				String decodeApkPath = sourcesPath + "\\app-debug-" + attrChannel[i].trim() + ".apk";
				String dosLine = "java -jar D:\\apktool\\apktool.jar d " + decodeApkPath + " -o " + sourcesPath
						+ "\\app-debug-" + attrChannel[i].trim();
				CommonTool.Log(dosLine);
				Process process = Runtime.getRuntime().exec(dosLine);
				CommonTool.outputDosMes(process);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 测试用
	private static void callDosLine1() {
		try {
			// 执行ping命令
			String dosLine = "ping 127.0.0.1";
			Process process = Runtime.getRuntime().exec(dosLine);
			// 记录dos命令的返回信息
			StringBuffer resStr = new StringBuffer();
			// 获取返回信息的流
			InputStream in = process.getInputStream();
			Reader reader = new InputStreamReader(in, "GBK"); // 因为控制台用的是gbk输出的，而代码是utf_8的
			BufferedReader bReader = new BufferedReader(reader);
			for (String res = ""; (res = bReader.readLine()) != null;) {
				resStr.append(res + "\n");
			}
			bReader.close();
			reader.close();
			System.out.println(resStr.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
