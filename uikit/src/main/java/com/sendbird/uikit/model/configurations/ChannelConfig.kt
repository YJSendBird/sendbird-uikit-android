package com.sendbird.uikit.model.configurations

import android.os.Parcelable
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.Role
import com.sendbird.uikit.consts.ReplyType
import com.sendbird.uikit.consts.ThreadReplySelectType
import com.sendbird.uikit.internal.model.serializer.ReplyTypeAsStringSerializer
import com.sendbird.uikit.internal.model.serializer.ThreadReplySelectTypeAsStringSerializer
import com.sendbird.uikit.internal.model.template_messages.KeySet
import com.sendbird.uikit.utils.Available
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.annotations.TestOnly

/**
 * This class is used to determine whether to display features in the channel.
 *
 * @since 3.6.0
 */
@Serializable
@Parcelize
data class ChannelConfig internal constructor(
    @SerialName(KeySet.enable_ogtag)
    private var _enableOgTag: Boolean = true,
    @SerialName(KeySet.enable_mention)
    private var _enableMention: Boolean = false,
    @SerialName(KeySet.enable_typing_indicator)
    private var _enableTypingIndicator: Boolean = true,
    @SerialName(KeySet.enable_reactions)
    private var _enableReactions: Boolean = true,
    @SerialName(KeySet.enable_voice_message)
    private var _enableVoiceMessage: Boolean = false,
    @SerialName(KeySet.thread_reply_select_type)
    @Serializable(with = ThreadReplySelectTypeAsStringSerializer::class)
    private var _threadReplySelectType: ThreadReplySelectType = ThreadReplySelectType.THREAD,
    @SerialName(KeySet.reply_type)
    @Serializable(with = ReplyTypeAsStringSerializer::class)
    private var _replyType: ReplyType = ReplyType.QUOTE_REPLY,
    /**
     * Returns <code>Input</code>, which is the configuration of the input area.
     *
     * @return The value of <code>Input</code>
     * @since 3.6.0
     */
    val input: Input = Input(),

    @Transient
    private var enableOgTagMutable: Boolean? = null,
    @Transient
    private var enableMentionMutable: Boolean? = null,
    @Transient
    private var enableTypingIndicatorMutable: Boolean? = null,
    @Transient
    private var enableReactionsMutable: Boolean? = null,
    @Transient
    private var enableVoiceMessageMutable: Boolean? = null,
    @Transient
    private var threadReplySelectTypeMutable: ThreadReplySelectType? = null,
    @Transient
    private var replyTypeMutable: ReplyType? = null
) : Parcelable {
    companion object {
        /**
         * Returns a value that determines whether to display the ogtag or not.
         * true, if channel displays the content within the ogtag in the message.
         * false, otherwise.
         *
         * This method is affected by the value of [Available.isSupportOgTag].
         * It is also affected by the ogtag value set in the application.
         *
         * @param channelConfig The channel configuration
         * @return true if the ogtag is enabled, false otherwise
         * @since 3.6.0
         */
        @JvmStatic
        fun getEnableOgTag(channelConfig: ChannelConfig): Boolean {
            return Available.isSupportOgTag() && channelConfig.enableOgTag
        }

        /**
         * Returns a value that determines whether to display the reactions or not.
         * true, if channel displays the reactions in the message.
         * false, otherwise.
         *
         * This method is affected by the value of [Available.isSupportReaction].
         * It is also affected by the reactions value set in the application and channel type.
         *
         * @param channelConfig The channel configuration
         * @param channel The channel
         * @return true if the reactions is enabled, false otherwise
         * @since 3.6.0
         */
        @JvmStatic
        fun getEnableReactions(channelConfig: ChannelConfig, channel: BaseChannel): Boolean {
            return if (channel is GroupChannel) {
                if (channel.isSuper || channel.isBroadcast || channel.isChatNotification) {
                    false
                } else {
                    Available.isSupportReaction() && channelConfig.enableReactions
                }
            } else {
                false
            }
        }

        /**
         * Returns a value that determines that reactions can be sent by the channel.
         * true, if channel can send the reactions in the message.
         *
         * @param channelConfig The channel configuration
         * @param channel The channel
         * @return true if the reactions can be sent, false otherwise
         * @since 3.6.0
         */
        @JvmStatic
        fun canSendReactions(channelConfig: ChannelConfig, channel: BaseChannel): Boolean {
            val useReaction = getEnableReactions(channelConfig, channel)
            if (channel is GroupChannel) {
                val isOperator = channel.myRole == Role.OPERATOR
                val isFrozen = channel.isFrozen
                return useReaction && (isOperator || !isFrozen)
            }
            return false
        }
    }

    var enableOgTag: Boolean
        /**
         * Returns a value that determines whether to display the ogtag or not.
         * true, if channel displays the content within the ogtag in the message.
         * false, otherwise.
         *
         * Only the values set in UIKit dashboard and UIKitConfig are affected.
         *
         * @return true if the ogtag is enabled, false otherwise
         * @since 3.6.0
         */
        get() = enableOgTagMutable ?: _enableOgTag
        /**
         * Sets whether to display the ogtag or not.
         *
         * @param value true if the ogtag is enabled, false otherwise
         * @since 3.6.0
         */
        set(value) {
            enableOgTagMutable = value
        }
    var enableMention: Boolean
        /**
         * Returns a value that determines whether to use the mention or not.
         * true, if channel uses the mention in the message.
         * false, otherwise.
         *
         * @return true if the mention is enabled, false otherwise
         * @since 3.6.0
         */
        get() = enableMentionMutable ?: _enableMention
        /**
         * Sets whether to use the mention or not.
         *
         * @param value true if the mention is enabled, false otherwise
         * @since 3.6.0
         */
        set(value) {
            enableMentionMutable = value
        }
    var enableTypingIndicator: Boolean
        /**
         * Returns a value that determines whether to display the typing indicator or not.
         * true, if channel displays the typing indicator.
         * false, otherwise.
         *
         * @return true if the typing indicator is enabled, false otherwise
         * @since 3.6.0
         */
        get() = enableTypingIndicatorMutable ?: _enableTypingIndicator
        /**
         * Sets whether to display the typing indicator or not.
         *
         * @param value true if the typing indicator is enabled, false otherwise
         * @since 3.6.0
         */
        set(value) {
            enableTypingIndicatorMutable = value
        }
    var enableReactions: Boolean
        /**
         * Returns a value that determines whether to display the reactions or not.
         * true, if channel displays the reactions in the message.
         * false, otherwise.
         *
         * Only the values set in UIKit dashboard and UIKitConfig are affected.
         * If you want to get the value used when actually drawing in uikit, use [ChannelConfig.getEnableReactions(ChannelConfig, GroupChannel)].
         *
         * @return true if the reactions is enabled, false otherwise
         * @since 3.6.0
         */
        get() = enableReactionsMutable ?: _enableReactions
        /**
         * Sets whether to display the reactions or not.
         *
         * @param value true if the reactions is enabled, false otherwise
         * @since 3.6.0
         */
        set(value) {
            enableReactionsMutable = value
        }
    var enableVoiceMessage: Boolean
        /**
         * Returns a value that determines whether to use the voice message or not.
         * true, if channel displays the voice message in the message.
         * false, otherwise.
         *
         * @return true if the voice message is enabled, false otherwise
         * @since 3.6.0
         */
        get() = enableVoiceMessageMutable ?: _enableVoiceMessage
        /**
         * Sets whether to use the voice message or not.
         *
         * @param value true if the voice message is enabled, false otherwise
         * @since 3.6.0
         */
        set(value) {
            enableVoiceMessageMutable = value
        }
    var threadReplySelectType: ThreadReplySelectType
        /**
         * Returns <code>ThreadReplySelectType</code>, which determines where to go when reply is selected.
         * <code>ThreadReplySelectType</code> can be applied when the reply type is <code>ReplyType.THREAD</code>.
         *
         * @return The value of <code>ThreadReplySelectType</code>
         * @since 3.6.0
         */
        get() = threadReplySelectTypeMutable ?: _threadReplySelectType
        /**
         * Sets <code>ThreadReplySelectType</code>, which determines where to go when reply is selected.
         * <code>ThreadReplySelectType</code> can be applied when the reply type is <code>ReplyType.THREAD</code>.
         *
         * @param value The value of <code>ThreadReplySelectType</code>
         * @since 3.6.0
         */
        set(value) {
            threadReplySelectTypeMutable = value
        }
    var replyType: ReplyType
        /**
         * Returns <code>ReplyType</code>, which is how replies are displayed in the message list.
         *
         * @return The value of <code>ReplyType</code>
         * @since 3.6.0
         */
        get() = replyTypeMutable ?: _replyType
        /**
         * Sets <code>ReplyType</code>, which is how replies are displayed in the message list.
         * @param value The value of <code>ReplyType</code>
         * @since 3.6.0
         */
        set(value) {
            replyTypeMutable = value
        }

    @JvmSynthetic
    internal fun merge(config: ChannelConfig): ChannelConfig {
        this._enableOgTag = config._enableOgTag
        this._enableMention = config._enableMention
        this._enableTypingIndicator = config._enableTypingIndicator
        this._enableReactions = config._enableReactions
        this._enableVoiceMessage = config._enableVoiceMessage
        this._threadReplySelectType = config._threadReplySelectType
        this._replyType = config._replyType
        this.input.merge(config.input)
        return this
    }

    @TestOnly
    @JvmSynthetic
    internal fun clear() {
        this.enableOgTagMutable = null
        this.enableMentionMutable = null
        this.enableTypingIndicatorMutable = null
        this.enableReactionsMutable = null
        this.enableVoiceMessageMutable = null
        this.threadReplySelectTypeMutable = null
        this.replyTypeMutable = null
        this.input.clear()
    }

    @Serializable
    @Parcelize
    data class Input internal constructor(
        @SerialName(KeySet.enable_document)
        private var _enableDocument: Boolean = true,
        /**
         * Returns <code>MediaMenu</code>, which is the configuration of the camera.
         *
         * @return The value of <code>MediaMenu</code>
         * @since 3.6.0
         */
        val camera: MediaMenu = MediaMenu(),
        /**
         * Returns <code>MediaMenu</code>, which is the configuration of the gallery.
         *
         * @return The value of <code>MediaMenu</code>
         * @since 3.6.0
         */
        val gallery: MediaMenu = MediaMenu(),

        @Transient
        private var enableDocumentMutable: Boolean? = null
    ) : Parcelable {
        var enableDocument: Boolean
            /**
             * Returns a value that determines whether to use the document or not.
             *
             * @return true if the document is enabled, false otherwise
             * @since 3.6.0
             */
            get() = enableDocumentMutable ?: _enableDocument
            /**
             * Sets whether to use the document or not.
             *
             * @param value true if the document is enabled, false otherwise
             * @since 3.6.0
             */
            set(value) {
                enableDocumentMutable = value
            }

        @JvmSynthetic
        internal fun merge(config: Input): Input {
            this.camera.merge(config.camera)
            this.gallery.merge(config.gallery)
            this._enableDocument = config._enableDocument
            return this
        }

        @TestOnly
        @JvmSynthetic
        internal fun clear() {
            this.enableDocumentMutable = null
            this.camera.clear()
            this.gallery.clear()
        }
    }
}
