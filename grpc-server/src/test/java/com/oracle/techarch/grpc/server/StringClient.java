package com.oracle.techarch.grpc.server;

import com.oracle.techarch.grpc.model.Model.StringMessage;
import io.grpc.stub.StreamObserver;
import io.helidon.microprofile.grpc.core.Bidirectional;
import io.helidon.microprofile.grpc.core.Grpc;
import io.helidon.microprofile.grpc.core.ServerStreaming;
import io.helidon.microprofile.grpc.core.Unary;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Grpc(name="StringService")
public interface StringClient {
    @Unary(name = "Upper")
    public StringMessage upper(StringMessage request);

    @Unary(name = "Lower")
    public StringMessage lower(StringMessage request);

    @ServerStreaming(name = "Split")
    public void split(StringMessage request, StreamObserver<StringMessage> response);

    @Bidirectional(name = "EchoWithSplit")
    public StreamObserver<StringMessage> echoWithSplit(StreamObserver<StringMessage> response);

}
