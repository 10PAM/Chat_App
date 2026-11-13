package dev.piceno.chat_app.listeners;

import dev.piceno.chat_app.models.User;

/**
 * This class handles User profile interactions.
 */
public interface UserListener {

    /**
     * Handles a User's profile being clicked.
     *
     * @param user The User of the clicked profile.
     */
    void onUserClicked(User user);
}
