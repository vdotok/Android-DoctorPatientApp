package com.vdotok.connect.ui.dashBoard.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.vdotok.connectSdk.manager.ChatManager
import com.vdotok.connectSdk.manager.ChatManagerCallback
import com.vdotok.connectSdk.models.*
import com.vdotok.connect.R
import com.vdotok.connect.databinding.ActivityDashboardBinding
import com.vdotok.connect.extensions.showSnackBar
import com.vdotok.connect.interfaces.FragmentRefreshListener
import com.vdotok.connect.models.GroupModel
import com.vdotok.connect.prefs.Prefs
import java.util.*
import kotlin.collections.ArrayList

class DashboardActivity : AppCompatActivity(), ChatManagerCallback{

    private val MY_PERMISSIONS_REQUEST_CAMERA = 100
    private val MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 101
    private val MY_PERMISSIONS_REQUEST_READ_STORAGE = 102

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var prefs: Prefs

    private var chatManger: ChatManager? = null
    var mListener: FragmentRefreshListener? = null

    // To save messages locally per session
    var mapGroupMessages: MutableMap<String, ArrayList<Message>> = mutableMapOf()
    var mapUnreadCount: MutableMap<String, Int> = mutableMapOf()
    var mapLastMessage: MutableMap<String, ArrayList<Message>> = mutableMapOf()
    var savedPresenceList: ArrayList<Presence> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askForPermissions()
        init()

    }

    private fun askForPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                MY_PERMISSIONS_REQUEST_CAMERA
            )
        }
    }


    private fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard)
        prefs = Prefs(this)
        prefs.clearPresenceData()

        //Initialize ChatManager for using ChatSDK methods
        initChatManager()

    }

    private fun initChatManager() {
        chatManger = ChatManager.getInstance(this)
        chatManger?.listener = this

        prefs.mConnection?.let {
            it.interval = 5
            it.reConnectivity = true
            chatManger?.connect(it)
        }
    }

    fun reconnect() {
        prefs.mConnection?.let {
            chatManger?.reConnect(it)
        }

    }

    fun publishCustomPacketMessage(classObject: Any, key: String, toGroup: String) {
        chatManger?.publishPacketMessage(classObject, key, toGroup)
    }

    /**
     * Function to send acknowledgement that file is fully received
     * @param message is the Message Object that we will be sending to the server
     * */
    fun sendAcknowledgmentOnMediaMessage(message: Message) {
        chatManger?.sendAcknowledgmentMessageReceived(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        chatManger?.disconnect()
    }

    companion object {

        fun createDashboardActivity(context: Context) = Intent(
            context,
            DashboardActivity::class.java
        ).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        }
    }


    fun saveUpdatePresenceList(list: ArrayList<Presence>) {

        list.forEach {
            addUniqueElements(it)
        }
    }

    private fun addUniqueElements(mPresence: Presence) {

        var isUpdated = false

        savedPresenceList.forEachIndexed { index, presence ->
            if (presence.username == mPresence.username) {
                savedPresenceList[index] = mPresence
                isUpdated = true
            }
        }

        if (isUpdated.not()) {
            savedPresenceList.add(mPresence)
        }

    }

    fun getPresenceList(): ArrayList<Presence> {
        return savedPresenceList
    }


    /**
     * Function to help in persisting local chat by updating local data till the user is connected to the socket
     * @param message message object we will be sending to the server
     * */
    private fun updateMessageMapData(message: Message) {
        if (mapGroupMessages.containsKey(message.to)) {
            val messageValue: ArrayList<Message> =
                mapGroupMessages[message.to] as ArrayList<Message>
            messageValue.add(message)
            mapGroupMessages[message.to] = messageValue
            mapLastMessage[message.to] = messageValue

        } else {
            val messageValue: ArrayList<Message> = ArrayList()
            messageValue.add(message)
            mapGroupMessages[message.to] = messageValue
            mapLastMessage[message.to] = messageValue


        }
    }


    /**
     * Handle group presence after group is subscribed
     * @param group GroupModel consisting group related details
     * */
    private fun handleSubscribedGroupData(group: GroupModel) {
        if (checkIfGroupIsSubscribed(group).not()) {
//            publishPresence(group)
//            savedSubscribedGroupList.add(group)
        }
    }

    /**
     * This function checks that whether the presence is published or not against local groups data storage
     * @param group GroupModel consisting group related details for subscription
     * */
    var savedSubscribedGroupList: ArrayList<GroupModel> = ArrayList()
    private fun checkIfGroupIsSubscribed(group: GroupModel): Boolean {
        savedSubscribedGroupList.let { item ->
            item.forEach {
                if (it.id == group.id) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Function to inform server that user is online hence providing online status to other users this user is connected to
     * @param groupModel is the group in which user will show online status
     * */
//    private fun publishPresence(groupModel: GroupModel) {
//        chatManger?.publishPresence(groupModel.channelKey, groupModel.channelName)
//    }


    /**
     * Callback method when socket server is connected successfully
     * */
    //Callbacks
    override fun onConnect() {
        Log.e("Connection Status", "Connected")
        binding.root.showSnackBar(R.string.sdk_connected)
        mListener?.onConnectionSuccess()
        //doSubscribe()
    }

    /**
     * Callback method when socket server failed to connect
     * @param cause is the error received from the server upon failed connection
     * */
    override fun onConnectionFailed(cause: Throwable) {
//        chatManger = null
        Log.e("Connection Status", cause.toString())
        mListener?.onConnectionFailed()
    }

    override fun onConnectionLost(cause: Throwable) {
//        chatManger = null
        Log.e("Connection Status", cause.toString())
        mListener?.onConnectionLost()
    }

    override fun onFileSendingStarted(fileHeaderId: String, fileType: Int) {

    }

    override fun onFileSendingComplete(fileHeaderId: String, fileType: Int) {
        mListener?.onFileSendingComplete()
    }

    override fun onFileSendingProgressChanged(fileHeaderId: String, progress: Int) {
        Log.d("file progress", progress.toString())
    }

    override fun onFileReceivingStarted(fileHeaderId: String) {

    }


    override fun onFileReceivingProgressChanged(fileHeaderId: String, progress: Int) {
        Log.d("file progress", progress.toString())
    }

    /**
     * Callback method when user is successfully subscribed to the server
     * @param topic is the value to differentiate groups and chats and perform connections later on
     * */
    override fun onSubscribe(topic: String) {
        Log.i("subscription Status", "successfully subscribed")
        mListener?.onTopicSubscribe(topic)

        prefs.getGroupList()?.let {
            for (group in it) {
                if (group.channelName == topic)
                    handleSubscribedGroupData(group)
            }
        }
    }

    /**
     * Callback method when user is not subscribed to the server
     * @param topic is the value to differentiate groups and chats and perform connections later on
     * @param cause is the error received from the server upon failed connection
     * */
    override fun onSubscribeFailed(topic: String, cause: Throwable?) {
        Log.e("subscription Failed", cause.toString())
    }

    /**
     * Callback method called when a presence of other user is received
     * @param who list of people who are showing presence i.e. online status
     * */
    override fun onPresenceReceived(who: ArrayList<Presence>) {
        runOnUiThread {
            saveUpdatePresenceList(who)
        }
        mListener?.onPresence(who)
    }

    /**
     * Callback method when other user has seen the message
     * @param model is the object received and will be used to view acknowledgement for a message
     * */
    override fun onReceiptReceived(model: ReadReceiptModel) {
        mListener?.onReceiptReceived(model)
    }

    /**
     * Callback method when user receives a message
     * @param myMessage message object we will be sending to the server
     * */
    override fun onMessageArrived(myMessage: Message) {
        mListener?.onNewMessage(myMessage)
        updateMessageMapData(myMessage)
    }

    /**
     * Callback method when user receives a typing message like "someone is typing"
     * @param myMessage message object we will be sending to the server
     * */
    override fun onTypingMessage(myMessage: Message) {
        mListener?.onTypingMessage(myMessage)
    }

    /**
     * Callback method when a ByteArray type message is received
     * @param payload is thee ByteArray formatted message object received directly
     * */
    override fun onBytesReceived(payload: ByteArray) {
        mListener?.onBytesArrayReceived(payload)
    }


    override fun onFileReceivingCompleted(
        headerModel: HeaderModel,
        byteArray: ByteArray,
        msgId: String
    ) {
        mListener?.onFileReceivedCompleted(headerModel, byteArray, msgId)
    }

    override fun onFileSendingFailed(headerId: String) {
        mListener?.onFileSendingFailed(headerId)
    }

    override fun onFileReceivingFailed() {
        Log.d("File Status", "onFileReceivingFailed")
    }

    /** callback fired if there is a fluctuation in network i.e. it is disconnected and reconnected
     * so to inform that the socket successfully reconnected again
     * @param connectionState Boolean informing the the socket is reconnected or not
     * */
    override fun reconnectAction(connectionState: Boolean) {
        if (connectionState) mListener?.onConnectionSuccess()
    }

    /** callback fired to inform socket is connected or not before sending messages **/
    override fun connectionError() {
        Log.d("Connection Status", "connection error")
        mListener?.onConnectionError()
    }

}