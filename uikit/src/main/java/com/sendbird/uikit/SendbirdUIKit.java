package com.sendbird.uikit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.android.AppInfo;
import com.sendbird.android.ConnectionState;
import com.sendbird.android.NotificationInfo;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.CompletionHandler;
import com.sendbird.android.handler.ConnectHandler;
import com.sendbird.android.handler.DisconnectHandler;
import com.sendbird.android.handler.InitResultHandler;
import com.sendbird.android.params.InitParams;
import com.sendbird.android.params.UserUpdateParams;
import com.sendbird.android.user.User;
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter;
import com.sendbird.uikit.consts.ReplyType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.consts.ThreadReplySelectType;
import com.sendbird.uikit.fragments.UIKitFragmentFactory;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.CustomUserListQueryHandler;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.internal.singleton.NotificationChannelManager;
import com.sendbird.uikit.internal.tasks.JobResultTask;
import com.sendbird.uikit.internal.tasks.TaskQueue;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.EmojiManager;
import com.sendbird.uikit.model.UserMentionConfig;
import com.sendbird.uikit.utils.FileUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.utils.UIKitPrefs;

import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sendbird UIKit Main Class.
 */
public class SendbirdUIKit {
    private static volatile SendbirdUIKitAdapter adapter;

    /**
     * UIKit log level. It depends on android Log level.
     */
    public enum LogLevel {
        ALL(Log.VERBOSE), INFO(Log.INFO), WARN(Log.WARN), ERROR(Log.ERROR), NONE(Integer.MAX_VALUE);

        final int level;

        LogLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * UIKit theme mode.
     */
    public enum ThemeMode {
        /**
         * Light mode.
         */
        Light(R.style.AppTheme_Sendbird, R.color.primary_300, R.color.secondary_300, R.color.onlight_03, R.color.error_300),
        /**
         * Dark mode.
         */
        Dark(R.style.AppTheme_Dark_Sendbird, R.color.primary_200, R.color.secondary_200, R.color.ondark_03, R.color.error_200);

        @StyleRes
        int resId;
        @ColorRes
        int primaryTintColorResId;
        @ColorRes
        int secondaryTintColorResId;
        @ColorRes
        int monoTintColorResId;
        @ColorRes
        int errorColorResId;

        ThemeMode(@StyleRes int resId, @ColorRes int primaryTintColorResId, @ColorRes int secondaryTintColorResId, @ColorRes int monoTintColorResId, @ColorRes int errorColorResId) {
            this.resId = resId;
            this.primaryTintColorResId = primaryTintColorResId;
            this.secondaryTintColorResId = secondaryTintColorResId;
            this.monoTintColorResId = monoTintColorResId;
            this.errorColorResId = errorColorResId;
        }

        /**
         * Returns the style resource id for the current theme.
         *
         * @return The style resource id for the current theme.
         */
        @StyleRes
        public int getResId() {
            return resId;
        }

        /**
         * Returns the resource id of the primary tint color for the current theme.
         *
         * @return The resource id of the primary tint color for the current theme.
         */
        @ColorRes
        public int getPrimaryTintResId() {
            return primaryTintColorResId;
        }

        /**
         * Returns the resource id of the secondary tint color for the current theme.
         *
         * @return The resource id of the secondary tint color for the current theme.
         */
        @ColorRes
        public int getSecondaryTintResId() {
            return secondaryTintColorResId;
        }

        /**
         * Returns the resource id of the mono tint color for the current theme.
         *
         * @return The resource id of the mono tint color for the current theme.
         */
        @ColorRes
        public int getMonoTintResId() {
            return monoTintColorResId;
        }

        /**
         * Returns the resource id of the error tint color for the current theme.
         *
         * @return The resource id of the error tint color for the current theme.
         */
        @ColorRes
        public int getErrorColorResId() {
            return errorColorResId;
        }

        /**
         * Returns the {@code ColorStateList} of the primary tint color for the current theme.
         *
         * @return {@code ColorStateList} of the primary tint color for the current theme.
         */
        @NonNull
        public ColorStateList getPrimaryTintColorStateList(@NonNull Context context) {
            return AppCompatResources.getColorStateList(context, primaryTintColorResId);
        }

