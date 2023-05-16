package com.sendbird.uikit.model;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.message.Emoji;
import com.sendbird.android.message.EmojiCategory;
import com.sendbird.android.message.EmojiContainer;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.utils.UIKitPrefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Manager providing emoji information from Sendbird server.
 *
 * since 1.1.0
 */
public final class EmojiManager {
    private EmojiManager() {}

    private static class EmojiManagerHolder {
        static final EmojiManager INSTANCE = new EmojiManager();
    }
    /**
     * Returns the {@link EmojiManager} working as a singleton.
     *
     * @return The {@link EmojiManager}
     * since 1.1.0
     */
    @NonNull
    public static EmojiManager getInstance() {
        return EmojiManagerHolder.INSTANCE;
    }

    @Nullable
    private String emojiHash = null;
    @NonNull
    private final Object emojiLock = new Object();
    @NonNull
    private LinkedHashMap<Long, EmojiCategory> emojiCategoryMap = new LinkedHashMap<>();
    @NonNull
    private LinkedHashMap<String, Emoji> allEmojiMap = new LinkedHashMap<>();

    public void init() {
        String emojiContainerStr = UIKitPrefs.getString(StringSet.KEY_EMOJI_CONTAINER);
        if (!TextUtils.isEmpty(emojiContainerStr)) {
            EmojiContainer container = decodeEmojiContainer(emojiContainerStr);
            if (container != null) {
                upsertEmojiContainer(container, false);
            }
        }
    }

    public void upsertEmojiContainer(@NonNull EmojiContainer emojiContainer) {
        upsertEmojiContainer(emojiContainer, true);
    }

    private void upsertEmojiContainer(@NonNull EmojiContainer emojiContainer, boolean saveToFile) {
        emojiHash = emojiContainer.getEmojiHash();
        synchronized (emojiLock) {
            emojiCategoryMap = new LinkedHashMap<>();
            allEmojiMap = new LinkedHashMap<>();
            for (EmojiCategory emojiCategory : emojiContainer.getEmojiCategories()) {
                emojiCategoryMap.put(emojiCategory.getId(), emojiCategory);
                for (Emoji emoji : emojiCategory.getEmojis()) {
                    allEmojiMap.put(emoji.getKey(), emoji);
                }
            }
        }

        if (saveToFile) {
            final String emojiContainerSerialized = EmojiManager.getInstance().encodeEmojiContainer(emojiContainer);
            UIKitPrefs.putString(StringSet.KEY_EMOJI_CONTAINER, emojiContainerSerialized);
        }
    }

    /**
     * Returns the emoji hash
     *
     * @return The emoji hash
     * since 1.1.0
     */
    @Nullable
    public String getEmojiHash() {
        return emojiHash;
    }

    /**
     * Returns the emoji url corresponding to emoji key
     *
     * @param key emoji key
     * @return The emoji url corresponding to emoji key
     * since 1.1.0
     */
    @Nullable
    public String getEmojiUrl(final @NonNull String key) {
        synchronized (emojiLock) {
            Emoji emoji = allEmojiMap.get(key);
            if (emoji != null) {
                return emoji.getUrl();
            }
        }
        return null;
    }

    /**
     * Returns the {@link List<EmojiCategory>} registering Sendbird server
     *
     * @return The {@link List<EmojiCategory>} Emoji category list
     * since 1.1.0
     */
    @NonNull
    public List<EmojiCategory> getAllEmojiCategories() {
        List<EmojiCategory> emojiCategoryList = new ArrayList<>(emojiCategoryMap.values());
        return Collections.unmodifiableList(emojiCategoryList);
    }

    /**
     * Returns the {@link List<Emoji>} registering Sendbird server
     *
     * @return The {@link List<Emoji>} registering Sendbird server
     * since 1.1.0
     */
    @NonNull
    public List<Emoji> getAllEmojis() {
        List<Emoji> emojiList = new ArrayList<>(allEmojiMap.values());
        return Collections.unmodifiableList(emojiList);
    }

    /**
     * Returns the {@link List<Emoji>} corresponding to emoji category id
     *
     * @param emojiCategoryId Emoji category id
     * @return The {@link List<Emoji>} corresponding to emoji category id
     * since 1.1.0
     */
    @Nullable
    public List<Emoji> getEmojis(long emojiCategoryId) {
        synchronized (emojiLock) {
            EmojiCategory emojiCategory = emojiCategoryMap.get(emojiCategoryId);
            if (emojiCategory != null) {
                return Collections.unmodifiableList(emojiCategory.getEmojis());
            }
        }
        return null;
    }

    @NonNull
    private String encodeEmojiContainer(@NonNull EmojiContainer container) {
        return Base64.encodeToString(container.serialize(), Base64.DEFAULT);
    }

    @Nullable
    private EmojiContainer decodeEmojiContainer(@NonNull String data) {
        byte[] array = Base64.decode(data, Base64.DEFAULT);
        return EmojiContainer.buildFromSerializedData(array);
    }
}
