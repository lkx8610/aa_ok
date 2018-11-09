package framework.base.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;

import framework.base.LoggerManager;


public class TestUtils {

	private static final Logger logger = LoggerManager.getLogger(TestUtils.class);

	private static final String REFLECTION_INDICATOR = "sun.reflect.NativeMethodAccessorImpl";
	
	
	/**
	 * 根据当前的日期和时间生成13位的字符.
	 */
	public synchronized static String Generate13UniqueId() {
		PerformanceTimer.wait(1);
		SimpleDateFormat format = new SimpleDateFormat("MMddkkmmssSSS");
		return format.format(new Date());
	}
	public synchronized static String Generate8UniqueId() {
		PerformanceTimer.wait(1);
		SimpleDateFormat format = new SimpleDateFormat("Mdkms");
		return format.format(new Date());
	}


	/**
	 * Return the stack trace up to the call stack where the test is being invoked by TestNG reflectively
	 */
	public static String getTruncatedStackTrace(Throwable throwable) {
		StringBuilder sb = new StringBuilder();
		for (String s : ExceptionUtils.getRootCauseStackTrace(throwable)) {
			if (s.contains(REFLECTION_INDICATOR)) {
				break;
			}
			/*String[] shortTrace = s.replace("\n", "/").split("/");
			sb.append(shortTrace[0]);
			break;*/
			
			sb.append(s).append("\n");
		}
		System.out.println(sb.toString());
		return sb.toString().replace("\t", "  ");
	}

	/**
	 * Returns an abbreviated 1-line Stack Trace and failure message
	 */
	public static String getFailureMessage(Throwable ex) {
		String failureLine = "";
		for (String s : ExceptionUtils.getRootCauseStackTrace(ex)) {
			if (s.contains(REFLECTION_INDICATOR)) {
				break;
			}
			failureLine = s;
		}
		return ex.getMessage() + "<br/>" + failureLine;
	}


	/**
	 * Copy all files in the directoryPath and compress them into the zip archive {zipFilePath}
	 *  
	 * @param directoryName
	 * @param zipfile
	 */
	@SuppressWarnings("all")
	public static void zipDirectory(String directoryName, String zipfile) throws IOException {
		File directory = new File(directoryName);
		Assert.assertTrue(directory.exists(), "Directory " + directoryName + " does not exist");
		Assert.assertTrue(directory.canRead(), "Directory " + directoryName + " is not readable");
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(directory);
		OutputStream out = new FileOutputStream(zipfile);
		Closeable currentResource = out;
		logger.info("Zipping " + directoryName + " to " + zipfile);
		try {
			ZipOutputStream zipOutputStream = new ZipOutputStream(out);
			currentResource = zipOutputStream;
			while (!queue.isEmpty()) {
				directory = queue.pop();
				for (File file : directory.listFiles()) {
					String name = base.relativize(file.toURI()).getPath();
					if (file.isDirectory()) {
						logger.debug("Processing directory " + name);
						queue.push(file);
						name = name.endsWith("/") ? name : name + "/";
						zipOutputStream.putNextEntry(new ZipEntry(name));
					} else {
						zipOutputStream.putNextEntry(new ZipEntry(name));
						copy(file, zipOutputStream);
						zipOutputStream.closeEntry();
					}
				}
			}
		} finally {
			currentResource.close();
			out.close();
		}
	}

	public static void copyFile(File source, File destination) throws FileNotFoundException, IOException {
		copy(source, new FileOutputStream(destination));
	}
	
	private static void copy(File file, OutputStream out) throws IOException {
		logger.debug("Copy " + file.getName());
		InputStream in = new FileInputStream(file);
		try {		
			byte[] buffer = new byte[16000];
			while (true) {
				int readCount = in.read(buffer);
				if (readCount < 0) {
					break;
				}
				out.write(buffer, 0, readCount);
			}
		} finally {
			in.close();
		}
	}
	
}
