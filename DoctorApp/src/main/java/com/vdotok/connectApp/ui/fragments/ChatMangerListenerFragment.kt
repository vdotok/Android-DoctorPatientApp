package com.vdotok.connectApp.ui.fragments

import androidx.fragment.app.Fragment
import com.vdotok.connect.models.*
import com.vdotok.connectApp.interfaces.FragmentRefreshListener
import com.vdotok.connectApp.ui.dashBoard.ui.DashboardActivity


/**
 * Created By: VdoTok
 * Date & Time: On 5/26/21 At 3:21 PM in 2021
 */
open class ChatMangerListenerFragment: Fragment(), FragmentRefreshListener {

//    var mListener: FragmentRefreshListener? = null

    override fun onStart() {
        super.onStart()
        (activity as DashboardActivity).mListener = this
    }

    override fun onConnectionSuccess() {}

    override fun onConnectionFailed() {}

    override fun onConnectionLost() {}

    override fun onConnectionError() {}

    override fun onTopicSubscribe(topic: String) {}

    override fun onNewMessage(message: Message) {}

    override fun onPresence(message: ArrayList<Presence>) {}

    override fun onTypingMessage(message: Message) {}

    override fun onReceiptReceived(model: ReadReceiptModel) {}

    override fun onBytesArrayReceived(payload: ByteArray?) {}

    override fun onFileReceivedCompleted(
        headerModel: HeaderModel,
        byteArray: ByteArray,
        msgId: String
    ) {}

    override fun onChunkReceived(fileModel: FileModel) {}

    override fun onFileSendingComplete() {}

    override fun onFileSendingFailed(headerId: String) {}

    override fun onSensorDataReceived(data: String) {}
}