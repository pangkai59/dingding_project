package com.controller;

import com.service.PhotoService;
import com.util.JsonUtils;
import com.util.PythonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import jnr.ffi.annotations.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: PK
 * Date: 2019/3/29
 * Time: 14:58
 */
@Api("图片上传处理需要的接口")
@RestController
@RequestMapping("/photo")
public class PhotoController {

    private final static Logger log= LoggerFactory.getLogger(PhotoController.class);

    @Value("${localtion}")
    private  String  localtion;

    @Value("${aliyunURL}")
    private  String  aliyunURL;

    @Autowired
    private PhotoService photoService;

    private final Map<String,Object> map=new ConcurrentHashMap<String,Object>();


    @ApiOperation(value = "图片上传" ,  notes="图片上传")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "图片", required = true, paramType = "query", dataType = "MultipartFile"),
            @ApiImplicitParam(name = "userId", value = "用户id", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "userName", value = "用户姓名", required = true, paramType = "query", dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value="/uploadFile",method = { RequestMethod.POST},produces = "application/json; charset=utf-8")
    public Map<String,Object> uploadFile( @RequestParam(value="file" ,required = true) MultipartFile file,@RequestParam(value="userId" ,required = true) Integer userId ,@RequestParam(value="userName" ,required = true) String userName) throws Exception {
        Map<String,Object> resultData=new HashMap<String,Object>();
        Map<String,Object> resultMap;
        String url="";
        String  numTool="";
        Map<String,Object> map=new HashMap<String,Object>();
        if (file.isEmpty()) {
            resultData.put("code",-1);
            resultData.put("msg","上传文件不可为空");
            return resultData;
        }

        Date date=new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString=sdf.format(date);
        String fileName = file.getOriginalFilename();
        fileName = userId+"_"+dateString + "_" +new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +"_"+ fileName;   //图片名称规则   用户id_日期_时间戳_名称
        String path =localtion+fileName;
        log.info("图片的存储路径------------------"+path);
        File dest = new File(path);
        if (dest.exists()) {
            resultData.put("code",-1);
            resultData.put("msg","文件已经存在");
            return resultData;
        }
        //判断文件父目录是否存在
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdir();
        }
        file.transferTo(dest); //保存文件

        try {
            //此处调用python算法判断图片是否合格
            //'num_tools'为工具数量;'edge_near'值为1代表有靠近边缘的工具；'tools_near'值为1表示有靠的太近（或重叠）的工具
            try {

                long time1=System.currentTimeMillis();
                String fileResult=PythonUtils.getFileResult(path);
                log.info("py脚本返回fileResult-----------------"+fileResult);
                long time2=System.currentTimeMillis();
                long aa=(time2-time1)/1000;
                log.info("py脚本执行时间----"+aa);
                System.out.println(aa);
                Map  result=JsonUtils.jsonToMap(fileResult);


                 numTool= result.get("num_tools").toString();
                String edgeNear=result.get("edge_near").toString();
                String toolsNear=result.get("tools_near").toString();
                if(edgeNear.equals("1") && toolsNear.equals("1")){
                    resultData.put("code",-1);
                    resultData.put("msg","有靠近边缘的工具或有靠的太近（或重叠）的工具，请重新拍摄");
                    return resultData;
                }
                if(edgeNear.equals("1") && toolsNear.equals("0")){
                    resultData.put("code",-1);
                    resultData.put("msg","有靠近边缘的工具，请重新拍摄");
                    return resultData;
                }
                if(edgeNear.equals("0") && toolsNear.equals("1")){
                    resultData.put("code",-1);
                    resultData.put("msg","有靠的太近（或重叠）的工具，请重新拍摄");
                    return resultData;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

          //  url="http://localhost:9005/"+path;    服务器
            path=aliyunURL+"/"+fileName;
            map.put("firstPhotoName",fileName);
            map.put("firstPhotoPath",path);
            map.put("firstPhotoUrl",path);    //本地
            //   服务器   map.put("firstPhotoUrl",url);
            map.put("userId",userId);
            map.put("userName",userName);
            map.put("numTool",numTool);
            resultMap=photoService.insertPhoto(map);
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

        return  resultMap;
    }




/*           base64   考虑并发量过大时传输数据过大  不考虑这种方式
   private static boolean upload(String img, String mobile) throws IOException {
        if("".equals(img) || img == null) {
            return  false;
        }
        try {
            String ctxPath="myimage";
            String path = localtion + "/" + ctxPath + "/" + mobile + "_yyzz";
            PrintStream ps = new PrintStream(new FileOutputStream(path));
            ps.println(img);// 往文件里写入字符串
            return true;
        } catch (IOException ignore) {
            ignore.printStackTrace();
            return false;
        }
    }*/


}