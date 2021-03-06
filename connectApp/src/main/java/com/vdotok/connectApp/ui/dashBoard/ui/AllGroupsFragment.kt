package com.vdotok.connectApp.ui.dashBoard.ui

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.navigation.Navigation
import com.vdotok.connect.manager.ChatManager
import com.vdotok.connect.models.*
import com.vdotok.connectApp.R
import com.vdotok.connectApp.databinding.LayoutFragmentInboxBinding
import com.vdotok.connectApp.dialogs.UpdateGroupNameDialog
import com.vdotok.connectApp.models.AllGroupsResponse
import com.vdotok.connectApp.models.DeleteGroupModel
import com.vdotok.connectApp.models.GroupModel
import com.vdotok.connectApp.network.ApiService
import com.vdotok.connectApp.network.Result
import com.vdotok.connectApp.network.RetrofitBuilder
import com.vdotok.connectApp.prefs.Prefs
import com.vdotok.connectApp.ui.account.ui.AccountActivity.Companion.createAccountsActivity
import com.vdotok.connectApp.ui.dashBoard.adapter.AllGroupsListAdapter
import com.vdotok.connectApp.ui.dashBoard.adapter.InterfaceOnGroupMenuItemClick
import com.vdotok.connectApp.ui.fragments.ChatMangerListenerFragment
import com.vdotok.connectApp.utils.ApplicationConstants
import com.vdotok.connectApp.utils.ApplicationConstants.LOGIN_INFO
import com.vdotok.connectApp.utils.safeApiCall
import com.vdotok.connectApp.utils.showDeleteGroupAlert
import com.vdotok.connect.manager.WearManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.TimeUnit


/**
 * Created By: VdoTok
 * Date & Time: On 5/11/21 At 2:15 AM in 2021
 */
class AllGroupsFragment : ChatMangerListenerFragment(), InterfaceOnGroupMenuItemClick {

    private val isSocketConnected = ObservableBoolean(false)
    private val isWatchConnected = ObservableBoolean(false)
    var username = ObservableField<String>()

    private lateinit var adapter: AllGroupsListAdapter

    val list = arrayListOf<String>()

    private var dataSet = ArrayList<GroupModel>()

    private lateinit var binding: LayoutFragmentInboxBinding
    private lateinit var prefs: Prefs

    private lateinit var cManger: ChatManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = LayoutFragmentInboxBinding.inflate(inflater, container, false)
        prefs = Prefs(activity)


        init()

