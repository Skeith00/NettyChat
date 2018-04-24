package netty.chat.command;

import io.netty.channel.Channel;
import netty.chat.beans.ChatRoom;
import netty.chat.beans.LoggedUserInfo;
import netty.chat.server.ChatSystem;

public class CommandFactory {
	
	@SuppressWarnings("finally")
	public ICommand createCommand(Channel incoming, String message) {
		
		ICommand command = () -> incoming.write("Uknown command\n");

		try { 
			String[] commandData = message.split(" ");
			if(message.startsWith("/login ")) {
				command = () -> {
					if(commandData.length==3) {
						ChatSystem.manageUser(commandData, incoming);
					}
					else {
						incoming.write("Malformed login command\n");
					}
				};				
			} else if(message.startsWith("/join ")) {
				//join(commandData, incoming);
				command = () -> {
					if(commandData.length==2) {
						LoggedUserInfo user = ChatSystem.loggedUsers.get(incoming);
						if(user!=null) {
							ChatRoom chatRoom = ChatSystem.manageRoom(commandData[1]);
							ChatSystem.addUserToRoom(user, chatRoom);
						} else {
							incoming.write("You first need to login\n");
						}
					} else {
						incoming.write("Malformed join command\n");
					}
				};
			} else if(message.equals("/leave")) {
				command = () -> {				
					LoggedUserInfo user = ChatSystem.loggedUsers.get(incoming);
					if(user!=null)
						ChatSystem.leaveRoom(user);
					incoming.write("Logging out...\n");
					ChatSystem.loggedUsers.remove(incoming);
					incoming.write("Disconnected!\n");
				};				
			} else if(message.equals("/users")) {
				//users(incoming);
				command = () -> {
					LoggedUserInfo user = ChatSystem.loggedUsers.get(incoming);
					if(user!=null) {
						ChatRoom currentRoom = user.getCurrentRoom();
						if(currentRoom!=null) {
							incoming.write("# Listing user in channel "+currentRoom.getNameRoom()+"\n");
							for(Channel channel : currentRoom.getRoom()) {
								incoming.write("@" + ChatSystem.loggedUsers.get(channel).getUser().getName()+"\n");
							}
						} else {
							incoming.write("You first need to join a room\n");
						}
					} else {
						incoming.write("You first need to login\n");
					}			
				};
			} else if(message.endsWith("CR")) {
				//write(incoming, message.substring(0, message.length()-2));
				command = () -> {
					LoggedUserInfo user = ChatSystem.loggedUsers.get(incoming);
					if(user!=null) {
						ChatRoom currentRoom = user.getCurrentRoom();
						if(currentRoom!=null) {
							for(Channel channel : currentRoom.getRoom()) {
								channel.write("@" + user.getUser().getName() + ": "+message.substring(0, message.length()-2)+"\n");
							}
						} else {
							incoming.write("You first need to join a room\n");
						}
					} else {
						incoming.write("You first need to login\n");
					}
				};
			}
		} catch (Exception e) {
			command = () -> incoming.write(e.getMessage());
		}
		finally {
			return command;
		}
	}
}
