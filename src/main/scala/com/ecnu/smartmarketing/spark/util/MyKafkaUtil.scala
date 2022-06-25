package com.ecnu.smartmarketing.spark.util

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}


object MyKafkaUtil {

  val kafkaParam: Map[String,Object] = Map(

    //1.消费者服务器
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "106.15.126.24:9092",
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    //2.消费者组
    ConsumerConfig.GROUP_ID_CONFIG -> "marketing",
    //3.如果没有初始化偏移量或者当前的偏移量不存在任何服务器上，可以使用这个配置属性
    //  可以使用这个配置，latest 自动重置偏移量为最新的偏移量
    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest",
    //4.如果是 true，则这个消费者的偏移量会在后台自动提交,但是 kafka 宕机容易丢失数据
    //  如果是 false，会需要手动维护 kafka 偏移量
    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (true: java.lang.Boolean)

  )

  // 创建 DStream，返回接收到的输入数据
  // LocationStrategies：根据给定的主题和集群地址创建 consumer
  // LocationStrategies.PreferConsistent：持续的在所有 Executor 之间分配分区
  // ConsumerStrategies：选择如何在 Driver 和 Executor 上创建和配置 Kafka Consumer
  // ConsumerStrategies.Subscribe：订阅一系列主题
  def getKafkaStream(topic: String, ssc: StreamingContext):
  InputDStream[ConsumerRecord[String, String]] = {
    val dStream: InputDStream[ConsumerRecord[String, String]] =
      KafkaUtils.createDirectStream[String, String](
        ssc,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Subscribe[String, String](Set(topic), kafkaParam))
    dStream
  }



}

