# 广告投放系统 

#### 1. 应用需求描述

广告系统中必不可少的部分是一个实时响应广告请求，并决策广告的投放引擎。一般来说，广告系统的投放引擎采用类搜索的架构，即检索加排序的两阶段决策过程。

在本项目中，我们主要实现了处理

###### 广告请求

主要流程包括：

> 接受广告前端 Web 服务器发来的请求 —> 解析请求 —> 搜索前预处理 —> 判断有效广告位 —> 记录请求 —> 是否进入搜索 —> 查询用户属性 —> 交易类型判断 —> lucene搜索 —> 构造回调基本信息 —> 封装响应 —> 返回

###### 广告检索

使用Lucene进行索引倒排，根据搜索关键词，获取排名前10的关键结果，从广告索引（ad index）中查找符合条件的广告候选。



广告投放机的主要任务是与其他各个功能模块打交道，并将它们串联起来完成在线广告投放决策。同时，我们实现了:

###### 广告监测功能

（1）在线反作弊（anti-spam）。实时判断流量来源中是否有作弊流量，并且将这部分流量从后续的计价和统计中去除掉。

（2）在线行为反馈，包括实时受众定向（real-time targeting）和实时点击反馈（realtimeclick feedback）等部分。

由于是学习使用的样例，因此没有真实的数据来源，所以先模拟真实的用户访问行为，将生成的广告日志发送至Kafka ，再通过Kafka进行消费，实现以下功能：

###### 广告黑名单

实现实时的动态黑名单机制，将每天对某个广告点击超过 100 次的用户拉黑。 注：黑名单保存到 MySQL 中。

###### 广告点击量实时统计

实时统计每天各地区各城市各广告的点击总流量，并将其存入 MySQL。

###### 最近一小时广告点击量

实时统计最近一小时的各个广告的点击量。



## 主要技术栈：

##### Spring Boot + Redis + Kafka + Lucene + Spark Streaming + Zookeeper 



## 小组分工：

尹丹：广告检索 + 广告黑名单 + 点击量实时统计 + 流程设计 + 文档撰写

黄伟勋：广告请求 + 最近一小时广告点击量 + 框架设计



---

#### 2. 实现思路

###### 处理广告请求：

1）使用切面限制用户分钟级访问次数，若当前用户已在黑名单中，请求结束。

2）若不在黑名单中，统计用户访问次数，限制一分钟之内只能访问10次

3）从redis中获取userId标签

4）从redis中获取url标签

5）取两个集合并集作为广告搜索的条件

7）广告排序-点击率和出价的乘积对广告排序

8）返回广告



###### 广告检索：

1）构建分词器 / 构建文档写入器 / 读取所有文件构建文档

2）构建索引读取器

3）构建索引查询器

4）执行查询，获取URL，标签等

5）返回检索结果



###### 广告黑名单：

1）读取 Kafka 数据之后，并对 MySQL 中存储的黑名单数据做校验；

 2）校验通过则对给用户点击广告次数累加一并存入 MySQL；

 3）在存入 MySQL 之后对数据做校验，如果单日超过 100 次则将该用户加入黑名单。

###### 广告点击量实时统计：

1）单个批次内对数据进行按照天维度的聚合统计;

 2）结合 MySQL 数据跟当前批次数据更新原有的数据。

###### 最近一小时广告点击量：

1）开窗确定时间范围； 

2）在窗口内将数据转换数据结构为((adid,hm),count); 

3）按照广告 id 进行分组处理，组内按照时分排序。



---

#### 3. 测试用例及结果截图

##### a.广告请求

![image-20220627213108726](src/main/resources/img/image-20220627213108726.png)

![image-20220627213126418](src/main/resources/img/image-20220627213126418.png)



![image-20220627213151646](src/main/resources/img/image-20220627213151646.png)

![image-20220627213204634](src/main/resources/img/image-20220627213204634.png)

![image-20220627213215269](src/main/resources/img/image-20220627213215269.png)

