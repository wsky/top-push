using System;
using System.Diagnostics;
using System.Net.WebSockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Web;

namespace WebSockeSample.Controllers
{
    public class WebSocketHandler : IHttpHandler
    {
        /// <summary>
        /// You will need to configure this handler in the Web.config file of your 
        /// web and register it with IIS before being able to use it. For more information
        /// see the following link: http://go.microsoft.com/?linkid=8101007
        /// </summary>
        #region IHttpHandler Members

        public bool IsReusable
        {
            // Return false in case your Managed Handler cannot be reused for another request.
            // Usually this would be false in case you have some state information preserved per request.
            get { return true; }
        }

        public void ProcessRequest(HttpContext context)
        {
            if (context.IsWebSocketRequest)
                context.AcceptWebSocketRequest(this.HandleWebSocket);
            else
                context.Response.StatusCode = 400;
        }

        #endregion

        private async Task HandleWebSocket(WebSocketContext context)
        {
            var maxMessageSize = 1024;
            var receiveBuffer = new byte[maxMessageSize];
            var socket = context.WebSocket;

            var total = 0;
            
            while (socket.State == WebSocketState.Open)
            {
                var ret = await socket.ReceiveAsync(new ArraySegment<byte>(receiveBuffer), CancellationToken.None);

                if (ret.MessageType == WebSocketMessageType.Close)              
                    await socket.CloseAsync(WebSocketCloseStatus.NormalClosure
                        , string.Empty
                        , CancellationToken.None);
                else if (ret.MessageType == WebSocketMessageType.Binary)
                    await socket.CloseAsync(WebSocketCloseStatus.InvalidMessageType
                        , "Cannot accept binary frame"
                        , CancellationToken.None);
                else
                {
                    var count = ret.Count;
                    while (!ret.EndOfMessage)
                    {
                        if (count >= maxMessageSize)
                        {
                            await socket.CloseAsync(WebSocketCloseStatus.MessageTooBig
                                , string.Format("Maximum message size: {0} bytes.", maxMessageSize)
                                , CancellationToken.None);
                            return;
                        }

                        ret = await socket.ReceiveAsync(new ArraySegment<byte>(receiveBuffer, count, maxMessageSize - count), CancellationToken.None);
                        count += ret.Count;
                    }

                    var receivedString = Encoding.UTF8.GetString(receiveBuffer, 0, count);

                    if (total == 0)
                    {
                        total = int.Parse(receivedString);
                        continue;
                    }

                    var outputBuffer = new ArraySegment<byte>(Encoding.UTF8.GetBytes(receivedString));
                    var w = new Stopwatch();
                    w.Start();
                    for (var i = 0; i < total; i++)
                        await socket.SendAsync(outputBuffer, WebSocketMessageType.Text, true, CancellationToken.None);
                    w.Stop();
                    Console.WriteLine(w.ElapsedMilliseconds);
                }
            }
        }
    }
}
