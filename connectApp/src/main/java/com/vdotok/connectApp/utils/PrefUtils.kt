package com.vdotok.connectApp.utils

import com.vdotok.connect.models.Connection
import com.vdotok.connectApp.models.LoginResponse
import com.vdotok.connectApp.prefs.Prefs

fun saveResponseToPrefs(prefs: Prefs, response: LoginResponse?) {
    prefs.loginInfo = response

    response?.let {

        val connection = response.messagingServer?.let { msgServerUrl ->
            response.refId?.let { refId ->
                response.authorizationToken?.let { token ->
                    Connection(
                            refId,
                            token,
                            msgServerUrl.host,
                            msgServerUrl.port,
                            true,
                            refId,
                            5,
                            true
                    )
                }
            }
        }
        prefs.mConnection = connection
    }

}
//tcp://ssl://vte3.vdotok.com:443:443