        return binding.root
    }

    private fun init() {

        prefs = Prefs(activity)
        activity?.let {
            cManger = ChatManager.getInstance(it)
        }

        (activity as DashboardActivity).mListener = this

        prefs.loginInfo?.let { info ->
            binding.customToolbar.title.text = info.fullName

            binding.tvUsername.text = info.fullName
        }

        binding.isSocketConnected = isSocketConnected
        binding.isWatchConnected = isWatchConnected

        binding.tvLogout.setOnClickListener {
            if (cManger.isConnected()) {
                cManger.disconnect()
                prefs.deleteKeyValuePair(LOGIN_INFO)
                activity?.let {
                    startActivity(createAccountsActivity(it))
                }
            } else {
                (activity as DashboardActivity).reconnect()
            }
        }
        binding.customToolbar.imgBack.hide()
        binding.customToolbar.title.text = getString(R.string.chat_room)
        binding.customToolbar.createGroupBtn.setImageResource(R.drawable.ic_plus1)


        binding.customToolbar.createGroupBtn.setOnClickListener {
            openContactFragment()
        }

        binding.btnNewChat.performSingleClick {
            openContactFragment()
        }

        binding.btnRefresh.performSingleClick {
            getAllGroups()
        }

        initRecyclerView()
        addPullToRefresh()
        getAllGroups()

    }

    private fun getAllGroups() {
        activity?.let {
            binding.progressBar.toggleVisibility()
            binding.swipeRefreshLay.isRefreshing = false
            val apiService: ApiService = RetrofitBuilder.makeRetrofitService(it)
            prefs.loginInfo?.authToken.let {
                CoroutineScope(Dispatchers.IO).launch {
                    val response =
                        safeApiCall { apiService.getAllGroups(auth_token = "Bearer $it") }
                    withContext(Dispatchers.Main) {
                        try {
                            when (response) {
                                is Result.Success -> {
                                    handleGroupSuccess(response.data)
                                }
                                is Result.Error -> {
                                    if (response.error.responseCode == ApplicationConstants.HTTP_CODE_NO_NETWORK) {
                                        binding.root.showSnackBar(getString(R.string.no_network_available))
                                    } else {
                                        binding.root.showSnackBar(response.error.message)
                                    }
                                }
                            }
                        } catch (e: HttpException) {
                            Log.e(
                                ApplicationConstants.API_ERROR,
                                "signUpUser: ${e.printStackTrace()}"
                            )
                        } catch (e: Throwable) {
                            Log.e(
                                ApplicationConstants.API_ERROR,
                                "signUpUser: ${e.printStackTrace()}"
                            )
                        }
                        binding.progressBar.toggleVisibility()
                    }
                }
            }
        }
    }

    private fun deleteGroup(model: DeleteGroupModel) {
        activity?.let {
            binding.progressBar.toggleVisibility()
            val apiService: ApiService = RetrofitBuilder.makeRetrofitService(it)
            prefs.loginInfo?.authToken.let {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = safeApiCall {
                        apiService.deleteGroup(
                            auth_token = "Bearer $it",
                            model = model
                        )
                    }
                    withContext(Dispatchers.Main) {
                        try {
                            when (response) {
                                is Result.Success -> {
                                    if (response.data.status == ApplicationConstants.SUCCESS_CODE) {
                                        getAllGroups()
                                        binding.root.showSnackBar(getString(R.string.group_deleted))
                                    } else {
                                        binding.root.showSnackBar(response.data.message)
                                    }
                                }
                                is Result.Error -> {
                                    if (response.error.responseCode == ApplicationConstants.HTTP_CODE_NO_NETWORK) {
                                        binding.root.showSnackBar(getString(R.string.no_network_available))
                                    } else {
                                        binding.root.showSnackBar(response.error.message)
                                    }
                                }
                            }
                        } catch (e: HttpException) {
                            Log.e(
                                ApplicationConstants.API_ERROR,
                                "signUpUser: ${e.printStackTrace()}"
                            )
                        } catch (e: Throwable) {
                            Log.e(
                                ApplicationConstants.API_ERROR,
                                "signUpUser: ${e.printStackTrace()}"
                            )
                        }
                        binding.progressBar.toggleVisibility()
                        binding.swipeRefreshLay.isRefreshing = false
                    }
                }
            }
        }
    }

    fun publishCustomPacketMessage(classObject: Any, key: String, toGroup: String) {
        cManger.publishPacketMessage(classObject, key, toGroup)
    }

    private fun addPullToRefresh() {
        binding.swipeRefreshLay.isEnabled = true
        binding.swipeRefreshLay.setOnRefreshListener {
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            getAllGroups()
        }
    }

    private fun handleGroupSuccess(response: AllGroupsResponse) {
        if (response.groups.isEmpty()) {
            binding.groupChatListing.show()
        } else {
            binding.groupChatListing.hide()
            prefs.saveUpdateGroupList(response.groups)
            adapter.updateData(response.groups)
            setGroupMapData(response.groups)
            dataSet.clear()
            dataSet.addAll(response.groups)
            doSubscribe()
        }
    }

    /**
     * Function to persist local chat till the user is connected to the socket
     * @param groupList list of all the groups user is connected to
     * */
    fun setGroupMapData(groupList: ArrayList<GroupModel>) {
        if (groupList.isNotEmpty()) {
            groupList.forEach { groupModel ->
                if (!(activity as DashboardActivity).mapGroupMessages.containsKey(groupModel.channelName)) {
                    (activity as DashboardActivity).mapGroupMessages[groupModel.channelName] =
                        arrayListOf()
                }
            }
        }
    }


    private fun initRecyclerView() {
        activity?.applicationContext?.let {
            adapter = prefs.loginInfo?.fullName?.let { name ->
                AllGroupsListAdapter(
                    it,
                    prefs,
                    name,
                    ArrayList(),
                    this,
                    { groupModel: GroupModel -> getUnreadCount(groupModel) },
                    { groupModel: GroupModel -> getMessageList(groupModel) }) { groupModel: GroupModel ->
                    openChatFragment(
                        groupModel
                    )
                }
            }!!
            binding.rcvUserList.adapter = adapter
            adapter.updatePresenceData((activity as DashboardActivity).getPresenceList())
        }
    }


    /** function to handle sending acknowledgment message to the group that the message is received and seen
     * @param myMessage MqttMessage object containing details sent for the acknowledgment in group
     * */
    private fun sendAcknowledgeMsgToGroup(myMessage: Message) {

        prefs.loginInfo?.refId?.let {
            myMessage.status = ReceiptType.DELIVERED.value
            if (myMessage.from != it) {
                val receipt = ReadReceiptModel(
                    ReceiptType.DELIVERED.value,
                    myMessage.key,
                    System.currentTimeMillis(),
                    myMessage.id,
                    it,
                    myMessage.to
                )

                cManger.publishPacketMessage(receipt, receipt.key, receipt.to)
            }
        }
    }


    private fun openChatFragment(groupModel: GroupModel) {
        val bundle = Bundle()
        (activity as DashboardActivity).mapUnreadCount.remove(groupModel.channelName)
        bundle.putParcelable(GroupModel.TAG, groupModel)
        Navigation.findNavController(binding.root).navigate(R.id.action_open_chat_fragment, bundle)
    }

    private fun openContactFragment() {
        Navigation.findNavController(binding.root).navigate(R.id.action_open_contact_fragment)
    }

    private fun connectionLost(){
        activity?.runOnUiThread {
            isSocketConnected.set(false)
        }
    }

    override fun onConnectionSuccess() {
        activity?.runOnUiThread {
            isSocketConnected.set(true)
            isWatchConnected.set(WearManager.getInstance(requireActivity()).isWatchConnected())
        }
        doSubscribe()
    }

    override fun onConnectionError() {
        connectionLost()
    }
    override fun onConnectionLost() {
        connectionLost()
    }
    override fun onConnectionFailed() {
        connectionLost()
    }

    override fun onTopicSubscribe(topic: String) {
//        for (group in dataSet) {
//            if (group.channelName == topic)
//                handleSubscribedGroupData(group)
//        }
    }

    override fun onNewMessage(message: Message) {
        if ((activity as DashboardActivity).mapUnreadCount.containsKey(message.to)) {
            val count = (activity as DashboardActivity).mapUnreadCount[message.to]
            (activity as DashboardActivity).mapUnreadCount[message.to] = count?.plus(1) ?: 0
            adapter.notifyDataSetChanged()

        } else {
            (activity as DashboardActivity).mapUnreadCount[message.to] = 1
            adapter.notifyDataSetChanged()
        }
        sendAcknowledgeMsgToGroup(message)
    }

    private fun getUnreadCount(groupModel: GroupModel): Int {
        return (activity as DashboardActivity).mapUnreadCount[groupModel.channelName]
            ?: return 0
    }

    private fun getMessageList(groupModel: GroupModel): ArrayList<Message> {
        return (activity as DashboardActivity).mapLastMessage[groupModel.channelName]
            ?: return ArrayList()
    }

    override fun onPresence(message: ArrayList<Presence>) {
        saveAndUpdatePresenceList(message)
    }


    private fun saveAndUpdatePresenceList(presenceList: ArrayList<Presence>) {
//        prefs.saveUpdatePresenceList(presenceList)
        activity?.runOnUiThread {
            adapter.updatePresenceData((activity as DashboardActivity).getPresenceList())
            adapter.notifyItemRangeChanged(0, dataSet.size)
        }
    }

