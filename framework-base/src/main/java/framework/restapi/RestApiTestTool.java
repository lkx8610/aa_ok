package framework.restapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import framework.base.LoggerManager;

public class RestApiTestTool {
	private static Logger log = LoggerManager.getLogger(RestApiTestTool.class.getSimpleName());

	/**
	 * @Description： get请求：请求参数在URL后边追加
	 * @author：James
	 * @return: 
	 * @throws：
	 */
	public static String doGet(String url) {
		String result = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		log.info("getting rest api url: " + url);

		HttpGet get = new HttpGet(url);
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(get);
			if (response != null && response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				result = EntityUtils.toString(entity);
			}
			log.info("response status code: " + response.getStatusLine().getStatusCode());
			log.debug("Response Body: \n" + result);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				httpClient.close();
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 
	 * @Description： get请求，请求参数放到Map中
	 * 
	 * @author：James
	 * @return: 
	 * @throws：
	 */
	@SuppressWarnings("all")
	public static String doGet(String url, Map<String, String> paramsMap) {
		String result = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();

		log.info("Invoking method ' doGet(String, Map) ' Request URL: " + url);
		log.info("Request Params: " + paramsMap.toString());

		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		CloseableHttpResponse response = null;
		try {
			URIBuilder builder = new URIBuilder(url);
			builder.setParameters(pairs);
			HttpGet get = new HttpGet(builder.build());
			response = httpClient.execute(get);
			if (response != null && response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				result = EntityUtils.toString(entity);
			}
			log.info("Response status code: " + response.getStatusLine().getStatusCode());
			log.debug("Response Body: \n" + result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				httpClient.close();
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}
	}

	/**
	 * @Description： Post请求，请求参数在Map中
	 * @author：James
	 * @return: 
	 * @throws：
	 */
	public String doPost(String url, Map<String, String> map) {
		String result = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost post = new HttpPost(url);
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();

		log.info("Invoking method: doPost(String, Map)...");
		log.info("Request URL：" + url);
		log.info("Request params:" + map.toString());

		for (Map.Entry<String, String> entry : map.entrySet()) {
			pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		CloseableHttpResponse response = null;
		try {
			post.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
			response = httpClient.execute(post);
			if (response != null && response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				result = EntityUtils.toString(entity);
			}
			log.info("Response status code: " + response.getStatusLine().getStatusCode());
			log.debug("Response body: \n" + result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				httpClient.close();
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @Description： Post 请求 请求参数类型为Json
	 * @author：James
	 * @return: String 
	 * @throws：
	 */
	public String doPost(String url, String jsonParams, Map<String, String> headers) {
		String result = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost post = new HttpPost(url);
		CloseableHttpResponse response = null;

		log.info("Invoking method: doPost(String, Map)...");
		log.info("Request URL：" + url);
		log.info("Request params:" + jsonParams);
		log.debug("Request headers: " + headers);

		try {
			// 添加 headers:
			if (null != headers && !headers.isEmpty()) {
				Iterator<String> it = headers.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					String value = headers.get(key);
					post.setHeader(key, value);
				}
			}

			post.setEntity(new ByteArrayEntity(jsonParams.getBytes("UTF-8")));
			response = httpClient.execute(post);
			if (response != null && response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				result = EntityUtils.toString(entity, "UTF-8");
			}

			log.info("Response status code: " + response.getStatusLine().getStatusCode());
			log.debug("Response Body: \n" + result);

			return result;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				httpClient.close();
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void main(String[] args) {
		RestApiTestTool api = new RestApiTestTool();
		String result = api.doGet("http://172.20.7.2:10005/webapp/index/login.html");
		// System.out.println(result);

		String url = "http://172.20.7.2:10003/rest/product.json/insert/63f4e6736299";
		String jsonParams = "{\"productype\":1,\"longurl\":\"http://www.baidu.com\",\"productname\":\"ADT-GAME-iOS\",\"platformname\":\"IOS\",\"publishstatus\":1,\"appkey\":\"\",\"appimg\":\"\",\"extValue\":{\"miniprogram_appid\":\"\",\"miniprogram_appsecret\":\"\"}}";
		Map<String, String> map = new HashMap<>();
		map.put("Content-Type ", "application/json");
		String r = api.doPost(url, jsonParams, map);
		System.out.println(r);
		
		
	}
}
