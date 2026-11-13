package dev.piceno.chat_app.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import dev.piceno.chat_app.adapters.ChatAdapter;
import dev.piceno.chat_app.databinding.ActivityChatBinding;
import dev.piceno.chat_app.models.ChatMessage;
import dev.piceno.chat_app.models.User;
import dev.piceno.chat_app.utilities.Constants;
import dev.piceno.chat_app.utilities.PreferenceManager;

/**
 * This class handles all user-to-user chat activity.
 */
public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    /**
     * Handles creation of Activity.
     *
     * @param savedInstanceState The instance state of the Activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        setListeners();
        initialize();
        ListenForMessages();
    }

    /**
     * Gets receiver information.
     */
    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.userName.setText(receiverUser.name);
    }

    /**
     * Decodes a Base64 encoded image and turns into Bitmap.
     *
     * @param encodedImage The encoded image data.
     * @return Returns a Bitmap created from the decoded image.
     */
    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    /**
     * Initializes all chat messages.
     */
    private void initialize() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecycler.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    /**
     * Handles message sending.
     */
    private void sendMessages() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        binding.inputMessage.setText(null);
    }

    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument()
                            .getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument()
                            .getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument()
                            .getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDataTime(documentChange.getDocument()
                            .getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.date_object = documentChange.getDocument()
                            .getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.date_object.compareTo(obj2.date_object));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeChanged(chatMessages.size(), chatMessages.size());
                binding.chatRecycler.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecycler.setVisibility(View.VISIBLE);
        }
        binding.progressionBar.setVisibility(View.GONE);
    });

    /**
     * Listens for any incoming User messages.
     */
    private void ListenForMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);

        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
    }

    /**
     * Sets all button listeners.
     */
    private void setListeners() {
        binding.buttonBack.setOnClickListener(view->{
            onBackPressed();
        });
        binding.buttonSend.setOnClickListener(view -> {
            sendMessages();
        });
    }

    /**
     * Formats message Time and Data in format: Month day, Year - Hour, Minute
     *
     * @param p_date The Date object.
     * @return Returns the formatted date.
     */
    private String getReadableDataTime(Date p_date) {
        return new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                .format(p_date);
    }
}
