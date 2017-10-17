package com.mprzybylak.kafkastreamsworkshop.ex

import com.madewithtea.mockedstreams.MockedStreams
import com.mprzybylak.kafkastreamsworkshop.internals.KafkaStreamsTest
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.{KGroupedStream, KStream, KTable}

class Exercise3_Aggregation extends KafkaStreamsTest {

  val strings: Serde[String] = Serdes.String()
  val integers: Serde[Integer] = Serdes.Integer()
  val longs: Serde[java.lang.Long] = Serdes.Long()

  it should "group input stream of concert ticket sold" in {

    // key = band name
    // value = first and last name of ticket buyer
    val inputTopic = Seq[(String, String)](
      ("Metallica", "Yaniv Jelinek"),("Rammstein", "Anthea Escamilla"),("Metallica", "Satu Sachs"),("Luis Fonsi", "Pilar Rademakers"),
      ("Metallica", "Augusta Dorsey"),("Rammstein", "Ghislain Kovachev"),("Rammstein", "Jaffar Hoedemaekers"),("Metallica", "Samson Kozel"),
      ("Luis Fonsi", "Rosana Meggyesfalvi"),("Rammstein", "Irmgard Borchard")
    )

    val outputTopic = Seq[(String, Long)](
      ("Metallica", 1),("Rammstein", 1),("Metallica", 2),("Luis Fonsi", 1),
      ("Metallica", 3),("Rammstein", 2),("Rammstein", 3),("Metallica", 4),
      ("Luis Fonsi", 2),("Rammstein", 4)
    )

    MockedStreams()
      .topology(builder => {
        val source: KStream[String, String] = builder.stream(INPUT_TOPIC_NAME)
        val group: KGroupedStream[String, String] = source.groupByKey(strings, strings)
        val count: KTable[String, java.lang.Long] = group.count("ticketCount")
        val intCount:KTable[String, Integer] = count.mapValues(v => v.toInt)
        intCount.toStream().to(strings, integers, OUTPUT_TOPIC_NAME)
      })
      .config(config(strings, strings))
      .input(INPUT_TOPIC_NAME, strings, strings, inputTopic)
      .output(OUTPUT_TOPIC_NAME, strings, integers, outputTopic.size) shouldEqual outputTopic


  }

}
