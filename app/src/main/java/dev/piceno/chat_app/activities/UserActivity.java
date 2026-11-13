package dev.piceno.chat_app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import dev.piceno.chat_app.R;
import dev.piceno.chat_app.databinding.ActivityUserBinding;
import dev.piceno.chat_app.utilities.PreferenceManager;

public class UserActivity extends AppCompatActivity {

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
     * Gets users.
     */
    private void getUsers() {

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
}