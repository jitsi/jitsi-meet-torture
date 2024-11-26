## A quick how to create a grid with latest selenium

You need docker and docker-compose installed locally.

### Images

We need images with some media on them to be used as input device - audio and video.

Create the images:
```
cd jitsi-meet-torture/doc/grid
cp -r ../../resources .
wget -P resources https://github.com/jitsi/jitsi-meet-torture/releases/download/example-video-source/FourPeople_1280x720_30.y4m
docker build --build-arg VERSION=latest --build-arg BROWSER=chrome -t jitsi/standalone-chrome:latest .
docker build --build-arg VERSION=latest --build-arg BROWSER=firefox -t jitsi/standalone-firefox:latest .
docker build --build-arg VERSION=beta --build-arg BROWSER=firefox -t jitsi/standalone-firefox:beta .
docker build --build-arg VERSION=beta --build-arg BROWSER=chrome -t jitsi/standalone-chrome:beta .
```

OR alternately create multi-arch images (only for chromium and firefox, no chrome arm64 builds available from Google.
You will need your own docker registry or project to push these to.  Example below
```
PRIVATE_DOCKER_REGISTRY=example/standalone-firefox
docker buildx build --build-arg VERSION=latest --build-arg BROWSER=firefox--platform=linux/arm64,linux/amd64 --push --pull --progress=plain --tag $PRIVATE_DOCKER_REGISTRY:latest .
```

### Start the grid
```
docker-compose-v3-dynamic-grid.yml
```

This will start the grid and a node and everytime a session is requested a new docker image with the browser will be run. 
Once the session is not needed anymore the node will be destroyed.
You can now open the grid dashboard at: http://localhost:4444/. 
The number of sessions/chrome instances will be limited to the resource of the machine running the node.
You can start several nodes on different machines, for more information: https://github.com/SeleniumHQ/docker-selenium#using-dynamic-grid-in-different-machinesvms
 

#### Example run 
```
mvn test -Djitsi-meet.instance.url=https://alpha.jitsi.net -Djitsi-meet.tests.toRun=UDPTest -Denable.headless=true \
-Dweb.participant1.isRemote=true -Dweb.participant2.isRemote=true -Dweb.participant3.isRemote=true -Dweb.participant4.isRemote=true \
-Dremote.resource.path=/usr/share/jitsi-meet-torture \
-Dremote.address=http://localhost:4444/wd/hub
```
