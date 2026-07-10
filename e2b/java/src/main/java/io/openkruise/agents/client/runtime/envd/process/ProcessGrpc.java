package io.openkruise.agents.client.runtime.envd.process;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.60.0)",
    comments = "Source: process/process.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ProcessGrpc {

  private ProcessGrpc() {}

  public static final String SERVICE_NAME = "process.Process";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ListRequest,
      ListResponse> getListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "List",
      requestType = ListRequest.class,
      responseType = ListResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ListRequest,
      ListResponse> getListMethod() {
    io.grpc.MethodDescriptor<ListRequest, ListResponse> getListMethod;
    if ((getListMethod = ProcessGrpc.getListMethod) == null) {
      synchronized (ProcessGrpc.class) {
        if ((getListMethod = ProcessGrpc.getListMethod) == null) {
          ProcessGrpc.getListMethod = getListMethod =
              io.grpc.MethodDescriptor.<ListRequest, ListResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "List"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ListRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ListResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ProcessMethodDescriptorSupplier("List"))
              .build();
        }
      }
    }
    return getListMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ConnectRequest,
      ConnectResponse> getConnectMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Connect",
      requestType = ConnectRequest.class,
      responseType = ConnectResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<ConnectRequest,
      ConnectResponse> getConnectMethod() {
    io.grpc.MethodDescriptor<ConnectRequest, ConnectResponse> getConnectMethod;
    if ((getConnectMethod = ProcessGrpc.getConnectMethod) == null) {
      synchronized (ProcessGrpc.class) {
        if ((getConnectMethod = ProcessGrpc.getConnectMethod) == null) {
          ProcessGrpc.getConnectMethod = getConnectMethod =
              io.grpc.MethodDescriptor.<ConnectRequest, ConnectResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Connect"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ConnectRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ConnectResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ProcessMethodDescriptorSupplier("Connect"))
              .build();
        }
      }
    }
    return getConnectMethod;
  }

  private static volatile io.grpc.MethodDescriptor<StartRequest,
      StartResponse> getStartMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Start",
      requestType = StartRequest.class,
      responseType = StartResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<StartRequest,
      StartResponse> getStartMethod() {
    io.grpc.MethodDescriptor<StartRequest, StartResponse> getStartMethod;
    if ((getStartMethod = ProcessGrpc.getStartMethod) == null) {
      synchronized (ProcessGrpc.class) {
        if ((getStartMethod = ProcessGrpc.getStartMethod) == null) {
          ProcessGrpc.getStartMethod = getStartMethod =
              io.grpc.MethodDescriptor.<StartRequest, StartResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Start"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  StartRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  StartResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ProcessMethodDescriptorSupplier("Start"))
              .build();
        }
      }
    }
    return getStartMethod;
  }

  private static volatile io.grpc.MethodDescriptor<UpdateRequest,
      UpdateResponse> getUpdateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Update",
      requestType = UpdateRequest.class,
      responseType = UpdateResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<UpdateRequest,
      UpdateResponse> getUpdateMethod() {
    io.grpc.MethodDescriptor<UpdateRequest, UpdateResponse> getUpdateMethod;
    if ((getUpdateMethod = ProcessGrpc.getUpdateMethod) == null) {
      synchronized (ProcessGrpc.class) {
        if ((getUpdateMethod = ProcessGrpc.getUpdateMethod) == null) {
          ProcessGrpc.getUpdateMethod = getUpdateMethod =
              io.grpc.MethodDescriptor.<UpdateRequest, UpdateResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Update"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  UpdateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  UpdateResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ProcessMethodDescriptorSupplier("Update"))
              .build();
        }
      }
    }
    return getUpdateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<StreamInputRequest,
      StreamInputResponse> getStreamInputMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StreamInput",
      requestType = StreamInputRequest.class,
      responseType = StreamInputResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<StreamInputRequest,
      StreamInputResponse> getStreamInputMethod() {
    io.grpc.MethodDescriptor<StreamInputRequest, StreamInputResponse> getStreamInputMethod;
    if ((getStreamInputMethod = ProcessGrpc.getStreamInputMethod) == null) {
      synchronized (ProcessGrpc.class) {
        if ((getStreamInputMethod = ProcessGrpc.getStreamInputMethod) == null) {
          ProcessGrpc.getStreamInputMethod = getStreamInputMethod =
              io.grpc.MethodDescriptor.<StreamInputRequest, StreamInputResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StreamInput"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  StreamInputRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  StreamInputResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ProcessMethodDescriptorSupplier("StreamInput"))
              .build();
        }
      }
    }
    return getStreamInputMethod;
  }

  private static volatile io.grpc.MethodDescriptor<SendInputRequest,
      SendInputResponse> getSendInputMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendInput",
      requestType = SendInputRequest.class,
      responseType = SendInputResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<SendInputRequest,
      SendInputResponse> getSendInputMethod() {
    io.grpc.MethodDescriptor<SendInputRequest, SendInputResponse> getSendInputMethod;
    if ((getSendInputMethod = ProcessGrpc.getSendInputMethod) == null) {
      synchronized (ProcessGrpc.class) {
        if ((getSendInputMethod = ProcessGrpc.getSendInputMethod) == null) {
          ProcessGrpc.getSendInputMethod = getSendInputMethod =
              io.grpc.MethodDescriptor.<SendInputRequest, SendInputResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendInput"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  SendInputRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  SendInputResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ProcessMethodDescriptorSupplier("SendInput"))
              .build();
        }
      }
    }
    return getSendInputMethod;
  }

  private static volatile io.grpc.MethodDescriptor<SendSignalRequest,
      SendSignalResponse> getSendSignalMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendSignal",
      requestType = SendSignalRequest.class,
      responseType = SendSignalResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<SendSignalRequest,
      SendSignalResponse> getSendSignalMethod() {
    io.grpc.MethodDescriptor<SendSignalRequest, SendSignalResponse> getSendSignalMethod;
    if ((getSendSignalMethod = ProcessGrpc.getSendSignalMethod) == null) {
      synchronized (ProcessGrpc.class) {
        if ((getSendSignalMethod = ProcessGrpc.getSendSignalMethod) == null) {
          ProcessGrpc.getSendSignalMethod = getSendSignalMethod =
              io.grpc.MethodDescriptor.<SendSignalRequest, SendSignalResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendSignal"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  SendSignalRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  SendSignalResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ProcessMethodDescriptorSupplier("SendSignal"))
              .build();
        }
      }
    }
    return getSendSignalMethod;
  }

  private static volatile io.grpc.MethodDescriptor<CloseStdinRequest,
      CloseStdinResponse> getCloseStdinMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CloseStdin",
      requestType = CloseStdinRequest.class,
      responseType = CloseStdinResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<CloseStdinRequest,
      CloseStdinResponse> getCloseStdinMethod() {
    io.grpc.MethodDescriptor<CloseStdinRequest, CloseStdinResponse> getCloseStdinMethod;
    if ((getCloseStdinMethod = ProcessGrpc.getCloseStdinMethod) == null) {
      synchronized (ProcessGrpc.class) {
        if ((getCloseStdinMethod = ProcessGrpc.getCloseStdinMethod) == null) {
          ProcessGrpc.getCloseStdinMethod = getCloseStdinMethod =
              io.grpc.MethodDescriptor.<CloseStdinRequest, CloseStdinResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CloseStdin"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  CloseStdinRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  CloseStdinResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ProcessMethodDescriptorSupplier("CloseStdin"))
              .build();
        }
      }
    }
    return getCloseStdinMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ProcessStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ProcessStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ProcessStub>() {
        @Override
        public ProcessStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ProcessStub(channel, callOptions);
        }
      };
    return ProcessStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ProcessBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ProcessBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ProcessBlockingStub>() {
        @Override
        public ProcessBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ProcessBlockingStub(channel, callOptions);
        }
      };
    return ProcessBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ProcessFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ProcessFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ProcessFutureStub>() {
        @Override
        public ProcessFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ProcessFutureStub(channel, callOptions);
        }
      };
    return ProcessFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void list(ListRequest request,
        io.grpc.stub.StreamObserver<ListResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListMethod(), responseObserver);
    }

    /**
     */
    default void connect(ConnectRequest request,
        io.grpc.stub.StreamObserver<ConnectResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getConnectMethod(), responseObserver);
    }

    /**
     */
    default void start(StartRequest request,
        io.grpc.stub.StreamObserver<StartResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getStartMethod(), responseObserver);
    }

    /**
     */
    default void update(UpdateRequest request,
        io.grpc.stub.StreamObserver<UpdateResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateMethod(), responseObserver);
    }

    /**
     * <pre>
     * Client input stream ensures ordering of messages
     * </pre>
     */
    default io.grpc.stub.StreamObserver<StreamInputRequest> streamInput(
        io.grpc.stub.StreamObserver<StreamInputResponse> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getStreamInputMethod(), responseObserver);
    }

    /**
     */
    default void sendInput(SendInputRequest request,
        io.grpc.stub.StreamObserver<SendInputResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSendInputMethod(), responseObserver);
    }

    /**
     */
    default void sendSignal(SendSignalRequest request,
        io.grpc.stub.StreamObserver<SendSignalResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSendSignalMethod(), responseObserver);
    }

    /**
     * <pre>
     * Close stdin to signal EOF to the process.
     * Only works for non-PTY processes. For PTY, send Ctrl+D (0x04) instead.
     * </pre>
     */
    default void closeStdin(CloseStdinRequest request,
        io.grpc.stub.StreamObserver<CloseStdinResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCloseStdinMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service Process.
   */
  public static abstract class ProcessImplBase
      implements io.grpc.BindableService, AsyncService {

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return ProcessGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service Process.
   */
  public static final class ProcessStub
      extends io.grpc.stub.AbstractAsyncStub<ProcessStub> {
    private ProcessStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected ProcessStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ProcessStub(channel, callOptions);
    }

    /**
     */
    public void list(ListRequest request,
        io.grpc.stub.StreamObserver<ListResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void connect(ConnectRequest request,
        io.grpc.stub.StreamObserver<ConnectResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getConnectMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void start(StartRequest request,
        io.grpc.stub.StreamObserver<StartResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getStartMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void update(UpdateRequest request,
        io.grpc.stub.StreamObserver<UpdateResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Client input stream ensures ordering of messages
     * </pre>
     */
    public io.grpc.stub.StreamObserver<StreamInputRequest> streamInput(
        io.grpc.stub.StreamObserver<StreamInputResponse> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncClientStreamingCall(
          getChannel().newCall(getStreamInputMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void sendInput(SendInputRequest request,
        io.grpc.stub.StreamObserver<SendInputResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendInputMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void sendSignal(SendSignalRequest request,
        io.grpc.stub.StreamObserver<SendSignalResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendSignalMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Close stdin to signal EOF to the process.
     * Only works for non-PTY processes. For PTY, send Ctrl+D (0x04) instead.
     * </pre>
     */
    public void closeStdin(CloseStdinRequest request,
        io.grpc.stub.StreamObserver<CloseStdinResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCloseStdinMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service Process.
   */
  public static final class ProcessBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<ProcessBlockingStub> {
    private ProcessBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected ProcessBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ProcessBlockingStub(channel, callOptions);
    }

    /**
     */
    public ListResponse list(ListRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<ConnectResponse> connect(
        ConnectRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getConnectMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<StartResponse> start(
        StartRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getStartMethod(), getCallOptions(), request);
    }

    /**
     */
    public UpdateResponse update(UpdateRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateMethod(), getCallOptions(), request);
    }

    /**
     */
    public SendInputResponse sendInput(SendInputRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSendInputMethod(), getCallOptions(), request);
    }

    /**
     */
    public SendSignalResponse sendSignal(SendSignalRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSendSignalMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Close stdin to signal EOF to the process.
     * Only works for non-PTY processes. For PTY, send Ctrl+D (0x04) instead.
     * </pre>
     */
    public CloseStdinResponse closeStdin(CloseStdinRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCloseStdinMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service Process.
   */
  public static final class ProcessFutureStub
      extends io.grpc.stub.AbstractFutureStub<ProcessFutureStub> {
    private ProcessFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected ProcessFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ProcessFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ListResponse> list(
        ListRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<UpdateResponse> update(
        UpdateRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<SendInputResponse> sendInput(
        SendInputRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSendInputMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<SendSignalResponse> sendSignal(
        SendSignalRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSendSignalMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Close stdin to signal EOF to the process.
     * Only works for non-PTY processes. For PTY, send Ctrl+D (0x04) instead.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<CloseStdinResponse> closeStdin(
        CloseStdinRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCloseStdinMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST = 0;
  private static final int METHODID_CONNECT = 1;
  private static final int METHODID_START = 2;
  private static final int METHODID_UPDATE = 3;
  private static final int METHODID_SEND_INPUT = 4;
  private static final int METHODID_SEND_SIGNAL = 5;
  private static final int METHODID_CLOSE_STDIN = 6;
  private static final int METHODID_STREAM_INPUT = 7;

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
        case METHODID_LIST:
          serviceImpl.list((ListRequest) request,
              (io.grpc.stub.StreamObserver<ListResponse>) responseObserver);
          break;
        case METHODID_CONNECT:
          serviceImpl.connect((ConnectRequest) request,
              (io.grpc.stub.StreamObserver<ConnectResponse>) responseObserver);
          break;
        case METHODID_START:
          serviceImpl.start((StartRequest) request,
              (io.grpc.stub.StreamObserver<StartResponse>) responseObserver);
          break;
        case METHODID_UPDATE:
          serviceImpl.update((UpdateRequest) request,
              (io.grpc.stub.StreamObserver<UpdateResponse>) responseObserver);
          break;
        case METHODID_SEND_INPUT:
          serviceImpl.sendInput((SendInputRequest) request,
              (io.grpc.stub.StreamObserver<SendInputResponse>) responseObserver);
          break;
        case METHODID_SEND_SIGNAL:
          serviceImpl.sendSignal((SendSignalRequest) request,
              (io.grpc.stub.StreamObserver<SendSignalResponse>) responseObserver);
          break;
        case METHODID_CLOSE_STDIN:
          serviceImpl.closeStdin((CloseStdinRequest) request,
              (io.grpc.stub.StreamObserver<CloseStdinResponse>) responseObserver);
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
        case METHODID_STREAM_INPUT:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.streamInput(
              (io.grpc.stub.StreamObserver<StreamInputResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getListMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ListRequest,
              ListResponse>(
                service, METHODID_LIST)))
        .addMethod(
          getConnectMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              ConnectRequest,
              ConnectResponse>(
                service, METHODID_CONNECT)))
        .addMethod(
          getStartMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              StartRequest,
              StartResponse>(
                service, METHODID_START)))
        .addMethod(
          getUpdateMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              UpdateRequest,
              UpdateResponse>(
                service, METHODID_UPDATE)))
        .addMethod(
          getStreamInputMethod(),
          io.grpc.stub.ServerCalls.asyncClientStreamingCall(
            new MethodHandlers<
              StreamInputRequest,
              StreamInputResponse>(
                service, METHODID_STREAM_INPUT)))
        .addMethod(
          getSendInputMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              SendInputRequest,
              SendInputResponse>(
                service, METHODID_SEND_INPUT)))
        .addMethod(
          getSendSignalMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              SendSignalRequest,
              SendSignalResponse>(
                service, METHODID_SEND_SIGNAL)))
        .addMethod(
          getCloseStdinMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              CloseStdinRequest,
              CloseStdinResponse>(
                service, METHODID_CLOSE_STDIN)))
        .build();
  }

  private static abstract class ProcessBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ProcessBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ProcessProto.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Process");
    }
  }

  private static final class ProcessFileDescriptorSupplier
      extends ProcessBaseDescriptorSupplier {
    ProcessFileDescriptorSupplier() {}
  }

  private static final class ProcessMethodDescriptorSupplier
      extends ProcessBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ProcessMethodDescriptorSupplier(String methodName) {
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
      synchronized (ProcessGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ProcessFileDescriptorSupplier())
              .addMethod(getListMethod())
              .addMethod(getConnectMethod())
              .addMethod(getStartMethod())
              .addMethod(getUpdateMethod())
              .addMethod(getStreamInputMethod())
              .addMethod(getSendInputMethod())
              .addMethod(getSendSignalMethod())
              .addMethod(getCloseStdinMethod())
              .build();
        }
      }
    }
    return result;
  }
}
