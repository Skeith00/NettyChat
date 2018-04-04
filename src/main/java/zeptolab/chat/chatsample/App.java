package zeptolab.chat.chatsample;

/* The task is to implement simple text based IRC server. If you are using Java then Netty framework of any version is preferable. Akka or another framework is allowed as well.

Better to have just a few classes, with no persistence (in memory only).

Please pay extra attention on concurrency and thread safety.

Command set for this server:

/login name password — if user does not exist create profile else login.

/join chat_room — try to join chat room (max 10 active clients per chat room). If chat room does not exist - create it first. If client’s limit exceeded - send an error, otherwise join chat room and send last N messages of activity. Server should support many chat rooms.

/leave - disconnect client.

/users — show users in the channel.

text message terminated with CR - sends message to current channel. Server must send new message to all connected to this channel clients.

We should be able to check this server via simple text based telnet command.
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
