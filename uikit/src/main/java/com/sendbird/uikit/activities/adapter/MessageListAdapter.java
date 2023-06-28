package com.sendbird.uikit.activities.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.uikit.internal.wrappers.SendbirdUIKitImpl;
import com.sendbird.uikit.internal.wrappers.SendbirdUIKitWrapper;
import com.sendbird.uikit.model.MessageListUIParams;

/**
 * MessageListAdapter provides a binding from a {@link BaseMessage} type data set to views that are displayed within a RecyclerView.
 */
public class MessageListAdapter extends BaseMessageListAdapter {
    public MessageListAdapter(boolean useMessageGroupUI) {
        this(null, useMessageGroupUI);
    }

    public MessageListAdapter(@Nullable GroupChannel channel) {
        this(channel, true);
    }

    public MessageListAdapter(@Nullable GroupChannel channel, boolean useMessageGroupUI) {
        this(channel, new MessageListUIParams.Builder()
                .setUseMessageGroupUI(useMessageGroupUI)
                .build());
    }

    public MessageListAdapter(@Nullable GroupChannel channel, @NonNull MessageListUIParams messageListUIParams) {
        this(channel, messageListUIParams, new SendbirdUIKitImpl());
    }

    @VisibleForTesting
    MessageListAdapter(@Nullable GroupChannel channel, @NonNull MessageListUIParams messageListUIParams, @NonNull SendbirdUIKitWrapper sendbirdUIKit) {
        super(channel,
                new MessageListUIParams.Builder(messageListUIParams)
                        .setUseQuotedView(true)
                        .build(),
                sendbirdUIKit);
    }
}
