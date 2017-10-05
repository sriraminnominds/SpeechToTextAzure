package com.sample.microsoft.stt.poc.socket;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Created by sgarimella on 27/04/17.
 */

public class SocketHelper implements SocketRequestContract {

    private Socket mSocket;
    private SocketResponseContract mContract;
    private String mSocketUrl;

    @Override
    public void initialise(String url, SocketResponseContract contract) {
        this.mSocketUrl = url;
        this.mContract = contract;
        connectToSocket();
    }

    @Override
    public void connectToSocket() {
        if (mSocket != null) {
            mSocket.close();
            mSocket.disconnect();
            mSocket = null;
        }
        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            mSocket = IO.socket(mSocketUrl, opts);
            mSocket.connect();

            if (mContract != null) {
                mContract.onLoginWithSocket();
            }

            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (mContract != null) {
                        mContract.onConnect();
                    }
                }
            });

            mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    socketFailedDialog();
                }
            });

            mSocket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    socketFailedDialog();
                }
            });

            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    socketFailedDialog();
                }
            });

            mSocket.on(SocketConstants.Messaging.EmitKeyWord.NEW_USER, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (mContract != null) {
                        mContract.onNewUser(args);
                    }
                }
            });

            mSocket.on(SocketConstants.Messaging.EmitKeyWord.USER_LEFT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String userLeft = args[0].toString();
                    if (mContract != null) {
                        mContract.onUserLeft(userLeft);
                    }
                }
            });

            mSocket.on(SocketConstants.Messaging.EmitKeyWord.SEND_TYPING, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String sendTyping = args[0].toString();
                    if (mContract != null) {
                        mContract.onTyping(sendTyping);
                    }
                }
            });

            mSocket.on(SocketConstants.Messaging.EmitKeyWord.NEW_MESSAGE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String newMessage = args[0].toString();
                    if (mContract != null) {
                        mContract.onMessageReceived(newMessage);
                    }
                }
            });

            mSocket.on(SocketConstants.Messaging.EmitKeyWord.MESSAGE_UPDATED, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String newMessage = args[0].toString();
                    if (mContract != null) {
                        mContract.onMessagesUpdated(newMessage);
                    }
                }
            });

            mSocket.on(SocketConstants.Messaging.EmitKeyWord.SOCKET_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String response = args[0].toString();
                    //ViridisLog.chatVerbose("Response ::: " + response);
                }
            });
        } catch (URISyntaxException e) {
            //throw new ViridisException("Error parsing Socket", e);
        }
    }

    protected void socketFailedDialog() {
        if (mContract != null) {
            mContract.onSocketFailed();
        }
    }

    /**
     * emit message to socket
     *
     * @param emitType   type of emit message
     * @param jsonObject data for send to server
     */
    public void emitMessage(String emitType, JSONObject jsonObject) {
        if (mSocket != null) {
            mSocket.emit(emitType, jsonObject);
        }
    }

    /**
     * close socket and disconnect to socket and set socket and listener to null
     */
    @Override
    public void closeAndDisconnectSocket() {
        if (mSocket != null) {
            mSocket.close();
            mSocket.disconnect();
            mSocket = null;
            mContract = null;
        }
    }

    /**
     * check if socket is connected
     */
    @Override
    public boolean isSocketConnected() {
        if (mSocket == null) {
            return false;
        }
        return mSocket.connected();
    }
}
