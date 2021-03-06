package com.vdotok.connectApp.ui.dashBoard.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.vdotok.connectApp.R
import com.vdotok.connectApp.databinding.LayoutSelectContactBinding
import com.vdotok.connectApp.extensions.*
import com.vdotok.connectApp.models.CreateGroupModel
import com.vdotok.connectApp.models.GetAllUsersResponseModel
import com.vdotok.connectApp.models.UserModel
import com.vdotok.connectApp.models.*
import com.vdotok.connectApp.network.ApiService
import com.vdotok.connectApp.network.Result
import com.vdotok.connectApp.network.RetrofitBuilder
import com.vdotok.connectApp.prefs.Prefs
import com.vdotok.connectApp.ui.dashBoard.adapter.OnChatItemClickCallbackListner
import com.vdotok.connectApp.ui.dashBoard.adapter.SelectUserContactAdapter
import com.vdotok.connectApp.utils.ApplicationConstants
import com.vdotok.connectApp.utils.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class SelectContactFragment: Fragment(), OnChatItemClickCallbackListner {

    lateinit var adapter: SelectUserContactAdapter
    private lateinit var binding: LayoutSelectContactBinding
    private lateinit var prefs: Prefs
    var title : String? = null

    var edtSearch = ObservableField<String>()
    private var userList = ArrayList<UserModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = LayoutSelectContactBinding.inflate(inflater, container, false)
        prefs = Prefs(activity)

        init()
        textListenerForSearch()
        getAllUsers()

        return binding.root
    }

    private fun init() {
        initUserListAdapter()

        binding.search = edtSearch

       binding.customToolbar.title.text = getString(R.string.new_chat)
       binding.customToolbar.createGroupBtn.hide()

       binding.tvGroupChat.setOnClickListener {
           activity?.hideKeyboard()
           openAllUserListFragment()
           edtSearch.set("")
       }

        binding.customToolbar.imgBack.setOnClickListener {
            activity?.hideKeyboard()
            activity?.onBackPressed()
        }

    }

    private fun initUserListAdapter() {
        adapter = SelectUserContactAdapter(ArrayList(),this)
        binding.rcvUserList.adapter = adapter
    }

    private fun onCreateGroupClick() {
        val selectedUsersList: List<UserModel> = adapter.getSelectedUsers()
        getGroupTitle(selectedUsersList).let {
            if (it != null) {
                createGroup(it)
            }
        }

    }

    /**
     * Function for creating a group
     * */
    private fun createGroup(title: String) {
        val selectedUsersList: List<UserModel> = adapter.getSelectedUsers()

        if(selectedUsersList.isNotEmpty()){

            val model = CreateGroupModel()
            model.groupTitle = title
            //model.auto_created -> set auto created group, set 1 for only single user, 0 for multiple users
            model.pariticpants = getParticipantsIds(selectedUsersList)

            when (selectedUsersList.size) {
                1 -> model.autoCreated = 1
                else -> model.autoCreated = 0
            }

            createGroupApiCall(model)
        }
    }

    private fun getGroupTitle(selectedUsersList: List<UserModel>): String? {

        var title = prefs.loginInfo?.fullName.plus("-")
        //In this case, we have only one item in list
        selectedUsersList.forEach {
            title = title.plus(it.userName.toString())
        }
        return title
    }

    private fun getAllUsers() {
        activity?.let {
            binding.progressBar.toggleVisibility()
            val apiService: ApiService = RetrofitBuilder.makeRetrofitService(it)
            prefs.loginInfo?.authToken.let {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = safeApiCall { apiService.getAllUsers (auth_token = "Bearer $it") }
                    withContext(Dispatchers.Main) {
                        try {
                            when (response) {
                                is Result.Success -> {
                                    populateDataToList(response.data)
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
                            Log.e(AllUserListFragment.API_ERROR, "allUser: ${e.printStackTrace()}")
                        } catch (e: Throwable) {
                            Log.e(AllUserListFragment.API_ERROR, "allUser: ${e.printStackTrace()}")
                        }
                        binding.progressBar.toggleVisibility()
                    }
                }
            }
        }

    }

    private fun createGroupApiCall(model: CreateGroupModel) {
        binding.progressBar.toggleVisibility()
        val apiService: ApiService =
                RetrofitBuilder.makeRetrofitService(activity?.applicationContext!!)
        prefs.loginInfo?.authToken.let {
            CoroutineScope(Dispatchers.IO).launch {
                val response = safeApiCall { apiService.createGroup (auth_token = "Bearer $it", model) }
                withContext(Dispatchers.Main) {
                    try {
                        when (response) {
                            is Result.Success -> {
                                Snackbar.make(
                                        binding.root,
                                        R.string.group_created,
                                        Snackbar.LENGTH_LONG
                                ).show()
                                handleCreateGroupSuccess(response.data)
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
                        Log.e(AllUserListFragment.API_ERROR, "allUser: ${e.printStackTrace()}")
                    } catch (e: Throwable) {
                        Log.e(AllUserListFragment.API_ERROR, "allUser: ${e.printStackTrace()}")
                    }
                    binding.progressBar.toggleVisibility()
                }
            }
        }
    }


    private fun handleCreateGroupSuccess(response: CreateGroupResponse) {
        activity?.hideKeyboard()
        openChatFragment(response.groupModel)
    }

    /**
     * Function for setting participants ids
     * @param selectedUsersList list of selected users to form a group with
     * @return Returns an ArrayList<Int> of selected user ids
     * */
    private fun getParticipantsIds(selectedUsersList: List<UserModel>): ArrayList<Int> {
        val list: ArrayList<Int> = ArrayList()
        selectedUsersList.forEach { userModel ->
            userModel.id?.let { list.add(it.toInt()) }
        }
        return list
    }

    private fun textListenerForSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                adapter.filter?.filter(s)
            }
        })
    }

    private fun populateDataToList(response: GetAllUsersResponseModel) {
        adapter.updateData(response.users)
    }

    private fun openAllUserListFragment() {
        Navigation.findNavController(binding.root).navigate(R.id.action_open_all_users_list_fragment)
    }

    private fun openChatFragment(model: GroupModel?) {
        val bundle = Bundle()
        bundle.putParcelable(GroupModel.TAG, model)
        Navigation.findNavController(binding.root).navigate(R.id.action_open_chat_fragment, bundle)
    }

    override fun onItemClick(position: Int) {
        val item = adapter.dataList[position]
        item.isSelected = item.isSelected.not()
        adapter.notifyItemChanged(position)
        onCreateGroupClick()
    }

    override fun searchResult(position: Int) {
        edtSearch.get()?.isNotEmpty()?.let {
            if (position == 0 && it){
                binding.check.show()
                binding.rcvUserList.hide()
            }else{
                binding.check.hide()
                binding.rcvUserList.show()
            }
        }
    }

    companion object {

        const val TAG_FRAGMENT_SELECT_USER = "TAG_FRAGMENT_SELECT_USER"
        const val API_ERROR = "API_ERROR"

        @JvmStatic
        fun newInstance() = SelectContactFragment()

    }
}



