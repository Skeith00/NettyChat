package netty.chat.server;

import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import netty.chat.beans.ChatRoom;
import netty.chat.beans.LoggedUserInfo;
import netty.chat.beans.User;

public class ChatSystem {
	//private static final ChannelGroup channelsWaitingRoom = new DefaultChannelGroup();	
	public static final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();
	public static final ConcurrentHashMap<String, ChatRoom> chatRooms = new ConcurrentHashMap<String, ChatRoom>();
	public static final ConcurrentHashMap<Channel, LoggedUserInfo> loggedUsers = new ConcurrentHashMap<Channel, LoggedUserInfo>();
	
	public static ChatRoom manageRoom(String nameRoom) {
		ChatRoom chatRoom = ChatSystem.chatRooms.get(nameRoom);
		if(chatRoom==null)
			chatRoom = new ChatRoom(nameRoom);
		ChatSystem.chatRooms.put(nameRoom, chatRoom);
		return chatRoom;
	}
	
	public static void leaveRoom(LoggedUserInfo user) {
		if(user!=null) {
			ChatRoom chatRoom = user.getCurrentRoom();
			if(chatRoom!=null) {
				chatRoom.write("[SERVER] @"+ user.getUser().getName() + " has left the room\n");			
				chatRoom.remove(user.getChannelUser());
				if(chatRoom.peopleConnected()<1)
					ChatSystem.chatRooms.remove(chatRoom.getNameRoom());
			}
			user.setCurrentRoom(null);	
		}
	}
	
	// User management 
	public static void manageUser(String[] commandData, Channel channelLogin) {
		User user = ChatSystem.users.get(commandData[1]);
		if(user==null) {
			log("Creating User "+commandData[1]);
			user =  new User(commandData[1], commandData[2]);
			ChatSystem.users.put(commandData[1], user);
			ChatSystem.loggedUsers.put(channelLogin, new LoggedUserInfo(user, channelLogin));
			channelLogin.write("User "+commandData[1]+" logged successfully.\n");		
		} else {
			if(user.validateUser(commandData[1], commandData[2])) {
				log("User "+commandData[1]+" logging in\n");
				ChatSystem.loggedUsers.put(channelLogin, new LoggedUserInfo(user, channelLogin));
				channelLogin.write("User "+commandData[1]+" logged successfully.\n");
			} else {
				channelLogin.write("Invalid login. Username or password are wrong.\n");		
			}
		}
	}
	
	public static void addUserToRoom(LoggedUserInfo user, ChatRoom chatRoom) {
		if(chatRoom.peopleConnected()<10) {
			chatRoom.addUserChannel(user.getChannelUser());
			chatRoom.write("[SERVER] @"+ user.getUser().getName() + " has joined the room "+chatRoom.getNameRoom()+"\n");
			moveUserToRoom(ChatSystem.loggedUsers.get(user.getChannelUser()), chatRoom);
		}
		else {
			user.getChannelUser().write("Room full (Max 10)\n");				
		}
	}
		
	private static void moveUserToRoom(LoggedUserInfo user, ChatRoom newRoom) {
		leaveRoom(user);
		newRoom.addUserChannel(user.getChannelUser());
		//Leave from other group necessary
		user.setCurrentRoom(newRoom);
	}	
	
	private static void log(String message) {
		System.out.println(message);
	}
}
