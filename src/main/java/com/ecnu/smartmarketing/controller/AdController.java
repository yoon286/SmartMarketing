package com.ecnu.smartmarketing.controller;

import com.ecnu.smartmarketing.entity.Result;
import com.ecnu.smartmarketing.service.AdService;
import com.ecnu.smartmarketing.service.KeyWordSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author yoon
 * @description: TODO
 * @date 2022/6/14 21:21
 */
@RestController
public class AdController {
    @Resource
    private AdService adService;

    @Resource
    private KeyWordSearchService keyWordSearchService;

    @GetMapping("/ad")
    public Result getAd(@RequestParam("userId") String userId, @RequestParam("url") String url){
        return Result.success(adService.getAd(userId,url));
    }

    @GetMapping("/search")
    public Result getKeyWord(@RequestParam("word") String keyWord) throws IOException {
        return Result.success(keyWordSearchService.searchByKeyWord(keyWord));
    }

}
