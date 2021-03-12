package com.yanxing.biometriclibrary

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.security.Key
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec

/**
 * 指纹识别
 * @author 李双祥 on 2019/12/16.
 */
object FingerprintUtil {

    private val mExecutor = Executor { command -> Handler(Looper.getMainLooper()).post(command) }
    private const val ACTION_SETTING = "android.settings.SETTINGS"
    private const val SPLIT="|"

    /**
     * 打开手机指纹设置界面
     */
    fun openFingerPrintSettingPage(context: Context) {
        val intent = Intent(ACTION_SETTING)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
        }
    }

    /**
     * 硬件是否支持指纹,true支持
     */
    fun isHasHardwareFingerprints(context: Context): Boolean {
        return BiometricManager.from(context).canAuthenticate() != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
    }

    /**
     * 用户是否录入了指纹,true录入了
     */
    fun isHasEnrolledFingerprints(context: Context): Boolean {
        return isHasHardwareFingerprints(context) && (BiometricManager.from(context).canAuthenticate()
                != BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
    }

    /**
     * 是否支持指纹验证，硬件支持，用户录入了指纹
     */
    fun canAuthenticate(context: Context): Boolean {
        return BiometricManager.from(context).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * 启动指纹验证
     */
    fun startAuthenticate(fragmentActivity: FragmentActivity,authenticationListener: AuthenticationListener) {
        authenticate(fragmentActivity, authenticationListener)
    }

    /**
     * 启动指纹验证
     */
    fun startAuthenticate(fragment: Fragment, authenticationListener: AuthenticationListener) {
        authenticate(fragment,authenticationListener)
    }

    /**
     * 用于指纹登录App，先注册账户信息
     */
    fun register(fragment: Fragment,authenticationListener: AuthenticationListener,loginName:String,password:String) {
        registerAuthenticate(fragment,authenticationListener,loginName+SPLIT+password)
    }

    /**
     * 用于指纹登录App，先注册账户信息
     */
    fun register(fragmentActivity: FragmentActivity,authenticationListener: AuthenticationListener,loginName:String,password:String) {
        registerAuthenticate(fragmentActivity,authenticationListener,loginName+SPLIT+password)
    }

    /**
     * 用于指纹登录App
     */
    fun login(fragmentActivity: FragmentActivity,authenticationListener: AuthenticationListener) {
        loginAuthenticate(fragmentActivity,authenticationListener)
    }

    /**
     * 用于指纹登录App
     */
    fun login(fragment: Fragment,authenticationListener: AuthenticationListener) {
        loginAuthenticate(fragment,authenticationListener)
    }

    private fun authenticate(any: Any,authenticationListener: AuthenticationListener) {
        val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                //11未注册任何指纹；7尝试次数过多，请稍后重试；5指纹操作已取消
                authenticationListener.onAuthenticationFail(errorCode)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                authenticationListener.onAuthenticationSuccess(null,null)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                authenticationListener.onAuthenticationFail(0)
            }
        }
        var biometricPrompt: BiometricPrompt? = null
        if (any is Fragment){
            biometricPrompt = BiometricPrompt(any, mExecutor, authenticationCallback)
        }
        if (any is FragmentActivity){
            biometricPrompt = BiometricPrompt(any, mExecutor, authenticationCallback)
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
        promptInfo.setTitle("指纹验证")
        promptInfo.setDescription("等待手指按下")
        promptInfo.setNegativeButtonText("取消")
        biometricPrompt?.authenticate(promptInfo.build())
    }

    private fun registerAuthenticate(any: Any,authenticationListener: AuthenticationListener,saveInfo:String) {
        val context = if (any is FragmentActivity){
            any
        }else{
            (any as Fragment).context!!
        }
        val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                //11未注册任何指纹；7尝试次数过多，请稍后重试；5指纹操作已取消
                authenticationListener.onAuthenticationFail(errorCode)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                //进行注册时，保存登录信息
                result.cryptoObject?.cipher ?.apply {
                    // 使用 cipher 对登录信息进行加密并保存
                    val encryptInfo = bytesToHexString(doFinal(saveInfo.toByteArray()))
                    // 保存 encryptInfo 到本地
                    saveInfo(context,encryptInfo)
                    // 保存加密向量到本地
                    saveInfoIV(context,bytesToHexString(iv))
                    val login=saveInfo.split(SPLIT)
                    authenticationListener.onAuthenticationSuccess(login[0],login[1])
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                authenticationListener.onAuthenticationFail(0)
            }
        }
        var biometricPrompt: BiometricPrompt? = null
        if (any is Fragment){
            biometricPrompt = BiometricPrompt(any, mExecutor, authenticationCallback)
        }
        if (any is FragmentActivity){
            biometricPrompt = BiometricPrompt(any, mExecutor, authenticationCallback)
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
        promptInfo.setTitle("指纹验证")
        promptInfo.setDescription("等待手指按下")
        promptInfo.setNegativeButtonText("取消")
        biometricPrompt?.authenticate(promptInfo.build(),BiometricPrompt.CryptoObject(loadEncryptCipher()))
    }

    private fun loginAuthenticate(any: Any,authenticationListener: AuthenticationListener) {
        val context = if (any is FragmentActivity){
            any
        }else{
            (any as Fragment).context!!
        }
        val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                //11未注册任何指纹；7尝试次数过多，请稍后重试；5指纹操作已取消
                authenticationListener.onAuthenticationFail(errorCode)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                //登录取出登录信息
                val encryptInfo= getInfo(context)
                // 认证成功，获取 Cipher 对象
                val cipher = result.cryptoObject?.cipher ?: throw RuntimeException("cipher is null!")
                // 使用 cipher 对登录信息进行解密
                val loginInfo = cipher.doFinal(hexToByteArray(encryptInfo))
                val login=String(loginInfo).split(SPLIT)
                authenticationListener.onAuthenticationSuccess(login[0],login[1])
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                authenticationListener.onAuthenticationFail(0)
            }
        }
        var biometricPrompt: BiometricPrompt? = null
        if (any is Fragment){
            biometricPrompt = BiometricPrompt(any, mExecutor, authenticationCallback)
        }
        if (any is FragmentActivity){
            biometricPrompt = BiometricPrompt(any, mExecutor, authenticationCallback)
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
        promptInfo.setTitle("指纹验证")
        promptInfo.setDescription("等待手指按下")
        promptInfo.setNegativeButtonText("取消")
        biometricPrompt?.authenticate(promptInfo.build(), BiometricPrompt.CryptoObject(loadDecodeCipher(context)))
    }

    /**
     * 解码
     */
    private fun loadDecodeCipher(context: Context): Cipher {
        val cipher = Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7
        )

        // 获取开启指纹登录时保存的加密向量数据
        val ivBytes=hexToByteArray(getInfoIV(context))
        val iv = IvParameterSpec(ivBytes)
        // 使用指纹登录，使用 Cipher.DECRYPT_MODE 和 iv 进行初始化
        cipher.init(Cipher.DECRYPT_MODE, getKeyStoreKey(), iv)
        return cipher
    }

    /**
     * 加密的Cipher对象
     */
    private fun loadEncryptCipher(): Cipher {
        val cipher = Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
        // 开启登录时用于加密，使用 Cipher.ENCRYPT_MODE 初始化
        cipher.init(Cipher.ENCRYPT_MODE, getKeyStoreKey())
        return cipher
    }

    private fun getKeyStoreKey():Key{
        val keyAlias="fingerprint"
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        // keyAlias 为密钥别名，可自己定义，加密解密要一致
        if (!keyStore.containsAlias(keyAlias)) {
            // 不包含改别名，重新生成
            // 秘钥生成器
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val builder = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(false)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        }
        // 根据别名获取密钥
        return keyStore.getKey(keyAlias, null)
    }

    interface AuthenticationListener {

        /**
         * 使用指纹识别登录取出的用户名和密码
         */
        fun onAuthenticationSuccess(username:String?,password: String?)

        /**
         * 11未注册任何指纹；7尝试次数过多，请稍后重试；5指纹操作已取消，0指纹不匹配
         */
        fun onAuthenticationFail(errorCode: Int)
    }

}