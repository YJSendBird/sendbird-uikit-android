package com.sendbird.uikit.customsample;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.multidex.MultiDexApplication;

import com.sendbird.android.ApplicationUserListQuery;
import com.sendbird.android.FileMessageParams;
import com.sendbird.android.GroupChannelParams;
import com.sendbird.android.OpenChannelParams;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessageParams;
import com.sendbird.android.handlers.InitResultHandler;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter;
import com.sendbird.uikit.customsample.consts.InitState;
import com.sendbird.uikit.customsample.fcm.MyFirebaseMessagingService;
import com.sendbird.uikit.customsample.models.CustomUser;
import com.sendbird.uikit.customsample.utils.PreferenceUtils;
import com.sendbird.uikit.customsample.utils.PushUtils;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.CustomUserListQueryHandler;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.interfaces.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class BaseApplication extends MultiDexApplication {

    private static final String APP_ID = "2D7B4CDB-932F-4082-9B09-A1153792DC8D";
    private static final MutableLiveData<InitState> initState = new MutableLiveData<>();

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceUtils.init(getApplicationContext());

        SendbirdUIKit.init(new SendbirdUIKitAdapter() {
            @NonNull
            @Override
            public String getAppId() {
                return APP_ID;
            }

            @Override
            public String getAccessToken() {
                return "";
            }

            @NonNull
            @Override
            public UserInfo getUserInfo() {
                return new UserInfo() {
                    @NonNull
                    @Override
                    public String getUserId() {
                        return PreferenceUtils.getUserId();
                    }

                    @Override
                    public String getNickname() {
                        return PreferenceUtils.getNickname();
                    }

                    @Override
                    public String getProfileUrl() {
                        return PreferenceUtils.getProfileUrl();
                    }
                };
            }

            @NonNull
            @Override
            public InitResultHandler getInitResultHandler() {
                return new InitResultHandler() {
                    @Override
                    public void onMigrationStarted() {
                        initState.setValue(InitState.MIGRATING);
                    }

                    @Override
                    public void onInitFailed(@NonNull SendBirdException e) {
                        initState.setValue(InitState.FAILED);
                    }

                    @Override
                    public void onInitSucceed() {
                        PushUtils.registerPushHandler(new MyFirebaseMessagingService());
                        SendbirdUIKit.setDefaultThemeMode(SendbirdUIKit.ThemeMode.Light);
                        SendbirdUIKit.setLogLevel(SendbirdUIKit.LogLevel.ALL);
                        SendbirdUIKit.setUseDefaultUserProfile(false);

                        initState.setValue(InitState.SUCCEED);
                    }
                };
            }
        }, this);

        SendbirdUIKit.setCustomParamsHandler(new CustomParamsHandler() {
            @Override
            public void onBeforeCreateGroupChannel(@NonNull GroupChannelParams groupChannelParams) {
                // You can set GroupChannelParams globally before creating a channel.
            }

            @Override
            public void onBeforeUpdateGroupChannel(@NonNull GroupChannelParams groupChannelParams) {
                // You can set GroupChannelParams globally before updating a channel.
            }

            @Override
            public void onBeforeSendUserMessage(@NonNull UserMessageParams userMessageParams) {
                // You can set UserMessageParams globally before sending a text message.
            }

            @Override
            public void onBeforeSendFileMessage(@NonNull FileMessageParams fileMessageParams) {
                // You can set FileMessageParams globally before sending a binary file message.
            }

            @Override
            public void onBeforeUpdateUserMessage(@NonNull UserMessageParams userMessageParams) {
                // You can set UserMessageParams globally before updating a text message.
            }

            @Override
            public void onBeforeUpdateOpenChannel(@NonNull OpenChannelParams openChannelParams) {
                // You can set OpenChannelParams globally before updating a channel.
            }
        });

        SendbirdUIKit.setCustomUserListQueryHandler(getCustomUserListQuery());
        SendbirdUIKit.setUIKitFragmentFactory(new CustomFragmentFactory());
    }

    @NonNull
    public static LiveData<InitState> initStateChanges() {
        return initState;
    }

    @NonNull
    public static CustomUserListQueryHandler getCustomUserListQuery() {
        final ApplicationUserListQuery userListQuery = SendBird.createApplicationUserListQuery();
        return new CustomUserListQueryHandler() {

            @Override
            public void loadInitial(@NonNull OnListResultHandler<UserInfo> handler) {
                userListQuery.setLimit(5);
                userListQuery.next((list, e) -> {
                    if (e != null) {
                        return;
                    }

                    final List<UserInfo> customUserList = new ArrayList<>();
                    for (User user : list) {
                        customUserList.add(new CustomUser(user));
                    }
                    handler.onResult(customUserList, null);
                });
            }

            @Override
            public void loadMore(@NonNull OnListResultHandler<UserInfo> handler) {
                userListQuery.next((list, e) -> {
                    if (e != null) {
                        return;
                    }

                    List<UserInfo> customUserList = new ArrayList<>();
                    for (User user : list) {
                        customUserList.add(new CustomUser(user));
                    }
                    handler.onResult(customUserList, null);
                });
            }

            @Override
            public boolean hasMore() {
                return userListQuery.hasNext();
            }
        };
    }
}
