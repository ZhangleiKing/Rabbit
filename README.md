﻿# Rabbit

从头搭建一个RPC框架，初步打算使用Netty进行数据通信，后期可能会增加BIO和NIO的方式

因为是小白入门级别，所以项目代号Rabbit

1、首先，先解释下RPC：

RPC = Remote Procedure Call ，远程过程调用，它能够通过网络从远程计算机上请求服务，且满足底层网络传输对用户透明化，让用户如同调用本地服务一般去调用远程服务。

2、RPC与HTTP请求的区别：

利用应用层的HTTP协议，也可以满足客户/服务器模式的信息交换，那为何还需要更为复杂的RPC框架呢？

我们知道，使用HTTP接口，一般有四个过程：（3次握手）建立（TCP协议）连接，发送请求信息，返回响应信息，（4次握手）关闭连接。之所以与RPC混为一谈，就是因为HTTP请求也包括了发送请求和返回消息这两个过程。


但是，这二者不是一种类型的事物：

RPC不是某一种专门的协议，而可以认为是一种编程模式，把对服务器的调用抽象为过程调用，其关键在于请求、响应消息体的封装、编码协议等，因为在RPC中也存在着网络传输，所以HTTP甚至可以用于RPC的传输协议。但是HTTP传输，本身更多的是关注底层数据的传输，而RPC不仅可以传输数据，也可以做到服务发现、函数代理调用等，可以看做是面向服务的封装。相对于HTTP，RPC做了更多的封装，使得用户开发更加轻松简单。

另一方面，RPC一般用于企业内网服务间的调用，HTTP一般用于外部，可以做到跨语言的传输。成熟的RPC框架一般使用二进制传输，而一般HTTP使用json，性能相对于RPC而言较低。HTTP自身的编码协议不够精简，往往包含太多的无用报文元数据。RPC可以自定义编码协议，能精简传输内容。

3、Rpc与动态代理

Rpc中，动态代理是非常重要的一个内容，主要用于做方法增强，即可以在不修改源码的情况下，做额外的事情。对应Rpc中，我们可以在invoke方法中获取Method对像，并在其之前（后）进行额外的操作

下面我将以Jdk动态代理的方式陈述一下Client端的调用过程。待本项目完成后，客户端使用流程如下：


    (1) RpcClient client = new RpcClient();

>     首先，new一个客户端。客户端是用于向用户提供API的，例如我们希望调用远程服务器上提供的DemoService类的helloWord()方法，则

    (2) DemoService service = client.getProxy(DemoService.class);

>     getProxy()使用了Jdk的动态代理，内部实际调用了Proxy.newProxyInstance方法

>     newProxyInstance()这个方法有三个参数
>     @ loader, 定义了一个ClassLoader对象，用于加载生成的代理对象
>     @ interfaces，给需要代理的对象提供一组接口，可认为该对象实现了这些接口，因此可调用这些接口中的方法
>     @ h，一个InvocationHandler对象，表示当这个动态代理对象在调用方法时，会关联到哪一个InvocationHandler上

    (3) service.helloWorld("Hello, World");
>     当调用helloWord方法时，系统会根据newProxyInstance中关联的InvocationHandler，调用它的invoke()方法，即调用具体所需的方法时，触发invoke()

    (4) Class RpcInvocationHandler: Object invoke(Object proxy, Method method, Object[] args) {
        ...
        rpcInvoker.invoker(request);
        ...
    }
>     客户端和服务端均使用同一个InvocationHandler类，这是因为invoke方法会去调用RpcInvoker的invoke方法，因此只需要将客户端RpcInvoker和服务器端RpcInvoker实现的不同即可。
>     该invoke方法有三个参数
>     @ proxy, 所代理的真实对象(服务端调用时的ServiceBeanImpl，而不是接口)
>     @ method，所要调用代理的某个方法
>     @ args，调用方法时传入的参数

    (5) Class ClientRpcInvoker: Object invoke(Object request) {
        ...
        connector.send(request);
        ...
    }

>     客户端和服务端的RpcInvoker逻辑不一样：
>     客户端，需要通过connector将request消息发送给服务端
>     服务端，接收到客户端传入的信息，通过method.invoke()调用真实对象，执行方法，获得结果并返回给服务端


4、代码目录结构：

client：服务消费方，提供client，供用户调用相关API（目前主要就是getBean这个接口）。

clientStub：负责编码消息体，并“发现”服务及其相关信息，比如服务提供方的IP地址，然后发送消息体。

common：常用工具包，主要包括序列化、压缩、缓冲等。

rpc: 最重要的部分,包含以下几个模块
> executor：线程池，服务端可获取线程用于处理来自客户端的请求；

> invoke： 定义了客户端和服务端需要使用的RpcInvoker接口等；

> monitor: 监控，主要用于监控当前任务执行情况，包括已完成、正在执行、等待执行等（目前暂未使用）；

> protocol：消息体，或者成为协议，用于定义客户端与服务器通信的数据格式；

> registry：用于向第三方zookeeper注册和发现服务；

> transmission：提供统一的数据通信调用接口（这样可以允许底层通信方式采用多种手段）

server：扫描自定义的服务并向第三方进行注册，启动监听来自客户端请求的线程；

serverStub：主要负责解码消息体，然后去线程池获取线程，处理完请求后返回结果；

transfer：主要为具体的底层通信方法的实现，目前只有Netty这一种方法；后期还考虑自定义实现NIO方式来实现通信。