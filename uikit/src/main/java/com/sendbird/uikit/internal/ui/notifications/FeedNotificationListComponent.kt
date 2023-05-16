package com.sendbird.uikit.internal.ui.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendbird.android.channel.FeedChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.interfaces.OnMessageListUpdateHandler
import com.sendbird.uikit.interfaces.OnNotificationTemplateActionHandler
import com.sendbird.uikit.internal.model.notifications.NotificationConfig
import com.sendbird.uikit.internal.ui.widgets.InnerLinearLayoutManager
import com.sendbird.uikit.model.Action

/**
 * This class creates and performs a view corresponding the notification message list area in Sendbird UIKit.
 *
 * since 3.5.0
 */
@JvmSuppressWildcards
internal open class FeedNotificationListComponent @JvmOverloads constructor(
    params: Params = Params(),
    uiConfig: NotificationConfig? = null
) : NotificationListComponent(params, uiConfig) {
    /**
     * Returns the feed notification list adapter.
     *
     * @return The adapter applied to this list component
     * since 3.5.0
     */
    private var adapter: FeedNotificationListAdapter? = null
        private set(value) {
            field = value
            notificationListView?.recyclerView?.let {
                if (value?.onMessageTemplateActionHandler == null) {
                    value?.onMessageTemplateActionHandler =
                        OnNotificationTemplateActionHandler { view: View, action: Action, message: BaseMessage ->
                            onMessageTemplateActionClicked(
                                view,
                                action,
                                message
                            )
                        }
                }
                it.adapter = value
            }
        }

    override fun onCreateView(context: Context, inflater: LayoutInflater, parent: ViewGroup, args: Bundle?): View {
        val layout = super.onCreateView(context, inflater, parent, args)
        val layoutManager = InnerLinearLayoutManager(context).apply { reverseLayout = false }
        notificationListView?.recyclerView?.layoutManager = layoutManager
        return layout
    }

    /**
     * Sets the last seen timestamp to update new badge UI.
     * This value is used to compare whether a message has been newly received.
     *
     * @param lastSeenAt the timestamp last viewed by the user.
     * since 3.5.0
     */
    @SuppressLint("NotifyDataSetChanged")
    fun notifyLastSeenUpdated(lastSeenAt: Long) {
        adapter?.let {
            it.updateLastSeenAt(lastSeenAt)
            it.notifyDataSetChanged()
        }
    }

    /**
     * Handles a new channel when data has changed.
     *
     * @param channel The latest group channel
     * since 3.5.0
     */
    fun notifyChannelChanged(channel: FeedChannel) {
        if (adapter == null) {
            adapter = FeedNotificationListAdapter(channel, uiConfig)
        }
    }

    /**
     * Handles the data needed to draw the message list has changed.
     *
     * @param notificationList The list of messages to be drawn
     * @param channel     The latest group channel
     * @param callback    Callback when the message list is updated
     * since 3.5.0
     */
    fun notifyDataSetChanged(
        notificationList: List<BaseMessage>,
        channel: FeedChannel,
        callback: OnMessageListUpdateHandler?
    ) {
        notificationListView?.let {
            adapter?.setItems(channel, notificationList, callback)
        }
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     *
     * **Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.**
     *
     * @see .getParams
     * since 3.5.0
     */
    open class Params : NotificationListComponent.Params() {
        /**
         * Apply data that matches keys mapped to Params' properties.
         *
         * @param context The `Context` this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.5.0
         */
        override fun apply(context: Context, args: Bundle): Params {
            return this
        }
    }
}