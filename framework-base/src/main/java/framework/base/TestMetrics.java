package framework.base;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;

public class TestMetrics {

	private Map<String, TestMetric> packageMetrics = new TreeMap<String, TestMetric>(); 
	private Map<String, TestMetric> groupMetrics = new TreeMap<String, TestMetric>();
	private Map<String, TestMetric> classMetrics = new TreeMap<String, TestMetric>();
	private List<ITestResult> testResults = new ArrayList<ITestResult>();
	private TestMetric allTestMetrics = new TestMetric();
	
	public TestMetric getAllTestMetrics() {
		return allTestMetrics;
	}

	public Map<String, TestMetric> getPackageMetrics() {
		return packageMetrics;
	}

	public Map<String, TestMetric> getGroupMetrics() {
		return groupMetrics;
	}

	public Map<String, TestMetric> getClassMetrics() {
		return classMetrics;
	}
	
	public void recordMetrics(ITestResult testResult) {
		testResults.add(testResult);
		updateMetric(testResult, allTestMetrics);
		
		String packageNameStr = testResult.getInstance().getClass().getPackage().getName();
		String packagePath[] = packageNameStr.split("\\.");
		String fullPath = "";
		String delimiter = "";
		for (String packageName : packagePath) {
			fullPath = fullPath + delimiter + packageName;
			updateMetrics(testResult, packageMetrics, fullPath);
			delimiter = ".";
		}
		
		ConstructorOrMethod constructorOrMethod = testResult.getMethod().getConstructorOrMethod();
		Method method = constructorOrMethod.getMethod();
		String groupName = getTestGroup(method);
		updateMetrics(testResult, groupMetrics, groupName);
		
		String className = testResult.getInstance().getClass().getName();
		updateMetrics(testResult, classMetrics, className);
	}
	
	private void updateMetrics(ITestResult testResult, Map<String, TestMetric> metricsMap, String key) {
		TestMetric testMetric = metricsMap.get(key);
		if (testMetric == null) testMetric = new TestMetric();
		updateMetric(testResult, testMetric);
		metricsMap.put(key, testMetric);			
	}
	
	private void updateMetric(ITestResult testResult, TestMetric testMetric) {
		testMetric.totalTests++;
		testMetric.totalTime += testResult.getEndMillis() - testResult.getStartMillis();
		testMetric.averageTime = (testMetric.totalTime / testMetric.totalTests);		

	}
	/**
	 * Returns the TestNG group specified in the @Test annotation for a test method.
	 */
	private String getTestGroup(Method method) {
		String testGroup = "";
		for (Annotation annotation : method.getAnnotations()) {
			if (annotation instanceof Test) {
				Test test = (Test) annotation;
				/*if(test.groups().length > 0){
					for(int i=0; i<test.groups().length; i++){
						if(i < test.groups().length){
							testGroup += test.groups()[i] + ",";
						}else{
							testGroup += test.groups()[i];
						}
					}
				}*/
				testGroup = test.groups().length > 0 ? test.groups()[0] : "";
				break;
			}
		}
		return testGroup;
	}

	public class TestMetric {
		public Integer totalTests = 0;
		public double totalTime = 0.0;
		public double averageTime = 0.0;
	}
}
