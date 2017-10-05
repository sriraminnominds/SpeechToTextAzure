package com.sample.microsoft.stt.poc.socket;

/**
 * Created by sgarimella on 05/10/17.
 */

public class SocketConstants {
    public static final class Messaging {
        private Messaging() {

        }

        public static final class EmitKeyWord {
            public static final String START_MEETING = "ackstartmeeting";
            public static final String END_MEETING = "ackendmeeting";
            public static final String NEW_USER = "newUser";
            public static final String NEW_MESSAGE = "newMessage";
            public static final String USER_LEFT = "userLeft";
            public static final String SOCKET_ERROR = "socketerror";

            private EmitKeyWord() {
            }
        }
    }
}
