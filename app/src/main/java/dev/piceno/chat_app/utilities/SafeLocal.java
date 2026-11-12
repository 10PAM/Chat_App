package dev.piceno.chat_app.utilities;

/**
 * This class contains all methods for saving and retrieving local data.
 */
public class SafeLocal {
    private static String m_profileImage = "";
    private final static String m_safetyKey = "123";
    private static String m_userName = "";

    /**
     * Gets the user image.
     * @return Return user image dat.
     */
    public static String getImage() {
        return m_profileImage;
    }

    /**
     * Sets the local image data from given data.
     *
     * @param key The safety key for manipulating data.
     * @param imageString The image data.
     */
    public static void setImage(String key, String imageString) {
        if (key.equals(getSafetyKey())) {
            m_profileImage = imageString;
        }
    }


    /**
     * Sets the local username data from given data.
     *
     * @param key The safety key for manipulating data.
     * @param userName The username data.
     */
    public static void setUserName(String key, String userName) {
        if (key.equals(getSafetyKey())) {
            m_userName = userName;
        }
    }

    /**
     * Gets the data manipulating key.
     *
     * @return Return safety key for editing data.
     */
    public static String getSafetyKey() {
        return m_safetyKey;
    }

    /**
     * Gets the username data.
     *
     * @return Return usename data.
     */
    public static String getUserName() {
        return m_userName;
    }
}
