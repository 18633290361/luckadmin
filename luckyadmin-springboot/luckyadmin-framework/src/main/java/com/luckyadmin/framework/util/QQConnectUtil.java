package com.luckyadmin.framework.util;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckyadmin.system.mapper.SysConfigMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QQConnectUtil {

	@Autowired
	SysConfigMapper configMapper;

	public String GetAppId() {
		return configMapper.selectValueByKey("sys.oauth.qq.appid");
	}
	public String GetAppKey() {
		return configMapper.selectValueByKey("sys.oauth.qq.appkey");
	}
	public String GetCallbackURL() {
		return configMapper.selectValueByKey("sys.oauth.qq.callback");
	}
	
	private static final String URL_AccessToken="https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=APPID&client_secret=APPKEY&code=CODE&redirect_uri=URI";
	private static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 去腾讯验证的URL
	 * @param state
	 * @return
	 */
	public String getLoginURL(String state) {
		String url="https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=APPID&redirect_uri=URI&state=STATE";
		url=url.replaceAll("APPID", GetAppId());
		url=url.replaceAll("URI", GetCallbackURL());
		url=url.replaceAll("STATE",state );
		return url;
	}
	
	public String getAccessToken(String authorizationCode) {
		String url = URL_AccessToken;
		url=url.replaceAll("APPID", GetAppId());
		url=url.replaceAll("APPKEY", GetAppKey());
		url=url.replaceAll("CODE", authorizationCode);
		url=url.replaceAll("URI", GetCallbackURL());
		System.out.println(url);
		String text=HttpClientUtil.doGet(url);
		int start = text.indexOf("=")+1;
		int end = text.indexOf("&");
		System.out.println(text);
		return text.substring(start,end);
	}
	
	public String getOpenid(String accessToken) throws IOException {
		String url = "https://graph.qq.com/oauth2.0/me?access_token="+accessToken;
		String text=HttpClientUtil.doGet(url);
		System.out.println(text);
		int start = text.indexOf("callback(")+9;
		int end = text.indexOf(")");
		text=text.substring(start,end);
		JsonNode node = objectMapper.readTree(text);
		return node.get("openid").asText();
	}
	
	public UserInfo getUserInfo(String openid,String token) throws IOException {
		String url="https://graph.qq.com/user/get_user_info?access_token=TOKEN&oauth_consumer_key=APPID&openid=OPENID";
		url=url.replaceAll("TOKEN", token);
		url=url.replaceAll("APPID", GetAppId());
		url=url.replaceAll("OPENID", openid);
		String text = HttpClientUtil.doGet(url);
		return objectMapper.readValue(text, UserInfo.class);
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class UserInfo {
		public int ret;
		public String msg;
		public int is_lost;
		public String nickname;
		public String gender;
		public String province;	//省
		public String city;
		public String year;		//出生年
		public String constellation;
		public String figureurl;	//QQ空间头像
		public String figureurl_1;
		public String figureurl_2;
		public String figureurl_qq_1;
		public String figureurl_qq_2;
		public String figureurl_qq;	//QQ头像
		public String figureurl_type;

		@Override
		public String toString() {
			try {
				return objectMapper.writeValueAsString(this);
			} catch (JsonProcessingException e) {
				return super.toString();
			}
		}
	}



}
