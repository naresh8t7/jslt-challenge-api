# REST API to retrieve ML features from a given JSON object using a supplied feature configuration.

Simple REST API built using Helidon framework to retrieve ML features from a given JSON object using a supplied feature configuration.
Transformation would be defined using https://github.com/schibsted/jslt library.

## Assumptions
Both feature config and input are provided in the single request.
Alternatively I thought to have a seperate endpoint for feature config and have it in memory and use it for transformations.

## Build and run

With JDK11+
```bash
mvn package
java -jar target/jstl-challenge-api.jar
```

## Exercise the application

```
curl -X GET http://localhost:8080/transform
{"message":"JSLT Transfor APP !"}

curl -X POST -H "Content-Type: application/json" -d '{"config" : {"id": 1,"name":"DeviceFeatures","transforms":[{"name":"device_os","useInML":true,"enabled":true,"jsltExpression":".device.osType"},{"name":"device_description","useInML" : true,"enabled" : true,"jsltExpression": ".device.osType + \" \" + .device.model"}]},"input":{"eventId": "878237843","device": {"osType": "Linux","model": "Laptop"},"ip" : "10.45.2.30","sessionId":"ads79uoijd098098"}}' http://localhost:8080/transform/retrieveml
{"device_os":"Linux","device_description":"Linux Laptop","eventId":"878237843"}


```

## Try health and metrics

```
curl -s -X GET http://localhost:8080/health
{"outcome":"UP",...
. . .

# Prometheus Format
curl -s -X GET http://localhost:8080/metrics
# TYPE base:gc_g1_young_generation_count gauge
. . .

# JSON Format
curl -H 'Accept: application/json' -X GET http://localhost:8080/metrics
{"base":...
. . .

```

## Build the Docker Image

```
docker build -t jstl-challenge-api .
```

## Start the application with Docker

```
docker run --rm -p 8080:8080 jstl-challenge-api:latest
```

Exercise the application as described above

## Deploy the application to Kubernetes

```
kubectl cluster-info                        # Verify which cluster
kubectl get pods                            # Verify connectivity to cluster
kubectl create -f app.yaml                  # Deploy application
kubectl get pods                            # Wait for quickstart pod to be RUNNING
kubectl get service helidon-quickstart-se   # Get service info
```

Note the PORTs. You can now exercise the application as you did before but use the second
port number (the NodePort) instead of 8080.

After you’re done, cleanup.

```
kubectl delete -f app.yaml
```

## Build a native image with GraalVM

GraalVM allows you to compile your programs ahead-of-time into a native
 executable. See https://www.graalvm.org/docs/reference-manual/aot-compilation/
 for more information.

You can build a native executable in 2 different ways:
* With a local installation of GraalVM
* Using Docker

### Local build

Download Graal VM at https://www.graalvm.org/downloads, the versions
 currently supported for Helidon are `20.1.0` and above.

```
# Setup the environment
export GRAALVM_HOME=/path
# build the native executable
mvn package -Pnative-image
```

You can also put the Graal VM `bin` directory in your PATH, or pass
 `-DgraalVMHome=/path` to the Maven command.

See https://github.com/oracle/helidon-build-tools/tree/master/helidon-maven-plugin#goal-native-image
 for more information.

Start the application:

```
./target/jstl-challenge-api
```

### Multi-stage Docker build

Build the "native" Docker Image

```
docker build -t jstl-challenge-api-native -f Dockerfile.native .
```

Start the application:

```
docker run --rm -p 8080:8080 jstl-challenge-api-native:latest
```

## Build a Java Runtime Image using jlink

You can build a custom Java Runtime Image (JRI) containing the application jars and the JDK modules
on which they depend. This image also:

* Enables Class Data Sharing by default to reduce startup time.
* Contains a customized `start` script to simplify CDS usage and support debug and test modes.

You can build a custom JRI in two different ways:
* Local
* Using Docker


### Local build

```
# build the JRI
mvn package -Pjlink-image
```

See https://github.com/oracle/helidon-build-tools/tree/master/helidon-maven-plugin#goal-jlink-image
 for more information.

Start the application:

```
./target/jstl-challenge-api-jri/bin/start
```

### Multi-stage Docker build

Build the JRI as a Docker Image

```
docker build -t jstl-challenge-api-jri -f Dockerfile.jlink .
```

Start the application:

```
docker run --rm -p 8080:8080 jstl-challenge-api-jri:latest
```

See the start script help:

```
docker run --rm jstl-challenge-api-jri:latest --help
```
