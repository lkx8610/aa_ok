package framework.base.dataparser.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description TODO
 * @author James Guo
 **/
public class DataSetBean {

	private Map<String, List<String>> locators = new HashMap<>();
	private Map<String, String> vars = new HashMap<String, String>();
	private Map<String, ArrayList<String>> varList = new HashMap<String, ArrayList<String>>();

	private String name;//对应于XML文件中的<locators name="xxx">和<dataset name="xxx">
	
	public DataSetBean() {
		
	}

	public DataSetBean(String name) {
		this.name = name; // 这里的name对应数据XML文件中某个<dataset>节点的name
	}

	public void addVar(String varName, String varVal) {
		vars.put(varName, varVal);
	}

	public void addVarList(String varName, ArrayList<String> valueList) {
		varList.put(varName, valueList);
	}

	public String getName() {
		return this.name;
	}

	public String getVarValue(String varName) {
		if (vars.containsKey(varName)) {
			return vars.get(varName);
		}
		return null;
	}

	public ArrayList<String> getVarValues(String listName) {
		if (varList.containsKey(listName)) {
			return varList.get(listName);
		}
		return null;
	}

	/**
	 * @return the locators
	 */
	public List<String> getLocator(String locatorName) {
		return locators.get(locatorName);
	}

	/**
	 * @param locators the locators to set
	 */
	public void addLocators(String name, List<String> list) {
		locators.put(name, list);
	}

	
}
