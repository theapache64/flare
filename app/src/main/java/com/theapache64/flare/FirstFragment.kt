package com.theapache64.flare

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.messaging.FirebaseMessaging
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private lateinit var etGroupName: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.etGroupName = view.findViewById<EditText>(R.id.et_group_name)
        view.findViewById<Button>(R.id.b_subscribe).setOnClickListener {

            Dexter.withActivity(activity)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : BasePermissionListener() {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        super.onPermissionGranted(response)
                        subscribeToGroup()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        super.onPermissionDenied(response)

                        Toast.makeText(
                            activity!!,
                            "Camera permission needed to turn on flash",
                            Toast.LENGTH_SHORT
                        ).show();
                    }
                }).check()
        }
    }

    private fun subscribeToGroup() {

        val groupName = etGroupName.text.toString().trim()

        if (groupName.isNotBlank()) {
            subscribe(groupName)
        } else {
            Toast.makeText(activity!!, "Group name must be given", Toast.LENGTH_SHORT).show();
        }
    }

    private fun subscribe(groupName: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(groupName)
        Toast.makeText(activity!!, "Subscribed to $groupName", Toast.LENGTH_SHORT).show();
    }
}
