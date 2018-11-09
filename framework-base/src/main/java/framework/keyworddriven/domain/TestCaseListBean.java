package framework.keyworddriven.domain;

/**
 * 封装Excel中“TestCaseLists”的JavaBean
 * @author James Guo
 */
public class TestCaseListBean {
	
	private String testCaseID;
	private String runMode;
	private String testLastRun;
	private String testResult; 
	private String testDescription;
	//由于封装这个List的时候，RunMode=yes的才封装，导致无法最后对该Case打结果，因为打结果需要指定所在的行，所以将其实际所在的行封装进去；
	private int testCaseNumInSheet;
	
	public int getTestCaseNumInSheet() {
		return testCaseNumInSheet;
	}
	public void setTestCaseNumInSheet(int testCaseNumInSheet) {
		this.testCaseNumInSheet = testCaseNumInSheet;
	}
	public String getTestCaseID() {
		return testCaseID;
	}
	public void setTestCaseID(String testCaseID) {
		this.testCaseID = testCaseID;
	}
	public String getRunMode() {
		return runMode;
	}
	public void setRunMode(String runMode) {
		this.runMode = runMode;
	}
	public String getTestLastRun() {
		return testLastRun;
	}
	public void setTestLastRun(String testLastRun) {
		this.testLastRun = testLastRun;
	}
	public String getTestResult() {
		return testResult;
	}
	public void setTestResult(String testResult) {
		this.testResult = testResult;
	}
	public String getTestDescription() {
		return testDescription;
	}
	public void setTestDescription(String testDescription) {
		this.testDescription = testDescription;
	}
	
	
}
