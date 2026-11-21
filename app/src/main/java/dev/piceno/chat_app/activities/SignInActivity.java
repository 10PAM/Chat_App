package dev.piceno.chat_app.activities;

import static android.content.ContentValues.TAG;
import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Executors;

import dev.piceno.chat_app.R;
import dev.piceno.chat_app.databinding.ActivitySignInBinding;
import dev.piceno.chat_app.utilities.Constants;
import dev.piceno.chat_app.utilities.PreferenceManager;
import dev.piceno.chat_app.utilities.SafeLocal;

/**
 * This class manages the sign-up process of users.
 */
public class SignInActivity extends AppCompatActivity {

    // To retrieve binding and the Preference Manager
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    /**
     * Binds the View upon creation.
     *
     * @param savedInstanceState The reference to the Bundle object for state restoration.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(getApplicationContext());
    }

    FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    };

    /**
     * Checks if user is existing or new.
     */
    @Override
    public void onStart() {
        super.onStart();
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            // Check if email exists in Firestore first
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            database.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_EMAIL, currentUser.getEmail())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));

                            SafeLocal.setImage(SafeLocal.getSafetyKey(), documentSnapshot.getString(Constants.KEY_IMAGE));
                            SafeLocal.setUserName(SafeLocal.getSafetyKey(), documentSnapshot.getString(Constants.KEY_NAME));

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(e -> {
                        // Does not have account
                        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    });
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            setContentView(binding.getRoot());
        }
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
     * Creates listeners for clicking sign in and register buttons.
     */
    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(view -> startActivity(
                new Intent(getApplicationContext(), SignUpActivity.class))
        );
        binding.buttonSignIn.setOnClickListener(v -> {
            signIn();
        });
    }

    /**
     * Google Authentication.
     */
    private void firebaseAuthWithGoogle(String IdToken) {
        if (IdToken == null) {
            updateUI(null);
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(IdToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        updateUI(null);
                    }
                });
    }

    /**
     * Register's User with Google if They are New.
     *
     * @param p_user The FirebaseUser.
     */
    private void updateUI(FirebaseUser p_user) {

        if (p_user != null) {

            FirebaseFirestore database = FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_USER_ID, p_user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));

                            SafeLocal.setImage(SafeLocal.getSafetyKey(), documentSnapshot.getString(Constants.KEY_IMAGE));
                            SafeLocal.setUserName(SafeLocal.getSafetyKey(), documentSnapshot.getString(Constants.KEY_NAME));
                        } else {
                            showToast("Unable to Sign In");
                        }
                    });

            loading(false);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            showToast("Successfully Signed In");
            startActivity(intent);
        } else {
            startActivity(new Intent(this, SignUpActivity.class));
            loading(false);
            showToast("Unable to Sign In");
        }
    }

    /**
     * Sign in with Google.
     */
    private void signIn() {
        loading(true);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(getBaseContext().getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(this,
                request,
                null,
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse getCredentialResponse) {
                        Credential credential = getCredentialResponse.getCredential();
                        if (credential instanceof CustomCredential customCredential && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                            Bundle credentialData = customCredential.getData();
                            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);
                            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
                        } else {
                            Log.w(TAG, "Credential is not of type Google ID!");
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        // New User
                        firebaseAuthWithGoogle(null);
                    }
                });
    }

    /**
     * Displays the loading icon over the button, indicating searching database process.
     *
     * @param isLoading Loading indicator boolean.
     */
    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


    /**
     *
     */
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }
}