package com.yby.tool1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class CommonChannel {

	static SAXReader reader = new SAXReader();
	static String documentSourceFile = "C:\\Users\\B-01\\Desktop\\博研\\1.xml";
	static String documentEndFile = "C:\\Users\\B-01\\Desktop\\博研\\2.xml";

	/**
	 * 循环value目录下文件 并加入 单一渠道 输入原包的value目录
	 */
	public void combineValueFile(String channelStr, String gameApkName, String configPath) { // 此时sourcePath中的内容一定是endpath的子集（因为之前py的复制）

		String[] attrChannel = channelStr.split(",");

		// String configurationPath="D:\\packages\\cydzz\\sources";
		String sourcesPath = configPath.replace("configuration", "sources");
		// String outputChannelPath="D:\\packages\\cydzz";
		String outputChannelPath = configPath.replace("\\configuration", "");

		String shorApkName = gameApkName.replace(".apk", "");

		for (int i = 0; i < attrChannel.length; i++) {

			String endValuePath = outputChannelPath + "\\" + attrChannel[i].trim() + "\\" + shorApkName
					+ "\\res\\values";
			String sourceValuePath = sourcesPath + "\\" + shorApkName + "\\res\\values";

			File sourceFile = new File(sourceValuePath);
			File endFile = new File(endValuePath);

			if (sourceFile.exists() || endFile.exists()) {
				File[] sourceFiles = sourceFile.listFiles();
				File[] endFiles = endFile.listFiles();
				if (sourceFiles.length == 0) {
					System.out.println("有个 value 文件夹是空的!");
					return;
				} else {
					for (File sFile : sourceFiles) {
						if (sFile.isDirectory()) { // value文件夹下不会再有文件夹
							System.out.println("纳尼！！有文件夹:" + sFile.getAbsolutePath()); // 这种情况应该不会发生
							// 递归 traverseFolder2(file2.getAbsolutePath());
						} else {
							System.out.println("文件:" + sFile.getAbsolutePath());
							// 开始往end包value下文件插入
							// if(endFiles!=null){
							for (File eFile : endFiles) {
								if (sFile.getName().equals(eFile.getName())) { // 遇到文件名字相同的开始插
									if (sFile.getName().contains("public")) {
										// 那就上面蛋疼的插法
										System.out.println("dan teng cha fa");
										publicInsert(sFile, eFile);
									} else {
										// 名字不同就插
										System.out.println("normal cha fa");
										normalInsert(sFile, eFile);
									}
								}
							}
							// }
						}
					}
				}

			} else {
				System.out.println(sourceValuePath + " or " + endValuePath + " : 不存在!");
			}
			System.out
					.println("***==================combie publi&ids " + attrChannel[i] + " done==================***");
		}
	}
	// ______________________________________________________________________________________________________________________________________________

	/**
	 * normal 插法
	 */
	public void normalInsert(File soureFile, File endFile) {

		try {
			// 通过read方法读取一个文件 转换成Document对象
			Document documentSource = reader.read(soureFile);
			Document documentEnd = reader.read(endFile);
			// 获取根节点元素对象
			Element nodeSource = documentSource.getRootElement();
			Element nodeEnd = documentEnd.getRootElement();

			List<Element> sourceNodeList = nodeSource.elements();
			List<Element> endNodeList = nodeEnd.elements();

			for (Element sNode : sourceNodeList) {
				boolean isHaveSame = false;
				for (Element eNode : endNodeList) {
					if (sNode.attribute("name").getValue().equals(eNode.attribute("name").getValue())) {
						isHaveSame = true;
					}
				}
				if (!isHaveSame) {
					nodeEnd.add(sNode.detach());
				}
			}

			CommonTool.writer(documentEnd, endFile.getAbsolutePath());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 蛋疼的插法
	 * 
	 * @throws Exception
	 */
	public void publicInsert(File soureFile, File endFile) {
		try {
			documentSourceFile = soureFile.getAbsolutePath();
			documentEndFile = endFile.getAbsolutePath();
			// 通过read方法读取一个文件 转换成Document对象
			Document documentSource = reader.read(soureFile);
			Document documentEnd = reader.read(endFile);
			// 获取根节点元素对象
			Element nodeSource = documentSource.getRootElement();
			Element nodeEnd = documentEnd.getRootElement();
			listNodesStart(nodeSource, nodeEnd, documentEnd);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void listNodesStart(Element nodeSource, Element nodeEnd, Document documentEnd) throws Exception {
		// 获取当前节点的所有属性节点
		List<Attribute> list = nodeSource.attributes();
		Map map = new HashMap();
		// 遍历属性节点
		for (Attribute attr : list) {
			if ("type".equals(attr.getName())) {
				map.put("type", attr.getValue());
			} else if ("name".equals(attr.getName())) {
				map.put("name", attr.getValue());
			} else if ("id".equals(attr.getName())) {
				map.put("id", attr.getValue());
			}
		}
		if (!map.isEmpty()) {
			// 判断是否有相同Name的
			if (isHaveName(map, updateFileData())) {
				// System.out.println("没有相同的Name");
				// 判断当前map 是否有相同的type 有则返回最大的ID
				if ("".equals(getMaxIdByType(map, updateFileData()))) {
					// 没有Type

					// 取得当前最大的ID
					String maxId = getMaxId(updateFileData());
					if ("".equals(maxId)) {
						maxId = "0x7f020000";
					} else {
						int five = Integer.parseInt(maxId.substring(4, 6), 16);
						five = five + 1;
						String five16 = Integer.toHexString(five);
						switch (five16.length()) {
						case 1:
							five16 = "0" + five16;
							break;
						default:
							five16 = "" + five16;
						}
						maxId = maxId.substring(0, 4) + five16 + "0000";
					}
					Element sbNew = nodeEnd.addElement("public");
					sbNew.addAttribute("type", map.get("type").toString());
					sbNew.addAttribute("name", map.get("name").toString());
					sbNew.addAttribute("id", maxId);
				} else {
					// 有type
					String maxId = getMaxIdByType(map, updateFileData());
					int ten = Integer.parseInt(maxId.substring(7, 10), 16);
					ten = ten + 1;
					String ten16 = Integer.toHexString(ten);
					switch (ten16.length()) {
					case 1:
						maxId = maxId.substring(0, 7) + "00" + ten16;
						break;
					case 2:
						maxId = maxId.substring(0, 7) + "0" + ten16;
						break;
					default:
						maxId = maxId.substring(0, 7) + "" + ten16;
					}
					if (map.isEmpty()) {
						return;
					}
					Element sbNew = nodeEnd.addElement("public");
					sbNew.addAttribute("type", map.get("type").toString());
					sbNew.addAttribute("name", map.get("name").toString());
					sbNew.addAttribute("id", maxId);
				}
				CommonTool.writer(documentEnd, documentEndFile);
			} else {
				// System.out.println("有相同的Name");
			}
		}
		// 当前节点下面子节点迭代器
		Iterator<Element> it = nodeSource.elementIterator();
		// 遍历
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element e = it.next();
			// 对子节点进行遍历
			listNodesStart(e, nodeEnd, documentEnd);
		}

	}

	private static String getMaxId(List<Map<String, String>> list) {
		String maxId = "";
		for (Map<String, String> mapList : list) {
			if (maxId.compareTo(mapList.get("id") == null ? "" : mapList.get("id")) < 0) {
				maxId = mapList.get("id");
			}
		}
		return maxId;
	}

	private static boolean isHaveName(Map map, List<Map<String, String>> list) {
		for (Map<String, String> mapList : list) {
			if (map.get("name").equals(mapList.get("name"))) {
				return false;
			}
		}
		return true;
	}

	private static String getMaxIdByType(Map map, List<Map<String, String>> list) {
		String maxId = "";
		for (Map<String, String> mapList : list) {
			if (map.get("type").equals(mapList.get("type"))) {
				if (maxId.compareTo(mapList.get("id") == null ? "" : mapList.get("id")) < 0) {
					maxId = mapList.get("id");
				}
			}
		}
		return maxId;
	}

	public static List<Map<String, String>> updateFileData() throws Exception {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		// 得到解析器
		SAXReader reader = new SAXReader();
		// 第二步 得到当前XML 文档的Document对象
		try {
			Document document = reader.read(new File(documentEndFile));
			// 获取document 根目录下
			Element root = document.getRootElement();
			// 遍历根节点的所有子节点
			for (Iterator iter = root.elementIterator(); iter.hasNext();) {
				// 封装属性值到HashMap 集合里
				HashMap<String, String> map = new HashMap<String, String>();
				// 遍历所有节点
				Element element = (Element) iter.next();
				// 判断 element 不等于null
				if (element == null)
					continue;
				// 获取属性和它的值
				for (Iterator attrs = element.attributeIterator(); attrs.hasNext();) {
					// 获取属性
					Attribute attr = (Attribute) attrs.next();
					// 判断属性 null
					if (attr == null)
						continue;
					// 获取属性
					String attrName = attr.getName();
					// 获取值
					String attrValue = attr.getValue();
					// 封装map集合里 把属性和值
					map.put(attrName, attrValue);
				}
				// 判断 只读
				if (element.isReadOnly()) {
					String elementName = element.getName();
					String elementValue = element.getText();
					map.put(elementName, elementValue);
				} else {
					// 遍历节点的所有孩子节点，并进行处理
					for (Iterator iterInner = element.elementIterator(); iterInner.hasNext();) {
						Element elementInner = (Element) iterInner.next();
						// 如果没有孩子节点，则直接取值
						if (elementInner == null) {
							String elementName = element.getName();
							String elementValue = element.getText();
							map.put(elementName, elementValue);
						}
						// 孩子节点的属性
						for (Iterator innerAttrs = elementInner.attributeIterator(); innerAttrs.hasNext();) {
							Attribute innerAttr = (Attribute) innerAttrs.next();
							if (innerAttr == null)
								continue;
							String innerAttrName = innerAttr.getName();
							String innerAttrValue = innerAttr.getValue();
							map.put(innerAttrName, innerAttrValue);
						}
						// 假设没有第三层嵌套，获得第二层的值
						String innerName = elementInner.getName();
						String innerValue = elementInner.getText();
						map.put(innerName, innerValue);

					}
				}
				// 封装list集合里
				list.add(map);
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return list;
	}

	// ______________________________________________________________________________________________________________________________________________

	/**
	 * 合并清单文件
	 * 
	 * @param channelStr
	 * @param gameApkName
	 * @param configPath
	 */
	public void fixed(String channelStr, String gameApkName, String configPath) {

		String shorApkName = gameApkName.replace(".apk", "");

		String[] attrChannel = channelStr.split(",");

		// String configurationPath="D:\\packages\\cydzz\\configuration";
		String configurationPath = configPath;
		// String outputChannelPath="D:\\packages\\cydzz";
		String outputChannelPath = configurationPath.replace("\\configuration", "");

		// "D:\\packages\\cydzz\\configuration\\channel-icon";
		String channelIconPath = configurationPath + "\\channel-icon";

		// 创建saxReader对象
		SAXReader reader = new SAXReader();

		for (int i = 0; i < attrChannel.length; i++) {
			String fileName = outputChannelPath + "\\" + attrChannel[i].trim() + "\\" + gameApkName
					+ "\\AndroidManifest.xml";
			// 配置的清单文件路径
			// "C:\\Users\\Administrator\\Desktop\\config\\yybconfig.xml";
			String maniConfigPath = configurationPath + "\\config" + "\\" + attrChannel[i].trim() + "Config.xml";

			// 读取两个manifest文件 转换成Document对象
			Document document = null;
			try {
				document = reader.read(new File(fileName));
			} catch (DocumentException e) {
				e.printStackTrace();
				System.out.print("please checkout the file path");
			}
			Document configDoc = null;
			try {
				configDoc = reader.read(new File(maniConfigPath));
			} catch (DocumentException e) {
				e.printStackTrace();
				System.out.print("please checkout the file path");
			}

			// 获取根节点元素对象
			Element rootNode = document.getRootElement();
			Element configRootNode = configDoc.getRootElement();

			// 得到配置文件的 包名 渠道号 和 需要修改的渠道icon名(有可能空)
			Attribute configPackageAttr = configRootNode.attribute("package");
			String packageName = configPackageAttr.getValue();

			Attribute configChannelidAttr = configRootNode.attribute("channelid");
			String channelId = configChannelidAttr.getValue();

			String channelIcon = "";
			Attribute configChannelIconAttr = configRootNode.attribute("channelIcon");
			if (configChannelIconAttr != null) {
				channelIcon = configChannelIconAttr.getValue();
			}

			// 设置包名
			Attribute packageAttr = rootNode.attribute("package");
			packageAttr.setValue(packageName);
			// 设置渠道号
			Node applicaNode = document.selectSingleNode("manifest/application");
			List<Node> appidNodeList = applicaNode.selectNodes("meta-data[@android:name='EMA_CHANNEL_ID']");
			Node appidNode = appidNodeList.get(0);
			Attribute appidAtr = ((Element) appidNode).attribute(1); // 疑问
																		// 为何用："android:value"
																		// 获取不到（null），只能用1（第二个）来获取到
			appidAtr.setValue(channelId);

			// 修改channelIcon
			// (当xxConfig中的channelIcon有值时,才改iconname并复制对应icon)
			// 更新 当xxConfig中的channelIcon有值 且 那个文件夹存在时才改名和复制drawable
			if (null != channelIcon && "" != channelIcon) {
				// "D:\\packages\\cydzz\\configuration\\channel-icon"; +"\\4399"
				String iconFileStr = channelIconPath + "\\" + attrChannel[i].trim();
				File iconFile = new File(iconFileStr);
				if (iconFile.exists()) {
					// 将对应icon拷入输出目录
					String targetDirName = outputChannelPath + "\\" + attrChannel[i].trim() + "\\" + shorApkName
							+ "\\res";
					CommonTool.copyDir(iconFileStr, targetDirName);

					// 改清单文件的appIconn名字
					Element appELe = rootNode.element("application");
					Attribute iconAtr = appELe.attribute("icon"); // 之所以不是"android:icon"的原因是命名空间
																	// 否则null
					iconAtr.setValue("@drawable/" + channelIcon);
				}
			}

			// 修改环境
			// 修改tag等

			// 修改个推两个权限的名字
			List<Attribute> attrListP = document.selectNodes("manifest/permission/@android:name");
			Iterator<Attribute> i1 = attrListP.iterator();
			while (i1.hasNext()) {
				Attribute attribute = i1.next();
				if (attribute.getValue().startsWith("getui.permission.GetuiService")) {
					attribute.setValue("getui.permission.GetuiService." + packageName);
				}
			}

			List<Attribute> attrListUp = document.selectNodes("manifest/uses-permission/@android:name");
			Iterator<Attribute> i2 = attrListUp.iterator();
			while (i2.hasNext()) {
				Attribute attribute = i2.next();
				if (attribute.getValue().startsWith("getui.permission.GetuiService")) {
					attribute.setValue("getui.permission.GetuiService." + packageName);
				}
			}
			List<Attribute> attrListProvi = document.selectNodes("manifest/application/provider/@android:authorities");
			Iterator<Attribute> i3 = attrListProvi.iterator();
			while (i3.hasNext()) {
				Attribute attribute = i3.next();
				if (attribute.getValue().startsWith("downloads")) {
					attribute.setValue("downloads." + packageName);
				}
			}

			// 合并
			Element mainNode = (Element) document.selectSingleNode("manifest");
			Element insertNode = (Element) configDoc.selectSingleNode("yyb");

			Element insertNodeAuth = (Element) insertNode.selectSingleNode("authority");
			mainNode.appendContent(insertNodeAuth);

			Element mainNodeAppliacton = (Element) mainNode.selectSingleNode("application");
			Element insertNodeComponent = (Element) insertNode.selectSingleNode("component");
			mainNodeAppliacton.appendContent(insertNodeComponent);

			// 写入
			try {
				CommonTool.writer(document, fileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("***==================AndroidManifest-" + attrChannel[i] + " done==================***");
		}

	}

}
