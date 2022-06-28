package com.ecnu.smartmarketing.mapper;

import com.ecnu.smartmarketing.entity.Advertisement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author yoon
 * @description: TODO
 * @date 2022/6/1522:08
 */
@Mapper
public interface AdvertisementLabelMapper {
    List<Advertisement> selectByLabel(@Param("labelList") Set<String> labelList);
}