        /**
         * Returns the {@code ColorStateList} of the secondary tint color for the current theme.
         *
         * @return {@code ColorStateList} of the secondary tint color for the current theme.
         */
        @NonNull
        public ColorStateList getSecondaryTintColorStateList(@NonNull Context context) {
            return AppCompatResources.getColorStateList(context, secondaryTintColorResId);
        }

        /**
         * Returns the {@code ColorStateList} of the mono tint color for the current theme.
         *
         * @return {@code ColorStateList} of the mono tint color for the current theme.
         */
        @NonNull
        public ColorStateList getMonoTintColorStateList(@NonNull Context context) {
            return AppCompatResources.getColorStateList(context, monoTintColorResId);
        }

        /**
         * Returns the {@code ColorStateList} of the error tint color for the current theme.
         *
         * @return {@code ColorStateList} of the error tint color for the current theme.
         */
        @NonNull
        public ColorStateList getErrorTintColorStateList(@NonNull Context context) {
            return AppCompatResources.getColorStateList(context, errorColorResId);
        }
    }

    private static final int DEFAULT_RESIZING_WIDTH_SIZE = 1080;
    private static final int DEFAULT_RESIZING_HEIGHT_SIZE = 1920;

    @NonNull
    private static volatile ThemeMode defaultThemeMode = ThemeMode.Light;
    private static volatile boolean useDefaultUserProfile = false;
    private static volatile boolean useCompression = true;
    @Nullable
    private static CustomUserListQueryHandler customUserListQueryHandler;
    @Nullable
    private static CustomParamsHandler customParamsHandler;
    private static int compressQuality = 70;
    @NonNull
    private static Pair<Integer, Integer> resizingSize = new Pair<>(DEFAULT_RESIZING_WIDTH_SIZE, DEFAULT_RESIZING_HEIGHT_SIZE);
    @NonNull
    private static ReplyType replyType = ReplyType.QUOTE_REPLY;
    @NonNull
    private static ThreadReplySelectType threadReplySelectType = ThreadReplySelectType.THREAD;
    @NonNull
    private static UIKitFragmentFactory fragmentFactory = new UIKitFragmentFactory();
    private static volatile boolean useChannelListTypingIndicators = false;
    private static volatile boolean useChannelListMessageReceiptStatus = false;
    private static volatile boolean useMention = false;
    private static volatile boolean useUserIdForNickname = true;
    private static UserMentionConfig userMentionConfig = new UserMentionConfig.Builder().build();
    private static volatile boolean useVoiceMessage = false;

    static void clearAll() {
        SendbirdUIKit.customUserListQueryHandler = null;
        defaultThemeMode = ThemeMode.Light;
        UIKitPrefs.clearAll();
        NotificationChannelManager.clearAll();
    }

    /**
     * Initializes Sendbird with given app ID.
     *
     * @param adapter The {@link SendbirdUIKitAdapter} providing an app ID, a information of the user.
     * @param context <code>Context</code> of <code>Application</code>.
     */
    public synchronized static void init(@NonNull SendbirdUIKitAdapter adapter, @NonNull Context context) {
        init(adapter, context, false);
    }

    /**
     * Initializes Sendbird from foreground with given app ID.
     *
     * @param adapter The {@link SendbirdUIKitAdapter} providing an app ID, a information of the user.
     * @param context <code>Context</code> of <code>Application</code>.
     * since 2.1.8
     */
    public synchronized static void initFromForeground(@NonNull SendbirdUIKitAdapter adapter, @NonNull Context context) {
        init(adapter, context, true);
    }

    private synchronized static void init(@NonNull SendbirdUIKitAdapter adapter, @NonNull Context context, boolean isForeground) {
        SendbirdUIKit.adapter = adapter;

        final InitResultHandler handler = adapter.getInitResultHandler();
        final InitResultHandler initResultHandler = new InitResultHandler() {
            @Override
            public void onMigrationStarted() {
                Logger.d(">> onMigrationStarted()");
                handler.onMigrationStarted();
            }

            @Override
            public void onInitFailed(@NonNull SendbirdException e) {
                Logger.d(">> onInitFailed() e=%s", e);
                Logger.e(e);
                handler.onInitFailed(e);
            }

            @Override
            public void onInitSucceed() {
                Logger.d(">> onInitSucceed()");
                FileUtils.removeDeletableDir(context.getApplicationContext());
                UIKitPrefs.init(context.getApplicationContext());
                NotificationChannelManager.init(context.getApplicationContext());
                EmojiManager.getInstance().init();

                try {
                    SendbirdChat.addExtension(StringSet.sb_uikit, BuildConfig.VERSION_NAME);
                } catch (Throwable ignored) {
                }

                handler.onInitSucceed();
            }
        };

        final com.sendbird.android.LogLevel logLevel = BuildConfig.DEBUG ? com.sendbird.android.LogLevel.VERBOSE : com.sendbird.android.LogLevel.WARN;
        // useCaching=true is required for UIKit
        final InitParams initParams = new InitParams(adapter.getAppId(), context, true, logLevel, isForeground);
        SendbirdChat.init(initParams, initResultHandler);
    }

