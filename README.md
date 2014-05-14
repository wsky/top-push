# Top Push

Message-push abstraction component, provide useful messaging components.

[![Build Status](https://travis-ci.org/wsky/top-push.png?branch=master)](https://travis-ci.org/wsky/top-push)

- Client
- ClientConnection
- MessageSender
- MessagingScheduler
- Pulling

```java
PushManager manager = new PushManager();
Client client = manager.getOrCreateClient(id);
client.addConnection(new Connection());

// send via global sender
client.send(msg, ordering, handler);

// send in new sender
MessageSender sender = client.newSender();
sender.send(msg);
```

## License

	Licensed under the Apache License, Version 2.0 (the "License");

	you may not use this file except in compliance with the License.

	You may obtain a copy of the License at

	     http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, 

	software distributed under the License is distributed on an "AS IS" BASIS, 

	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

	See the License for the specific language governing permissions and limitations under the License.
