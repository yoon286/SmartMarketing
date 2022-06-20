package com.ecnu.smartmarketing.service;

import com.ecnu.smartmarketing.entity.Advertisement;
import com.ecnu.smartmarketing.mapper.AdvertisementLabelMapper;
import org.redisson.api.RList;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.DoubleCodec;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author yoon
 * @description: TODO
 * @date 2022/6/1520:25
 */
@Service
public class AdService {
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AdvertisementLabelMapper advertisementLabelMapper;

    public String getAd(String userId,String page){

        //1.从redis中获取userId标签
        RList<String> userLabelRList = redissonClient.getList("label:userId:"+userId,new StringCodec());
        List<String> userLabelList = userLabelRList.range(0,-1);
        //2.从redis中获取url标签
        RList<String> pageLabelRList = redissonClient.getList("label:page:"+page,new StringCodec());
        List<String> pageLabelList = pageLabelRList.range(0,-1);
        //3.取两个集合并集作为广告搜索的条件
        userLabelList.addAll(pageLabelList);
        Set<String> labelList=new HashSet<>(userLabelList);
        //4.根据标签进行广告检索
        List<Advertisement> advertisementList=advertisementLabelMapper.selectByLabel(labelList);
        System.out.println(advertisementList);
        //5.广告排序-点击率和出价的乘积对广告排序
        HashMap<Advertisement,Double> map=new HashMap<>();
        for (Advertisement ad : advertisementList) {
            Double rtc= (Double) redissonClient.getBucket("ctr:"+ad.getId(),new DoubleCodec()).get();
            map.put(ad,rtc*ad.getPrice());
        }
        List<Map.Entry<Advertisement, Double>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        list.forEach(System.out::println);
        //6.返回广告
        return list.get(list.size()-1).getKey().getUrl();
    }
}