    /**
     * @param level set the displaying log level. {@link LogLevel}
     *
     * since 1.0.2
     */
    public static void setLogLevel(@NonNull LogLevel level) {
        Logger.setLogLevel(level.getLevel());
    }

    /**
     * Returns the default theme mode.
     *
     * @see #setDefaultThemeMode(ThemeMode)
     */
    @NonNull
    public static ThemeMode getDefaultThemeMode() {
        return defaultThemeMode;
    }

    /**
     * Sets the default theme mode. This is the default value used for all components, but can
     * be overridden locally via the builder of a fragment.
     *
     * <p>Defaults to {@link ThemeMode#Light}.</p>
     *
     * @see #getDefaultThemeMode() ()
     */
    public static void setDefaultThemeMode(@NonNull ThemeMode themeMode) {
        defaultThemeMode = themeMode;
    }

    /**
     * Determines whether the theme mode is {@link ThemeMode#Dark}.
     *
     * @return True if the theme mode is the dark, false otherwise.
     */
    public static boolean isDarkMode() {
        return defaultThemeMode == ThemeMode.Dark;
    }

    /**
     * Returns the adapter of SendBirdUIKit.
     *
     * @see #init(SendbirdUIKitAdapter, Context)
     */
    @Nullable
    public static SendbirdUIKitAdapter getAdapter() {
        return adapter;
    }

    /**
     * Returns the custom user list query handler.
     *
     * @return The callback handler.
     */
    @Nullable
    public static CustomUserListQueryHandler getCustomUserListQueryHandler() {
        return customUserListQueryHandler;
    }

    /**
     * Returns the custom params handler.
     *
     * @return The callback handler.
     * since 1.2.2
     */
    @Nullable
    public static CustomParamsHandler getCustomParamsHandler() {
        return customParamsHandler;
    }

    /**
     * Returns set value whether the user profile uses.
     *
     * @return the value whether the user profile uses.
     * since 1.2.2
     */
    public static boolean shouldUseDefaultUserProfile() {
        return useDefaultUserProfile;
    }

    /**
     * Sets whether the user profile uses.
     *
     * @param useDefaultUserProfile If <code>true</code> the default user profile included the UIKit will be shown, <code>false</code> other wise.
     * since 1.2.2
     */
    public static void setUseDefaultUserProfile(boolean useDefaultUserProfile) {
        SendbirdUIKit.useDefaultUserProfile = useDefaultUserProfile;
    }

    /**
     * Sets whether the typing indicator is used on the channel list screen.
     *
     * @param useChannelListTypingIndicators If <code>true</code> the typing indicator will be shown at the channel list item, <code>false</code> other wise.
     * since 3.0.0
     */
    public static void setUseChannelListTypingIndicators(boolean useChannelListTypingIndicators) {
        SendbirdUIKit.useChannelListTypingIndicators = useChannelListTypingIndicators;
    }

    /**
     * Returns set value whether the typing indicator is used on the channel list screen.
     *
     * @return the value whether the typing indicator is used on the channel list screen.
     * since 3.0.0
     */
    public static boolean isUsingChannelListTypingIndicators() {
        return useChannelListTypingIndicators;
    }

    /**
     * Sets whether the states read-receipt and delivery-receipt are used on the channel list screen.
     *
     * @param useChannelListMessageReceiptStatus If <code>true</code> the states read-receipt and delivery-receipt will be shown at the channel list item, <code>false</code> other wise.
     * since 3.0.0
     */
    public static void setUseChannelListMessageReceiptStatus(boolean useChannelListMessageReceiptStatus) {
        SendbirdUIKit.useChannelListMessageReceiptStatus = useChannelListMessageReceiptStatus;
    }

