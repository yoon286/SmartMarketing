package com.ecnu.smartmarketing;


import com.ecnu.smartmarketing.service.AdService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author yoon
 * @description: TODO
 * @date 2022/6/1420:59
 */
@SpringBootTest(classes= SmartMarketingApplication.class)
@RunWith(SpringRunner.class)
public class Test {
    @Autowired
    AdService adService;

    @org.junit.Test
    public void test(){
        adService.getAd("171475","www.baidu.com");
    }
}
