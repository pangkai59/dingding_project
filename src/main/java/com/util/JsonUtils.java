package com.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * JSON操作工具类
 */
public class JsonUtils {

    /**
     * 字符串转json对象
     *
     * @param str
     * @param split
     * @return
     */
    public static JSONObject decode(String str, String split)
    {
        JSONObject json = new JSONObject();
        try {
            String[] arrStr = str.split(split);
            for (int i = 0; i < arrStr.length; i++) {
                String[] arrKeyValue = arrStr[i].split("=");
                json.put(arrKeyValue[0], arrStr[i].substring(arrKeyValue[0].length() + 1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    /**
     * 字符串转对象
     * @param str
     * @return
     */
    public static JSONObject decode(String str)
    {
        try {
            return JSON.parseObject(str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    /**
     * 把对象变成JSON字符串
     * @param obj obj对象必须带上setter和getter
     * @return
     */
    public static String encode(Object obj)
    {
        try {
            return JSON.toJSONString(obj);
        }catch (Exception e) {
            e.printStackTrace();
        }


        return "";
    }

    /**
     * 将json格式的字符串解析成Map对象 <li>
     * json格式：{"name":"admin","retries":"3fff","testname"
     * :"ddd","testretries":"fffffffff"}
     */
    public static Map<String, Object> jsonToMap(Object object)
    {
        if (object == null) return null;
        Map<String, Object> data = new HashMap<String, Object>();
        // 将json字符串转换成jsonObject
        JSONObject jsonObject = null;
        if (object instanceof String == false) {
            jsonObject = JSON.parseObject(JSON.toJSONString(object));
        }else{
            jsonObject = JSON.parseObject((String)object);
        }
        Iterator it = jsonObject.keySet().iterator();
        // 遍历jsonObject数据，添加到Map对象
        while (it.hasNext())
        {
            String key = String.valueOf(it.next());
            data.put(key, jsonObject.get(key));
        }
        return data;
    }

    /**
     *
     * 将json数组格式的字符串解析成Map对象 <li>
     * json格式：{"name":"admin","testname":"ddd","data":[{"retries":"3fff","testretries":"fffffffff"}]}
     * @param data <Object,Object> 空的集合存放Map数据
     * @param object json字符串
     * @return Map<String,Object> 集合
     * @throws
     * @author lyt
     */
    public static Map<Object, Object> jsonToMap(Map<Object, Object> data, Object object)
    {
        JSONObject jsonObject = null;
        if (object instanceof String == false) {
            jsonObject = JSON.parseObject(JSON.toJSONString(object));
        }else{
            jsonObject = JSON.parseObject((String)object);
        }
        // 将json字符串转换成jsonObject

        Iterator it = jsonObject.keySet().iterator();
        // 遍历jsonObject数据，添加到Map对象
        while (it.hasNext())
        {
            String key = String.valueOf(it.next());
            Object value =  jsonObject.get(key);
            if(value instanceof JSONObject){

                Iterator it2 = ((JSONObject) value).keySet().iterator();
                while (it2.hasNext()){
                    String key2 = String.valueOf(it2.next());
                    Object value2 =  ((JSONObject) value).get(key2);
                    data.put(key2, value2);
                }

            }
            data.put(key, value);
        }
        return data;
    }

    public static JSONArray jsonTojsonArray(Object obj)
    {
        if (obj == null) return null;
        // 将json字符串转换成jsonObject
        JSONObject jsonObject = null;
        if (obj instanceof String == false) {
            jsonObject = JSON.parseObject(JSON.toJSONString(obj));
        }else{
            jsonObject = JSON.parseObject((String)obj);
        }
        Iterator it = jsonObject.keySet().iterator();
        while (it.hasNext())
        {
            String key = String.valueOf(it.next());
            Object value =  jsonObject.get(key);
            if(value instanceof JSONArray){
                return (JSONArray)value;
            }
        }
        return null;
    }

    /**
     * 通过json字符串，返回当个实体类
     */
    public static <T> T getObjectJson(String str,Class<T> clazz){
        return JSON.parseObject(str,clazz);
    }
}
