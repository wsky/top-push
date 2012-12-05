using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Net.WebSockets;
using System.Threading;
using System.Threading.Tasks;
using System.Text;
using System.Diagnostics;

namespace WebSocketTest
{
    [TestClass]
    public class UnitTest1
    {
        private int _counter;

        [TestMethod]
        public void TestMethod1()
        {
            var total = 10000;
            var msg = "hello";
            var client = new ClientWebSocket();
            client.ConnectAsync(new Uri("ws://localhost:8889/ws.axd"), CancellationToken.None).Wait();
            client.SendAsync(new ArraySegment<byte>(Encoding.UTF8.GetBytes(total.ToString())), WebSocketMessageType.Text, true, CancellationToken.None).Wait();
            client.SendAsync(new ArraySegment<byte>(Encoding.UTF8.GetBytes(msg)), WebSocketMessageType.Text, true, CancellationToken.None).Wait();

            var w = new Stopwatch();
            w.Start();
            ReceiveMessages(client, msg, total).Wait();
            w.Stop();
            Trace.WriteLine(w.ElapsedMilliseconds);

            while (_counter < total) Thread.Sleep(10);
        }

        private async Task ReceiveMessages(ClientWebSocket socket,string msg, int total)
        {
            var maxMessageSize = 1024;
            var receiveBuffer = new byte[maxMessageSize];

            for (var i = 0; i < 10; i++)
            {
                var ret = await socket.ReceiveAsync(new ArraySegment<byte>(receiveBuffer), CancellationToken.None);

                if (ret.MessageType != WebSocketMessageType.Text) continue;

                var count = ret.Count;
                while (!ret.EndOfMessage)
                {
                    ret = await socket.ReceiveAsync(new ArraySegment<byte>(receiveBuffer, count, maxMessageSize - count), CancellationToken.None);
                    count += ret.Count;
                }

                var receivedString = Encoding.UTF8.GetString(receiveBuffer, 0, count);
                Trace.WriteLine(receivedString);
                Interlocked.Increment(ref _counter);
            }
        }
    }
}
