package com.mprzybylak.kafkastreamsworkshop.answer

import com.madewithtea.mockedstreams.MockedStreams
import com.mprzybylak.kafkastreamsworkshop.internals.KafkaStreamsTest
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.{KStream, ValueMapper}

class Answer2_BasicFunctions extends KafkaStreamsTest {

  val strings: Serde[String] = Serdes.String()
  val integers: Serde[Integer] = Serdes.Integer()

  it should "capitalize first letter of words from input topic and pass it to output topic" in {

    // GIVEN
    val inputTopic = Seq(("1", "lorem"), ("2", "ipsum"), ("3", "dolor"), ("4", "sit"), ("5", "amet"))
    val outputTopic = Seq(("1", "Lorem"), ("2", "Ipsum"), ("3", "Dolor"), ("4", "Sit"), ("5", "Amet"))

    MockedStreams()

      // WHEN
      .topology(
        builder => {

          // create kstream from topic
          val source: KStream[String, String] = builder.stream(INPUT_TOPIC_NAME)

          // method mapValues allows to transform values in stream (without touching keys)
          val map = source.mapValues(_.capitalize)

          // store results in output topic
          map.to(OUTPUT_TOPIC_NAME)
        }
      )
      .config(config(strings, strings))
      .input(INPUT_TOPIC_NAME, strings, strings, inputTopic)

      // THEN
      .output(OUTPUT_TOPIC_NAME, strings, strings, outputTopic.size) shouldEqual outputTopic
  }

  it should "filter out odd numbers from input topic" in {

    // GIVEN
    val inputTopic = Seq[(String, Integer)](("1", 1), ("2", 2), ("3", 3), ("4", 4), ("5", 5))
    val outputTopic = Seq(("2", 2), ("4", 4))

    MockedStreams()

      // WHEN
      .topology(
        builder => {

          // create kstream from topic
          val source: KStream[String, Integer] = builder.stream(INPUT_TOPIC_NAME)

          // method filter allows to filter elements based on key and/or values
          // if we return true - current key-value pair will advance to the next processor
          // if we return false - current key-value pair will not advance to the next processor
          val filter: KStream[String, Integer] = source.filter((k, v) => v % 2 == 0)

          // store results in output topic
          filter.to(OUTPUT_TOPIC_NAME)
        }
      )
      .config(config(strings, integers))
      .input(INPUT_TOPIC_NAME, strings, integers, inputTopic)

      // THEN
      .output(OUTPUT_TOPIC_NAME, strings, integers, outputTopic.size) shouldEqual outputTopic
  }

  it should "log each request to blogging platform from request topic" in {

    // GIVEN
    val requestLogger = new RequestLogger() // use this class to log requests

    val inputTopic: Seq[(Integer, String)] = Seq(
      (12, "GET /posts"), // key = userId, value = request
      (88, "GET /posts/72"),
      (72, "POST /posts/72/comment { user: abc, comment: good article }"),
      (34, "POST /posts/72/comment { user: xyz, comment: I disagree }"),
      (72, "DELETE /posts/72/comment/15")
    )

    val expectedLogs: Seq[String] = Seq[String](
      "Request from user 12: GET /posts",
      "Request from user 88: GET /posts/72",
      "Request from user 72: POST /posts/72/comment { user: abc, comment: good article }",
      "Request from user 34: POST /posts/72/comment { user: xyz, comment: I disagree }",
      "Request from user 72: DELETE /posts/72/comment/15",
    )

    MockedStreams()

      // WHEN
      .topology(
        builder => {

          // create kstream from topic
          val source: KStream[Integer, String] = builder.stream(INPUT_TOPIC_NAME)

          // foreach method allows you to perform action, well - for each element in stream
          // it is so called terminal operation. That means that after it there will be no more processing
          // you cannot attach another processor after it (even sink)
          source.foreach((k, v) => requestLogger.log(k, v))
        })
      .config(config(integers, strings))
      .input(INPUT_TOPIC_NAME, integers, strings, inputTopic)
      .output(OUTPUT_TOPIC_NAME, integers, strings, expectedLogs.size)

    // THEN
    requestLogger.logList() shouldEqual expectedLogs
  }

  it should "spilit stream of sentences into stream of words" in {

    // GIVEN
    val inputTopic: Seq[(Integer, String)] = Seq[(Integer, String)](
      (1, "Lorem ipsum dolor sit amet, consectetur adipiscing elit."),
      (2, "Nulla euismod dui orci, porta bibendum sem aliquam quis."),
      (3, "Nam volutpat ultrices mauris vel rhoncus")
    )

    val expectedOutput: Seq[(Integer, String)] = Seq[(Integer, String)](
      (1, "Lorem"), (1, "ipsum"), (1, "dolor"), (1, "sit"), (1, "amet,"), (1, "consectetur"), (1, "adipiscing"), (1, "elit."),
      (2, "Nulla"), (2, "euismod"), (2, "dui"), (2, "orci,"), (2, "porta"), (2, "bibendum"), (2, "sem"), (2, "aliquam"), (2, "quis."),
      (3, "Nam"), (3, "volutpat"), (3, "ultrices"), (3, "mauris"), (3, "vel"), (3, "rhoncus")
    )

    MockedStreams()
      .topology(
        builder => {

          // create kstream from input topic
          val stream: KStream[Integer, String] = builder.stream(INPUT_TOPIC_NAME)

          // function String -> java.util.List[String] we will use it to split setences into list of words
          val stringToWords: ValueMapper[String, java.util.List[String]] = (k: String) => java.util.Arrays.asList(k.split(" "): _*)

          // even if our stringToWords is returning List of Strings - the flat maps will "unpack" those Lists
          // so we still have Integer->String stream here
          val flatMapValue: KStream[Integer, String] = stream.flatMapValues(stringToWords)

          // store results in output topic
          flatMapValue.to(OUTPUT_TOPIC_NAME)
        }
      )
      .config(config(integers, strings))
      .input(INPUT_TOPIC_NAME, integers, strings, inputTopic)
      .output(OUTPUT_TOPIC_NAME, integers, strings, expectedOutput.size)
  }
}