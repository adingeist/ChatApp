/*
 * Author: Adin Geist
 * Description: Interface the provides the layout for UserStatusListeners, which should have online and offline
 *              method implementation.
 */


public interface UserStatusListener {
    public void online(String login);
    public void offline(String login);
}
