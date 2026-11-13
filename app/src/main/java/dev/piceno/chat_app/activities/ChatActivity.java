package dev.piceno.chat_app.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
    private List<ChatMessage> chatMessagess;
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
        chatMessagess = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessagess,
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

    /**
     * Sets all button listeners.
     */
    private void setListeners() {
        binding.buttonBack.setOnClickListener(view->{
            onBackPressed();
        });

    }
}
