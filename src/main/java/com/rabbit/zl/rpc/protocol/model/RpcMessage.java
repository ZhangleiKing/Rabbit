package com.rabbit.zl.rpc.protocol.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RPC messages use generic message format for transferring data.
 * <pre>
 *      rpc-message = rpc-header + [ rpc-body ]
 *      rpc-body is optional
 * </pre>
 *
 * @author Vincent
 * Created  on 2017/11/10.
 */
@ToString
@EqualsAndHashCode(of = {"header", "body"})
public class RpcMessage implements Serializable{

    private static final long serialVersionUID = 5138100956693144357L;

    @Getter @Setter private RpcHeader header;

    @Getter @Setter private RpcBody body;

    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    private static RpcMessage newRpcMessage() {
        RpcMessage message = new RpcMessage();
        RpcHeader header = new RpcHeader();
        RpcBody body = new RpcBody();
        message.setHeader(header);
        message.setBody(body);
        return message;
    }

    public static RpcMessage newRequestMessage(Class<?> rpcInterface, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
        RpcMessage request = newRpcMessage();
        request.setMid(ID_GENERATOR.incrementAndGet());
        RpcBody body = request.getBody();
        body.setRpcInterface(rpcInterface);
        RpcMethod rpcMethod = new RpcMethod(methodName, parameterTypes, parameters);
        body.setRpcMethod(rpcMethod);
        body.setRpcOption(new RpcOption());
        return request;
    }

    public static RpcMessage newResponseMessage(long id, Object returnObject) {
        RpcMessage response = newRpcMessage();
        response.setMid(id);
        response.setResponse(true);
        response.setRpcReturn(returnObject);
        return response;
    }

    /**
     * Unpack the response message, if it has exception, then throw the exception
     * Get the "return object" and return it.
     *
     * @param response
     * @return
     * @throws Exception
     */
    public static Object unpackResponseMessage(RpcMessage response) throws Exception{
        if(response == null) {
            return null;
        }

        Exception e = response.getException();
        if(e != null) {
            throw e;
        }

        return response.getRpcReturn();
    }

    public boolean isOneWay() {
        return header.isOw();
    }

    public void setOneWay(boolean oneway) {
        if(oneway)
            header.setOw();
    }

    public boolean isResponse() {
        return header.isRp();
    }

    public void setResponse(boolean response) {
        if(response)
            header.setRp();
    }

    public boolean isHeartBeat() {
        return header.isHb();
    }

    public void setHeartBeat(boolean heartBeat) {
        if(heartBeat)
            header.setHb();
    }

    public long getMid() {
        return header.getMid();
    }

    public void setMid(long id) {
        header.setMid(id);
    }

    public String getRpcId() {
        return body.getRpcId();
    }

    public void setRpcId(String id) {
        body.setRpcId(id);
    }

    public Class<?> getRpcInterface() {
        return body.getRpcInterface();
    }

    public void setRpcInterface(Class<?> rpcInterface) {
        body.setRpcInterface(rpcInterface);
    }

    public String getApplication() {
        return body.getApplication();
    }

    public void setApplication(String application) {
        body.setApplication(application);
    }

    public void setRpcAttachments(Map<String, String> attachments) {
        body.setRpcAttachments(attachments);
    }

    public Map<String, String> getRpcAttachments() {
        return body.getRpcAttachments();
    }

    public Exception getException() {
        return body.getRpcException();
    }

    public void setException(Exception e) {
        body.setRpcException(e);
    }

    public int getRpcTimeoutInMillis() {
        return body.getRpcOption().getRpcTimeoutInMillis();
    }

    public void setRpcTimeoutInMillis(int rpcTimeoutInMillis) {
        body.getRpcOption().setRpcTimeoutInMillis(rpcTimeoutInMillis);
    }

    public Object getRpcReturn() {
        return body.getRpcReturn();
    }

    public void setRpcReturn(Object obj) {
        body.setRpcReturn(obj);
    }

    public void setServerAddress(InetSocketAddress serverAddress) {
        body.getRpcOption().setServerAddress(serverAddress);
    }

    public InetSocketAddress getServerAddress() {
        return body.getRpcOption().getServerAddress();
    }

    public void setClientAddress(InetSocketAddress clientAddress) {
        body.getRpcOption().setClientAddress(clientAddress);
    }

    public InetSocketAddress getClientAddress() {
        return body.getRpcOption().getClientAddress();
    }
}
