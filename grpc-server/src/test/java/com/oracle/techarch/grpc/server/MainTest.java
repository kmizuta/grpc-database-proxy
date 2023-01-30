
package com.oracle.techarch.grpc.server;

import com.oracle.techarch.grpc.StringServiceGrpc;
import com.oracle.techarch.grpc.model.Model;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.helidon.microprofile.grpc.client.GrpcChannel;
import io.helidon.microprofile.grpc.client.GrpcProxy;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@HelidonTest
class MainTest {
    static final Logger logger = LoggerFactory.getLogger(MainTest.class);

    @Inject
    @GrpcProxy
    @GrpcChannel(name = "test-server")
    private StringClient client;

    @Test
    void testGeneratedClient() {
        logger.info("testGeneratedClient()+");
        var host = "localhost";
        var port = 8080;
        var channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        var client = StringServiceGrpc.newBlockingStub(channel);
        var sm = Model.StringMessage.newBuilder().setText("hello").build();
        var response = client.upper(sm);
        assertThat(response.getText(), is("HELLO"));

        sm = Model.StringMessage.newBuilder().setText("Hello, There").build();
        var resIter = client.split(sm);
        resIter.forEachRemaining( s -> logger.info(s.getText()));
        logger.info("testGeneratedClient()-");
    }

    @Test
    void testMpClient() {
        logger.info("testMpClient()+");
        var sm = Model.StringMessage.newBuilder().setText("hello").build();
        assertThat(client.upper(sm).getText(), is("HELLO"));

        sm = Model.StringMessage.newBuilder().setText("Hello, There").build();

        CompletableFuture future = new CompletableFuture();
        var response = new StreamObserver<Model.StringMessage>() {
            @Override
            public void onNext(Model.StringMessage stringMessage) {
                logger.info(stringMessage.getText());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("Something unexpected happened", throwable);
                future.complete(throwable);
            }

            @Override
            public void onCompleted() {
                future.complete(null);
            }
        };
        client.split(sm, response);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        logger.info("testMpClient()-");
    }

    @Test
    void testBiDirectional() {
        logger.info("testBiDirectional()+");

        var manager = new StringRequestResponseManager();
        manager.requestAndWait("hello");
        manager.requestAndWait("there");
        manager.requestAndWait("ken");
        manager.complete();

        logger.info("testBiDirectional()-");
    }

    class StringRequestResponseManager implements StreamObserver<Model.StringMessage> {
        final private StreamObserver<Model.StringMessage> requestObserver;
        private CompletableFuture<Object> requestLock;

        StringRequestResponseManager() {
            this.requestObserver = client.echoWithSplit(this);
        }

        public void requestAndWait(String msg) {
            logger.info(String.format("Request = \"%s\"", msg));

            try {
                requestLock = new CompletableFuture();
                requestObserver.onNext(Model.StringMessage.newBuilder().setText(msg).build());
                requestLock.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } finally {
                requestLock = null;
            }
        }

        public void complete() {
            requestObserver.onCompleted();
        }

        @Override
        public void onNext(Model.StringMessage stringMessage) {
            var msg = stringMessage.getText();
            if ("".equals(msg)) {
                if (requestLock != null)
                    requestLock.complete(null);
            } else {
                logger.info(msg);
            }
        }


        @Override
        public void onError(Throwable throwable) {
            if (requestLock != null)
                requestLock.complete(throwable);
        }

        @Override
        public void onCompleted() {
            if (requestLock != null)
                requestLock.complete(null);
        }
    }
}
