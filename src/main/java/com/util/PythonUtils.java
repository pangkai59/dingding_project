package com.util;

import com.controller.PhotoController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * User: PK
 * Date: 2019/4/2
 * Time: 15:46
 */
public class PythonUtils {

    private final static Logger log= LoggerFactory.getLogger(PythonUtils.class);

    //输入：图片路径字符串（注意字符串里的路径是\\形式）
   //输出：json结构的结果out_data，其中'num_tools'为工具数量;'edge_near'值为1代表有靠近边缘的工具；'tools_near'值为1表示有靠的太近（或重叠）的工具。

    public   static String getFileResult (String fileUrl) throws Exception  {
        Process pr;
        String line="pu脚本错误返回。如果返回这个说明while没走--------------------------------------";
            //   本地   String[] args1=new String[]{"C:\\Users\\admin\\AppData\\Local\\Programs\\Python\\Python37\\Python.exe","D:\\local_work\\dingding_github\\src\\main\\java\\com\\config\\tools_recognize.py",fileUrl};
        log.info("py脚本进入----------------------------------------------------------------图片路径："+fileUrl);
          String[] args1=new String[]{"/usr/bin/python3","/opt/py/pythonScript.py",fileUrl};
        log.info("py脚本arg1----------------------------------------------------------------："+args1);
          try{
               pr=Runtime.getRuntime().exec(args1);
               log.info(Runtime.getRuntime().exec(args1)+"");
              BufferedReader in = new BufferedReader(new InputStreamReader(
                      pr.getInputStream()));
              log.info("py脚本中in的日志---------------"+   in);
              while ((line = in.readLine()) != null) {
                  in.close();
                  pr.waitFor();
                  log.info("py脚本返回"+   line);
                  return line;
              }
          }
        catch(Exception e){
              throw new Exception(e.getMessage());
        }

            return line;
    }

/*    public static void main(String[] args) throws IOException, InterruptedException {
        String a=getFileResult("D:\\photo\\4b8671a9cf62cf8cebdf26f303d309d.jpg");
        System.out.println(a);
    }*/

/*
    public static void main(String[] args) {
        try {
            System.out.println("start");
            String fileUrl="D:\\photo\\4b8671a9cf62cf8cebdf26f303d309d.jpg";
            String[] args1=new String[]{"C:\\Users\\admin\\AppData\\Local\\Programs\\Python\\Python37\\Python.exe","D:\\local_work\\dingding_github\\src\\main\\java\\com\\config\\tools_recognize.py",fileUrl};
            Process pr=Runtime.getRuntime().exec(args1);

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    pr.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            pr.waitFor();
            System.out.println("end");
        } catch (Exception e) {
            e.printStackTrace();
        }}
*/

}