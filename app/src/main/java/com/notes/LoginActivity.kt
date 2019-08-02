package com.notes

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.notes.ui.MainActivity
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var mVerificationId: String
    private var mVerificationInProgress: Boolean = false
    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()
        prepLogin()

        buttonStartVerification.setOnClickListener{
            if (!validatePhoneNumber()) {
                return@setOnClickListener
            }
            startPhoneNumberVerification(
                if (fieldPhoneNumber.text.toString().length == 10)
                    "+91" + fieldPhoneNumber.text.toString()
                else
                    fieldPhoneNumber.text.toString()
            )
        }

        buttonVerifyPhone.setOnClickListener {
            val code = fieldVerificationCode.text.toString()
            if (TextUtils.isEmpty(code)) {
                fieldVerificationCode.error = "Cannot be empty."
                return@setOnClickListener
            }

            verifyPhoneNumberWithCode(mVerificationId, code)
        }
        buttonResend.setOnClickListener {
            mResendToken?.let { resendVerificationCode(fieldVerificationCode.text.toString(), it) }
        }
        signOutButton.setOnClickListener{
            signOut()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }


    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
        // [START_EXCLUDE]
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(fieldPhoneNumber.text.toString())
        }
        // [END_EXCLUDE]
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
//        sharedPreferences.edit().putString("phoneNumber", phoneNumber).apply()
        Log.d(TAG, "Starting verification of: $phoneNumber")
        PhoneAuthProvider.getInstance()
            .verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, mCallbacks)
        // [END start_phone_auth]
        mVerificationInProgress = true
    }

    private fun validatePhoneNumber(): Boolean = when {
        TextUtils.isEmpty(fieldPhoneNumber.text.toString()) -> {
            fieldPhoneNumber.error = "Invalid phone number."
            false
        }
        else -> true
    }

    private fun prepLogin() {
        mAuth = FirebaseAuth.getInstance()
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                // [START_EXCLUDE silent]
                mVerificationInProgress = false
                // [END_EXCLUDE]
                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential)
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                // [START_EXCLUDE silent]
                mVerificationInProgress = false
                // [END_EXCLUDE]

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    fieldPhoneNumber.error = "Invalid phone number."
                    // [END_EXCLUDE]
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Toast.makeText(
                        this@LoginActivity,
                        "Quota exceeded.",
                        Toast.LENGTH_SHORT
                    ).show()
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED)
                // [END_EXCLUDE]
            }

            override fun onCodeSent(
                verificationId: String?,
                token: PhoneAuthProvider.ForceResendingToken?
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId!!)

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token

                // [START_EXCLUDE]
                // Update UI
                updateUI(STATE_CODE_SENT)
                // [END_EXCLUDE]
            }
        }
    }

    private fun updateUI(uiState: Int) {
        updateUI(uiState, mAuth.currentUser, null)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user)
            Log.d(TAG, "Here")
        } else {
            updateUI(STATE_INITIALIZED)
        }
    }

    private fun updateUI(uiState: Int, user: FirebaseUser) {
        updateUI(uiState, user, null)
    }

    private fun updateUI(uiState: Int, cred: PhoneAuthCredential) {
        updateUI(uiState, null, cred)
    }

    private fun updateUI(uiState: Int, user: FirebaseUser?, cred: PhoneAuthCredential?) {
        when (uiState) {
            STATE_INITIALIZED -> {
                // Initialized state, show only the phone number field and start button
                enableViews(buttonStartVerification, fieldPhoneNumber)
                disableViews(buttonVerifyPhone, buttonResend, fieldVerificationCode)
                detail.text = null
            }
            STATE_CODE_SENT -> {
                // Code sent state, show the verification field, the
                enableViews(buttonVerifyPhone, buttonResend, fieldPhoneNumber, fieldVerificationCode)
                disableViews(buttonStartVerification)
                detail.setText(R.string.status_code_sent)
            }
            STATE_VERIFY_FAILED -> {
                // Verification has failed, show all options
                enableViews(
                    buttonStartVerification, buttonVerifyPhone, buttonResend, phoneAuthFields,
                    fieldVerificationCode
                )
                detail.setText(R.string.status_verification_failed)
            }
            STATE_VERIFY_SUCCESS -> {
                // Verification has succeeded, proceed to firebase sign in
                disableViews(
                    buttonStartVerification, buttonVerifyPhone, buttonResend, phoneAuthFields,
                    fieldVerificationCode
                )
                detail.setText(R.string.status_verification_succeeded)

                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.smsCode != null) {
                        fieldVerificationCode.setText(cred.smsCode)
                    } else {
                        fieldVerificationCode.setText(R.string.instant_validation)
                    }
                }
            }
            STATE_SIGNIN_FAILED ->
                // No-op, handled by sign-in check
                detail.setText(R.string.status_sign_in_failed)
            STATE_SIGNIN_SUCCESS -> {
            }
        }// Np-op, handled by sign-in check

        if (user == null) {
            // Signed out
            phoneAuthFields.visibility = View.VISIBLE
            signedInButtons.visibility = View.GONE

            status.setText(R.string.signed_out)
        } else {
            startChatBox(user)
            // Signed in
            phoneAuthFields.visibility = View.GONE
            signedInButtons.visibility = View.VISIBLE

            enableViews(fieldPhoneNumber, fieldVerificationCode)
            fieldPhoneNumber.text = null
            fieldVerificationCode.text = null

            status.setText(R.string.signed_in)
            detail.text = getString(R.string.firebase_status_fmt, user.uid)
            phoneNumber = fieldPhoneNumber.text
        }
    }

    private fun startChatBox(user: FirebaseUser) {
        // Toast.makeText(this@LoginActivity, "TO-DO", Toast.LENGTH_LONG).show()
        enableViews(buttonStartVerification, fieldPhoneNumber)
        disableViews(buttonVerifyPhone, buttonResend, fieldVerificationCode)
        detail.text = null
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
    }

    private fun enableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = true
        }
    }

    private fun disableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = false
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result!!.user
                    startChatBox(user)
                    // [START_EXCLUDE]
                    //updateUI(STATE_SIGNIN_SUCCESS, user);
                    // [END_EXCLUDE]
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        // [START_EXCLUDE silent]
                        fieldVerificationCode.error = "Invalid code."
                        // [END_EXCLUDE]
                    }
                    // [START_EXCLUDE silent]
                    // Update UI
                    updateUI(STATE_SIGNIN_FAILED)
                    // [END_EXCLUDE]
                }
            }
    }

    private fun signOut() {
        mAuth.signOut()
        updateUI(STATE_INITIALIZED)
    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        // [START verify_with_code]
        Log.d(TAG, "Verifying phone number")
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }

    // [START resend_verification]
    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            mCallbacks, // OnVerificationStateChangedCallbacks
            token
        )             // ForceResendingToken from callbacks
    }

    private fun checkLogin() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_CODE_SENT = 2
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6
        // [START declare_auth]
        private lateinit var mAuth: FirebaseAuth
        var phoneNumber: String? = null


    }
}