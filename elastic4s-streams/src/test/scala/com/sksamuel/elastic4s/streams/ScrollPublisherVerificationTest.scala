package com.sksamuel.elastic4s.streams

import akka.actor.ActorSystem
import com.sksamuel.elastic4s.jackson.ElasticJackson
import com.sksamuel.elastic4s.searches.RichSearchHit
import com.sksamuel.elastic4s.testkit.{AbstractElasticSugar, ClassLocalNodeProvider, ElasticSugar}
import org.reactivestreams.Publisher
import org.reactivestreams.tck.{PublisherVerification, TestEnvironment}
import org.scalatest.testng.TestNGSuiteLike

class ScrollPublisherVerificationTest
  extends PublisherVerification[RichSearchHit](
    new TestEnvironment(DEFAULT_TIMEOUT_MILLIS),
    PUBLISHER_REFERENCE_CLEANUP_TIMEOUT_MILLIS
  ) with AbstractElasticSugar with TestNGSuiteLike with ClassLocalNodeProvider {

  import ElasticJackson.Implicits._

  implicit val system = ActorSystem()

  ensureIndexExists("scrollpubver")

  client.execute {
    bulk(
      indexInto("scrollpubver" / "empires")source Empire("Parthian", "Persia", "Ctesiphon"),
      indexInto("scrollpubver" / "empires")source Empire("Ptolemaic", "Egypt", "Alexandria"),
      indexInto("scrollpubver" / "empires")source Empire("British", "Worldwide", "London"),
      indexInto("scrollpubver" / "empires")source Empire("Achaemenid", "Persia", "Babylon"),
      indexInto("scrollpubver" / "empires")source Empire("Sasanian", "Persia", "Ctesiphon"),
      indexInto("scrollpubver" / "empires")source Empire("Mongol", "East Asia", "Avarga"),
      indexInto("scrollpubver" / "empires")source Empire("Roman", "Mediterranean", "Rome"),
      indexInto("scrollpubver" / "empires")source Empire("Sumerian", "Mesopotamia", "Uruk"),
      indexInto("scrollpubver" / "empires")source Empire("Klingon", "Space", "Kronos"),
      indexInto("scrollpubver" / "empires")source Empire("Romulan", "Space", "Romulus"),
      indexInto("scrollpubver" / "empires")source Empire("Cardassian", "Space", "Cardassia Prime"),
      indexInto("scrollpubver" / "empires")source Empire("Egyptian", "Egypt", "Memphis"),
      indexInto("scrollpubver" / "empires")source Empire("Babylonian", "Levant", "Babylon")
    )
  }

  blockUntilCount(13, "scrollpubver")

  val query = search("scrollpubver") query "*:*" scroll "1m" limit 2

  override def boundedDepthOfOnNextAndRequestRecursion: Long = 2l

  override def createFailedPublisher(): Publisher[RichSearchHit] = null

  override def createPublisher(elements: Long): Publisher[RichSearchHit] = {
    new ScrollPublisher(client, query, elements)
  }
}

case class Empire(name: String, location: String, capital: String)
