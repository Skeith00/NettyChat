package chat.chat.beans;

public class UserInfo {
	
	private User user;
	private ChatRoom currentRoom;
	
	public UserInfo(User user) {
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
}
