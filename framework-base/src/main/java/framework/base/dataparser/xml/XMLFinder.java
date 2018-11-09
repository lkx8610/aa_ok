package framework.base.dataparser.xml;

import java.io.InputStream;

import org.apache.log4j.Logger;

import framework.base.LoggerManager;


/**
 * @description 查找和当前测试类所在的相同包中的具有和测试类相同的XML数据文件
 * @author James Guo
 **/
public class XMLFinder {

	private static final Logger log = LoggerManager.getLogger(XMLParser.class);

	/*public static InputStream getInputFileAsStream(TestCaseBase testClass) {
		return getInputFileAsStream(testClass.getClass());
	}*/

	/**
		获取当前测试类所在包下的同名XML文件，返回字节流对象
	*/
	public static InputStream getInputFileAsStream(Class<?> testClass) {
		String packageName = "";
		if (testClass.getPackage() != null) {
			packageName = testClass.getPackage().getName() + "."; // 获取包名；
		}
		// 获取完整类名：
		String resourceBase = (packageName + testClass.getSimpleName()).replace('.', '/');
		
		String resourceName = resourceBase + ".xml"; // 得到类同路径下的XML文件；
		InputStream in = null;
		try {
			in = testClass.getClassLoader().getResourceAsStream(resourceName);
		}catch(Exception e) {
			log.error("parse xml: \\" + resourceName + "\\ failed...");
		}
		log.info("searching  for  default input  file: " + resourceName);
		return in;
	}

}
