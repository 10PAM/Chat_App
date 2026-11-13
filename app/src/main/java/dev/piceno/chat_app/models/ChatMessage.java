package dev.piceno.chat_app.models;

import java.util.Date;

/**
 * This class handles messaging functionality between two users.
 */
public class ChatMessage {
    public String senderId, receiverId, message, dateTime;

    public Date date_object;
}
