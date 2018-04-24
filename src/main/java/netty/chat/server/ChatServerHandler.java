package netty.chat.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import netty.chat.command.CommandFactory;
import netty.chat.command.ICommand;

public class ChatServerHandler extends ChannelInboundMessageHandlerAdapter<String> {

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		incoming.write("Welcome. You need to log in first in order to access Rooms\n");
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		ICommand command = new CommandFactory().createCommand(ctx.channel(), "/leave");
		command.execute();
	}	
	
	public void messageReceived(ChannelHandlerContext ctx, String message) {
		Channel incoming = ctx.channel();
		try { 
			ICommand command = new CommandFactory().createCommand(incoming, message);
			command.execute();
		} catch (Exception e) {
			incoming.write(e.getMessage());
		}
	}
}
