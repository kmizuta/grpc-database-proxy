package com.oracle.techarch.grpc.server;

import com.oracle.techarch.grpc.StringServiceGrpc;
import com.oracle.techarch.grpc.model.Model.StringMessage;
import io.grpc.stub.StreamObserver;
import io.helidon.microprofile.grpc.core.Bidirectional;
import io.helidon.microprofile.grpc.core.Grpc;
import io.helidon.microprofile.grpc.core.ServerStreaming;
import io.helidon.microprofile.grpc.core.Unary;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@ApplicationScoped
@Grpc(name="StringService")
public class StringService {

    @Unary(name = "Upper")
    public void upper(StringMessage request, StreamObserver<StringMessage> responseObserver) {
        var s = request.getText();
        var sm = s == null ? null : StringMessage.newBuilder().setText(request.getText().toUpperCase()).build();
        responseObserver.onNext(sm);
        responseObserver.onCompleted();
    }

    @Unary(name = "Lower")
    public void lower(StringMessage request, StreamObserver<StringMessage> responseObserver) {
        var s = request.getText();
        var sm = s == null ? null : StringMessage.newBuilder().setText(request.getText().toLowerCase()).build();
        responseObserver.onNext(sm);
        responseObserver.onCompleted();
    }

    @ServerStreaming(name = "Split")
    public void split(StringMessage request, StreamObserver<StringMessage> responseObserver) {
        CompletableFuture.runAsync( () -> {
            splitImpl(request, responseObserver);
            responseObserver.onCompleted();
        }); //, Executors.newVirtualThreadPerTaskExecutor());
    }

    private void splitImpl(StringMessage request, StreamObserver<StringMessage> responseObserver) {
        var bytes = request.getText().getBytes();
        for (int i = 0; i < bytes.length; i++) {
            String text = ":" +
                    new String(new byte[]{bytes[i]})
                    + ":";
            responseObserver.onNext(StringMessage.newBuilder().setText(text).build());
        }
    }

    @Bidirectional(name = "EchoWithSplit")
    public StreamObserver<StringMessage> echoWithSplit(StreamObserver<StringMessage> responseObserver) {
        var requestObserver = new StreamObserver<StringMessage>() {
            @Override
            public void onNext(StringMessage stringMessage) {
                splitImpl(stringMessage, responseObserver);
                responseObserver.onNext(StringMessage.newBuilder().setText("").build());
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };

        return requestObserver;
    }
}