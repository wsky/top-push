# Top Push

Message-Push abstraction component,

provide a default server implementation via websocket(jetty).

As a component, it was designed to be extensible, you can easily replace the transport, message protocol.

[![Build Status](https://travis-ci.org/wsky/top-push.png?branch=master)](https://travis-ci.org/wsky/top-push)

server implementation:
- jetty-support: build-in server, impl message forwarder.
	- matched client: top-push-client https://github.com/wsky/top-push-client
- top-link-support: poll/push mix server sample showing how it can be used
	- https://github.com/wsky/top-link

latest refact defail here:
https://github.com/wsky/top-push/issues/35

## Concept

- Client/Connection
- StateHandler
- Scheduler
- Queue

## Sever

```bash
mvn clean jetty:run
```

## Build

dev version v1.0.1 
https://github.com/wsky/top-push/issues?milestone=3&state=open

- core

```bash
mvn clean -f pom-core.xml package
```

## Stress tests on AWS

install

```bash
sudo sh build.sh
```
then log on bridge server:
```bash
sudo sh scripts/aws-install.sh
```

## Performance

## Tuning

## License

	Licensed under the Apache License, Version 2.0 (the "License");

	you may not use this file except in compliance with the License.

	You may obtain a copy of the License at

	     http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, 

	software distributed under the License is distributed on an "AS IS" BASIS, 

	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

	See the License for the specific language governing permissions and limitations under the License.
