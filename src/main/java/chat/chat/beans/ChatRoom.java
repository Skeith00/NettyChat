package chat.chat.beans;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;

public class ChatRoom {

	private String nameRoom;
	private ChannelGroup room;
	
	public ChatRoom(String nameRoom) {
		this.nameRoom = nameRoom;
		this.room = new DefaultChannelGroup();
	}	
	public String getNameRoom() {
		return nameRoom;
	}
	public void setNameRoom(String nameRoom) {
		this.nameRoom = nameRoom;
	}
	public ChannelGroup getRoom() {
		return room;
	}
	public void setRoom(ChannelGroup room) {
		this.room = room;
	}	
	public int peopleConnected() {
		return room.size();
	}
	public void addUserChannel(Channel user) {
		room.add(user);
	}
	
	public void write(String message) {
		room.write(message);
	}
}
