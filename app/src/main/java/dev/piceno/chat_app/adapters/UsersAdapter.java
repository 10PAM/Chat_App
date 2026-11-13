package dev.piceno.chat_app.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import dev.piceno.chat_app.databinding.ItemContainerUserBinding;
import dev.piceno.chat_app.models.User;

/**
 * This provides the Adapter functionality for User objects.
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> users;

    /**
     * Constructs a Users Adapter with predefined users list.
     *
     * @param p_users The User object's list.
     */
    public UsersAdapter(List<User> p_users) {
        this.users = p_users;
    }

    /**
     * Creates a UserViewHolder to contain all added User views.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return Returns a UserViewHolder object containing the User binding.
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(itemContainerUserBinding);
    }

    /**
     * Listens for BindViewHolder call and binds the View.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    /**
     * Returns a total amount of items in the ViewHolder.
     *
     * @return Returns total amount of User items in UserViewHolder.
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * This class provides the functionality for creating a ViewHolder for User objects.
     */
    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemContainerUserBinding binding;

        /**
         * Constructs a ViewHolder.
         *
         * @param itemContainerUserBinding The binding view containing the items.
         */
        public UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        /**
         * Applies visual user data.
         *
         * @param user The user item.
         */
        public void setUserData(User user) {
            binding.userName.setText(user.name);
            binding.userEmail.setText(user.email);
            binding.profileImage.setImageBitmap(getUserImage(user.image));
        }
    }

    /**
     * Gets a User's encoded image data, decodes, and returns a Bitmap of the image.
     *
     * @param encodedImage The data of the Base64 encoded image.
     * @return Returns a Bitmap image using decoded image data.
     */
    private Bitmap getUserImage(String encodedImage) {
        byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
