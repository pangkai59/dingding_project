package com.mapper;

import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * User: PK
 * Date: 2019/4/1
 * Time: 10:27
 */
@Repository
public interface PhotoMapper {

    int  insertPhoto(Map<String,Object> map);

    Map<String,Object> getDataByCurrentDate(Map<String,Object> map);

    int updatePhoto(Map<String,Object> map);

}