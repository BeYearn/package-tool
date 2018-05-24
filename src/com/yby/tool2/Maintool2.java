package com.yby.tool2;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yby.tool1.CommonTool;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Properties;


public class Maintool2 {

	public Maintool2() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {

		try {
			System.out.println("please input root path==>");
			String rootPath = CommonTool.valueInput();
			Properties popt = new Properties();
			popt.load(new FileInputStream(rootPath+"\\channels.properties"));
			
			String ApkName=popt.getProperty("ApkName");
			String channelJson = popt.getProperty("channels");
			
			
			/*File unzipDir = new File(rootPath+"/"+ApkName);
			if(!unzipDir.exists()){				
				//开始解压apk, 得提前将.apk改为.zip
				CompressUtil.unzip(rootPath+"/"+ApkName+".zip",rootPath+"/"+ApkName, null);
			}*/
			
			//现在由手动来反编译 然后删掉assets的art中的history中的中文文件
			
			JSONObject json = JSONObject.parseObject(channelJson);
			
			JSONArray jsonArr = json.getJSONArray("channels");
			
			for(int i=0;i<jsonArr.size();i++){
				String channel = jsonArr.getString(i).split("-")[0];
				String channelTag = jsonArr.getString(i).split("-")[1];
				
				File channelFile = new File(rootPath+"\\"+ApkName+"\\assets\\channel.ini");
				File channelTagFile = new File(rootPath+"\\"+ApkName+"\\assets\\config.plist");
				
				//改channel
				if(channelFile.isFile()&&channelFile.exists()){
					FileWriter fw = new FileWriter(channelFile);
					fw.write("CHANNEL="+channel);
					fw.close();
				}
				
				//改channeltag
				SAXReader saxReader = new SAXReader();
	            Document document = saxReader.read(channelTagFile);
	            List<Element> stringList = document.selectNodes("/plist/dict/dict/string");
	            Element channelTagEl = stringList.get(2);//plist中String的第三个
	            channelTagEl.setText(channelTag);
	            CommonTool.writer(document, rootPath+"\\"+ApkName+"\\assets\\config.plist");
	            
	            //回编一个
	            String dosLine = "java -jar "+rootPath+"\\apktool.jar b " + 
	            					rootPath+"\\"+ApkName;
				CommonTool.Log(dosLine+" :"+channel+"-"+channelTag);
				Process process = Runtime.getRuntime().exec(dosLine);
				CommonTool.outputDosMes(process);
	            
	            //编好了复制出来
				File outApkDir = new File(rootPath+"\\out");
				if(!outApkDir.exists()){
					outApkDir.mkdirs();
				}
				String sourceFileStr = rootPath+"\\"+ApkName+"\\dist\\"+ApkName+".apk";
				String destFileStr = rootPath+"\\out\\"+ApkName+"_"+channel+"-"+channelTag+".apk";
				fileCopy(sourceFileStr,destFileStr);
				
				//签名
	            String dosLineSign = "java -jar " + 
				rootPath+"\\signapk.jar "+
	            		rootPath+"\\key\\pem.x509.pem "+rootPath+"\\key\\pk8.pk8 "+
				rootPath+"\\out\\"+ApkName+"-"+channelTag+".apk "+
				rootPath+"\\out\\"+ApkName+"-"+channelTag+"_signed.apk";
	            CommonTool.Log(dosLineSign);
				Process processSign = Runtime.getRuntime().exec(dosLineSign);
				CommonTool.outputDosMes(processSign);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	 /* 
     * 使用文件通道的方式复制文件 
     * String srcDirName = "E:/360cse_official.exe";//待复制的文件名  
       String destDirName ="E:/360cse_official_test.exe";//目标文件名  
     */  
    public static void fileCopy(String srcDirName,String destDirName){  
        FileInputStream fi = null;  
        FileOutputStream fo = null;  
        FileChannel in = null;  
        FileChannel out = null;  
        try {  
            fi = new FileInputStream(new File(srcDirName));  
            fo = new FileOutputStream( new File(destDirName));  
            in = fi.getChannel();//得到对应的文件通道  
            out = fo.getChannel();//得到对应的文件通道  
            /* 
             *       public abstract long transferTo(long position, long count, 
                     WritableByteChannel target)throws IOException; 
             *          position - 文件中的位置，从此位置开始传输；必须为非负数   
             *          count - 要传输的最大字节数；必须为非负数   
             *          target - 目标通道    
             *          返回：   
                        实际已传输的字节数，可能为零   
             */  
            in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道中  
        } catch (FileNotFoundException e) {
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }finally{  
            try {  
                fi.close();  
                in.close();  
                fo.close();  
                out.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }

}
