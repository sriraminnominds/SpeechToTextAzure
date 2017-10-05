package com.sample.microsoft.stt.poc.socket;

/**
 * Created by sgarimella on 05/10/17.
 */

public class SocketConstants {
    public static final class Messaging {
        private Messaging() {

        }

        public static final class EmitKeyWord {
            public static final String LOGIN = "login";
            public static final String SEND_MESSAGE = "sendMessage";
            public static final String NEW_USER = "newUser";
            public static final String NEW_MESSAGE = "newMessage";
            public static final String SEND_TYPING = "sendTyping";
            public static final String OPEN_MESSAGE = "openMessage";
            public static final String MESSAGE_UPDATED = "messageUpdated";
            public static final String DELETE_MESSAGE = "deleteMessage";
            public static final String USER_LEFT = "userLeft";
            public static final String SOCKET_ERROR = "socketerror";

            private EmitKeyWord() {
            }
        }
    }
}