![image-20220627213223641](src/main/resources/img/image-20220627213223641.png)

![image-20220627213237426](src/main/resources/img/image-20220627213237426.png)

##### b.广告检索

- 建立倒排索引

![image-20220627225421572](src/main/resources/img/image-20220627225421572.png)

![image-20220627230251281](src/main/resources/img/image-20220627230251281.png)



![image-20220626170305804](src/main/resources/img/image-20220626170305804.png)

##### c. 模拟数据生成

```java
/**
 * 广告日志表
 * @param timestamp 时间戳
 * @param area 地区
 * @param city 城市
 * @param userid 用户ID
 * @param adid 广告ID
 */
```

![img.png](src/main/resources/img/img.png)

![img.png](src/main/resources/img/img0.png)

##### d.广告黑名单

![image-20220626165355401](src/main/resources/img/image-20220626165355401.png)

##### e.最近一小时广告点击量

![image-20220626165847253](src/main/resources/img/image-20220626165847253.png)

##### f.广告点击量实时统计

![img.png](src/main/resources/img/img2.png)



##### g.控制台日志归类

![image-20220626163451256](src\main\resources\img\image-20220626163451256.png)



#### 4. 程序的主要代码片段

##### a. 广告请求

```java
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
```

##### b.广告检索

```java
// 1. 构建分词器（IKAnalyzer）
IKAnalyzer ikAnalyzer = new IKAnalyzer();

// 2. 构建文档写入器配置（IndexWriterConfig）
IndexWriterConfig indexWriterConfig = new IndexWriterConfig(ikAnalyzer);

// 3. 构建文档写入器（IndexWriter）
IndexWriter indexWriter = new IndexWriter(
        FSDirectory.open(Paths.get("D:\\Learning\\MasterCourses\\SmartMarketing\\index")), indexWriterConfig);

// 4. 读取所有文件构建文档
File articleDir = new File("D:\\Learning\\MasterCourses\\SmartMarketing\\data");
File[] fileList = articleDir.listFiles();


for (File file : Objects.requireNonNull(fileList)) {

    // 5. 文档中添加字段
    CsvReader reader = CsvUtil.getReader();
    //从文件中读取CSV数据
    CsvData data = reader.read(file);
    List<CsvRow> rows = data.getRows();
    //遍历行
    for (CsvRow csvRow : rows) {
        //getRawList返回一个List列表，列表的每一项为CSV中的一个单元格（既逗号分隔部分）
        Console.log(csvRow.getRawList());

        // 6. 写入文档
        Document document = new Document();
        document.add(new TextField("url", csvRow.get(0), Field.Store.YES));
        document.add(new TextField("keyWord", csvRow.toString(), Field.Store.NO));
        document.add(new StoredField("path", file.getAbsolutePath() + "/" + file.getName()));
        indexWriter.addDocument(document);
    }
}
// 7. 关闭写入器
indexWriter.close();
```

##### c. MarketingApp 主程序

```java
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

    //5.根据 MySQL 中的黑名单过滤当前数据集
    val filterAdsLogDStream: DStream[AdsLog] =
      BlackListHandler.filterByBlackList(adsLogDStream)

    //6.将满足要求的用户写入黑名单
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
```

##### d. BlackListHandler

```java
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
```

##### e. DateAreaCityAdCountHandler

