package com.duzi.kotlinsample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.duzi.kotlinsample.api.model.AuthTokenProvider
import com.duzi.kotlinsample.api.provideAuthApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.AnkoLogger

class SignInActivity : AppCompatActivity(), AnkoLogger {

    private val api by lazy { provideAuthApi() }
    private val authTokenProvider by lazy { AuthTokenProvider(this) }
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        btnActivitySignInStart.setOnClickListener {
            val authUri = Uri.Builder().run {
                scheme("https")
                authority("github.com")
                appendPath("login")
                appendPath("oauth")
                appendPath("authorize")
                appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
                build()
            }

            // 앱 내부에서 링크를 자연스럽게 연결시키면서 빠르게 보여줄때 사용
            CustomTabsIntent.Builder().build().run {
                launchUrl(this@SignInActivity, authUri)
            }
        }

        if(authTokenProvider.token != null) {
            launchMainActivity()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        showProgress()
        intent.data.let {
            getAccessToken(it.getQueryParameter("code"))
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun getAccessToken(code: String) {
        api.getAccessToken(BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showProgress() }
                .doOnTerminate { hideProgress() }
                .map { it.access_token }
                .subscribe({ token ->
                    authTokenProvider.updateToken(token)
                    launchMainActivity() })
                { error -> error.printStackTrace() }
                .let{ compositeDisposable += it }
    }

    private fun launchMainActivity() {
        startActivity(
                Intent(this@SignInActivity, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun showProgress() {
        btnActivitySignInStart.visibility = View.GONE
        pbActivitySignIn.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        btnActivitySignInStart.visibility = View.VISIBLE
        pbActivitySignIn.visibility = View.GONE
    }

}
