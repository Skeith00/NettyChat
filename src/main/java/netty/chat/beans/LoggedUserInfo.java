package netty.chat.beans;

import io.netty.channel.Channel;

public class LoggedUserInfo {
	
	private User user;
	private Channel channelUser;
	private ChatRoom currentRoom;
	
	public LoggedUserInfo(User user, Channel channelUser) {
		this.user = user;
		this.channelUser = channelUser;
		currentRoom = null;
	}
	public User getUser() {
		return user;
	}
	public ChatRoom getCurrentRoom() {
		return currentRoom;
	}
	public void setCurrentRoom(ChatRoom currentRoom) {
		this.currentRoom = currentRoom;
	}
	public Channel getChannelUser() {
		return channelUser;
	}

	public void setChannelUser(Channel channelUser) {
		this.channelUser = channelUser;
	}	
}
