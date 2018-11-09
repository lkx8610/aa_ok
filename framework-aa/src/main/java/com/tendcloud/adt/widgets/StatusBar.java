package com.tendcloud.adt.widgets;

import com.tendcloud.adt.pages.common.AbstractBasePage;

/**
 * 在main page的一些元素是在其它页面中都存在的，故提取到一个类中；
 * 这些元素如：
 * 	--搜索框
 *  --今日、昨日、近7日
 * @author James Guo
 *
 */
public class StatusBar extends AbstractBasePage{

	//top: 今日、昨日、最近7日
		enum TopDateButton{
			TODAY("//div[@class='chooseTime']/descendant::li[1]"),
			YESTERDAY("//div[@class='chooseTime']/descendant::li[2]"),
			LAST7DAYS("//div[@class='chooseTime']/descendant::li[last()");
			String xpath; 
			
			TopDateButton(String xpath){
				this.xpath = xpath;
			}
			
			public String getName(){
				return this.xpath;
			}
		}
}
