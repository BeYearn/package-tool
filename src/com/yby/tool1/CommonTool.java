package com.yby.tool1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class CommonTool {

	public CommonTool() {
	}

	/*
	 * 如果在控制台输入“abc”按回车，控制台会输出5个整形数值，最后两个是13，10
	 * 13和10正好是\n\r的ascii码值。那么对reader2在进行优化，当键盘输入over 的时候，终止这个阻塞式方法，让其不再等待键盘的录入。
	 */
	public static String valueInput() {
		int ch;
		StringBuilder sb = new StringBuilder();
		InputStream in = System.in;
		String dir = "";
		try {
			while ((ch = in.read()) != -1) {
				if (ch == '\n') {
					continue;
				}
				if (ch == '\r') {
					dir = sb.toString();
					sb.delete(0, sb.length());// 一行读取结束后将sb清空，这里面运用的是删除元素的方法
					break;
				} else {
					sb.append((char) ch);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dir;
	}

	/**
	 * 把document对象写入新的文件
	 * 
	 * @param document
	 * @throws Exception
	 */
	public static void writer(Document document, String filename) throws Exception {
		// 排版缩进的格式
		OutputFormat format = OutputFormat.createPrettyPrint();
		// 设置编码
		format.setEncoding("UTF-8");
		// 创建XMLWriter对象,指定了写出文件及编码格式
		XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(new File(filename)), "UTF-8"),
				format);
		// 写入
		writer.write(document);
		// 立即写入
		writer.flush();
		// 关闭操作
		writer.close();
	}

	/**
	 * 实时打印dos返回信息
	 * 
	 * @param process
	 */
	public static void outputDosMes(Process process) {
		// 记录dos命令的返回信息
		StringBuffer resStr = new StringBuffer();
		// 获取返回信息的流
		InputStream in = process.getInputStream();
		Reader reader;
		try {
			reader = new InputStreamReader(in, "GBK");// 因为控制台用的是gbk输出的，而代码是utf_8的
			BufferedReader bReader = new BufferedReader(reader);
			for (String res = ""; (res = bReader.readLine()) != null;) {
				resStr.append(res + "\n");
				System.out.println(res);
			}
			System.out.println("=========done========");
			bReader.close();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Log(String s) {
		System.out.println("###" + s + "###");
	}

	/**
	 * 合并多个文件到一个文件夹
	 */
	public static void combineDirectory(String[] sourceDirNames, String targetDirName) {

		if (sourceDirNames == null || sourceDirNames.length == 0) {
			throw new RuntimeException("待合并的文件夹不存在...");
		}

		for (int i = 0; i < sourceDirNames.length; i++) {

			copyDir(sourceDirNames[i], targetDirName);
		}
		System.out.println("合并所有的文件夹完成...");
	}

	/**
	 * 一个目录拷入另一个目录,递归的方法
	 * 
	 * @param sourceDirName
	 * @param targetDirName
	 */
	public static void copyDir(String sourceDirName, String targetDirName) {

		File sourceDir = new File(sourceDirName);
		File targetDir = new File(targetDirName);

		if (sourceDir == null || !sourceDir.exists()) {
			throw new RuntimeException("待拷贝的文件夹不存在..." + sourceDir.getAbsolutePath());
		}

		if (!sourceDir.isDirectory()) {
			throw new RuntimeException("待拷贝的文件不是目录..." + sourceDir.getAbsolutePath());
		}

		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}

		File[] files = sourceDir.listFiles();

		for (int i = 0; files != null && i < files.length; i++) {

			if (files[i].isFile()) { // 复制文件

				copyFile(files[i], new File(targetDirName + File.separator + files[i].getName()));

			} else if (files[i].isDirectory()) { // 复制目录,递归的方法

				// 复制目录
				String dir1 = sourceDirName + File.separator + files[i].getName();
				String dir2 = targetDirName + File.separator + files[i].getName();
				copyDir(dir1, dir2);
			}

		}

		System.out.println("拷贝文件夹成功..." + sourceDir.getAbsolutePath());
	}

	/**
	 * 拷贝单个的文件
	 * 
	 * @param sourceFile 源文件
	 * @param targetFile 目标文件
	 */
	private static void copyFile(File sourceFile, File targetFile) {
		FileInputStream in = null;
		BufferedInputStream bis = null;
		FileOutputStream out = null;
		BufferedOutputStream bos = null;
		try {
			// 新建文件输入流并对它进行缓冲
			in = new FileInputStream(sourceFile);
			bis = new BufferedInputStream(in);

			// 新建文件输出流并对它进行缓冲
			out = new FileOutputStream(targetFile);
			bos = new BufferedOutputStream(out);

			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = bis.read(b)) != -1) {
				bos.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			bos.flush();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bos != null)
					bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (bis != null)
					bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
