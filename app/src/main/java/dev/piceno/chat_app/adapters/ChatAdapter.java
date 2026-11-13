package dev.piceno.chat_app.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import dev.piceno.chat_app.databinding.ItemContainerReceivedMessageBinding;
import dev.piceno.chat_app.databinding.ItemContainerSentMessageBinding;
import dev.piceno.chat_app.models.ChatMessage;

/**
 * Handles the Chat message items.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Bitmap receiverProfileImage;
    private final List<ChatMessage> chatMessages;
    private final String sentId;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    /**
     * Manages the ChatAdapter creation.
     *
     * @param p_image The image being applied.
     * @param p_messages The message items being applied.
     * @param p_sentId The sender id.
     */
    public ChatAdapter(List<ChatMessage> p_messages, Bitmap p_image, String p_sentId) {
        this.chatMessages = p_messages;
        this.receiverProfileImage = p_image;
        this.sentId = p_sentId;
    }

    /**
     * Listens for ViewHolder creation.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return Returns a corresponding ViewHolder for sent or received messages.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(ItemContainerSentMessageBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new ReceiverMessageViewHolder(ItemContainerReceivedMessageBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    /**
     * Listens for Binding of ViewHodler.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder)
                    .setData(chatMessages.get(position));
        } else {
            ((ReceiverMessageViewHolder) holder)
                    .setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    /**
     * Gets a total count of all items.
     *
     * @return Returns the total amount of chat messages.
     */
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    /**
     * Get's a message type.
     *
     * @param p_position Gets a message item's position.
     * @return Return's the message item's position.
     */
    @Override
    public int getItemViewType(int p_position) {
        if (chatMessages.get(p_position).senderId.equals(sentId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    /**
     * Handles ViewHolder for user sent messages.
     */
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerSentMessageBinding binding;

        /**
         * Creates ViewHolder.
         *
         * @param p_itemContainerSentMessageBinding The binding being applied.
         */
        public SentMessageViewHolder(ItemContainerSentMessageBinding p_itemContainerSentMessageBinding) {
            super(p_itemContainerSentMessageBinding.getRoot());
            binding = p_itemContainerSentMessageBinding;
        }

        /**
         * Sets all sent chat messages.
         *
         * @param p_message The message being applied.
         */
        void setData(ChatMessage p_message) {
            binding.textMessage.setText(p_message.message);
            binding.textTimeData.setText(p_message.dateTime);
        }
    }

    /**
     * Handles ViewHolder for user received messages.
     */
    static class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerReceivedMessageBinding binding;

        /**
         * Creates ViewHolder.
         *
         * @param p_itemContainerReceivedMessageBinding The binding being applied.
         */
        public ReceiverMessageViewHolder(ItemContainerReceivedMessageBinding p_itemContainerReceivedMessageBinding) {
            super(p_itemContainerReceivedMessageBinding.getRoot());
            binding = p_itemContainerReceivedMessageBinding;
        }

        /**
         * Sets all received chat messages.
         *
         * @param p_message The message being applied.
         */
        void setData(ChatMessage p_message, Bitmap p_receiverProfileImage) {
            binding.textMessage.setText(p_message.message);
            binding.textTimeData.setText(p_message.dateTime);
            binding.profileImage.setImageBitmap(p_receiverProfileImage);
        }
    }
}
