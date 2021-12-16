/*
 * Author: Adin Geist
 * Description: Interface the provides the layout for MessageListeners
 */

public interface MessageListener {
    public void onMessage(String fromLogin, String msgBody);
}
