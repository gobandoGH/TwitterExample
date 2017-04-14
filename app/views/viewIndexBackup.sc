@(message: String)(implicit request: RequestHeader)

@main(message) {

  <div id="tweets"></div>

    <script type="text/javascript">
      function appendTweet(text) {
      var tweet = document.createElement("p");
      var message = document.createTextNode(text);
      tweet.appendChild(message);
      document.getElementById("tweets").appendChild(tweet);
      }
      function connect(attempt) {
      var connectionAttempt = attempt;
      var tweetSocket = new WebSocket("@routes.Application.tweets().webSocketURL()");
      tweetSocket.onmessage = function (event) {
        console.log(event);
        var data = JSON.parse(event.data);
        appendTweet(data.text);
      };
      tweetSocket.onopen = function() {
        connectionAttempt = 1;
        tweetSocket.send("subscribe");
      };
      tweetSocket.onclose = function() {
        if (connectionAttempt <= 3) {
          appendTweet("WARNING: Connection with the server lost, attempting to reconnect. Attempt number " + connectionAttempt);
          setTimeout(function() {
            connect(connectionAttempt + 1);
          }, 5000);
        } else {
          alert("The connection with the server was lost. Please try again later. Sorry about that.");
        }
      };
      }
      connect(1);
    </script>

}