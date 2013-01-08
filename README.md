# Top Push Server

Push Server via websocket(jetty implementation).

By the way, It was designed to be extensible, you can easily replace the transport, message protocol.

[![Build Status](https://travis-ci.org/wsky/top-push.png?branch=master)](https://travis-ci.org/wsky/top-push)

top-push-client https://github.com/wsky/top-push-client

custom usage https://github.com/wsky/top-push-integration

## Build

```bash
mvn package
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

	Copyright (c) Alibaba.  All rights reserved. - http://open.taobao.com/

	Licensed under the Apache License, Version 2.0 (the "License");

	you may not use this file except in compliance with the License.

	You may obtain a copy of the License at

	     http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, 

	software distributed under the License is distributed on an "AS IS" BASIS, 

	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

	See the License for the specific language governing permissions and limitations under the License.