//    override fun onTypingMessage(message: Message) {
//        Log.d("", "")
//    }
//
//    override fun onReceiptReceived(model: ReadReceiptModel) {
//        //TODO("Not yet implemented")
//    }
//
//    override fun onBytesArrayReceived(payload: ByteArray?) {
//        //TODO("Not yet implemented")
//    }
//
//    override fun onFileReceivedCompleted(
//        headerModel: HeaderModel,
//        byteArray: ByteArray,
//        msgId: String
//    ) {
//        //TODO("Not yet implemented")
//    }
//
//    override fun onChunkReceived(fileModel: FileModel) {
//        //TODO("Not yet implemented")
//    }


    private fun doSubscribe() {
        for (group in dataSet) {
            cManger.subscribeTopic(group.channelKey, group.channelName)
        }
    }

    private fun doUnSubscribe() {
        for (group in dataSet) {
            cManger.unSubcribeTopic(group.channelKey, group.channelName)
            Toast.makeText(activity, "t", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dialogdeleteGroup(groupId: Int) {
        showDeleteGroupAlert(this.activity, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                val model = DeleteGroupModel()
                model.groupId = groupId
                deleteGroup(model)

            }
        })
    }


    override fun onEditClcik(groupModel: GroupModel) {
        activity?.supportFragmentManager?.let {
            UpdateGroupNameDialog(
                groupModel,
                this::getAllGroups
            ).show(it, UpdateGroupNameDialog.UPDATE_GROUP_TAG)
        }

    }

    override fun onDeleteClcik(position: Int) {
        dialogdeleteGroup(position)
    }

    override fun onBluetoothStateChanged(turnedOff: Boolean) {
        activity?.runOnUiThread {

            when {
                turnedOff -> isWatchConnected.set(false)
                else -> {

                    Handler(Looper.getMainLooper()).postDelayed({
                        isWatchConnected.set(WearManager.getInstance(requireActivity()).isWatchConnected())
                    }, TimeUnit.SECONDS.toMillis(5))
                }
            }
        }
    }

    companion object {
        const val PRESENCE_KEY = "PRESENCE_KEY"
    }


}
