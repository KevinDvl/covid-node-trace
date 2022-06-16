package com.covid.nodetrace.ui

import android.content.Context
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.covid.nodetrace.ContactService
import com.covid.nodetrace.R

/**
 * In the settings of the app the user can configure how the application behaves. It also
 * contains information about how data is handled within the app, specifying how private information is protected.
 */
class SettingsFragment: Fragment() {
    private val model: AppViewModel by activityViewModels()

    private lateinit var advertiseOrScanSwitch : Switch
    private lateinit var appModeOrBroadcast : Switch
    private lateinit var layoutNodeOrUserSwitch : View
    private var devModeClicks : Int = 0
    private final val DEV_CLICKS_NEEDED = 6

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.settings_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val sp = requireContext().getSharedPreferences("Communication State", Context.MODE_PRIVATE)
        val communicationTypeFromStorage: Int = sharedPref.getInt(getString(R.string.communication_type_state), 2)
        Log.d("Communication Type", communicationTypeFromStorage.toString())

        advertiseOrScanSwitch = view.findViewById(R.id.switch_node_user)
        appModeOrBroadcast = view.findViewById(R.id.switch_app_broadcast)
        layoutNodeOrUserSwitch = view.findViewById(R.id.layout_nodeOrUser_switch)

        //initialize the node or user switch
        if (communicationTypeFromStorage == 0) {
            appModeOrBroadcast.setChecked(false)
            advertiseOrScanSwitch.setChecked(false)
        } else if (communicationTypeFromStorage == 2) {
            appModeOrBroadcast.setChecked(true)
            layoutNodeOrUserSwitch.isVisible = false
        } else {
            appModeOrBroadcast.setChecked(false)
            advertiseOrScanSwitch.setChecked(true)
        }

        //checkedChangeListener app or broadcast switch
        appModeOrBroadcast.setOnCheckedChangeListener { switchView, isChecked ->
            var communicationTypeState = 0

            layoutNodeOrUserSwitch.isVisible = !isChecked
            if(isChecked) {
                model.communicationType.value = ContactService.CommunicationType.SCAN_AND_ADVERTISE
                communicationTypeState = ContactService.CommunicationType.SCAN_AND_ADVERTISE.ordinal
            }

            //update the communication type preferences
            with(requireActivity().getPreferences(Context.MODE_PRIVATE).edit()) {
                putInt(
                    resources.getString(R.string.communication_type_state),
                    communicationTypeState
                )
                apply()
            }
        }

        //checkedChangeListener node or user switch (visible only when in app mode)
        advertiseOrScanSwitch.setOnCheckedChangeListener { switchView, isChecked ->
            var communicationTypeState = 0

            if (isChecked) {
                //The app only scans for devices if it's set to 'USER'
                model.communicationType.value = ContactService.CommunicationType.SCAN
                communicationTypeState = ContactService.CommunicationType.ADVERTISE.ordinal
            } else {
                //The app only advertises if it's set to 'NODE'
                model.communicationType.value = ContactService.CommunicationType.ADVERTISE
                communicationTypeState = ContactService.CommunicationType.SCAN.ordinal
            }

            //update the communication type preferences
            with(requireActivity().getPreferences(Context.MODE_PRIVATE).edit()) {
                putInt(
                    resources.getString(R.string.communication_type_state),
                    communicationTypeState
                )
                apply()
            }
        }

        /*var communicationTypeState = 0

        model.communicationType.value = ContactService.CommunicationType.SCAN_AND_ADVERTISE
        communicationTypeState = ContactService.CommunicationType.SCAN_AND_ADVERTISE.ordinal

        with(requireActivity().getPreferences(Context.MODE_PRIVATE).edit()) {
            putInt(
                resources.getString(R.string.communication_type_state),
                communicationTypeState
            )
            apply()
        }*/




        val devMode : Boolean = sharedPref.getBoolean(getString(R.string.dev_mode), false)

        if (devMode) {
            val devSection = view.findViewById(R.id.dev_section) as LinearLayout
            devSection.visibility = View.VISIBLE
        }
        else {
            val devSection = view.findViewById(R.id.dev_section) as LinearLayout
            devSection.visibility = View.GONE
        }

        val settingsIcon = view.findViewById(R.id.privacy_icon) as ImageView
        settingsIcon.setOnClickListener {
            devModeClicks++

            if (devModeClicks >= 2 && devModeClicks < DEV_CLICKS_NEEDED) {
                Toast.makeText(requireContext(), "${DEV_CLICKS_NEEDED - devModeClicks} steps away from dev mode", Toast.LENGTH_SHORT).show()
            }
            else if (devModeClicks >= DEV_CLICKS_NEEDED) {
                val devSection = view.findViewById(R.id.dev_section) as LinearLayout
                devSection.visibility = View.VISIBLE

                with (requireActivity().getPreferences(Context.MODE_PRIVATE).edit()) {
                    putBoolean(resources.getString(R.string.dev_mode), true)
                    apply()
                }
            }
        }
    }
}