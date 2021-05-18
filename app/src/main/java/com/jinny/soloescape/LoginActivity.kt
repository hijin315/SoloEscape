package com.jinny.soloescape

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jinny.soloescape.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private var binding: ActivityLoginBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager // 페북 로그인 콜백 매니저

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        auth = Firebase.auth
        callbackManager = CallbackManager.Factory.create() // 콜백 매니저 초기화
        initLoginButton()
        initSignUpButton()
        initEmailAndPassWordEditText()
        initFacebookLogin()
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            1
        )
    }

    private fun initLoginButton() {
        binding!!.loginBtn.setOnClickListener {
            val email = binding!!.emailEditText.text.toString()
            val password = binding!!.passWordEditText.text.toString()

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    successLogin() //로그인 액티비티 종료
                } else {
                    Toast.makeText(this, "로그인에 실패했습니다. 이메일 또는 비밀번호를 확인 해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun initSignUpButton() {
        binding!!.signBtn.setOnClickListener {
            val email = binding!!.emailEditText.text.toString()
            val password = binding!!.passWordEditText.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "회원가입 성공! 로그인 버튼을 눌러 로그인 하세요", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "이미 가입된 이메일이거나 회원가입에 실패 했습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }

    }

    private fun initFacebookLogin() {
        binding!!.facebookLoginBtn.apply {
            setPermissions("email", "public_profile") // 이메일과 프로필을 가져오겠다!
            registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    // 로그인 성공
                    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener(this@LoginActivity) { task ->
                            if (task.isSuccessful) {
                                successLogin()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "페이스북 로그인 실패 입니다",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }

                override fun onCancel() {
                }

                override fun onError(error: FacebookException?) {
                    Toast.makeText(this@LoginActivity, "페이스북 로그인 실패", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun initEmailAndPassWordEditText() {
        binding!!.apply {
            emailEditText.addTextChangedListener {
                val enable = emailEditText.text.isNotEmpty() && passWordEditText.text.isNotEmpty()
                loginBtn.isEnabled = enable
                signBtn.isEnabled = enable
            }
            passWordEditText.addTextChangedListener {
                val enable = emailEditText.text.isNotEmpty() && passWordEditText.text.isNotEmpty()
                loginBtn.isEnabled = enable
                signBtn.isEnabled = enable
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun successLogin() {
        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
            return
        }
        val userID = auth.currentUser?.uid.orEmpty()
        val currentUserDB = Firebase.database.reference.child("Users").child(userID)
        val user = mutableMapOf<String, Any>()
        user["userID"] = userID
        currentUserDB.updateChildren(user)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}
