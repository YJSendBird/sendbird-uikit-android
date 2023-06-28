package com.sendbird.uikit_messaging_android;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.multidex.MultiDexApplication;

import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.InitResultHandler;
import com.sendbird.android.params.OpenChannelCreateParams;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter;
import com.sendbird.uikit.consts.ReplyType;
import com.sendbird.uikit.consts.ThreadReplySelectType;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.configurations.UIKitConfig;
import com.sendbird.uikit_messaging_android.consts.InitState;
import com.sendbird.uikit_messaging_android.consts.StringSet;
import com.sendbird.uikit_messaging_android.fcm.MyFirebaseMessagingService;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;
import com.sendbird.uikit_messaging_android.utils.PushUtils;

/**
 * Base application to initialize Sendbird UIKit.
 */
public class BaseApplication extends MultiDexApplication {

    private static final String APP_ID = "2D7B4CDB-932F-4082-9B09-A1153792DC8D";
    private static final MutableLiveData<InitState> initState = new MutableLiveData<>();

    /**
     * Initializes Sendbird UIKit
     */
    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceUtils.init(getApplicationContext());

        SendbirdUIKit.init(new SendbirdUIKitAdapter() {
            @Override
            @NonNull
            public String getAppId() {
                String appId;
                if (com.sendbird.uikit_messaging_android.BuildConfig.DEBUG) {
                    appId = PreferenceUtils.getAppId();
                    if (appId.isEmpty()) {
                        appId = APP_ID;
                    }
                } else {
                    appId = APP_ID;
                }
                Logger.d("++ app id : %s", appId);
                return appId;
            }

            @Override
            @NonNull
            public String getAccessToken() {
                return "";
            }

            @Override
            @NonNull
            public UserInfo getUserInfo() {
                return new UserInfo() {
                    @NonNull
                    @Override
                    public String getUserId() {
                        return PreferenceUtils.getUserId();
                    }

                    @NonNull
                    @Override
                    public String getNickname() {
                        return PreferenceUtils.getNickname();
                    }

                    @NonNull
                    @Override
                    public String getProfileUrl() {
                        return PreferenceUtils.getProfileUrl();
                    }
                };
            }

            @Override
            @NonNull
            public InitResultHandler getInitResultHandler() {
                return new InitResultHandler() {
                    @Override
                    public void onMigrationStarted() {
                        initState.setValue(InitState.MIGRATING);
                    }

                    @Override
                    public void onInitFailed(@NonNull SendbirdException e) {
                        initState.setValue(InitState.FAILED);
                    }

                    @Override
                    public void onInitSucceed() {
                        initState.setValue(InitState.SUCCEED);
                    }
                };
            }
        }, this);

        boolean useDarkTheme = PreferenceUtils.isUsingDarkTheme();
        SendbirdUIKit.setDefaultThemeMode(useDarkTheme ? SendbirdUIKit.ThemeMode.Dark : SendbirdUIKit.ThemeMode.Light);
        // register push notification
        PushUtils.registerPushHandler(new MyFirebaseMessagingService());
        // set logger
        SendbirdUIKit.setLogLevel(SendbirdUIKit.LogLevel.ALL);
        // set whether to use user profile
        UIKitConfig.getCommon().setEnableUsingDefaultUserProfile(true);
        // set whether to use typing indicators in channel list
        UIKitConfig.getGroupChannelListConfig().setEnableTypingIndicator(true);
        // set whether to use read/delivery receipt in channel list
        UIKitConfig.getGroupChannelListConfig().setEnableMessageReceiptStatus(true);
        // set whether to use user mention
        UIKitConfig.getGroupChannelConfig().setEnableMention(true);
        // set reply type
        UIKitConfig.getGroupChannelConfig().setReplyType(ReplyType.THREAD);
        UIKitConfig.getGroupChannelConfig().setThreadReplySelectType(ThreadReplySelectType.THREAD);
        // set whether to use voice message
        UIKitConfig.getGroupChannelConfig().setEnableVoiceMessage(true);

        // set custom params
        SendbirdUIKit.setCustomParamsHandler(new CustomParamsHandler() {
            @Override
            public void onBeforeCreateOpenChannel(@NonNull OpenChannelCreateParams params) {
                // You can set OpenChannelCreateParams globally before creating a open channel.
                params.setCustomType(StringSet.SB_COMMUNITY_TYPE);
            }
        });
    }

    /**
     * Returns the state of the result from initialization of Sendbird UIKit.
     *
     * @return the {@link InitState} instance
     */
    @NonNull
    public static LiveData<InitState> initStateChanges() {
        return initState;
    }
}
