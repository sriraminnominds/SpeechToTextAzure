package com.sample.microsoft.stt.poc.socket;

/**
 * Created by sgarimella on 27/04/17.
 */

public interface SocketResponseContract {
    /**
     * when socket connected
     */
    void onConnect();

    /**
     * socket failed while trying to connect
     */
    void onSocketFailed();

    /**
     * socket connected, connect to room
     */
    void onLoginWithSocket();

    /**
     * user left from room
     */
    void onUserLeft(String user);

    /**
     * received typing
     */
    void onTyping(String typing);

    /**
     * received message
     */
    void onMessageReceived(String message);

    /**
     * received messages update
     */
    void onMessagesUpdated(String messages);

    /**
     * receive new user connect to room
     */
    void onNewUser(Object... args);

    /**
     * receive socket error
     */
    void onSocketError(int code);
}
