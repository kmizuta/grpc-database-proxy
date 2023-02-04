# grpc-server

Minimal Helidon MP project suitable to start from scratch.

## Build and run


With JDK17+
```bash
mvn package
java -jar target/grpc-server.jar
```

## Exercise the application
```
curl -X GET http://localhost:8080/simple-greet
{"message":"Hello World!"}
```



## Using CORS

### Sending "simple" CORS requests

The following requests illustrate the CORS protocol with the example app.

By setting `Origin` and `Host` headers that do not indicate the same system we trigger CORS processing in the
 server:

```bash
# Follow the CORS protocol for GET
curl -i -X GET -H "Origin: http://foo.com" -H "Host: here.com" http://localhost:8080/cors

HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Content-Type: application/json
Date: Thu, 30 Apr 2020 17:25:51 -0500
Vary: Origin
connection: keep-alive
content-length: 27

{"greeting":"Hello World!"}
```
Note the new headers `Access-Control-Allow-Origin` and `Vary` in the response.

These are what CORS calls "simple" requests; the CORS protocol for these adds headers to the request and response which
the client and server exchange anyway.

### "Non-simple" CORS requests

The CORS protocol requires the client to send a _pre-flight_ request before sending a request
that changes state on the server, such as `PUT` or `DELETE`, and to check the returned status
and headers to make sure the server is willing to accept the actual request. CORS refers to such `PUT` and `DELETE`
requests as "non-simple" ones.

This command sends a pre-flight `OPTIONS` request to see if the server will accept a subsequent `PUT` request from the
specified origin to change the greeting:
```bash
curl -i -X OPTIONS \
    -H "Access-Control-Request-Method: PUT" \
    -H "Origin: http://foo.com" \
    -H "Host: here.com" \
    http://localhost:8080/cors/greeting

HTTP/1.1 200 OK
Access-Control-Allow-Methods: PUT
Access-Control-Allow-Origin: http://foo.com
Date: Thu, 30 Apr 2020 17:30:59 -0500
transfer-encoding: chunked
connection: keep-alive
```
The successful status and the returned `Access-Control-Allow-xxx` headers indicate that the
 server accepted the pre-flight request. That means it is OK for us to send `PUT` request to perform the actual change
 of greeting. (See below for how the server rejects a pre-flight request.)
```bash
curl -i -X PUT \
    -H "Origin: http://foo.com" \
    -H "Host: here.com" \
    -H "Access-Control-Allow-Methods: PUT" \
    -H "Access-Control-Allow-Origin: http://foo.com" \
    http://localhost:8080/greet/Hola

HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://foo.com
Date: Thu, 30 Apr 2020 17:32:55 -0500
Vary: Origin
connection: keep-alive

Hola World!
```

Note that the tests in the example `MainTest` class follow these same steps.


## Try metrics

```
# Prometheus Format
curl -s -X GET http://localhost:8080/metrics
# TYPE base:gc_g1_young_generation_count gauge
. . .

# JSON Format
curl -H 'Accept: application/json' -X GET http://localhost:8080/metrics
{"base":...
. . .
```



## Try health

```
curl -s -X GET http://localhost:8080/health
{"outcome":"UP",...

```


## Tracing

### Set up Jaeger

First, you need to run the Jaeger tracer. Helidon will communicate with this tracer at runtime.

Run Jaeger within a docker container:
```
docker run -d --name jaeger\
   -e COLLECTOR_ZIPKIN_HOST_PORT=:9411\
   -e COLLECTOR_OTLP_ENABLED=true\
   -p 6831:6831/udp\
   -p 6832:6832/udp\
   -p 5778:5778\
   -p 16686:16686\
   -p 4317:4317\   
   -p 4318:4318\
   -p 14250:14250\
   -p 14268:14268\
   -p 14269:14269\
   -p 9411:9411\
   jaegertracing/all-in-one:1.38
```

### View Tracing Using Jaeger UI

Jaeger provides a web-based UI at http://localhost:16686, where you can see a visual representation of
the same data and the relationship between spans within a trace.


## Building a Native Image

Make sure you have GraalVM locally installed:

```
$GRAALVM_HOME/bin/native-image --version
```

Build the native image using the native image profile:

```
mvn package -Pnative-image
```

This uses the helidon-maven-plugin to perform the native compilation using your installed copy of GraalVM. It might take a while to complete.
Once it completes start the application using the native executable (no JVM!):

```
./target/grpc-server
```

Yep, it starts fast. You can exercise the application’s endpoints as before.


## Building the Docker Image
```
docker build -t grpc-server .
```

## Running the Docker Image

```
docker run --rm -p 8080:8080 grpc-server:latest
```

Exercise the application as described above.
                                

## Deploy the application to Kubernetes

```
kubectl cluster-info                        # Verify which cluster
kubectl get pods                            # Verify connectivity to cluster
kubectl create -f app.yaml                  # Deploy application
kubectl get pods                            # Wait for quickstart pod to be RUNNING
kubectl get service  grpc-server         # Get service info
```

Note the PORTs. You can now exercise the application as you did before but use the second
port number (the NodePort) instead of 8080.

After you’re done, cleanup.

```
kubectl delete -f app.yaml
```
                                

## Building a Custom Runtime Image

Build the custom runtime image using the jlink image profile:

```
mvn package -Pjlink-image
```

This uses the helidon-maven-plugin to perform the custom image generation.
After the build completes it will report some statistics about the build including the reduction in image size.

The target/grpc-server-jri directory is a self contained custom image of your application. It contains your application,
its runtime dependencies and the JDK modules it depends on. You can start your application using the provide start script:

```
./target/grpc-server-jri/bin/start
```

Class Data Sharing (CDS) Archive
Also included in the custom image is a Class Data Sharing (CDS) archive that improves your application’s startup
performance and in-memory footprint. You can learn more about Class Data Sharing in the JDK documentation.

The CDS archive increases your image size to get these performance optimizations. It can be of significant size (tens of MB).
The size of the CDS archive is reported at the end of the build output.

If you’d rather have a smaller image size (with a slightly increased startup time) you can skip the creation of the CDS
archive by executing your build like this:

```
mvn package -Pjlink-image -Djlink.image.addClassDataSharingArchive=false
```

For more information on available configuration options see the helidon-maven-plugin documentation.
                                
