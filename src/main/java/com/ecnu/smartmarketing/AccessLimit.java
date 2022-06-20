//package com.ecnu.smartmarketing;
//
//import org.redisson.api.RBucket;
//import org.redisson.api.RKeys;
//import org.redisson.api.RedissonClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Iterator;
//
///**
// * @author BWZW_171475
// * @description: TODO
// * @date 2022/6/1420:38
// */
//@Component
//public class AccessLimit {
//    @Autowired
//    private RedissonClient redissonClient;
//
//    public void bucket(){
//        while(true){
//            RKeys keys = redissonClient.getKeys();
//            Iterable<String> foundedKeys = keys.getKeysByPattern("access:limit*");
//            for(String key:foundedKeys){
//                RBucket<String> bucket=redissonClient.getBucket(key);
//                System.out.println(bucket);
//
//            }
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//    }
//
//    public void initUserLimit(String userId){
//
//    }
//}
