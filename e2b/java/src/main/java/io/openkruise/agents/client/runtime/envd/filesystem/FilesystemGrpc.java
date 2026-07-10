package io.openkruise.agents.client.runtime.envd.filesystem;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.60.0)",
    comments = "Source: filesystem/filesystem.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class FilesystemGrpc {

  private FilesystemGrpc() {}

  public static final String SERVICE_NAME = "filesystem.Filesystem";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<StatRequest,
      StatResponse> getStatMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Stat",
      requestType = StatRequest.class,
      responseType = StatResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<StatRequest,
      StatResponse> getStatMethod() {
    io.grpc.MethodDescriptor<StatRequest, StatResponse> getStatMethod;
    if ((getStatMethod = FilesystemGrpc.getStatMethod) == null) {
      synchronized (FilesystemGrpc.class) {
        if ((getStatMethod = FilesystemGrpc.getStatMethod) == null) {
          FilesystemGrpc.getStatMethod = getStatMethod =
              io.grpc.MethodDescriptor.<StatRequest, StatResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Stat"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  StatRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  StatResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FilesystemMethodDescriptorSupplier("Stat"))
              .build();
        }
      }
    }
    return getStatMethod;
  }

  private static volatile io.grpc.MethodDescriptor<MakeDirRequest,
      MakeDirResponse> getMakeDirMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "MakeDir",
      requestType = MakeDirRequest.class,
      responseType = MakeDirResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<MakeDirRequest,
      MakeDirResponse> getMakeDirMethod() {
    io.grpc.MethodDescriptor<MakeDirRequest, MakeDirResponse> getMakeDirMethod;
    if ((getMakeDirMethod = FilesystemGrpc.getMakeDirMethod) == null) {
      synchronized (FilesystemGrpc.class) {
        if ((getMakeDirMethod = FilesystemGrpc.getMakeDirMethod) == null) {
          FilesystemGrpc.getMakeDirMethod = getMakeDirMethod =
              io.grpc.MethodDescriptor.<MakeDirRequest, MakeDirResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "MakeDir"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  MakeDirRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  MakeDirResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FilesystemMethodDescriptorSupplier("MakeDir"))
              .build();
        }
      }
    }
    return getMakeDirMethod;
  }

  private static volatile io.grpc.MethodDescriptor<MoveRequest,
      MoveResponse> getMoveMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Move",
      requestType = MoveRequest.class,
      responseType = MoveResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<MoveRequest,
      MoveResponse> getMoveMethod() {
    io.grpc.MethodDescriptor<MoveRequest, MoveResponse> getMoveMethod;
    if ((getMoveMethod = FilesystemGrpc.getMoveMethod) == null) {
      synchronized (FilesystemGrpc.class) {
        if ((getMoveMethod = FilesystemGrpc.getMoveMethod) == null) {
          FilesystemGrpc.getMoveMethod = getMoveMethod =
              io.grpc.MethodDescriptor.<MoveRequest, MoveResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Move"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  MoveRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  MoveResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FilesystemMethodDescriptorSupplier("Move"))
              .build();
        }
      }
    }
    return getMoveMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ListDirRequest,
      ListDirResponse> getListDirMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListDir",
      requestType = ListDirRequest.class,
      responseType = ListDirResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ListDirRequest,
      ListDirResponse> getListDirMethod() {
    io.grpc.MethodDescriptor<ListDirRequest, ListDirResponse> getListDirMethod;
    if ((getListDirMethod = FilesystemGrpc.getListDirMethod) == null) {
      synchronized (FilesystemGrpc.class) {
        if ((getListDirMethod = FilesystemGrpc.getListDirMethod) == null) {
          FilesystemGrpc.getListDirMethod = getListDirMethod =
              io.grpc.MethodDescriptor.<ListDirRequest, ListDirResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListDir"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ListDirRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ListDirResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FilesystemMethodDescriptorSupplier("ListDir"))
              .build();
        }
      }
    }
    return getListDirMethod;
  }

  private static volatile io.grpc.MethodDescriptor<RemoveRequest,
      RemoveResponse> getRemoveMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Remove",
      requestType = RemoveRequest.class,
      responseType = RemoveResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<RemoveRequest,
      RemoveResponse> getRemoveMethod() {
    io.grpc.MethodDescriptor<RemoveRequest, RemoveResponse> getRemoveMethod;
    if ((getRemoveMethod = FilesystemGrpc.getRemoveMethod) == null) {
      synchronized (FilesystemGrpc.class) {
        if ((getRemoveMethod = FilesystemGrpc.getRemoveMethod) == null) {
          FilesystemGrpc.getRemoveMethod = getRemoveMethod =
              io.grpc.MethodDescriptor.<RemoveRequest, RemoveResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Remove"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  RemoveRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  RemoveResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FilesystemMethodDescriptorSupplier("Remove"))
              .build();
        }
      }
    }
    return getRemoveMethod;
  }

  private static volatile io.grpc.MethodDescriptor<WatchDirRequest,
      WatchDirResponse> getWatchDirMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WatchDir",
      requestType = WatchDirRequest.class,
      responseType = WatchDirResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<WatchDirRequest,
      WatchDirResponse> getWatchDirMethod() {
    io.grpc.MethodDescriptor<WatchDirRequest, WatchDirResponse> getWatchDirMethod;
    if ((getWatchDirMethod = FilesystemGrpc.getWatchDirMethod) == null) {
      synchronized (FilesystemGrpc.class) {
        if ((getWatchDirMethod = FilesystemGrpc.getWatchDirMethod) == null) {
          FilesystemGrpc.getWatchDirMethod = getWatchDirMethod =
              io.grpc.MethodDescriptor.<WatchDirRequest, WatchDirResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WatchDir"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  WatchDirRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  WatchDirResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FilesystemMethodDescriptorSupplier("WatchDir"))
              .build();
        }
      }
    }
    return getWatchDirMethod;
  }

  private static volatile io.grpc.MethodDescriptor<CreateWatcherRequest,
      CreateWatcherResponse> getCreateWatcherMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateWatcher",
      requestType = CreateWatcherRequest.class,
      responseType = CreateWatcherResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<CreateWatcherRequest,
      CreateWatcherResponse> getCreateWatcherMethod() {
    io.grpc.MethodDescriptor<CreateWatcherRequest, CreateWatcherResponse> getCreateWatcherMethod;
    if ((getCreateWatcherMethod = FilesystemGrpc.getCreateWatcherMethod) == null) {
      synchronized (FilesystemGrpc.class) {
        if ((getCreateWatcherMethod = FilesystemGrpc.getCreateWatcherMethod) == null) {
          FilesystemGrpc.getCreateWatcherMethod = getCreateWatcherMethod =
              io.grpc.MethodDescriptor.<CreateWatcherRequest, CreateWatcherResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateWatcher"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  CreateWatcherRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  CreateWatcherResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FilesystemMethodDescriptorSupplier("CreateWatcher"))
              .build();
        }
      }
    }
    return getCreateWatcherMethod;
  }

  private static volatile io.grpc.MethodDescriptor<GetWatcherEventsRequest,
      GetWatcherEventsResponse> getGetWatcherEventsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetWatcherEvents",
      requestType = GetWatcherEventsRequest.class,
      responseType = GetWatcherEventsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<GetWatcherEventsRequest,
      GetWatcherEventsResponse> getGetWatcherEventsMethod() {
    io.grpc.MethodDescriptor<GetWatcherEventsRequest, GetWatcherEventsResponse> getGetWatcherEventsMethod;
    if ((getGetWatcherEventsMethod = FilesystemGrpc.getGetWatcherEventsMethod) == null) {
      synchronized (FilesystemGrpc.class) {
        if ((getGetWatcherEventsMethod = FilesystemGrpc.getGetWatcherEventsMethod) == null) {
          FilesystemGrpc.getGetWatcherEventsMethod = getGetWatcherEventsMethod =
              io.grpc.MethodDescriptor.<GetWatcherEventsRequest, GetWatcherEventsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWatcherEvents"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  GetWatcherEventsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  GetWatcherEventsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FilesystemMethodDescriptorSupplier("GetWatcherEvents"))
              .build();
        }
      }
    }
    return getGetWatcherEventsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<RemoveWatcherRequest,
      RemoveWatcherResponse> getRemoveWatcherMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RemoveWatcher",
      requestType = RemoveWatcherRequest.class,
      responseType = RemoveWatcherResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<RemoveWatcherRequest,
      RemoveWatcherResponse> getRemoveWatcherMethod() {
    io.grpc.MethodDescriptor<RemoveWatcherRequest, RemoveWatcherResponse> getRemoveWatcherMethod;
    if ((getRemoveWatcherMethod = FilesystemGrpc.getRemoveWatcherMethod) == null) {
      synchronized (FilesystemGrpc.class) {
        if ((getRemoveWatcherMethod = FilesystemGrpc.getRemoveWatcherMethod) == null) {
          FilesystemGrpc.getRemoveWatcherMethod = getRemoveWatcherMethod =
              io.grpc.MethodDescriptor.<RemoveWatcherRequest, RemoveWatcherResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RemoveWatcher"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  RemoveWatcherRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  RemoveWatcherResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FilesystemMethodDescriptorSupplier("RemoveWatcher"))
              .build();
        }
      }
    }
    return getRemoveWatcherMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static FilesystemStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FilesystemStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FilesystemStub>() {
        @Override
        public FilesystemStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FilesystemStub(channel, callOptions);
        }
      };
    return FilesystemStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static FilesystemBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FilesystemBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FilesystemBlockingStub>() {
        @Override
        public FilesystemBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FilesystemBlockingStub(channel, callOptions);
        }
      };
    return FilesystemBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static FilesystemFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FilesystemFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FilesystemFutureStub>() {
        @Override
        public FilesystemFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FilesystemFutureStub(channel, callOptions);
        }
      };
    return FilesystemFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void stat(StatRequest request,
        io.grpc.stub.StreamObserver<StatResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getStatMethod(), responseObserver);
    }

    /**
     */
    default void makeDir(MakeDirRequest request,
        io.grpc.stub.StreamObserver<MakeDirResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getMakeDirMethod(), responseObserver);
    }

    /**
     */
    default void move(MoveRequest request,
        io.grpc.stub.StreamObserver<MoveResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getMoveMethod(), responseObserver);
    }

    /**
     */
    default void listDir(ListDirRequest request,
        io.grpc.stub.StreamObserver<ListDirResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListDirMethod(), responseObserver);
    }

    /**
     */
    default void remove(RemoveRequest request,
        io.grpc.stub.StreamObserver<RemoveResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRemoveMethod(), responseObserver);
    }

    /**
     */
    default void watchDir(WatchDirRequest request,
        io.grpc.stub.StreamObserver<WatchDirResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getWatchDirMethod(), responseObserver);
    }

    /**
     * <pre>
     * Non-streaming versions of WatchDir
     * </pre>
     */
    default void createWatcher(CreateWatcherRequest request,
        io.grpc.stub.StreamObserver<CreateWatcherResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateWatcherMethod(), responseObserver);
    }

    /**
     */
    default void getWatcherEvents(GetWatcherEventsRequest request,
        io.grpc.stub.StreamObserver<GetWatcherEventsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWatcherEventsMethod(), responseObserver);
    }

    /**
     */
    default void removeWatcher(RemoveWatcherRequest request,
        io.grpc.stub.StreamObserver<RemoveWatcherResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRemoveWatcherMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service Filesystem.
   */
  public static abstract class FilesystemImplBase
      implements io.grpc.BindableService, AsyncService {

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return FilesystemGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service Filesystem.
   */
  public static final class FilesystemStub
      extends io.grpc.stub.AbstractAsyncStub<FilesystemStub> {
    private FilesystemStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected FilesystemStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FilesystemStub(channel, callOptions);
    }

    /**
     */
    public void stat(StatRequest request,
        io.grpc.stub.StreamObserver<StatResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getStatMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void makeDir(MakeDirRequest request,
        io.grpc.stub.StreamObserver<MakeDirResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getMakeDirMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void move(MoveRequest request,
        io.grpc.stub.StreamObserver<MoveResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getMoveMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listDir(ListDirRequest request,
        io.grpc.stub.StreamObserver<ListDirResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListDirMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void remove(RemoveRequest request,
        io.grpc.stub.StreamObserver<RemoveResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRemoveMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void watchDir(WatchDirRequest request,
        io.grpc.stub.StreamObserver<WatchDirResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getWatchDirMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Non-streaming versions of WatchDir
     * </pre>
     */
    public void createWatcher(CreateWatcherRequest request,
        io.grpc.stub.StreamObserver<CreateWatcherResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateWatcherMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getWatcherEvents(GetWatcherEventsRequest request,
        io.grpc.stub.StreamObserver<GetWatcherEventsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWatcherEventsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void removeWatcher(RemoveWatcherRequest request,
        io.grpc.stub.StreamObserver<RemoveWatcherResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRemoveWatcherMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service Filesystem.
   */
  public static final class FilesystemBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<FilesystemBlockingStub> {
    private FilesystemBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected FilesystemBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FilesystemBlockingStub(channel, callOptions);
    }

    /**
     */
    public StatResponse stat(StatRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getStatMethod(), getCallOptions(), request);
    }

    /**
     */
    public MakeDirResponse makeDir(MakeDirRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getMakeDirMethod(), getCallOptions(), request);
    }

    /**
     */
    public MoveResponse move(MoveRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getMoveMethod(), getCallOptions(), request);
    }

    /**
     */
    public ListDirResponse listDir(ListDirRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListDirMethod(), getCallOptions(), request);
    }

    /**
     */
    public RemoveResponse remove(RemoveRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRemoveMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<WatchDirResponse> watchDir(
        WatchDirRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getWatchDirMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Non-streaming versions of WatchDir
     * </pre>
     */
    public CreateWatcherResponse createWatcher(CreateWatcherRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateWatcherMethod(), getCallOptions(), request);
    }

    /**
     */
    public GetWatcherEventsResponse getWatcherEvents(GetWatcherEventsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWatcherEventsMethod(), getCallOptions(), request);
    }

    /**
     */
    public RemoveWatcherResponse removeWatcher(RemoveWatcherRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRemoveWatcherMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service Filesystem.
   */
  public static final class FilesystemFutureStub
      extends io.grpc.stub.AbstractFutureStub<FilesystemFutureStub> {
    private FilesystemFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected FilesystemFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FilesystemFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<StatResponse> stat(
        StatRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getStatMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<MakeDirResponse> makeDir(
        MakeDirRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getMakeDirMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<MoveResponse> move(
        MoveRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getMoveMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ListDirResponse> listDir(
        ListDirRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListDirMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<RemoveResponse> remove(
        RemoveRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRemoveMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Non-streaming versions of WatchDir
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<CreateWatcherResponse> createWatcher(
        CreateWatcherRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateWatcherMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<GetWatcherEventsResponse> getWatcherEvents(
        GetWatcherEventsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWatcherEventsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<RemoveWatcherResponse> removeWatcher(
        RemoveWatcherRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRemoveWatcherMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_STAT = 0;
  private static final int METHODID_MAKE_DIR = 1;
  private static final int METHODID_MOVE = 2;
  private static final int METHODID_LIST_DIR = 3;
  private static final int METHODID_REMOVE = 4;
  private static final int METHODID_WATCH_DIR = 5;
  private static final int METHODID_CREATE_WATCHER = 6;
  private static final int METHODID_GET_WATCHER_EVENTS = 7;
  private static final int METHODID_REMOVE_WATCHER = 8;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_STAT:
          serviceImpl.stat((StatRequest) request,
              (io.grpc.stub.StreamObserver<StatResponse>) responseObserver);
          break;
        case METHODID_MAKE_DIR:
          serviceImpl.makeDir((MakeDirRequest) request,
              (io.grpc.stub.StreamObserver<MakeDirResponse>) responseObserver);
          break;
        case METHODID_MOVE:
          serviceImpl.move((MoveRequest) request,
              (io.grpc.stub.StreamObserver<MoveResponse>) responseObserver);
          break;
        case METHODID_LIST_DIR:
          serviceImpl.listDir((ListDirRequest) request,
              (io.grpc.stub.StreamObserver<ListDirResponse>) responseObserver);
          break;
        case METHODID_REMOVE:
          serviceImpl.remove((RemoveRequest) request,
              (io.grpc.stub.StreamObserver<RemoveResponse>) responseObserver);
          break;
        case METHODID_WATCH_DIR:
          serviceImpl.watchDir((WatchDirRequest) request,
              (io.grpc.stub.StreamObserver<WatchDirResponse>) responseObserver);
          break;
        case METHODID_CREATE_WATCHER:
          serviceImpl.createWatcher((CreateWatcherRequest) request,
              (io.grpc.stub.StreamObserver<CreateWatcherResponse>) responseObserver);
          break;
        case METHODID_GET_WATCHER_EVENTS:
          serviceImpl.getWatcherEvents((GetWatcherEventsRequest) request,
              (io.grpc.stub.StreamObserver<GetWatcherEventsResponse>) responseObserver);
          break;
        case METHODID_REMOVE_WATCHER:
          serviceImpl.removeWatcher((RemoveWatcherRequest) request,
              (io.grpc.stub.StreamObserver<RemoveWatcherResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getStatMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              StatRequest,
              StatResponse>(
                service, METHODID_STAT)))
        .addMethod(
          getMakeDirMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              MakeDirRequest,
              MakeDirResponse>(
                service, METHODID_MAKE_DIR)))
        .addMethod(
          getMoveMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              MoveRequest,
              MoveResponse>(
                service, METHODID_MOVE)))
        .addMethod(
          getListDirMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ListDirRequest,
              ListDirResponse>(
                service, METHODID_LIST_DIR)))
        .addMethod(
          getRemoveMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              RemoveRequest,
              RemoveResponse>(
                service, METHODID_REMOVE)))
        .addMethod(
          getWatchDirMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              WatchDirRequest,
              WatchDirResponse>(
                service, METHODID_WATCH_DIR)))
        .addMethod(
          getCreateWatcherMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              CreateWatcherRequest,
              CreateWatcherResponse>(
                service, METHODID_CREATE_WATCHER)))
        .addMethod(
          getGetWatcherEventsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              GetWatcherEventsRequest,
              GetWatcherEventsResponse>(
                service, METHODID_GET_WATCHER_EVENTS)))
        .addMethod(
          getRemoveWatcherMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              RemoveWatcherRequest,
              RemoveWatcherResponse>(
                service, METHODID_REMOVE_WATCHER)))
        .build();
  }

  private static abstract class FilesystemBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    FilesystemBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return FilesystemProto.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Filesystem");
    }
  }

  private static final class FilesystemFileDescriptorSupplier
      extends FilesystemBaseDescriptorSupplier {
    FilesystemFileDescriptorSupplier() {}
  }

  private static final class FilesystemMethodDescriptorSupplier
      extends FilesystemBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    FilesystemMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (FilesystemGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new FilesystemFileDescriptorSupplier())
              .addMethod(getStatMethod())
              .addMethod(getMakeDirMethod())
              .addMethod(getMoveMethod())
              .addMethod(getListDirMethod())
              .addMethod(getRemoveMethod())
              .addMethod(getWatchDirMethod())
              .addMethod(getCreateWatcherMethod())
              .addMethod(getGetWatcherEventsMethod())
              .addMethod(getRemoveWatcherMethod())
              .build();
        }
      }
    }
    return result;
  }
}
