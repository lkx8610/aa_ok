package framework.base;

import java.text.SimpleDateFormat;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
/**
 * 自定义 Log4j HTMLLayout 
 * @author James Guo
 *
 */
public class HTMLLayout extends org.apache.log4j.HTMLLayout {

	private long lastTime = System.currentTimeMillis();
	private long startTime = System.currentTimeMillis();
	private String testName;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
	
	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
		setTitle(testName);
	}

	@Override
	public String format(LoggingEvent event) {
		StringBuffer sbuf = new StringBuffer(BUF_SIZE);

		sbuf.append(Layout.LINE_SEP + "<tr>");

		sbuf.append("<td  style='text-align:center'>").append(dateFormat.format(event.timeStamp)).append("</td>");

		String loggerName = event.getLoggerName();
		if (loggerName.indexOf(".") > -1) {
			loggerName = loggerName.substring(loggerName.indexOf(".")+1);
		}
		sbuf.append("<td>").append(loggerName).append("</td>");
		
		sbuf.append("<td>").append(event.getRenderedMessage()).append("</td>");

		sbuf.append("<td style=\"text-align:center\">");
		double elapsedTime = System.currentTimeMillis() - lastTime;
		elapsedTime = elapsedTime / 1000;
		sbuf.append(String.format("%,8.2f s", elapsedTime));
		sbuf.append("</td>");
		
		
		sbuf.append("<td style=\"text-align:center\">");
		elapsedTime = System.currentTimeMillis() - startTime;
		elapsedTime = elapsedTime / 1000;
		sbuf.append(String.format("%,8.2f s", elapsedTime));
		sbuf.append("</td>");		
		
		sbuf.append("</tr>");
		lastTime = System.currentTimeMillis();
		return sbuf.toString();
	}

	/**
	 * HTML headers.
	 */
	@Override
	public String getHeader() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">" + Layout.LINE_SEP);
		sbuf.append("<html>");
		sbuf.append("<head>");
		sbuf.append("<title>" + getTitle() + "</title>");
		sbuf.append("<style type=\"text/css\">");
		sbuf.append("<!--" + Layout.LINE_SEP);
		sbuf.append("body, table {font-family: arial,sans-serif; font-size: small;}");
		sbuf.append("th {background: #336699; color: #FFFFFF; text-align: left;}" + Layout.LINE_SEP);
		sbuf.append(".action { color : purple; font-weight: bold; }"  + Layout.LINE_SEP);
		sbuf.append(".data { color: black; font-weight: bold; }"  + Layout.LINE_SEP);
		sbuf.append(".warn { color: yellow; background-color: red; font-weight: bold; }"  + Layout.LINE_SEP);
		sbuf.append(".error { color: red; font-weight: bold; }"  + Layout.LINE_SEP);
		sbuf.append(".verify { color: green; font-weight: bold; }"  + Layout.LINE_SEP);
		sbuf.append(".step { color: #0000FF; font-weight: bold; }"  + Layout.LINE_SEP);
		sbuf.append("-->" + Layout.LINE_SEP);
		sbuf.append("</style>" + Layout.LINE_SEP);
		sbuf.append("</head>");
		sbuf.append("<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">" + Layout.LINE_SEP);
		sbuf.append("<h2>").append(getTestName()).append("</h2>");
		sbuf.append("<br>" + Layout.LINE_SEP);
		sbuf.append("<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">" + Layout.LINE_SEP);
		sbuf.append("<tr>");
		sbuf.append("<th style='text-align:center; width:75px;'>Time</th>");
		sbuf.append("<th style='width:125px;'>Category</th>");
		sbuf.append("<th>Message</th>");
		sbuf.append("<th style='text-align:center; width:75px;'>Delta</th>");
		sbuf.append("<th style='text-align:center; width:75px;'>Elapsed<br> Time</th>");		
		sbuf.append("</tr>");
		return sbuf.toString();
	}
}
