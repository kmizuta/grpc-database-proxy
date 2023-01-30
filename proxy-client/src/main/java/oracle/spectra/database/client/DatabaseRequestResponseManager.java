package oracle.spectra.database.client;

import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import oracle.spectra.database.DatabaseProxyGrpc;
import oracle.spectra.database.model.CommandModel.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DatabaseRequestResponseManager implements StreamObserver<DatabaseResult> {
    private final StreamObserver<DatabaseCommand> requestObserver;
    private CompletableFuture<DatabaseResult> requestLock;

    public DatabaseRequestResponseManager(Channel channel) {
        var client = DatabaseProxyGrpc.newStub(channel);
        this.requestObserver = client.executeCommand(this);
    }

    public DatabaseResult execute(DatabaseCommand command) {
        try {
            requestLock = new CompletableFuture<>();
            requestObserver.onNext(command);
            return requestLock.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            requestLock = null;
        }
    }

    public void close() {
        requestObserver.onCompleted();
    }

    public void complete() {
        requestObserver.onCompleted();
    }

    @Override
    public void onNext(DatabaseResult result) {
        requestLock.complete(result);
    }


    @Override
    public void onError(Throwable throwable) {
        if (requestLock != null) {
            var sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            var error = ErrorResponse.newBuilder()
                    .setErrorMessage(throwable.getMessage())
                    .setStackTrace(sw.toString())
                    .build();
            requestLock.complete(DatabaseResult.newBuilder()
                    .setType(Command.COMMAND_ERROR)
                    .setError(error)
                    .build());
        }
        throw new RuntimeException(throwable);
    }

    @Override
    public void onCompleted() {
        if (requestLock != null)
            requestLock.complete(null);
    }
}
