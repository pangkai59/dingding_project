package com.util;

import com.config.Constant;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiGetJsapiTicketRequest;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGetJsapiTicketResponse;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.oapi.lib.aes.DingTalkEncryptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.security.MessageDigest;
import java.util.Formatter;

import static com.config.URLConstant.URL_GET_TOKKEN;

/**
 * 获取access_token工具类
 */
public class AccessTokenUtil {
    private static final Logger log = LoggerFactory.getLogger(AccessTokenUtil.class);

    public static String getToken() throws RuntimeException {
            DefaultDingTalkClient client = new DefaultDingTalkClient(URL_GET_TOKKEN);
            OapiGettokenRequest request = new OapiGettokenRequest();

            request.setAppkey(Constant.APP_KEY);
            request.setAppsecret(Constant.APP_SECRET);
            request.setHttpMethod("GET");
        OapiGettokenResponse response = null;
        try {
            response = client.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String accessToken = response.getAccessToken();
            return accessToken;

    }

    public static void main(String[] args){
        String accessToken = AccessTokenUtil.getToken();
        String  JsTicket=getJsTicket();
        System.out.println(accessToken);
        System.out.println(JsTicket);
    }


    public static String getJsTicket() {
        DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/get_jsapi_ticket");
        OapiGetJsapiTicketRequest req = new OapiGetJsapiTicketRequest();
        req.setHttpMethod("GET");
        OapiGetJsapiTicketResponse execute = null;
        try {
            execute = client.execute(req, getToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return execute.getTicket();
    }

    public static String getJsApiSingnature(String url, String nonce, Long timeStamp, String jsTicket) throws DingTalkEncryptException {
        String plainTex = "jsapi_ticket=" + jsTicket + "&noncestr=" + nonce + "&timestamp=" + timeStamp + "&url=" + url;
        System.out.println(plainTex);
        String signature = "";

        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(plainTex.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
            return signature;
        } catch (Exception var7) {
            throw new DingTalkEncryptException(Integer.valueOf(900006));
        }
    }


    private static String byteToHex(byte[] hash) {
        Formatter formatter = new Formatter();
        byte[] var2 = hash;
        int var3 = hash.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            formatter.format("%02x", new Object[]{Byte.valueOf(b)});
        }

        String result = formatter.toString();
        formatter.close();
        return result;
    }

}
