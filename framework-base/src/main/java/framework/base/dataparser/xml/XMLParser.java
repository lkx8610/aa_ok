package framework.base.dataparser.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import framework.base.LoggerManager;

/**
 * @description 解析XML的类
 * @author James Guo
 **/
public class XMLParser {
	private static final Logger log = LoggerManager.getLogger(XMLParser.class.getName());
	private Document doc = null;
	/*// 存放所有<datasets></datasets>下的数据的集合对象：
	ArrayList<DataSetBean> dataSetList = new ArrayList<DataSetBean>();*/
	
	//存放各个DataSetBean对象，以DataSetBean的name为Key：
	Map<String, DataSetBean> dataSetBeanMap = new HashMap<>();

	public XMLParser(InputStream in) {
		SAXReader reader = new SAXReader();
		try {
			doc = reader.read(in);
		} catch (Exception e) {
			log.error("error when attemping to parse input file", e);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, DataSetBean> parseXml() {//解析类同名的XML数据文件的方法：

		// <workingDataSet name="login" />，得到workingDataSet的元素及name属性值：
		Element workingDS = (Element) this.doc.selectSingleNode("//workingDataSet");
		String workingDSName = null; // name="login"
		if (workingDS != null) {
			// 获取当前<workingDataSet name="login" />名称：
			workingDSName = workingDS.attributeValue("name");
		}
		log.debug("======parsing current working data set: \\" + workingDSName + "\\");

		// if (workingDSName != null) {//目前这个name可以为空，如果以后有需要再打开这里的注释

		// <datasets>
		Element datasets = (Element) this.doc.selectSingleNode("//datasets");

		// 封装所有的“locator”：
		for (Iterator<Element> lo = datasets.elementIterator("locators"); lo.hasNext();) {
			Element locators = lo.next();
			String locatorsName = locators.attributeValue("name");// 得到name值：<locators name="loginLocators">
			// 以locatorsName封装DataSetBean对象：
			if (null != locatorsName || !StringUtils.isEmpty(locatorsName)) {
				DataSetBean LocatorDataSetBean = new DataSetBean(locatorsName);
				// 解析<locator> </locator>
				for (Iterator<Element> it = locators.elementIterator("locator"); it.hasNext();) {
					Element locator = it.next();// <locator name="login_input" by="id">login</locator>
					String locatorName = locator.attributeValue("name");//得到locator name的值；
					String byVal = locator.attributeValue("by");//得到locator by的值；
					String locatorVal = locator.getText();
					List<String> list = new ArrayList<>();// 存放by,和locator的值；
					list.add(byVal);
					list.add(locatorVal);

					LocatorDataSetBean.addLocators(locatorName, list);
					dataSetBeanMap.put(locatorsName, LocatorDataSetBean);
				}
			} else {
				throw new RuntimeException("locator name is empty in \\" + workingDSName + "\\");
			}
		}
		//解析<dataset>节点：<dataset name="loginWithoutPassword">
		for (Iterator<Element> d = datasets.elementIterator("dataset"); d.hasNext();) {
			// 以<dataset></dataset>为单位进行解析：
			Element dataSet = d.next();
			String dataSetName = dataSet.attributeValue("name");
			DataSetBean varDataSetBean = new DataSetBean(dataSetName);
			// 解析<var> </var>:
			for (Iterator<Element> v = dataSet.elementIterator("var"); v.hasNext();) {
				Element elem = v.next();
				varDataSetBean.addVar(elem.valueOf("@name"), elem.valueOf("."));// elem.valueOf(".")等价于elem.getText();
			}
			// 解析 <list> </list>:
			for (Iterator<Element> l = dataSet.elementIterator("list"); l.hasNext();) {
				Element elem = l.next();
				String listName = elem.attributeValue("name");
				ArrayList<String> varList = new ArrayList<String>();
				for (Iterator<Element> v = elem.elementIterator("var"); v.hasNext();) {
					Element listElem = v.next();
					varList.add(listElem.getText());
				}
				if (varList.size() > 0) {
					varDataSetBean.addVarList(listName, varList);
				}
			}
			dataSetBeanMap.put(dataSetName, varDataSetBean);
		}
		// }
		/*
		 * else { // throw new Exception(" no data set defined"); }
		 */
		return dataSetBeanMap;
	}

}
