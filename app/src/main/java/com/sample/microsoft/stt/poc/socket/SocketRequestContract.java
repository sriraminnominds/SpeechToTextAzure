package com.sample.microsoft.stt.poc.socket;

import org.json.JSONObject;

/**
 * Created by sgarimella on 27/04/17.
 */

public interface SocketRequestContract {
    void initialise(String url, SocketResponseContract contract);

    void connectToSocket();

    void closeAndDisconnectSocket();

    boolean isSocketConnected();

    void emitMessage(String emitType, JSONObject jsonObject);

}
