

build:
	mkdir -p java
	protoc --plugin=protoc-gen-grpc-java=/jUsers/kmizuta/Downloads/protoc-gen-grpc-java-1.52.1-osx-x86_64.exe --java_out=java src/main/proto/Strings.proto
	protoc --plugin=/Users/kmizuta/Downloads/protoc-gen-grpc-java-1.52.1-osx-x86_64.exe --java_out=java src/main/proto/Strings.proto

clean:
	/bin/rm -rf java

