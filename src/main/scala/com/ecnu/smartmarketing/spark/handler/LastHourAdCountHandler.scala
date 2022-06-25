package com.ecnu.smartmarketing.spark.handler

import com.ecnu.smartmarketing.spark.bean.AdsLog

import java.text.SimpleDateFormat
import java.util.Date
import org.apache.spark.streaming.Minutes
import org.apache.spark.streaming.dstream.DStream

object LastHourAdCountHandler {

  private val sdf: SimpleDateFormat = new SimpleDateFormat("HH:mm")

  /**
   * 统计最近一小时(2分钟)广告分时点击总数
   *
   * @param filterAdsLogDStream 过滤后的数据集
   * @return
   */
  def getAdHourMintToCount(filterAdsLogDStream: DStream[AdsLog]):
  DStream[(String, List[(String, Long)])] = {

    //1.开窗 => 时间间隔为 1 个小时 window()
    val windowAdsLogDStream: DStream[AdsLog] =
      filterAdsLogDStream.window(Minutes(2))

    //2.转换数据结构 AdsLog =>((adid,hm),1L) map()
    val adHmToOneDStream: DStream[((String, String), Long)] =
      windowAdsLogDStream.map(
        adsLog => {
          val timestamp: Long = adsLog.timestamp
          val hm: String = sdf.format(new Date(timestamp))
          ((adsLog.adid, hm), 1L)
        }
      )

    //3.统计总数 ((adid,hm),1L)=>((adid,hm),sum) reduceBykey(_+_)
    val adHmToCountDStream: DStream[((String, String), Long)] =
      adHmToOneDStream.reduceByKey(_ + _)

    //4.转换数据结构 ((adid,hm),sum)=>(adid,(hm,sum)) map()
    val adToHmCountDStream: DStream[(String, (String, Long))] =
      adHmToCountDStream.map {
        case ((adid, hm), count) =>
          (adid, (hm, count))
      }

    //5.按照 adid 分组 (adid,(hm,sum))=>(adid,Iter[(hm,sum),...]) groupByKey
    adToHmCountDStream.groupByKey().mapValues(
      iter =>
        iter.toList.sortWith(_._1 < _._1)
    )

  }
}

