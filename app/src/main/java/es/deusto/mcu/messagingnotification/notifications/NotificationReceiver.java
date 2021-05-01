package es.deusto.mcu.messagingnotification.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import es.deusto.mcu.messagingnotification.model.ChatMessage;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = NotificationReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, String.format(">>> onReceive(%s, %s)", context, intent));
        ChatMessage cm = AppNotificationsManager.getChatMessageFromIntent(intent);
        if (cm != null) {
            int notificationId =  AppNotificationsManager.getNotificationId(intent);
            switch (intent.getAction()) {
                case AppNotificationsManager.ACTION_MARK_READ:
                    markConversationRead(cm);
                    AppNotificationsManager.cancelMessageReceivedNotification(context, notificationId);
                    break;
                case AppNotificationsManager.ACTION_REPLY:
                    replyConversation(cm, AppNotificationsManager.getTextFromRemoteInput(intent));
                    AppNotificationsManager
                            .updateMessageReceivedNotificationReply(context, notificationId);
                    break;
            }
        }
        Log.d(TAG, String.format("<<< onReceive(%s, %s)", context, intent));
    }

    private void markConversationRead(ChatMessage cm) {
        Log.d(TAG, String.format("Mark Read conversation %d", cm.getConversationId()));
    }

    private void replyConversation(ChatMessage cm, CharSequence replyMessage) {
        Log.d(TAG, String.format("Replied to %s: %s", cm.getSender(), cm.getMsg()));
        Log.d(TAG, String.format("Reply: %s", replyMessage));
    }
}