package com.sendbird.uikit.customsample.openchannel;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.OpenChannel;

abstract public class OpenChannelListViewHolder extends RecyclerView.ViewHolder {
    public OpenChannelListViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    abstract protected void bind(@Nullable OpenChannel openChannel);
}
