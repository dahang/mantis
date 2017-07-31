package io.iohk.ethereum.jsonrpc

import akka.actor.ActorRef
import akka.agent.Agent
import akka.util.Timeout
import io.iohk.ethereum.network.PeerManagerActor
import io.iohk.ethereum.utils.ServerStatus.{Listening, NotListening}
import io.iohk.ethereum.utils.{Config, NodeStatus}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object NetService {
  case class VersionRequest()
  case class VersionResponse(value: String)

  case class ListeningRequest()
  case class ListeningResponse(value: Boolean)

  case class PeerCountRequest()
  case class PeerCountResponse(value: Int)
}

class NetService(nodeStatusHolder: Agent[NodeStatus], peerManager: ActorRef) {
  import NetService._

  def version(req: VersionRequest): ServiceResponse[VersionResponse] = {
    Future.successful(Right(VersionResponse(Config.Network.protocolVersion)))
  }

  def listening(req: ListeningRequest): ServiceResponse[ListeningResponse] = {
    Future.successful {
      Right(
        nodeStatusHolder().serverStatus match {
          case _: Listening => ListeningResponse(true)
          case NotListening => ListeningResponse(false)
        }
      )
    }
  }

  def peerCount(req: PeerCountRequest): ServiceResponse[PeerCountResponse] = {
    import akka.pattern.ask
    implicit val timeout = Timeout(5.seconds)

    (peerManager ? PeerManagerActor.GetPeers)
      .mapTo[PeerManagerActor.Peers]
      .map { peers => Right(PeerCountResponse(peers.handshaked.size)) }
  }

}
