package com.ecnu.smartmarketing.spark.handler

import com.ecnu.smartmarketing.spark.bean.AdsLog

import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.Date
import com.ecnu.smartmarketing.spark.util.JDBCUtil
import org.apache.spark.streaming.dstream.DStream

object BlackListHandler {

  private val sdf = new SimpleDateFormat("yyyy-MM-dd")

  // TODO 判断点击用户是否在黑名单中
  def filterByBlackList(adsLogDStream: DStream[AdsLog]): DStream[AdsLog] = {
    adsLogDStream.transform(
      rdd => {
        rdd.filter(
          adsLog => {
            val connection: Connection = JDBCUtil.getConnection
            val bool: Boolean = JDBCUtil.isExist(connection,
              """
                |select * from black_list
                |where userid =?
                      """.stripMargin
              , Array(adsLog.userid))
            connection.close()
            !bool
          })
      })
  }

  // TODO 如果用户不在黑名单中，那么进行统计数量（每个采集周期）
  def addBlackList(filterAdsLogDSteam: DStream[AdsLog]): Unit = {

    //1.将数据接转换结构,统计当前批次中单日每个用户点击每个广告的总次数
    //  ads_log=>((date,user,adid),1)
    val dateUserAdToOne: DStream[((String, String, String), Long)] =
    filterAdsLogDSteam.map(
      adsLog => {
        val date: String = sdf.format(new Date(adsLog.timestamp))
        ((date, adsLog.userid, adsLog.adid), 1L)
      }
    )

    //2.统计单日每个用户点击每个广告的总次数
    //  ((date, user, adid), 1) => ((date, user, adid), count)
    val dateUserAdToCount: DStream[((String, String, String), Long)] =
    dateUserAdToOne.reduceByKey(_ + _)
    dateUserAdToCount.foreachRDD(
      rdd => {
        rdd.foreachPartition(
          iter => {
            val connection: Connection = JDBCUtil.getConnection
            iter.foreach {

              // TODO 如果没有超过阈值，那么需要将当天的广告点击数量进行更新。
              case ( ( dt, user, ad ), count ) => JDBCUtil.executeUpdate( connection,
                            """
                              |INSERT INTO user_ad_count (dt,userid,adid,count)
                              |VALUES (?,?,?,?)
                              |ON DUPLICATE KEY
                              |UPDATE count=count+?
                            """.stripMargin
                , Array(dt, user, ad, count, count))

                // TODO 判断更新后的点击数据是否超过阈值
                val ct: Long = JDBCUtil.getDataFromMysql(connection,
                            """
                              |select count from
                              |user_ad_count where dt =? and userid =? and adid =?
                            """.stripMargin
                  , Array(dt, user, ad))

                // TODO 如果统计数量超过点击阈值(30)，那么将用户拉入到黑名单
                if (ct >= 30) {
                  JDBCUtil.executeUpdate(connection,
                            """
                              |INSERT INTO black_list (userid)
                              |VALUES (?) ON DUPLICATE KEY update userid =?
                            """.stripMargin
                    , Array(user, user))
                }

            }
            connection.close()
          })
      })
  }


}
