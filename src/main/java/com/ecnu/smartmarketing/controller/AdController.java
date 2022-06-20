package com.ecnu.smartmarketing.controller;

import com.ecnu.smartmarketing.entity.Result;
import com.ecnu.smartmarketing.service.AdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yoon
 * @description: TODO
 * @date 2022/6/14 21:21
 */
@RestController
public class AdController {
    @Autowired
    private AdService adService;
    @GetMapping("/ad")
    public Result getAd(@RequestParam("userId") String userId, @RequestParam("url") String url){
        return Result.success(adService.getAd(userId,url));
    }
}
