



def tweets = Action.async {

  // val loggingIteratee = Iteratee.foreach[Array[Byte]] { array =>
  //   Logger.info(array.map(_.toChar).mkString)
  // }

  val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]

  val jsonStream: Enumerator[JsObject] =
    enumerator &>
      Encoding.decode() &>
      Enumeratee.grouped(JsonIteratees.jsSimpleObject)

  val loggingIteratee = Iteratee.foreach[JsObject] { value =>
    Logger.info(value.toString)
  }

  jsonStream run loggingIteratee

  credentials.map { case (consumerKey, requestToken) =>
    WS
      .url("https://stream.twitter.com/1.1/statuses/filter.json")
      .sign(OAuthCalculator(consumerKey, requestToken))
      .withQueryString("track" -> "Nairo")
      .get {
        response => Logger.info("Status: " + response.status)
          iteratee
      }
      .map { _ =>
        Ok ("Stream Closed")
      }
  } getOrElse {
    Future.successful {
      InternalServerError("Twitter credentials missing")
    }
  }
}

def credentials: Option [(ConsumerKey, RequestToken)] = for {
  apiKey <- Play.configuration.getString("twitter.apiKey")
  apiSecret <- Play.configuration.getString("twitter.apiSecret")
  token <- Play.configuration.getString("twitter.token")
  tokenSecret <- Play.configuration.getString("twitter.tokenSecret")
} yield (
  ConsumerKey(apiKey, apiSecret),
  RequestToken(token, tokenSecret)
  )