package es.deusto.mcu.messagingnotification;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import es.deusto.mcu.messagingnotification.model.ChatMessage;
import es.deusto.mcu.messagingnotification.notifications.AppNotificationsManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private final int mConversationId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppNotificationsManager.createChannels(this);
        findViewById(R.id.bSendMessageNotification).setOnClickListener(v -> showMessageNotification());
        findViewById(R.id.bCancelMessageNotification).setOnClickListener(v -> cancelMessageNotification());
        processIntent();
    }

    private void showMessageNotification() {
        ChatMessage cm = new ChatMessage(mConversationId, "Chat Title","Android User",
                "Hello, how are you?");
        AppNotificationsManager.showMessageReceivedNotification(this, cm, 3);
    }
    private void cancelMessageNotification() {
        AppNotificationsManager.cancelMessageReceivedNotification(this, mConversationId);
    }

    private void processIntent() {
        Intent intent = getIntent();
        ChatMessage cm = AppNotificationsManager.getChatMessageFromIntent(intent);
        if (cm != null) {
            if (AppNotificationsManager.ACTION_OPEN_CONVERSATION.equals(intent.getAction())) {
                Log.d(TAG, String.format("Open Chat Conversation %d [%s said: %s]",
                        cm.getConversationId(), cm.getSender(), cm.getMsg()));
            }
        }
    }
}