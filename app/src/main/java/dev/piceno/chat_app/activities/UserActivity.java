package dev.piceno.chat_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import dev.piceno.chat_app.adapters.UsersAdapter;
import dev.piceno.chat_app.databinding.ActivityUserBinding;
import dev.piceno.chat_app.listeners.UserListener;
import dev.piceno.chat_app.models.User;
import dev.piceno.chat_app.utilities.Constants;
import dev.piceno.chat_app.utilities.PreferenceManager;

public class UserActivity extends AppCompatActivity implements UserListener {

    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;

    /**
     * Sets all View's upon creating for UserActivity.
     *
     * @param savedInstanceState The instance state of the View.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        getUsers();
    }

    /**
     * Sets up all button listeners.
     */
    private void setListeners() {
        binding.buttonBack.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    /**
     * Gets users from Firebase excluding current user. Saves into List, and appends to Adapter.
     */
    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).get()
                .addOnCompleteListener(task ->{
                   loading(false);
                   String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                   if (task.isSuccessful() && task.getResult() != null) {
                       List<User> users = new ArrayList<>();
                       for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                           if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                               continue;
                           }
                           User user = new User();
                           user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                           user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                           user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                           user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                           user.id = queryDocumentSnapshot.getId();
                           users.add(user);
                       }
                       if (!users.isEmpty()) {
                           UsersAdapter usersAdapter = new UsersAdapter(users, this);
                           binding.usersRecyclerView.setAdapter(usersAdapter);
                           binding.usersRecyclerView.setVisibility(View.VISIBLE);
                       } else {
                           showErrorMessage("Error retrieving other users.");
                       }
                   } else {
                       showErrorMessage("Error retrieving other users...");
                   }
                });
    }

    /**
     * Displays a textual error message with a specified string. Used for visual feedback on the
     * user end in the UsersActivity XML.
     *
     * @param message The message being displayed to the user.
     */
    private void showErrorMessage(String message) {
        binding.textErrorMessages.setText(message);
        binding.textErrorMessages.setVisibility(View.VISIBLE);
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
     * Display progress bar.
     *
     * @param isLoading Loading status.
     */
    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressionBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressionBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Handles User profile click.
     *
     * @param user The User of the clicked profile.
     */
    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}