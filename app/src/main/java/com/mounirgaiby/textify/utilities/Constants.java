package com.mounirgaiby.textify.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "USERS";
    public static final String User_UID = "UID";
    public static final String KEY_NAME = "NOM";
    public static final String KEY_EMAIL = "EMAIL";
    public static final String KEY_COLLECTION_PASSWORDS = "PASSWORDS";
    public static final String KEY_PASSWORD = "PASSWORD";
    public static final String KEY_APROPOS = "A PROPOS";
    public static final String KEY_NOMCOMPLET = "NOM COMPLET";
    public static final String KEY_PREFERENCE_NOM = "PreferenceChatApp";
    public static final String KEY_IS_SIGNED_IN = "EstConnecter";
    public static final String KEY_IMAGE = "IMAGE";
    public static final String KEY_FCM_TOKEN = "FCM TOKEN";
    public static final String KEY_USER = "USER";
    public static final String KEY_COLLECTION_CHAT = "CHAT";
    public static final String KEY_SENDER_ID = "SENDER ID";
    public static final String KEY_RECEIVER_ID = "RECEIVER ID";
    public static final String KEY_MESSAGE = "MESSAGE";
    public static final String KEY_TIMESTAMP = "TIMESTAMP";
    public static final String KEY_COLLECTION_CONVERSATIONS = "CONVERSATIONS";
    public static final String KEY_SENDER_NAME = "SenderName";
    public static final String KEY_RECEIVER_NAME = "ReceiverName";
    public static final String KEY_SENDER_IMAGE = "SenderImage";
    public static final String KEY_RECEIVER_IMAGE = "ReceiverImage";
    public static final String KEY_LAST_MESSAGE = "LastMessage";
    public static final String KEY_DISPONIBLE = "AVAILABILITY";
    public static final String KEY_LAST_SEEN = "LAST SEEN";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION = "registration_ids";

    public static HashMap<String,String> remoteMsgHeaders = null;
    public static HashMap<String,String> getRemoteMsgHeaders(){
        if(remoteMsgHeaders == null){
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "Key=AAAArhWu8mE:APA91bFn0F34j4gnGMnGa-DxKzc1Ik4ORXVOKBFR4a7aSkwAJiZ9JL7gK0BFB0gqMnJJYKOL-hMfUQxUiMBzbl24nc3udXyfr-FEC2GNZ2VFOWLMrOtZVzP92QwH9ZkiHT6qfY7yFUOz"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return  remoteMsgHeaders;
    }


}
