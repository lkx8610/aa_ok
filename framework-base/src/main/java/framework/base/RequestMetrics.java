
package framework.base;

public class RequestMetrics {

	private String url;
	private String method;
	private int sqlCount;
	private int responseCode;
	private long queryTime;
	private long totalRPCTime;
	private long responseSize;
	
	public RequestMetrics(int responseCode, String url, String method, String sqlCount, String queryTime) {
		this.responseCode = responseCode;
		this.url = url;
		this.method = method == null ? "" : method;
		this.sqlCount = (int) toNumber(sqlCount);
		this.queryTime = toNumber(queryTime);
	}
	
	public int getResponseCode() {
		return responseCode;
	}
	
	public String getUrl() {
		return url;
	}

	public String getMethod() {
		return method;
	}

	public int getSqlCount() {
		return sqlCount;
	}

	public long getQueryTime() {
		return queryTime;
	}

	private long toNumber(String str) {
		long value = 0;
		try {
			value = Long.parseLong(str);
		} catch (Exception ex) {
			// 
		}
		return value;
	}

	public long getTotalRPCTime() {
		return totalRPCTime;
	}

	public void setTotalRPCTime(long totalTime) {
		this.totalRPCTime = totalTime;
	}

	public long getResponseSize() {
		return responseSize;
	}

	public void setResponseSize(long responseSize) {
		this.responseSize = responseSize;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(responseCode).append(" ").append(url).append(" ").append(method).append(" SqlCount = ").append(sqlCount).append(", QueryTime = ").append(queryTime);
		return sb.toString();
	}
}
