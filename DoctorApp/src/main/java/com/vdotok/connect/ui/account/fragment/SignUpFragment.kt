package com.vdotok.connect.ui.account.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.vdotok.connect.R
import com.vdotok.connect.databinding.LayoutFragmentSignupBinding
import com.vdotok.connect.extensions.*
import com.vdotok.connect.models.CheckUserModel
import com.vdotok.connect.models.LoginResponse
import com.vdotok.connect.models.SignUpModel
import com.vdotok.connect.network.HttpResponseCodes
import com.vdotok.connect.network.RetrofitBuilder
import com.vdotok.connect.network.Result
import com.vdotok.connect.prefs.Prefs
import com.vdotok.connect.ui.dashBoard.ui.DashboardActivity
import com.vdotok.connect.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response


/**
 * Created By: VdoTok
 * Date & Time: On 5/3/21 At 1:26 PM in 2021
 */
class SignUpFragment: Fragment() {

    private lateinit var binding: LayoutFragmentSignupBinding
    var email : ObservableField<String> = ObservableField<String>()
    var fullName : ObservableField<String> = ObservableField<String>()
    var password : ObservableField<String> = ObservableField<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = LayoutFragmentSignupBinding.inflate(inflater, container, false)

        binding.userEmail = email
        binding.fullName = fullName
        binding.password = password

        init()

        return binding.root
    }

    private fun init() {
      //  binding.customToolbar.title.text = getString(R.string.register_user)

        binding.btnSignUp.setOnClickListener {
            if (it.checkUserName(fullName.get().toString()) && it.checkPassword(password.get().toString()) && it.checkEmail(email.get().toString())) {
                checkUserEmail(email.get().toString())
                binding.btnSignUp.disable()
            }
        }

       binding.tvSignIn.setOnClickListener {
           moveToLogin(it)
        }

        configureBackPress()
    }

    private fun checkUserEmail(email: String) {
        activity?.let {
            binding.progressBar.toggleVisibility()
            val service = RetrofitBuilder.makeRetrofitService(it)
            CoroutineScope(Dispatchers.IO).launch {
                val response = safeApiCall { service.checkEmail (CheckUserModel(email)) }
                withContext(Dispatchers.Main) {
                    binding.btnSignUp.enable()
                    try {
                        when (response) {
                            is Result.Success -> {
                                handleCheckfullNameResponse(response.data)
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
                        Log.e(API_ERROR, "signUpUser: ${e.printStackTrace()}")
                    } catch (e: Throwable) {
                        Log.e(API_ERROR, "signUpUser: ${e.printStackTrace()}")
                    }
                    binding.progressBar.toggleVisibility()
                }
            }
        }
    }

    private fun handleCheckfullNameResponse(response: LoginResponse) {
        when(response.status) {
            HttpResponseCodes.SUCCESS.value -> {
                signUp()
            }
            else -> {
                binding.root.showSnackBar(response.message)
            }
        }
    }


    private fun signUp() {
        binding.btnSignUp.disable()
        activity?.let {
            binding.progressBar.toggleVisibility()
            val service = RetrofitBuilder.makeRetrofitService(it)
            CoroutineScope(Dispatchers.IO).launch {
                val response = service.signUp(SignUpModel(fullName.get().toString(), email.get().toString(),
                    password.get().toString()))

                withContext(Dispatchers.Main) {
                    binding.btnSignUp.enable()
                    try {
                        if (response.isSuccessful) {
                            handleSignUpResponse(response)
                        } else {
                            binding.root.showSnackBar(response.message())
                        }
                    } catch (e: HttpException) {
                        Log.e(API_ERROR, "signUpUser: ${e.printStackTrace()}")
                    } catch (e: Throwable) {
                        Log.e(API_ERROR, "signUpUser: ${e.printStackTrace()}")
                    }
                    binding.progressBar.toggleVisibility()
                }
            }
        }
    }

    private fun handleSignUpResponse(response: Response<LoginResponse>) {
        when(response.body()?.status) {
            HttpResponseCodes.SUCCESS.value -> {
                binding.root.showSnackBar(resources.getString(R.string.account_created_success))
                saveResponseToPrefs(Prefs(activity), response.body())
                startActivity(activity?.applicationContext?.let { DashboardActivity.createDashboardActivity(it) })
            }
            else -> {
                binding.root.showSnackBar(response.body()?.message)
            }
        }
    }

    private fun moveToLogin(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_move_to_login_user)
    }

    private fun configureBackPress() {
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    moveToLogin(binding.root)
                }
            })
    }

    companion object {
        const val API_ERROR = "API_ERROR"
    }
}