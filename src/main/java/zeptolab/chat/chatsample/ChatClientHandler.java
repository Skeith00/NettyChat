package zeptolab.chat.chatsample;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

public class ChatClientHandler extends ChannelInboundMessageHandlerAdapter<String> {

	public void messageReceived(ChannelHandlerContext arg0, String arg1) throws Exception {
		System.out.println(arg1);		
	}

}