```java
package com.ecnu.smartmarketing.spark.handler

import com.ecnu.smartmarketing.spark.bean.AdsLog
import com.ecnu.smartmarketing.spark.util.JDBCUtil
import org.apache.spark.streaming.dstream.DStream

import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.Date

object DateAreaCityAdCountHandler {
  //时间格式化对象
  private val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")

  /**
   * 统计每天各大区各个城市广告点击总数并保存至 MySQL 中
   *
   * @param filterAdsLogDStream 根据黑名单过滤后的数据集
   */
  def saveDateAreaCityAdCountToMysql(filterAdsLogDStream: DStream[AdsLog]): Unit
  = {
    //1.统计每天各大区各个城市广告点击总数
    val dateAreaCityAdToCount: DStream[((String, String, String, String), Long)] =
      filterAdsLogDStream.map(ads_log => {
        //a.取出时间戳
        val timestamp: Long = ads_log.timestamp
        //b.格式化为日期字符串
        val dt: String = sdf.format(new Date(timestamp))
        //c.组合,返回
        ((dt, ads_log.area, ads_log.city, ads_log.adid), 1L)
      }).reduceByKey(_ + _)
    //2.将单个批次统计之后的数据集合 MySQL 数据对原有的数据更新
    dateAreaCityAdToCount.foreachRDD(rdd => {
      //对每个分区单独处理
      rdd.foreachPartition(iter => {
        //a.获取连接
        val connection: Connection = JDBCUtil.getConnection
        //b.写库
        iter.foreach { case ((dt, area, city, adid), count) =>
          JDBCUtil.executeUpdate(connection,
            """
              |INSERT INTO area_city_ad_count (dt,area,city,adid,count)
              |VALUES(?,?,?,?,?)
              |ON DUPLICATE KEY
              |UPDATE count=count+?;
            """.stripMargin,
            Array(dt, area, city, adid, count, count))
        }
        //c.释放连接
        connection.close()
      })
    })
  }
}
```

##### f. LastHourAdCountHandler

```java
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
```

##### g. 建表语句

```sql
/*
 Navicat Premium Data Transfer

 Source Server         : smartMarketing
 Source Server Type    : MySQL
 Source Server Version : 80029
 Source Host           : 106.15.126.24:3306
 Source Schema         : marketing

 Target Server Type    : MySQL
 Target Server Version : 80029
 File Encoding         : 65001

 Date: 23/06/2022 03:22:12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for advertisement
-- ----------------------------
DROP TABLE IF EXISTS `advertisement`;
CREATE TABLE `advertisement`
(
    `id`    int                                                    NOT NULL AUTO_INCREMENT,
    `name`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '广告名称',
    `url`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '广告url',
    `price` double(10, 2)                                          NULL DEFAULT NULL COMMENT '广告报价',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of advertisement
-- ----------------------------
INSERT INTO `advertisement`
VALUES (1, '汽车广告1', 'www.car1.com', 11.00);
INSERT INTO `advertisement`
VALUES (2, '汽车广告2', 'www.car2.com', 44.00);

-- ----------------------------
-- Table structure for advertisement_label
-- ----------------------------
DROP TABLE IF EXISTS `advertisement_label`;
CREATE TABLE `advertisement_label`
(
    `advertisementId` int NOT NULL COMMENT '广告id',
    `labelId`         int NULL DEFAULT NULL COMMENT '标签id',
    PRIMARY KEY (`advertisementId`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of advertisement_label
-- ----------------------------
INSERT INTO `advertisement_label`
VALUES (1, 1);
INSERT INTO `advertisement_label`
VALUES (2, 1);

-- ----------------------------
-- Table structure for label
-- ----------------------------
DROP TABLE IF EXISTS `label`;
CREATE TABLE `label`
(
    `id`   int                                                    NOT NULL AUTO_INCREMENT,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '标签名称',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of label
-- ----------------------------
INSERT INTO `label`
VALUES (1, 'car');

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for black_list
-- ----------------------------

CREATE TABLE black_list
(
    userid CHAR(1) PRIMARY KEY
);


-- ----------------------------
-- Table structure for user_ad_count
-- ----------------------------

CREATE TABLE user_ad_count
(
    dt     varchar(255),
    userid CHAR(1),
    adid   CHAR(1),
    count  BIGINT,
    PRIMARY KEY (dt, userid, adid)
);

-- ----------------------------
-- Table structure for area_city_ad_count
-- ----------------------------
CREATE TABLE area_city_ad_count
(
    dt    VARCHAR(20),
    area  VARCHAR(100),
    city  VARCHAR(100),
    adid  VARCHAR(20),
    count BIGINT,
    PRIMARY KEY (dt, area, city, adid)
);

```