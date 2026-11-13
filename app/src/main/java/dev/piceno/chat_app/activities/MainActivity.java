package dev.piceno.chat_app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import dev.piceno.chat_app.databinding.ActivityMainBinding;
import dev.piceno.chat_app.utilities.Constants;
import dev.piceno.chat_app.utilities.PreferenceManager;

/**
 * This class contains all the methods for the non sign-in and sign-out in-app functionality.
 */
public class MainActivity extends AppCompatActivity {

    // To retrieve binding and the Preference Manager
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    /**
     * Binds the View upon creation.
     *
     * @param savedInstanceState The reference to the Bundle object for state restoration.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        loadUserDetails();
        setListeners();
        getToken();
    }

    /**
     * Signs a user out.
     */
    private void setListeners() {
        binding.signOut.setOnClickListener(view -> signOut());
        binding.buttonAdd.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), UserActivity.class));
        });
    }

    /**
     * Loads and displays local user data into view.
     */
    private void loadUserDetails() {
        binding.userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        ImageView image = binding.profileImage;
        RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(getResources(), decodeImage(preferenceManager.getString(Constants.KEY_IMAGE)));
        d.setCircular(true);
        image.setImageDrawable(d);
    }

    /**
     * Displays a Toast message with a specified string. Used for visual feedback on the user end.
     *
     * @param message The message being displayed to the user.
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Decodes Base64 encoded Bitmap image.
     *
     * @param imageData The encoded image data.
     * @return Returns the decoded image data.
     */
    private Bitmap decodeImage(String imageData) {
        byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    /**
     * Retrieves token.
     */
    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    /**
     * Updates database with provided token.
     *
     * @param token The token being amended to the Firestore.
     */
    private void updateToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused -> showToast("Token update successful"))
                .addOnFailureListener(e -> showToast("Unable to update Token"));
    }

    /**
     * Updates the firebase to sign user out.
     */
    private void signOut() {
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                }).addOnFailureListener(e -> showToast("Unable to sign out!"));
    }
}