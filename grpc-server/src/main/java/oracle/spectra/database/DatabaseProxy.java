package oracle.spectra.database;

import oracle.spectra.database.model.CommandModel.*;
import io.grpc.stub.StreamObserver;
import io.helidon.microprofile.grpc.core.Bidirectional;
import io.helidon.microprofile.grpc.core.Grpc;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Grpc(name="DatabaseProxy")
public interface DatabaseProxy {
    @Bidirectional(name = "ExecuteCommand")
    public StreamObserver<DatabaseCommand> executeCommand(StreamObserver<DatabaseResult> result);
}
