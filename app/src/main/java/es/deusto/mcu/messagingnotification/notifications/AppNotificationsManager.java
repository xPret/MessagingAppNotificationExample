package es.deusto.mcu.messagingnotification.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;

import es.deusto.mcu.messagingnotification.MainActivity;
import es.deusto.mcu.messagingnotification.model.ChatMessage;

public class AppNotificationsManager {

    private static final String MESSAGES_CHANNEL_ID = "MessagesChannel";
    private static final String MESSAGES_CHANNEL_NAME = "Messages";
    private static final String MESSAGES_CHANNEL_DESC = "This is the Message Notifications channel";


    /**
     * General method to create the defined Channels
     * @param context used to create channels
     */
    public static void createChannels(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createMessagesChannel(context);
            // create more channels
        }
    }

    /**
     * It creates a Message Notification Channel
     * @param context used to create the channel
     */
    private static void createMessagesChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    MESSAGES_CHANNEL_ID,
                    MESSAGES_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(MESSAGES_CHANNEL_DESC);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.GREEN);
            mChannel.enableVibration(true);
            NotificationManagerCompat.from(context).createNotificationChannel(mChannel);
        }
    }

    /**
     * It shows a Notification using Remote Input and MessagingStyle
     * @param context used to manage the notifications
     * @param chatMessage containing a mock
     * @param messageCount the number of messages to draw in the details
     */
    public static void showMessageReceivedNotification(Context context,
                                                       ChatMessage chatMessage, int messageCount) {
        // Create Actions
        NotificationCompat.Action actionReplyChat = ActionBuilder.buildActionReplyChat(
                context,
                android.R.drawable.ic_dialog_email,
                "Quick Reply",
                chatMessage,
                "Type the message"
        );

        NotificationCompat.Action actionMarkAsRead = ActionBuilder.buildActionMarkAsRead(
                context,
                android.R.drawable.ic_menu_save,
                "Mark as Read",
                chatMessage
        );

        Person sender = new Person.Builder()
                .setName(chatMessage.getSender())
                .build();

        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(sender);
        messagingStyle.addMessage(new NotificationCompat.MessagingStyle.Message(
                (CharSequence) chatMessage.getMsg(),
                System.currentTimeMillis(),
                sender));

        // Build and send Notification
        NotificationManagerCompat.from(context.getApplicationContext()).notify(
                chatMessage.getConversationId(),
                new NotificationCompat.Builder(context, MESSAGES_CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle(String.format("New message from %s:", chatMessage.getSender()))
                        .setContentText(chatMessage.getMsg())
                        .setAutoCancel(true)
                        .setNumber(messageCount)
                        .setStyle(messagingStyle)
                        .setContentIntent(
                                ActionBuilder.buildPendingIntentForActionOpenChat(context, chatMessage))
                        .addAction(actionReplyChat)
                        .addAction(actionMarkAsRead)
                        .build()
                );
    }

    /**
     * It removes the notification
     * @param context used to manage the notifications
     * @param notificationId of the notification to remove
     */
    public static void cancelMessageReceivedNotification(Context context, int notificationId) {
        NotificationManagerCompat.from(context.getApplicationContext()).cancel(notificationId);
    }

    /**
     * It updates a message notification with the "sent" content
     * @param context used to manage the notifications
     * @param notificationId of the replied notification
     */
    public static void updateMessageReceivedNotificationReply(Context context, int notificationId) {
        NotificationManagerCompat.from(context.getApplicationContext()).notify(
                notificationId,
                new NotificationCompat.Builder(context, MESSAGES_CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("Sent")
                        .setAutoCancel(true)
                        .setTimeoutAfter(3000)
                        .build()
                );
    }

    /**
     * Abstract class to set actions to Notifications
     */
    private static class ActionBuilder {

        private static PendingIntent buildPendingIntentForActionMarkAsRead(Context context,
                                                                           ChatMessage chatMessage) {
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction(ACTION_MARK_READ);
            setNotificationId(intent, chatMessage);
            addChatMessageToIntent(intent, chatMessage);

            return PendingIntent.getBroadcast(
                    context,
                    getNotificationId(intent),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        private static PendingIntent buildPendingIntentForActionOpenChat(Context context,
                                                                         ChatMessage chatMessage) {

            Intent intent = new Intent(context, MainActivity.class);
            intent.setAction(ACTION_OPEN_CONVERSATION);
            setNotificationId(intent, chatMessage);
            addChatMessageToIntent(intent, chatMessage);

            return PendingIntent.getActivity(
                    context,
                    getNotificationId(intent),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        private static PendingIntent buildPendingIntentForActionReplyChat(Context context,
                                                                          ChatMessage chatMessage) {
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction(ACTION_REPLY);

            setNotificationId(intent, chatMessage);
            addChatMessageToIntent(intent, chatMessage);

            return PendingIntent.getBroadcast(
                    context.getApplicationContext(),
                    getNotificationId(intent),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        public static NotificationCompat.Action buildActionReplyChat(Context context,
                                                                      int icon,
                                                                      String title,
                                                                      ChatMessage chat,
                                                                      String textHint) {
            return new NotificationCompat.Action.Builder(icon, title,
                    buildPendingIntentForActionReplyChat(context, chat))
                    .addRemoteInput(createRemoteInputForNotification(textHint))
                    .build();
        }

        public static NotificationCompat.Action buildActionMarkAsRead(Context context,
                                                                      int icon,
                                                                      String title,
                                                                      ChatMessage chatMessage) {
            return new NotificationCompat.Action.Builder(icon, title,
                    buildPendingIntentForActionMarkAsRead(context, chatMessage))
                    .build();
        }

    }


    /** Key to store and get the result of a remote input */
    private static final String REM_INPUT_RESULT_KEY = "REM_INPUT_RESULT_KEY";

    /**
     * It creates the remote input to get the a text reply from the notification
     * @param textHint is the hint shown inside the input
     * @return the object to be attached to the notification
     */
    private static RemoteInput createRemoteInputForNotification(String textHint) {
        return new RemoteInput.Builder(REM_INPUT_RESULT_KEY)
                .setLabel(textHint)
                .build();
    }

    /**
     * It gets the typed text from an Intent received from a Notification with RemoteInput
     * @param intent which contains the remote input
     * @return the typed text in CharSequence format
     */
    public static CharSequence getTextFromRemoteInput(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(REM_INPUT_RESULT_KEY);
        }
        return null;
    }


    private static final String EXTRA_KEY_NOTIFICATION_ID = "E.NOTIF_ID";

    private static final String EXTRA_KEY_CONVERSATION_ID = "E.CONVERSATION_ID";
    private static final String EXTRA_KEY_TITLE = "E.TITLE";
    private static final String EXTRA_KEY_SENDER = "E.SENDER";
    private static final String EXTRA_KEY_MSG = "E.MSG";

    public static final String ACTION_REPLY = "A.REPLY";
    public static final String ACTION_MARK_READ = "A.MARK.READ";
    public static final String ACTION_OPEN_CONVERSATION = "A.OPEN.CONV";


    public static int getNotificationId(Intent i) {
        return i.getIntExtra(EXTRA_KEY_NOTIFICATION_ID, -1);
    }

    private static void setNotificationId(Intent i, ChatMessage cm) {
        i.putExtra(EXTRA_KEY_NOTIFICATION_ID, cm.getConversationId());
    }

    /**
     * It adds a ChatMessage to an Intent content
     *
     * @param i is the intent which will contain the ChatMessage
     * @param cm the ChatMessage object to add
     */
    public static void addChatMessageToIntent(Intent i, ChatMessage cm) {
        i.putExtra(EXTRA_KEY_CONVERSATION_ID, cm.getConversationId());
        i.putExtra(EXTRA_KEY_TITLE, cm.getTitle());
        i.putExtra(EXTRA_KEY_SENDER, cm.getSender());
        i.putExtra(EXTRA_KEY_MSG, cm.getMsg());
    }


    /**
     * It gets a ChatMessage contained in an Intent
     *
     * @param intent which contains a ChatMessage
     * @return the ChatMessage object
     */
    public static ChatMessage getChatMessageFromIntent(Intent intent) {
        Bundle b = intent.getExtras();
        if (b != null && b.getInt(EXTRA_KEY_CONVERSATION_ID, -1) != -1) {
            return new ChatMessage(
                    b.getInt(EXTRA_KEY_CONVERSATION_ID),
                    b.getString(EXTRA_KEY_TITLE),
                    b.getString(EXTRA_KEY_SENDER),
                    b.getString(EXTRA_KEY_MSG)
            );
        }
        return null;
    }
}