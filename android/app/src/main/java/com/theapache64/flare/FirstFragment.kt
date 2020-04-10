package com.theapache64.flare

import android.Manifest
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            etGroupName.removeTextChangedListener(this)
            val newText = etGroupName.text.toString()
                .toLowerCase()
                .replace(" ", "_")
            etGroupName.setText(newText)
            etGroupName.setSelection(newText.length)
            etGroupName.addTextChangedListener(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.etGroupName = view.findViewById(R.id.et_group_name)
        this.etGroupName.addTextChangedListener(textWatcher)

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
        AlertDialog.Builder(activity!!)
            .setTitle(R.string.dialog_title_subscribed)
            .setMessage("Subscribed to `$groupName`")
            .setCancelable(false)
            .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                etGroupName.setText("")
                dialogInterface.dismiss()
            }
            .create()
            .show()
    }
}
