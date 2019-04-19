package com.service.serviceImpl;

import com.mapper.PhotoMapper;
import com.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: PK
 * Date: 2019/4/1
 * Time: 10:26
 */
@Service
public class PhotoServiceImpl implements PhotoService {

    @Autowired
    private PhotoMapper photoMapper;

    @Override
    public Map<String,Object> insertPhoto(Map<String, Object> map ) {
        Map<String,Object> resultData=new HashMap<String,Object>();
        Map<String, Object> param=new HashMap<String, Object>();
        Date date=new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString=sdf.format(date);
        param.put("currentDateInfo",dateString);
        param.put("userId",map.get("userId"));
        Map<String,Object> map1=photoMapper.getDataByCurrentDate(param);//当天的记录
        map.put("currentDateInfo",dateString);
        if(null==map1){
            //第一次存储图片
            map.put("firstCreateTime",sdf.format(new Date()));
            map.put("status",1);
            map.put("firstNumTool",map.get("numTool"));
            photoMapper.insertPhoto(map);
        }
        //第二次 第三次  第四次或者更多
        if(null!=map1){
            map.put("status",2);
            map.put("secondNumTool",map.get("numTool"));
            map.put("secondCreateTime",sdf.format(new Date()));
            map.put("secondPhotoName",map.get("firstPhotoName"));
            map.put("secondPhotoPath",map.get("firstPhotoPath"));
            map.put("secondPhotoUrl",map.get("firstPhotoUrl"));
            photoMapper.updatePhoto(map);

            //比较两张涂工具数量
            Map<String,Object> map2=photoMapper.getDataByCurrentDate(param);
            String firstNumTool=map2.get("firstNumTool")+"";
            String secondNumTool=map2.get("secondNumTool")+"";
            if(!firstNumTool.equals(secondNumTool)){
                map.put("tip","第一次上传的图片图中数量与第二次不等");
            }
        }
        //之后
        map.remove("userId");
        return  map;
    }
}