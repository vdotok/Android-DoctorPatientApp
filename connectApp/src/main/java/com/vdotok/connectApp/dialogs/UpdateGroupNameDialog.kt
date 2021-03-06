package com.vdotok.connectApp.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.ObservableField
import androidx.fragment.app.DialogFragment
import com.vdotok.connectApp.R
import com.vdotok.connectApp.databinding.UpdateGroupNameBinding
import com.vdotok.connectApp.extensions.*
import com.vdotok.connectApp.models.GroupModel
import com.vdotok.connectApp.models.UpdateGroupNameModel
import com.vdotok.connectApp.network.ApiService
import com.vdotok.connectApp.network.Result
import com.vdotok.connectApp.network.RetrofitBuilder
import com.vdotok.connectApp.prefs.Prefs
import com.vdotok.connectApp.utils.ApplicationConstants
import com.vdotok.connectApp.utils.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class UpdateGroupNameDialog(private val groupModel: GroupModel, private val updateGroup : () -> Unit) : DialogFragment(){

    private lateinit var binding: UpdateGroupNameBinding
    private lateinit var prefs: Prefs
    var edtGroupName = ObservableField<String>()


    init {
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        prefs = Prefs(activity)

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        binding = UpdateGroupNameBinding.inflate(inflater, container, false)
        binding.groupName = edtGroupName


        binding.imgClose.setOnClickListener {
            dismiss()
        }
        edtGroupName.set(groupModel.groupTitle)



        binding.btnDone.setOnClickListener {
            edtGroupName.get()?.isNotEmpty()?.let {groupName ->
                if (groupName) {
                    val model = UpdateGroupNameModel()
                    model.groupId = groupModel.id
                    model.groupTitle = edtGroupName.get()
                    editGroup(model)
                    dismiss()
                    updateGroup.invoke()
                } else {
                    binding.root.showSnackBar(R.string.group_name_empty)
                }
            }
        }

        return binding.root
    }

    private fun editGroup(model: UpdateGroupNameModel) {
        activity?.let {
            binding.progressBar.toggleVisibility()
            val apiService: ApiService = RetrofitBuilder.makeRetrofitService(it)
            prefs.loginInfo?.authToken.let {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = safeApiCall { apiService.updateGroupName (auth_token = "Bearer $it",model = model) }
                    withContext(Dispatchers.Main) {
                        try {
                            when (response) {
                                is Result.Success -> {
                                    binding.root.showSnackBar(getString(R.string.group_deleted))
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
                            Log.e(ApplicationConstants.API_ERROR, "signUpUser: ${e.printStackTrace()}")
                        } catch (e: Throwable) {
                            Log.e(ApplicationConstants.API_ERROR, "signUpUser: ${e.printStackTrace()}")
                        }
                        binding.progressBar.toggleVisibility()
                    }
                }
            }
        }
    }

    companion object{
        const val UPDATE_GROUP_TAG = "UPDATE_GROUP_DIALOG"
    }

}