    /**
     * Returns set value whether the states read-receipt and delivery-receipt are used on the channel list screen.
     *
     * @return the value whether the states read-receipt and delivery-receipt are used on the channel list screen.
     * since 3.0.0
     */
    public static boolean isUsingChannelListMessageReceiptStatus() {
        return useChannelListMessageReceiptStatus;
    }

    /**
     * Sets whether the user mention is used on the channel screen.
     *
     * @param useMention If <code>true</code> the mention will be used at the channel, <code>false</code> other wise.
     * since 3.0.0
     */
    public static void setUseUserMention(boolean useMention) {
        SendbirdUIKit.useMention = useMention;
    }

    /**
     * Sets the user mention configuration.
     *
     * @param config The configuration to be applied for the mention
     * @see UserMentionConfig
     * since 3.0.0
     */
    public static void setMentionConfig(@NonNull UserMentionConfig config) {
        SendbirdUIKit.userMentionConfig = config;
    }

    /**
     * Sets whether a nickname uses a user ID when there is no user nickname based on the user ID.
     *
     * @param useUserIdForNickname If <code>true</code> the user's nickname uses user ID when the nickname is empty, <code>false</code> other wise.
     * since 3.3.0
     */
    public static void setUseUserIdForNickname(boolean useUserIdForNickname) {
        SendbirdUIKit.useUserIdForNickname = useUserIdForNickname;
    }

    /**
     * Returns the user mention configuration.
     *
     * @return The configuration applied for the user mention
     * since 3.0.0
     */
    @NonNull
    public static UserMentionConfig getUserMentionConfig() {
        return userMentionConfig;
    }

    /**
     * Returns set value whether the user mention is used on the channel screen.
     *
     * @return The value whether the user mention is used on the channel screen.
     * since 3.0.0
     */
    public static boolean isUsingUserMention() {
        return SendbirdUIKit.useMention;
    }

    /**
     * Returns set value whether a nickname uses a user ID when there is no user nickname based on the user ID.
     *
     * @return The value whether a nickname uses a user ID when there is no user nickname based on the user ID.
     * since 3.3.0
     */
    public static boolean isUsingUserIdForNickname() {
        return useUserIdForNickname;
    }

    /**
     * Sets whether the voice message is used on the channel screen and message thread screen.
     * The voice message is only active in group channels.
     *
     * @param useVoiceMessage If <code>true</code> the voice message will be used, <code>false</code> other wise.
     * since 3.4.0
     */
    public static void setUseVoiceMessage(boolean useVoiceMessage) {
        SendbirdUIKit.useVoiceMessage = useVoiceMessage;
    }

    /**
     * Returns set value whether the voice message is used on the channel screen and message thread screen.
     * The voice message is only active in group channels.
     *
     * @return The value whether the voice message is used on the channel screen, message thread screen.
     * since 3.4.0
     */
    public static boolean isUsingVoiceMessage() {
        return SendbirdUIKit.useVoiceMessage;
    }

