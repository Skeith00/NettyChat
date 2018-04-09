package netty.chat.server;

import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import netty.chat.beans.ChatRoom;
import netty.chat.beans.User;
import netty.chat.beans.UserInfo;

public class ChatServerHandler extends ChannelInboundMessageHandlerAdapter<String> {

	//private static final ChannelGroup channelsWaitingRoom = new DefaultChannelGroup();	
	private static final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();
	private static final ConcurrentHashMap<String, ChatRoom> chatRooms = new ConcurrentHashMap<String, ChatRoom>();
	private static final ConcurrentHashMap<Channel, UserInfo> loggedUsers = new ConcurrentHashMap<Channel, UserInfo>();

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		/*channelsWaitingRoom.add(ctx.channel());
		for(Channel channel : channelsWaitingRoom) {
			channel.write("[SERVER] + "+ incoming.remoteAddress() + " has joined\n");
		}*/
		incoming.write("Welcome. You need to log in first in order to access Rooms\n");
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		leave(ctx.channel());
	}	
	
	public void messageReceived(ChannelHandlerContext ctx, String message) {
		Channel incoming = ctx.channel();
		try { 
			String[] commandData = message.split(" ");
			if(message.startsWith("/login ")) {
				login(commandData, incoming);
			} else if(message.startsWith("/join ")) {
				join(commandData, incoming);
			} else if(message.equals("/leave")) {
				leave(incoming);
			} else if(message.equals("/users")) {
				users(incoming);
			} else if(message.endsWith("CR")) {
				write(incoming, message.substring(0, message.length()-2));
			} else {
				//incoming.write("Welcome. You need to log in first in order to access Rooms\n");
				incoming.write("Uknown command\n");
			}
		} catch (Exception e) {
			//incoming.write("Welcome. You need to log in first in order to access Rooms\n");
			incoming.write(e.getMessage());
			//System.out.println(e.getMessage());
		}
	}
	
	private void login(String[] commandData, Channel channelLogin) {
		if(commandData.length==3) {
			manageUser(commandData, channelLogin);
		}
		else {
			channelLogin.write("Malformed login command\n");
		}
	}
	
	private void join(String[] commandData, Channel channelJoining) {
		if(commandData.length==2) {
			UserInfo user = loggedUsers.get(channelJoining);			
			if(user!=null) {
				ChatRoom chatRoom = manageRoom(commandData[1]);
				addUserToRoom(user, chatRoom);
			} else {
				channelJoining.write("You first need to login\n");
			}
		} else {
			channelJoining.write("Malformed join command\n");
		} 
	}
	
	private void leave(Channel channelLeave) {
		UserInfo user = loggedUsers.get(channelLeave);
		if(user!=null)
			leaveRoom(user);
		channelLeave.write("Logging out...\n");
		loggedUsers.remove(channelLeave);
		channelLeave.write("Disconnected!\n");
	}
	
	private void users(Channel channelListing) {
		UserInfo user = loggedUsers.get(channelListing);
		if(user!=null) {
			ChatRoom currentRoom = user.getCurrentRoom();
			if(currentRoom!=null) {
				channelListing.write("# Listing user in channel "+currentRoom.getNameRoom()+"\n");
				for(Channel channel : currentRoom.getRoom()) {
					channelListing.write("@" + loggedUsers.get(channel).getUser().getName()+"\n");
				}
			} else {
				channelListing.write("You first need to join a room\n");
			}
		} else {
			channelListing.write("You first need to login\n");
		}
	}
	
	private void write(Channel channelWritting, String message) {
		UserInfo user = loggedUsers.get(channelWritting);
		if(user!=null) {
			ChatRoom currentRoom = user.getCurrentRoom();
			if(currentRoom!=null) {
				for(Channel channel : currentRoom.getRoom()) {
					channel.write("@" + user.getUser().getName() + ": "+message+"\n");
				}
			} else {
				channelWritting.write("You first need to join a room\n");
			}
		} else {
			channelWritting.write("You first need to login\n");
		}
	}
	
	private ChatRoom manageRoom(String nameRoom) {
		ChatRoom chatRoom = chatRooms.get(nameRoom);
		if(chatRoom==null)
			chatRoom = new ChatRoom(nameRoom);
		chatRooms.put(nameRoom, chatRoom);
		return chatRoom;
	}
	
	private void leaveRoom(UserInfo user) {
		if(user!=null) {
			ChatRoom chatRoom = user.getCurrentRoom();
			if(chatRoom!=null) {
				chatRoom.write("[SERVER] @"+ user.getUser().getName() + " has left the room\n");			
				chatRoom.remove(user.getChannelUser());
				if(chatRoom.peopleConnected()<1)
					chatRooms.remove(chatRoom.getNameRoom());
			}
			user.setCurrentRoom(null);	
		}
	}
	
	// User management 
	private void manageUser(String[] commandData, Channel channelLogin) {
		User user = users.get(commandData[1]);
		if(user==null) {
			log("Creating User "+commandData[1]);
			user =  new User(commandData[1], commandData[2]);
			users.put(commandData[1], user);
			loggedUsers.put(channelLogin, new UserInfo(user, channelLogin));
			channelLogin.write("User "+commandData[1]+" logged successfully.\n");		
		} else {
			if(user.validateUser(commandData[1], commandData[2])) {
				log("User "+commandData[1]+" logging in\n");
				loggedUsers.put(channelLogin, new UserInfo(user, channelLogin));
				channelLogin.write("User "+commandData[1]+" logged successfully.\n");
			} else {
				channelLogin.write("Invalid login. Username or password are wrong.\n");		
			}
		}
	}
	
	private void addUserToRoom(UserInfo user, ChatRoom chatRoom) {
		if(chatRoom.peopleConnected()<10) {
			chatRoom.addUserChannel(user.getChannelUser());
			chatRoom.write("[SERVER] @"+ user.getUser().getName() + " has joined the room "+chatRoom.getNameRoom()+"\n");
			moveUserToRoom(loggedUsers.get(user.getChannelUser()), chatRoom);
		}
		else {
			user.getChannelUser().write("Room full (Max 10)\n");				
		}
	}
		
	private void moveUserToRoom(UserInfo user, ChatRoom newRoom) {
		leaveRoom(user);
		newRoom.addUserChannel(user.getChannelUser());
		//Leave from other group necessary
		user.setCurrentRoom(newRoom);		
	}	
	
	private void log(String message) {
		System.out.println(message);
	}
}
