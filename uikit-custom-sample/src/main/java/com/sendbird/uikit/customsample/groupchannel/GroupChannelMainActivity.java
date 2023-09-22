package com.sendbird.uikit.customsample.groupchannel;

import static com.sendbird.uikit.customsample.consts.StringSet.PUSH_REDIRECT_CHANNEL;
import static com.sendbird.uikit.customsample.consts.StringSet.PUSH_REDIRECT_MESSAGE_ID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.handler.UserEventHandler;
import com.sendbird.android.params.GroupChannelTotalUnreadMessageCountParams;
import com.sendbird.android.user.User;
import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.SettingsFragment;
import com.sendbird.uikit.customsample.widgets.CustomTabView;
import com.sendbird.uikit.providers.FragmentProviders;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Displays a group channel list screen.
 */
public class GroupChannelMainActivity extends AppCompatActivity {
    private static final String USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY";
    private CustomTabView unreadCountTab;
    private ViewPager mainPage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPage();
    }

    private void initPage() {
        mainPage = findViewById(R.id.vpMain);
        mainPage.setAdapter(new MainAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));

        TabLayout tabLayout = findViewById(R.id.tlMain);
        tabLayout.setupWithViewPager(mainPage);

        unreadCountTab = new CustomTabView(this);
        unreadCountTab.setBadgeVisibility(View.GONE);
        unreadCountTab.setTitle(getString(R.string.text_tab_channels));
        unreadCountTab.setIcon(R.drawable.icon_chat_filled);

        CustomTabView settingsTab = new CustomTabView(this);
        settingsTab.setBadgeVisibility(View.GONE);
        settingsTab.setTitle(getString(R.string.text_tab_settings));
        settingsTab.setIcon(R.drawable.icon_settings_filled);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setCustomView(unreadCountTab);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setCustomView(settingsTab);

        redirectChannelIfNeeded(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        SendbirdChat.getTotalUnreadMessageCount(new GroupChannelTotalUnreadMessageCountParams(), (totalCount, e) -> {
            if (e != null) {
                return;
            }

            if (totalCount > 0) {
                unreadCountTab.setBadgeVisibility(View.VISIBLE);
                unreadCountTab.setBadgeCount(totalCount > 99 ?
                        getString(R.string.text_tab_badge_max_count) :
                        String.valueOf(totalCount));
            } else {
                unreadCountTab.setBadgeVisibility(View.GONE);
            }
        });

        SendbirdChat.addUserEventHandler(USER_EVENT_HANDLER_KEY, new UserEventHandler() {
            @Override
            public void onFriendsDiscovered(@NonNull List<User> list) {}

            @Override
            public void onTotalUnreadMessageCountChanged(int totalCount, @NonNull Map<String, Integer> totalCountByCustomType) {
                if (totalCount > 0) {
                    unreadCountTab.setBadgeVisibility(View.VISIBLE);
                    unreadCountTab.setBadgeCount(totalCount > 99 ?
                            getString(R.string.text_tab_badge_max_count) :
                            String.valueOf(totalCount));
                } else {
                    unreadCountTab.setBadgeVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SendbirdChat.removeUserEventHandler(USER_EVENT_HANDLER_KEY);
    }

    @Override
    protected void onNewIntent(@Nullable Intent intent) {
        super.onNewIntent(intent);
        redirectChannelIfNeeded(intent);
    }

    @NonNull
    public static Intent newRedirectToChannelIntent(@NonNull Context context,
                                                    @NonNull String channelUrl,
                                                    long messageId) {
        Intent intent = new Intent(context, GroupChannelMainActivity.class);
        intent.putExtra(PUSH_REDIRECT_CHANNEL, channelUrl);
        intent.putExtra(PUSH_REDIRECT_MESSAGE_ID, messageId);
        return intent;
    }

    private void redirectChannelIfNeeded(Intent intent) {
        if (intent == null) return;

        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            intent.removeExtra(PUSH_REDIRECT_CHANNEL);
            intent.removeExtra(PUSH_REDIRECT_MESSAGE_ID);
        }
        if (intent.hasExtra(PUSH_REDIRECT_CHANNEL)) {
            String channelUrl = intent.getStringExtra(PUSH_REDIRECT_CHANNEL);

            if (intent.hasExtra(PUSH_REDIRECT_MESSAGE_ID)) {
                long messageId = intent.getLongExtra(PUSH_REDIRECT_MESSAGE_ID, 0L);
                if (messageId > 0L) {
                    startActivity(ChannelActivity.newRedirectToMessageThreadIntent(this, channelUrl, messageId));
                    intent.removeExtra(PUSH_REDIRECT_MESSAGE_ID);
                }
            } else {
                startActivity(ChannelActivity.newIntent(this, channelUrl));
            }
            intent.removeExtra(PUSH_REDIRECT_CHANNEL);
        }
    }

    public void moveToSettings() {
        mainPage.setCurrentItem(1);
    }

    private static class MainAdapter extends FragmentPagerAdapter {
        private static final int PAGE_SIZE = 2;

        public MainAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return FragmentProviders.getChannelList().provide(new Bundle());
            } else {
                return new SettingsFragment();
            }
        }

        @Override
        public int getCount() {
            return PAGE_SIZE;
        }
    }
}
