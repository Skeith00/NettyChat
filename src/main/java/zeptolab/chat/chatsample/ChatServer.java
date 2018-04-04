package zeptolab.chat.chatsample;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

//https://www.youtube.com/watch?v=tsz-assb1X8
public class ChatServer {
	
	public static void main(String[] args) throws InterruptedException {
		new ChatServer(8000).run();
	}
	
	private final int port;
	
	public ChatServer(int port){
		this.port = port;
	}
	
	public void run() throws InterruptedException {
		EventLoopGroup bossgroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap bootstrap = new ServerBootstrap()
					.group(bossgroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChatServerInitializer());
			
			// https://stackoverflow.com/questions/41505852/netty-closefuture-sync-channel-blocks-rest-api
			// https://stackoverflow.com/questions/28032092/shutdown-netty-programmatically/28034689
			
			bootstrap.bind(port).sync().channel().closeFuture().sync();
			
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			bossgroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
