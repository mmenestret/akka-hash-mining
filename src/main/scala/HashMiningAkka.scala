import java.security.MessageDigest
import java.util

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.duration._

object HashMiningAkka {

  val chunckSize = 100000
  val nOfActors = 8
  val key = "yzbqklnj"
  val sixZerosMD5asBytes = md5("yzbqklnj9962624").take(3)

  def md5(s: String) = {
    MessageDigest.getInstance("MD5").digest(s.getBytes())
  }

  case class HashRequest(chunck: Seq[Int])
  case class HashAnswer(resp: Option[Int])

  class Hasher extends Actor {
    def testIfContainsHash(chunck: Seq[Int]): Option[Int] = {
      for (c <- chunck) {
        if (util.Arrays.equals(md5(s"$key$c").take(3), sixZerosMD5asBytes)) {
          return Some(c)
        }
      }
      None
    }

    override def receive: Receive = {
      case HashRequest(c) =>
        val startTime = System.currentTimeMillis()
        val answer = testIfContainsHash(c)
        answer match {
          case Some(i) =>
            println(s"Solution found: $i")
            context.system.shutdown()
          case _ => println(f"Tested chunck starting at ${c.head} in ${(System.currentTimeMillis() - startTime)/1000f}%2.2fs")
        }
    }
  }

  def main(args: Array[String]) {
    val system = ActorSystem("Hasher")
    implicit val timeout = Timeout(2.second)
    implicit val ec = system.dispatcher
    val startTime = System.currentTimeMillis()

    val hashers: Seq[ActorRef] = (1 to nOfActors).map(n => system.actorOf(Props[Hasher], s"Hasher_$n"))

    def addStream(a: Int): Stream[Int] = a #:: addStream(a + 1)
    val infiniteIteration = addStream(0)

    infiniteIteration.grouped(chunckSize).grouped(nOfActors).foreach {
      listOfChuncks => {
        hashers.zip(listOfChuncks.map(HashRequest(_))).foreach {
          hasherWithRequest => {
            hasherWithRequest match {
              case (act: ActorRef, req: HashRequest) => act ! req
            }
          }
        }
      }
    }
  }
}