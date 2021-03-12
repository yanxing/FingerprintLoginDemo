package com.yanxing.fingerprintlogindemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.yanxing.biometriclibrary.FingerprintUtil
import com.yanxing.fingerprintlogindemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layoutInflater = LayoutInflater.from(this)
        layoutInflater.inflate(R.layout.activity_main, null)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        //开启指纹登录
        viewBinding.openFinger.setOnClickListener {
            if (checkFingerprint()) {
                FingerprintUtil.register(this, object : FingerprintUtil.AuthenticationListener {

                    override fun onAuthenticationSuccess(username: String?, password: String?) {
                        showToast("指纹登录开启成功，用户名为"+username+"  密码"+password)
                    }

                    override fun onAuthenticationFail(errorCode: Int) {

                    }
                }, "李双祥", "000000")
            }
        }

        //指纹登录
        viewBinding.loginFinger.setOnClickListener {
            if (checkFingerprint()){
                FingerprintUtil.login(this, object : FingerprintUtil.AuthenticationListener {

                    override fun onAuthenticationSuccess(username: String?, password: String?) {
                        showToast("指纹登录成功，用户名"+username+" 密码"+password)
                    }

                    override fun onAuthenticationFail(errorCode: Int) {

                    }
                })
            }
        }

    }

    private fun checkFingerprint(): Boolean {
        if (FingerprintUtil.isHasHardwareFingerprints(this)) {
            if (FingerprintUtil.isHasEnrolledFingerprints(this)) {
                return true

            } else {
                showToast("设备没有录入指纹")
                FingerprintUtil.openFingerPrintSettingPage(this)
            }
        } else {
            showToast("设备不支持指纹")
        }
        return false
    }

    private fun showToast(msg: String) {
        val toast = Toast.makeText(this, "", Toast.LENGTH_LONG)
        toast.setText(msg)
        toast.show()
    }
}