package com.controller;

import com.config.Constant;
import com.config.URLConstant;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.dingtalk.oapi.lib.aes.DingTalkJsApiSingnature;
import com.dingtalk.oapi.lib.aes.Utils;
import com.taobao.api.internal.toplink.embedded.websocket.util.StringUtil;
import com.util.AccessTokenUtil;
import com.util.AuthHelper;
import com.util.RedisUtil;
import com.util.ServiceResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业内部E应用Quick-Start示例代码 实现了最简单的免密登录（免登）功能
 */
@Api("对接钉钉第三方需要的接口")
@Controller
@RequestMapping("/dingding")
public class IndexController {

    @Autowired
    private RedisUtil redisUtil;

    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    public static final String APP_SECRET="X7jL-WJHxm0ieBdRfOpwsBygGTZJu6kjEeNirL3aw0XP-YSHFigH3dczQ7YA4w__";


    @ApiOperation(value = "获取accessToken  jsTicket" ,  notes="获取accessToken  jsTicket     ")
    /**
    *@Description:    获取accessToken  jsTicket
    *@Param:
    *@Author: PK
    *@date: 2019/3/29
    */
    @RequestMapping(value = "/getTokens", method = RequestMethod.GET)
    @ResponseBody
    public ServiceResult getTokens( ) throws Exception{
        String accessToken;
        String  jsTicket;
        if(null==redisUtil.get("accessToken") || redisUtil.get("accessToken").equals("")){
             accessToken = AccessTokenUtil.getToken();
            redisUtil.set("accessToken",accessToken,Long.parseLong("7200"));
        } else{
            accessToken=redisUtil.get("accessToken");
        }

        synchronized(IndexController.class){
            if(null==redisUtil.get("jsTicket") || redisUtil.get("jsTicket").equals("")){
                jsTicket = AccessTokenUtil.getJsTicket();
                redisUtil.set("jsTicket",jsTicket,Long.parseLong("7200"));
            } else{
                jsTicket=redisUtil.get("jsTicket");
            }
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("jsTicket",jsTicket);
        resultMap.put("accessToken",accessToken);

        ServiceResult serviceResult = ServiceResult.success(resultMap);
        return serviceResult;
    }


    /**
    *@Description:    获取免登录秘钥
    *@Param:
    *@Author: PK
    *@date: 2019/3/29
    */
    @ApiOperation(value = "获取免登录秘钥" ,  notes="获取免登录秘钥")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "url", value = "免登的地址，就是钉钉管理平台上配置的url", required = true, paramType = "query", dataType = "String"),
    })
    @RequestMapping(value="/getJsConfig" ,method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object>  getJsConfig(@RequestParam(value = "url" ,required = false) String url) {
        Map<String,Object> JsApiConfig = new HashMap<String,Object>();
        String JSTicket;
        synchronized(IndexController.class){
            if(null==redisUtil.get("jsTicket") || redisUtil.get("jsTicket").equals("")){
                JSTicket = AccessTokenUtil.getJsTicket();
                redisUtil.set("jsTicket",JSTicket,Long.parseLong("7200"));
            } else{
                JSTicket=redisUtil.get("jsTicket");
            }
        }

        String nonceStr = Utils.getRandomStr(8);
        Long  timeStamp = System.currentTimeMillis();
       log.info(url);
        try {
            String signature = DingTalkJsApiSingnature.getJsApiSingnature(url ,nonceStr ,timeStamp ,JSTicket);
            log.info(JSTicket+","+nonceStr+","+timeStamp+","+url);

            JsApiConfig.put("url",url);
            JsApiConfig.put("jsTicket",JSTicket);
            JsApiConfig.put("signature",signature);
            JsApiConfig.put("nonceStr",nonceStr);
            JsApiConfig.put("timeStamp",timeStamp);
            JsApiConfig.put("corpId", Constant.CORP_ID);
            JsApiConfig.put("agentId", Constant.AGENT_ID);

            log.info(signature+","+nonceStr+","+timeStamp+","+Constant.CORP_ID+","+url);

        } catch (Exception e) {
            e.printStackTrace();
            log.info(" message ", e.getMessage());

        }
        return JsApiConfig;

    }




    /**
     * 钉钉用户登录，显示当前登录用户的userId和名称
     *
     * @param authCode 免登临时code
     */
    @ApiOperation(value = "钉钉用户登录，显示当前登录用户的userId和名称" ,  notes="钉钉用户登录，显示当前登录用户的userId和名称")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authCode", value = "免登临时code", required = true, paramType = "query", dataType = "String")
    })
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    @ResponseBody
    public ServiceResult login(@RequestParam(value = "authCode") String authCode)throws  Exception {
        Map<String, Object> resultMap = new HashMap<>();

        //获取accessToken,注意正是代码要有异常流处理
        String accessToken = AccessTokenUtil.getToken();

        //获取用户信息
        DingTalkClient client = new DefaultDingTalkClient(URLConstant.URL_GET_USER_INFO);
        OapiUserGetuserinfoRequest request = new OapiUserGetuserinfoRequest();
        request.setCode(authCode);
        request.setHttpMethod("GET");

        OapiUserGetuserinfoResponse response;
            response = client.execute(request, accessToken);
        //3.查询得到当前用户的userId
        // 获得到userId之后应用应该处理应用自身的登录会话管理（session）,避免后续的业务交互（前端到应用服务端）每次都要重新获取用户身份，提升用户体验
        String userId = response.getUserid();

        String userName = getUserName(accessToken, userId);
        System.out.println(userName);
        //返回结果
        resultMap.put("userId", userId);
        resultMap.put("userName", userName);

        ServiceResult serviceResult = ServiceResult.success(resultMap);
        return serviceResult;
    }


    /**
     * 获取用户详情
     *
     * @param    userId
     */
    @ApiOperation(value = "获取用户详情" ,  notes="获取用户详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "accessToken", value = "token", required = true, paramType = "query", dataType = "String")
    })
    @RequestMapping(value = "/getUserInfo", method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> login(@RequestParam(value = "accessToken") String accessToken,@RequestParam(value = "userId") String userId)throws  Exception {
        Map<String,Object> result=new HashMap<String,Object>();

        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/get");
        OapiUserGetRequest request = new OapiUserGetRequest();
        request.setUserid(userId);
        request.setHttpMethod("GET");
        OapiUserGetResponse response = client.execute(request, accessToken);
        result.put("userInfo",response);

        List<Object> b=new ArrayList<Object>();
        List<Long> list=response.getDepartment();
        //部门详情
        DingTalkClient client1 = new DefaultDingTalkClient("https://oapi.dingtalk.com/department/get");
        for( Long a : list){
            OapiDepartmentGetRequest request1 = new OapiDepartmentGetRequest();
            request1.setId(a+"");
            request1.setHttpMethod("GET");
            OapiDepartmentGetResponse response1 = client1.execute(request1, accessToken);
            b.add(response1);
        }
        result.put("depInfo",b);


        return result;
    }


    /**
     * 个人免登场景签名
     *
     * @param
     */
    @ApiOperation(value = "个人免登场景签名" ,  notes="个人免登场景签名")
    @RequestMapping(value = "/getUrlEncodeSignature", method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> getUrlEncodeSignature()throws  Exception {
        Map<String,Object> result=new HashMap<String,Object>();

        String stringToSign = System.currentTimeMillis()+"";
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(APP_SECRET.getBytes("UTF-8"), "HmacSHA256"));
        byte[] signatureBytes = mac.doFinal(stringToSign.getBytes("UTF-8"));
         String signature = new String(Base64.encodeBase64(signatureBytes));
        String urlEncodeSignature = urlEncode(signature,"utf-8");
        result.put("urlEncodeSignature",urlEncodeSignature);
        result.put("code",200);
        return result;
    }


    public static String urlEncode(String value, String encoding) {
        if (value == null) {
            return "";
        }
        try {
            String encoded = URLEncoder.encode(value, encoding);
            return encoded.replace("+", "%20").replace("*", "%2A")
                    .replace("~", "%7E").replace("/", "%2F");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("FailedToEncodeUri", e);
        }
    }


    /**
     * 获取用户姓名
     *
     * @param accessToken
     * @param userId
     * @return
     */
    private String getUserName(String accessToken, String userId) throws  Exception{
            DingTalkClient client = new DefaultDingTalkClient(URLConstant.URL_USER_GET);
            OapiUserGetRequest request = new OapiUserGetRequest();
            request.setUserid(userId);
            request.setHttpMethod("GET");
        OapiUserGetResponse response = null;
            response = client.execute(request, accessToken);
        return response.getName();
    }


    /**
     * 获取企业下的自定义空间
     *
     */
    @ApiOperation(value = "获取企业下的自定义空间" ,  notes="获取企业下的自定义空间")
    @RequestMapping(value = "/getCustomSpace", method = RequestMethod.GET)
    @ResponseBody
    public OapiCspaceGetCustomSpaceResponse getCustomSpace( )throws  Exception {
        String  accessToken = AccessTokenUtil.getToken();
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/cspace/get_custom_space");
        OapiCspaceGetCustomSpaceRequest request = new OapiCspaceGetCustomSpaceRequest();
        request.setAgentId(Constant.AGENT_ID);
        request.setDomain("test");
        request.setHttpMethod("GET");
        OapiCspaceGetCustomSpaceResponse response = client.execute(request,accessToken);
        return response;
    }


    /**
     * 授权用户访问企业自定义空间
     *
     */
    @ApiOperation(value = "授权用户访问企业自定义空间" ,  notes="授权用户访问企业自定义空间")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true, paramType = "query", dataType = "String"),
    })
    @RequestMapping(value = "/grantCustomSpacePermission", method = RequestMethod.GET)
    @ResponseBody
    public OapiCspaceGrantCustomSpaceResponse grantCustomSpacePermission( @RequestParam(value = "userId") String userId)throws  Exception {
          String  accessToken = AccessTokenUtil.getToken();
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/cspace/grant_custom_space");
        OapiCspaceGrantCustomSpaceRequest request = new OapiCspaceGrantCustomSpaceRequest();
        request.setAgentId(Constant.AGENT_ID);
        request.setDomain("test");
        request.setType("add");
        request.setUserid(userId);
        request.setPath("/test/");
        request.setDuration(10000L);
        request.setHttpMethod("GET");
        OapiCspaceGrantCustomSpaceResponse response = client.execute(request,accessToken);
        return response;
    }


}


