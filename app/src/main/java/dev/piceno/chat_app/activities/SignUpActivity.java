package dev.piceno.chat_app.activities;

import static android.content.ContentValues.TAG;
import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Executors;

import dev.piceno.chat_app.R;
import dev.piceno.chat_app.databinding.ActivitySignInBinding;
import dev.piceno.chat_app.databinding.ActivitySignUpBinding;
import dev.piceno.chat_app.utilities.Constants;
import dev.piceno.chat_app.utilities.PreferenceManager;
import dev.piceno.chat_app.utilities.SafeLocal;

/**
 * This class handles the sign-up process.
 */
public class SignUpActivity extends AppCompatActivity {

    // To retrieve binding and the Preference Manager
    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodeImage;
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
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        credentialManager = CredentialManager.create(getApplicationContext());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Checks if user is existing or new.
     */
    @Override
    public void onStart() {
        super.onStart();
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
                        setContentView(binding.getRoot());
                    });
            //startActivity(new Intent(getApplicationContext(), MainActivity.class));
        } else {
            setContentView(binding.getRoot());
        }
    }

    /**
     * Creates listeners for clicking sign up, add image, and sign in buttons.
     */
    private void setListeners() {
        binding.textSignIn.setOnClickListener(view -> onBackPressed());
        binding.buttonSignUp.setOnClickListener(view -> {
            if (validSignUp()) {
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
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
     * Recreates Firebase User's Profile Picture to Bitmap if Available.
     *
     * @param p_user The Firebase User.
     * @return Returns Bitmap if user has profile picture; Null otherwise.
     */
    private Bitmap bitmapFirebaseUser(FirebaseUser p_user) {
        Bitmap bitmap = null;
        if (p_user.getPhotoUrl() == null) {
            return bitmap;
        }
        try (InputStream imageData = new URL(p_user.getPhotoUrl().toString()).openStream()) {
            bitmap = BitmapFactory.decodeStream(imageData);
        }
        catch (Exception exception) {
            Log.e(exception.toString(), "Image Retrieval Issue");
        }
        return bitmap;
    }

    /**
     * Register's User with Google if They are New.
     *
     * @param p_user The FirebaseUser.
     */
    private void updateUI(FirebaseUser p_user) {

        if (p_user != null) {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            showToast("Successfully Signed In");
            startActivity(intent);
        } else {
            showToast("Unable to Sign Up");
        }
        loading(false);
    }

    /**
     * Google Authentication.
     */
    private void firebaseAuthWithGoogle(String IdToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(IdToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = mAuth.getCurrentUser();

                        FirebaseFirestore database = FirebaseFirestore.getInstance();
                        HashMap<String, String> user = new HashMap<>();
                        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
                        user.put(Constants.KEY_EMAIL, mAuth.getCurrentUser().getEmail());
                        user.put(Constants.KEY_IMAGE, encodeImage);

                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .add(user)
                                .addOnSuccessListener(documentReference -> {
                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                                    preferenceManager.putString(Constants.KEY_IMAGE, encodeImage);
                                }).addOnFailureListener(exception -> {
                                    showToast(exception.getMessage());
                                });

                        updateUI(currentUser);
                    } else {
                        updateUI(null);
                    }
                });
    }

    /**
     * Sign up with Google.
     */
    private void signUp() {
        loading(true);

        /*

        OAuthProvider provider = OAuthProvider.newBuilder("google.com")
                .addCustomParameter("prompt", "select_amount")
                .build();

        Task<AuthResult> authTask = mAuth.getPendingAuthResult();

        if (authTask != null) {
            authTask.addOnSuccessListener(authResult -> {
                FirebaseUser user = authResult.getUser();
            }).addOnFailureListener(e -> {
                Log.e("Authentication Issue", "Sign in Failed");
            });
        } else {
            authTask.addOnSuccessListener(authResult -> {
                FirebaseUser user = authResult.getUser();
            }).addOnFailureListener(e -> {
                Log.e("Authentication Issue", "Failed Sign In");
            });
        }*/

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
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
                            Log.w(TAG, "Credential is type Google ID!");
                        } else {
                            Log.w(TAG, "Credential is not of type Google ID!");
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {

                        Log.w(TAG, "Credential ERR!" + e.getMessage());
                    }
                });
    }

    /**
     * Encodes bitmap to JPEG for retrieval and use.
     *
     * @param bitmap The bitmap being converted (compressed) to JPEG.
     * @return Returns the compressed encoded bitmap image.
     */
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 15, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri imageUrl = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUrl);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.imageProfile.setImageBitmap(bitmap);
                        binding.textAddImage.setVisibility(View.GONE);
                        encodeImage = encodeImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    /**
     * Determines if the Signup credentials are properly formatted prior to searching database
     *
     * @return Returns true if valid-credentials; false otherwise.
     */
    private Boolean validSignUp() {
        if (encodeImage == null) {
            showToast("Please select an image");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Please Enter your Name");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Displays the loading icon over the button, indicating searching database process.
     *
     * @param isLoading Loading indicator boolean.
     */
    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}