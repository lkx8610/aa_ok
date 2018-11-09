package framework.keyworddriven.domain;

/**
 * 封装每条测试用例的JavaBean：
 * @author James Guo
 */
public class TestCaseStepBean {
	private String testCaseID;
	private int testStep;
	private String stepDescription;
	private String keywords;
	private String locatorType;
	private String locator;
	private String colStepNum;//在Excel中步骤前边的数字，如果只是描述信息，前边一般不加数字，结果置为“SKIPPED”

	private String actualResult;//该列：test_data/locator/actual_reault
	private String expecteResult;//该列：varible/expected_result
	private String testResult;
	private String comments;
	
	private int testcaseCount;
	private int currentCaseCount;
	
	
	public String getColStepNum() {
		return colStepNum;
	}
	public void setColStepNum(String colStepNum) {
		this.colStepNum = colStepNum;
	}
	public String getTestCaseID() {
		return testCaseID;
	}
	public void setTestCaseID(String testCaseID) {
		this.testCaseID = testCaseID;
	}
	public int getTestStep() {
		return testStep;
	}
	public void setTestStep(int testStep) {
		this.testStep = testStep;
	}
	public String getStepDescription() {
		return stepDescription;
	}
	public void setStepDescription(String stepDescription) {
		this.stepDescription = stepDescription;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public String getLocatorType() {
		return locatorType;
	}
	public void setLocatorType(String locatorType) {
		this.locatorType = locatorType;
	}
	public String getLocator() {
		return locator;
	}
	public void setLocator(String locator) {
		this.locator = locator;
	}
	public String getActualResult() {
		return actualResult;
	}
	public void setActualResult(String actualResult) {
		this.actualResult = actualResult;
	}
	public String getExpecteResult() {
		return expecteResult;
	}
	public void setExpecteResult(String expecteResult) {
		this.expecteResult = expecteResult;
	}
	public String getTestResult() {
		return testResult;
	}
	public void setTestResult(String testResult) {
		this.testResult = testResult;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public int getTestcaseCount() {
		return testcaseCount;
	}
	public void setTestcaseCount(int testcaseCount) {
		this.testcaseCount = testcaseCount;
	}
	public int getCurrentCaseCount() {
		return currentCaseCount;
	}
	public void setCurrentCaseCount(int currentCaseCount) {
		this.currentCaseCount = currentCaseCount;
	}
	
	
}
