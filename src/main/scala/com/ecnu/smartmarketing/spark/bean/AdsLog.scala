package com.ecnu.smartmarketing.spark.bean

/**
 * 广告日志表
 * @param timestamp 时间戳
 * @param area 地区
 * @param city 城市
 * @param userid 用户ID
 * @param adid 广告ID
 */
case class AdsLog(timestamp: Long,
                  area: String,
                  city: String,
                  userid: String,
                  adid: String)
