package com.ecnu.smartmarketing.spark

import com.ecnu.smartmarketing.spark.bean.AdsLog
import com.ecnu.smartmarketing.spark.handler.{BlackListHandler, DateAreaCityAdCountHandler, LastHourAdCountHandler}
import com.ecnu.smartmarketing.spark.util.MyKafkaUtil
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object MarketingApp {

  def main(args: Array[String]): Unit = {

    //1.创建 SparkConf
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("MarketingApp")

    //2.创建 StreamingContext
    val ssc = new StreamingContext(sparkConf, Seconds(3))

    //3.读取 Kafka 数据
    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] =
      MyKafkaUtil.getKafkaStream("marketingNew", ssc)

    //4.将每一行数据转换为样例类对象
    val adsLogDStream: DStream[AdsLog] = kafkaDStream.map(record => {
      val arr: Array[String] = record.value().split(" ")
      AdsLog(arr(0).toLong, arr(1), arr(2), arr(3), arr(4))
    })

    //5.需求一：根据 MySQL 中的黑名单过滤当前数据集
    val filterAdsLogDStream: DStream[AdsLog] =
      BlackListHandler.filterByBlackList(adsLogDStream)

    //6.需求一：将满足要求的用户写入黑名单
    //BlackListHandler.addBlackList(filterAdsLogDStream)

    //7.统计每天各大区各个城市广告点击总数并保存至 MySQL 中
    //DateAreaCityAdCountHandler.saveDateAreaCityAdCountToMysql(filterAdsLogDStream)

    //8.统计最近一小时(2 分钟)广告分时点击总数
    val adToHmCountListDStream: DStream[(String, List[(String, Long)])] =
      LastHourAdCountHandler.getAdHourMintToCount(filterAdsLogDStream)

    //9.打印
    adToHmCountListDStream.print()


    //启动任务
    ssc.start()
    ssc.awaitTermination()
  }
}
