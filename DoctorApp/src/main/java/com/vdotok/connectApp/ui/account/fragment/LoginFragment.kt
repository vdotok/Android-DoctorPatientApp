package com.vdotok.connectApp.ui.account.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.vdotok.connectApp.R
import com.vdotok.connectApp.databinding.LayoutFragmentLoginBinding
import com.vdotok.connectApp.extensions.*
import com.vdotok.connectApp.models.LoginResponse
import com.vdotok.connectApp.models.LoginUserModel
import com.vdotok.connectApp.network.HttpResponseCodes
import com.vdotok.connectApp.network.Result
import com.vdotok.connectApp.network.RetrofitBuilder
import com.vdotok.connectApp.prefs.Prefs
import com.vdotok.connectApp.ui.dashBoard.ui.DashboardActivity.Companion.createDashboardActivity
import com.vdotok.connectApp.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.TimeUnit


/**
 * Created By: VdoTok
 * Date & Time: On 5/3/21 At 1:26 PM in 2021
 */
class LoginFragment: Fragment() {

    private lateinit var binding: LayoutFragmentLoginBinding
    var email : ObservableField<String> = ObservableField<String>("")
    var password : ObservableField<String> = ObservableField<String>("")
    private lateinit var prefs: Prefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = LayoutFragmentLoginBinding.inflate(inflater, container, false)

        binding.userEmail = email
        binding.password = password

        init()

        return binding.root
    }

    private fun init() {

        prefs = Prefs(activity)

        binding.signInBtn.setOnClickListener {
            if (it.checkPassword(password.get().toString()) && it.checkValidation(email.get().toString())) {
                loginUser(email.get().toString(), password.get().toString())
                binding.signInBtn.disable()
            }
        }
        binding.signUpBtn.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_move_to_signup_user)
        }

    }

    private fun loginUser(email: String, password: String) {
        activity?.let {
            binding.progressBar.toggleVisibility()
            val service = RetrofitBuilder.makeRetrofitService(it)
            CoroutineScope(Dispatchers.IO).launch {
                val response = safeApiCall {service.loginUser(LoginUserModel(email,password))}
                        withContext(Dispatchers.Main) {
                            binding.signInBtn.enable()
                    try {
                        when (response) {
                            is Result.Success -> {
                                handleLoginResponse(response.data)

                            }
                            is Result.Error -> {
                                if (response.error.responseCode == ApplicationConstants.HTTP_CODE_NO_NETWORK) {
                                    binding.root.showSnackBar(getString(R.string.no_network_available))
                                    Handler(Looper.getMainLooper()).postDelayed({
                                    }, TimeUnit.SECONDS.toMillis(2))

                                } else {
                                    binding.root.showSnackBar(response.error.message)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                    }, TimeUnit.SECONDS.toMillis(2))
                                }

                            }

                        }
                    } catch (e: HttpException) {
                        Log.e(API_ERROR, "loginUser: ${e.printStackTrace()}")
                    } catch (e: Throwable) {
                        Log.e(API_ERROR, "loginUser: ${e.printStackTrace()}")
                    }
                    binding.progressBar.toggleVisibility()
                }
            }
        }
    }

    private fun handleLoginResponse(response: LoginResponse) {
        when(response.status) {
            HttpResponseCodes.SUCCESS.value -> {
                saveResponseToPrefs(prefs, response)
                startActivity(activity?.applicationContext?.let { createDashboardActivity(it) })
            }
            else -> {
                binding.root.showSnackBar(response.message)
            }
        }
    }



    companion object {

        const val API_ERROR = "API_ERROR"

    }
}