package chat.chat.server;

import java.util.concurrent.ConcurrentHashMap;

import chat.chat.beans.ChatRoom;
import chat.chat.beans.User;
import chat.chat.beans.UserInfo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;

public class ChatServerHandler extends ChannelInboundMessageHandlerAdapter<String> {

	private static final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();
	private static final ConcurrentHashMap<String, ChatRoom> chatRooms = new ConcurrentHashMap<String, ChatRoom>();
	private static final ConcurrentHashMap<Channel, UserInfo> loggedUsers = new ConcurrentHashMap<Channel, UserInfo>();

	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		/*Channel incoming = ctx.channel();
		for(Channel channel : channelsWaitingRoom) {
			channel.write("[SERVER] + "+ incoming.remoteAddress() + " has joined\n");
		}
		channelsWaitingRoom.add(ctx.channel());*/
		ctx.channel().write("Welcome. You need to log in first in order to access Rooms");
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		leave(ctx.channel());
	}
	
	
	public void messageReceived(ChannelHandlerContext ctx, String message) throws Exception {
		String[] commandData = message.split(" ");
		if(message.startsWith("/login ")) {
			login(commandData, ctx.channel());
		} else if(message.startsWith("/join ")) {
			join(commandData, ctx.channel());
		} else if(message.equals("/leave")) {
			leave(ctx.channel());
		} else if(message.equals("/users")) {
			
		} else {
			ctx.channel().write("Uknown command");
		}
	}
	
	private void login(String[] commandData, Channel channelLogin) {
		if(commandData.length==3) {
			manageUser(commandData, channelLogin);
		}
		else {
			channelLogin.write("Malformed login command");
		}
	}
	
	private void join(String[] commandData, Channel channelLogin) {
		if(commandData.length==2) {
			UserInfo user = loggedUsers.get(channelLogin);
			if(user!=null) {
				addToRoom(commandData[1], channelLogin);
			} else {
				channelLogin.write("You first need to login");
			}
		}
	}
	
	private void leave(Channel channelLeave) {
		leaveRoom(channelLeave);
		channelLeave.write("Logging out...");
		loggedUsers.remove(channelLeave);
		channelLeave.write("Disconnected!");
	}
	
	private void addToRoom(String nameRoom, Channel channelToAdd) {
		ChatRoom chatRoom = chatRooms.get(nameRoom);
		if(chatRoom == null) {
			chatRoom = new ChatRoom(nameRoom);
			chatRooms.put(nameRoom, chatRoom);
			moveUserToRoom(loggedUsers.get(channelToAdd), chatRoom);
		} else {
			if(chatRoom.peopleConnected()<10) {
				chatRoom.addUserChannel(channelToAdd);
				chatRoom.write("[SERVER] + "+ loggedUsers.get(channelToAdd).getUser().getName() + " has joined the room\n");
				moveUserToRoom(loggedUsers.get(channelToAdd), chatRoom);
			}
			else {
				channelToAdd.write("Room full (Max 10)");				
			}
		}
	}
	
	private void leaveRoom(Channel channelUser) {
		UserInfo user = loggedUsers.get(channelUser);
		if(user!=null) {
			ChannelGroup chatRoom = user.getCurrentRoom();
			if(chatRoom!=UserInfo.NOROOM) {
				chatRoom.write("[SERVER] + "+ loggedUsers.get(channelUser).getUser().getName() + " has left the room\n");			
				chatRoom.remove(channelUser);
				if(chatRoom.size()<1)
					chatRooms.remove(key);
			}
		}
	}
	
	// User management 
	private void manageUser(String[] commandData, Channel channelLogin) {
		User user = users.get(commandData[1]);
		if(user==null) {
			user =  new User(commandData[1], commandData[2]);
			users.put(commandData[1], user);
			loggedUsers.put(channelLogin, new UserInfo(user));
		} else {
			if(user.validateUser(commandData[1], commandData[2]))
				loggedUsers.put(channelLogin, new UserInfo(user));
			else
				channelLogin.write("Invalid login. Username or password are wrong.");		
		}
	}
	private void moveUserToRoom(UserInfo user, ChatRoom newRoom) {
		if(user.getCurrentRoom()==null)
		newRoom.addUserChannel(loggedUsers.get(key));
		//Leave from other group necessary
		user.setCurrentRoom(newRoom);
		
	}	
}
