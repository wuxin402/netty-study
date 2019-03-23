package com.wuxin.netty.socket.io;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketIoClient {

	public static void main(String[] args) {

		IO.Options options = new IO.Options();
		// 设置协议为websocket
		options.transports = new String[] { "websocket" };
		// 失败重连次数
		options.reconnectionAttempts = 5;
		// 失败重连的时间间隔
		options.reconnectionDelay = 3000;
		// 连接超时时间(ms)
		options.timeout = 3000;
		// 开启重连
		options.reconnection = true;
		// 可以携带一些连接参数
		// options.query="token=1234";

		try {
			final Socket socket = IO.socket("http://app.52rbl.com:8004/delivery", options);
			// 连接服务器
			socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					System.out.println(System.currentTimeMillis() + ":client connect! ");

					// 连接成功，马上向服务器发送信息，例如：用户注册
					socket.emit("register_event", "发给服务器的消息", new Ack() {
						@Override
						public void call(Object... aobj) {
							System.out.println(aobj[0]);
						}
					});
				}
			});

			// 监听断线
			socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					System.out.println(System.currentTimeMillis() + ":client disconnect!");
				}
			});

			// 监听ping,其实这个监听没有多少意义
			socket.on(Socket.EVENT_PING, new Emitter.Listener() {
				@Override
				public void call(Object... arg0) {
					// 往服务器写ping
					System.out.println("ping:" + arg0);
				}
			});

			// 监听pong,这个监听没有多少意义
			socket.on(Socket.EVENT_PONG, new Emitter.Listener() {
				@Override
				public void call(Object... arg0) {
					// ping了以后接收服务器的pong响应
					System.out.println("pong:" + arg0[0]);
				}
			});

			// 监听服务器发送的消息，也可以自定义一个简单字符串
			socket.on(Socket.EVENT_MESSAGE, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					// 接收消息后，往服务器发送一个ack,告诉服务器消息收到了
					Ack ack = (Ack) args[args.length - 1];
					ack.call(args[0].hashCode());

					for (Object obj : args) {
						System.out.println(System.currentTimeMillis() + ":receive server message=" + obj.toString());
					}
				}
			});

			// 连接服务器
			socket.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