    /**
     * Connects to Sendbird with given <code>UserInfo</code>.
     *
     * @param handler Callback handler.
     */
    public static void connect(@Nullable ConnectHandler handler) {
        TaskQueue.addTask(new JobResultTask<Pair<User, SendbirdException>>() {
            @Override
            public Pair<User, SendbirdException> call() throws Exception {
                final Pair<User, SendbirdException> data = connect();
                final User user = data.first;
                final SendbirdException error = data.second;
                if (SendbirdChat.getConnectionState() == ConnectionState.OPEN && user != null) {
                    UserInfo userInfo = adapter.getUserInfo();
                    String userId = userInfo.getUserId();
                    String nickname = TextUtils.isEmpty(userInfo.getNickname()) ? user.getNickname() : userInfo.getNickname();
                    if (useUserIdForNickname && TextUtils.isEmpty(nickname)) nickname = userId;
                    String profileUrl = TextUtils.isEmpty(userInfo.getProfileUrl()) ? user.getProfileUrl() : userInfo.getProfileUrl();
                    if (!nickname.equals(user.getNickname()) || (!TextUtils.isEmpty(profileUrl) && !profileUrl.equals(user.getProfileUrl()))) {
                        final UserUpdateParams params = new UserUpdateParams();
                        params.setNickname(nickname);
                        params.setProfileImageUrl(profileUrl);
                        updateUserInfoBlocking(params);
                    }

                    Logger.dev("++ user nickname = %s, profileUrl = %s", user.getNickname(), user.getProfileUrl());

                    final AppInfo appInfo = SendbirdChat.getAppInfo();
                    if (appInfo != null) {
                        if (appInfo.getUseReaction() &&
                                appInfo.needUpdateEmoji(EmojiManager.getInstance().getEmojiHash())) {
                            updateEmojiList();
                        }

                        final NotificationInfo notificationInfo = appInfo.getNotificationInfo();
                        if (notificationInfo != null && notificationInfo.isEnabled()) {
                            // Even if the request fails, it should not affect the result of the connection request.
                            try {
                                // if the cache exists or no need to update, blocking is released right away
                                final String latestToken = notificationInfo.getTemplateListToken();
                                NotificationChannelManager.requestTemplateListBlocking(latestToken);
                            } catch (Exception ignore) {
                            }
                            try {
                                // if the cache exists or no need to update, blocking is released right away
                                final long settingsUpdatedAt = notificationInfo.getSettingsUpdatedAt();
                                NotificationChannelManager.requestNotificationChannelSettingBlocking(settingsUpdatedAt);
                            } catch (Exception ignore) {
                            }
                        }
                    }
                }

                return new Pair<>(user, error);
            }

            @Override
            public void onResultForUiThread(@Nullable Pair<User, SendbirdException> data, @Nullable SendbirdException e) {
                final User user = data != null ? data.first : null;
                final SendbirdException error = data != null ? data.second : e;
                Logger.d("++ user=%s, error=%s", user, error);
                if (handler != null) {
                    handler.onConnected(user, error);
                }
            }
        });
    }

    @NonNull
    private static Pair<User, SendbirdException> connect() throws InterruptedException {
        AtomicReference<User> result = new AtomicReference<>();
        AtomicReference<SendbirdException> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        UserInfo userInfo = adapter.getUserInfo();
        String userId = userInfo.getUserId();
        String accessToken = adapter.getAccessToken();

        SendbirdChat.connect(userId, accessToken, (user, e) -> {
            result.set(user);
            if (e != null) {
                error.set(e);
            }

            latch.countDown();
        });
        latch.await();
        return new Pair<>(result.get(), error.get());
    }

    /**
     * Disconnects from SendbirdChat. Call this when user logged out.
     *
     * @param handler Callback handler.
     */
    public static void disconnect(@Nullable DisconnectHandler handler) {
        SendbirdChat.disconnect(() -> {
            clearAll();
            if (handler != null) {
                handler.onDisconnected();
            }
        });
    }

    /**
     * Updates current <code>UserInfo</code>.
     *
     * @param params   Params for update current users.
     * @param handler    Callback handler.
     */
    public static void updateUserInfo(@NonNull UserUpdateParams params, @Nullable CompletionHandler handler) {
        SendbirdChat.updateCurrentUserInfo(params, handler);
    }

    private static void updateUserInfoBlocking(@NonNull UserUpdateParams params) throws SendbirdException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<SendbirdException> error = new AtomicReference<>();
        SendbirdChat.updateCurrentUserInfo(params, e -> {
            if (e != null) error.set(e);
            latch.countDown();
        });
        latch.await();
        if (error.get() != null) throw error.get();
    }

    /**
     * Sets the handler that loads the list of custom user.
     *
     * @param handler The callback that will run.
     */
    public static void setCustomUserListQueryHandler(@NonNull CustomUserListQueryHandler handler) {
        SendbirdUIKit.customUserListQueryHandler = handler;
    }

    /**
     * Sets the handler so that common custom data can be set.
     *
     * @param handler The callback that will run.
     * since 1.2.2
     */
    public static void setCustomParamsHandler(@NonNull CustomParamsHandler handler) {
        SendbirdUIKit.customParamsHandler = handler;
    }

    /**
     * Sets the factory that creates fragments generated by UIKit's basic activities.
     *
     * since 3.0.0
     */
    public static void setUIKitFragmentFactory(@NonNull UIKitFragmentFactory factory) {
        SendbirdUIKit.fragmentFactory = factory;
    }

    /**
     * Returns the factory that creates fragments generated by UIKit's basic activities.
     *
     * @return {@link UIKitFragmentFactory} that creates fragments generated by UIKit's basic activities.
     * since 3.0.0
     */
    @NonNull
    public static UIKitFragmentFactory getFragmentFactory() {
        return fragmentFactory;
    }

