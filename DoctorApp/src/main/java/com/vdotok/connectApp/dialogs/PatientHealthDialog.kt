package com.vdotok.connectApp.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.vdotok.connectApp.databinding.PatientDialogueBinding
import com.vdotok.connect.models.SensorType

class PatientHealthDialog(private val fetchSensor : (sensorType: SensorType) -> Unit) : DialogFragment(){

    private lateinit var binding: PatientDialogueBinding

    init {
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        binding = PatientDialogueBinding.inflate(inflater, container, false)

        setListeners()

        return binding.root
    }

    private fun setListeners(){
        binding.imgClose.setOnClickListener {
            dismiss()
        }
        binding.imgHeart.performSingleClick {
            fetchSensor.invoke(SensorType.HEART_RATE)
            dismiss()
        }

        binding.imgOxygen.performSingleClick {
            fetchSensor.invoke(SensorType.BLOOD_OXYGEN)
            dismiss()
        }


        binding.imgWalk.performSingleClick {
            fetchSensor.invoke(SensorType.STEP_COUNT)
            dismiss()
        }

        binding.imgWrist.performSingleClick {
            fetchSensor.invoke(SensorType.OFF_BODY)
            dismiss()
        }
    }

    companion object{
        const val TAG = "ATTACHMENT"
    }

}
