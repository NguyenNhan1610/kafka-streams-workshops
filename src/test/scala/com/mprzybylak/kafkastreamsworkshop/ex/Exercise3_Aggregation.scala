package com.mprzybylak.kafkastreamsworkshop.ex

import com.madewithtea.mockedstreams.MockedStreams
import com.mprzybylak.kafkastreamsworkshop.internals.KafkaStreamsTest
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.{KGroupedStream, KStream, KTable}

class Exercise3_Aggregation extends KafkaStreamsTest {

  val strings: Serde[String] = Serdes.String()
  val integers: Serde[Integer] = Serdes.Integer()
  val longs: Serde[java.lang.Long] = Serdes.Long()

  private val BCN = "Barcelona"
  private val MUN = "Munich"

  it should "group input stream of concert ticket sold" in {

    // key = band name
    // value = first and last name of ticket buyer
    val inputTopic = Seq[(String, String)](
      ("Metallica", "Yaniv Jelinek"), ("Rammstein", "Anthea Escamilla"), ("Metallica", "Satu Sachs"), ("Luis Fonsi", "Pilar Rademakers"),
      ("Metallica", "Augusta Dorsey"), ("Rammstein", "Ghislain Kovachev"), ("Rammstein", "Jaffar Hoedemaekers"), ("Metallica", "Samson Kozel"),
      ("Luis Fonsi", "Rosana Meggyesfalvi"), ("Rammstein", "Irmgard Borchard")
    )

    val outputTopic = Seq[(String, Long)](
      ("Metallica", 1), ("Rammstein", 1), ("Metallica", 2), ("Luis Fonsi", 1),
      ("Metallica", 3), ("Rammstein", 2), ("Rammstein", 3), ("Metallica", 4),
      ("Luis Fonsi", 2), ("Rammstein", 4)
    )

    MockedStreams()
      .topology(builder => {
        // FILL ME
      })
      .config(config(strings, strings))
      .input(INPUT_TOPIC_NAME, strings, strings, inputTopic)
      .output(OUTPUT_TOPIC_NAME, strings, integers, outputTopic.size) shouldEqual outputTopic
  }

  it should "find maximum high score on flipper machine per user" in {

    // GIVEN
    val inputTopic = Seq[(String, Integer)](
      ("user1", 101), // key = player name; value = score
      ("johny16", 212),
      ("johny16", 512),
      ("johny16", 300),
      ("user1", 1000),
      ("asdf", 24),
      ("user1", 1112),
      ("asdf", 0),
      ("johny16", 680),
      ("user1", 900)
    )

    val outputTopic = Seq[(String, Integer)](
      ("user1", 101),
      ("johny16", 212),
      ("johny16", 512),
      ("johny16", 512),
      ("user1", 1000),
      ("asdf", 24),
      ("user1", 1112),
      ("asdf", 24),
      ("johny16", 680),
      ("user1", 1112),
    )

    MockedStreams()

      //WHEN
      .topology(builder => {
        // FILL ME
      })
      .config(config(strings, integers))
      .input(INPUT_TOPIC_NAME, strings, integers, inputTopic)

      // THEN
      .output(OUTPUT_TOPIC_NAME, strings, integers, outputTopic.size) shouldEqual outputTopic
  }
}