    private static void updateEmojiList() {
        Logger.d(">> SendBirdUIkit::updateEmojiList()");
        SendbirdChat.getAllEmoji((emojiContainer, e) -> {
            if (e != null) {
                Logger.e(e);
            } else {
                if (emojiContainer != null) {
                    EmojiManager.getInstance().upsertEmojiContainer(emojiContainer);
                }
            }
        });
    }

    /**
     * Sets whether the image file compress when trying to send image file message. Default value is <code>true</code>.
     * The target image types are 'image/jpg`, `image/jpeg`, and `image/png`, the others will be ignored.
     *
     * @param useCompression If <code>true</code> the image file will be transferred to the original image, <code>false</code> other wise.
     * since 2.0.1
     */
    public static void setUseImageCompression(boolean useCompression) {
        SendbirdUIKit.useCompression = useCompression;
    }

    /**
     * Returns the value whether the sending image file will be compressing.
     *
     * @return the value whether the sending image file will be compressing.
     * since 2.0.1
     */
    public static boolean shouldUseImageCompression() {
        return SendbirdUIKit.useCompression;
    }

    /**
     * Image compression quality value that will be used when sending image. Default value is 70.
     * It has to be bigger than 0 and cannot exceed 100.
     *
     * @param compressQuality Hint to the compressor, 0-100. 0 meaning compress for
     *                        smallest size, 100 meaning compress for max quality. Some
     *                        formats, like PNG which is lossless, will ignore the
     *                        quality setting
     * @see android.graphics.Bitmap#compress(Bitmap.CompressFormat format, int quality, OutputStream stream)
     * since 2.0.1
     */
    public static void setCompressQuality(int compressQuality) {
        SendbirdUIKit.compressQuality = compressQuality;
    }

    /**
     * Returns the value of image compression.
     *
     * @return The value of image compression.
     * since 2.0.1
     */
    public static int getCompressQuality() {
        return SendbirdUIKit.compressQuality;
    }

    /**
     * Image resizing size value that will be used when displaying image. Default resizing size values are set to 1080x1920.
     * The first value is width, the second is height.
     * When drawing a thumbnail, half the set size is used, and the minimum value is 100x100.
     *
     * @param resizingSize The value of the image to resize.
     * since 2.0.1
     */
    public static void setResizingSize(@NonNull Pair<Integer, Integer> resizingSize) {
        SendbirdUIKit.resizingSize = resizingSize;
    }

    /**
     * Returns a size value to resize.
     *
     * @return The value of the image to resize.
     * since 2.0.1
     */
    @NonNull
    public static Pair<Integer, Integer> getResizingSize() {
        return SendbirdUIKit.resizingSize;
    }

    /**
     * Sets <code>ReplyType</code>, which is how replies are displayed in the message list.
     *
     * @param replyType A type that represents how to display replies in message list
     * since 2.2.0
     */
    public static void setReplyType(@NonNull ReplyType replyType) {
        SendbirdUIKit.replyType = replyType;
    }

    /**
     * Sets <code>ThreadReplySelectType</code>, which is how replies are displayed in the message list.
     * <code>ThreadReplySelectType</code> can be applied when the reply type is <code>ReplyType.THREAD</code>.
     *
     * @param threadReplySelectType A type that represents where to go when selecting a reply
     * since 3.3.0
     */
    public static void setThreadReplySelectType(@NonNull ThreadReplySelectType threadReplySelectType) {
        SendbirdUIKit.threadReplySelectType = threadReplySelectType;
    }

    /**
     * Returns <code>ReplyType</code>, which is how replies are displayed in the message list.
     *
     * @return The value of <code>ReplyType</code>.
     * since 2.2.0
     */
    @NonNull
    public static ReplyType getReplyType() {
        return SendbirdUIKit.replyType;
    }

    /**
     * Returns <code>ThreadReplySelectType</code>, which determines where to go when reply is selected.
     * <code>ThreadReplySelectType</code> can be applied when the reply type is <code>ReplyType.THREAD</code>.
     *
     * @return The value of <code>ThreadReplySelectType</code>.
     * since 3.3.0
     */
    @NonNull
    public static ThreadReplySelectType getThreadReplySelectType() {
        return SendbirdUIKit.threadReplySelectType;
    }

    public static void runOnUIThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
