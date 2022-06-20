package com.ecnu.smartmarketing.filter;

import com.ecnu.smartmarketing.entity.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author yoon
 * @description: TODO
 * @date 2022/6/1421:12
 */
@Order(0)
@Aspect
@Component
public class AccessInterceptor{

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 定义拦截器规则
     */
    @Pointcut("execution(* com.ecnu.smartmarketing.controller.AdController.*(..))")
    public void pointCut() {
    }

    @Around(value = "pointCut()")
    public Object before(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map map = request.getParameterMap();
        String userId = ((String[]) map.get("userId"))[0];
        RList<String> blackRList = redissonClient.getList("black:userId:list");
        List<String> blackList = blackRList.range(0,-1);
        for (String blackId : blackList) {
            //当前用户已在黑名单中，请求结束
            if (userId.equals(blackId)) {
                return Result.error("-1","您已被加入黑名单");
            }
        }
        //统计用户访问次数，限制一分钟之内只能访问10次
        RAtomicLong count = redissonClient.getAtomicLong("access:limit:userId:" + userId);
        if (!count.isExists()) {
            count.incrementAndGet();
            count.expire(60,TimeUnit.SECONDS);

        } else {
            if (count.get() >= 5) {
                //将用户加入黑名单
                blackRList.add(userId);

            } else {
                count.incrementAndGet();
            }
        }
        //调用执行目标方法
        return point.proceed();
    }
}
