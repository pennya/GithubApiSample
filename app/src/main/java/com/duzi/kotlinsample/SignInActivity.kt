package com.duzi.kotlinsample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.duzi.kotlinsample.api.AuthApi
import com.duzi.kotlinsample.api.GithubApiProvider
import com.duzi.kotlinsample.api.model.AuthTokenProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.AnkoLogger

class SignInActivity : AppCompatActivity(), AnkoLogger {

    private lateinit var api: AuthApi
    private lateinit var authTokenProvider: AuthTokenProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        btnActivitySignInStart.setOnClickListener {
            val authUri: Uri = Uri.Builder().scheme("https").authority("github.com")
                    .appendPath("login")
                    .appendPath("oauth")
                    .appendPath("authorize")
                    .appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
                    .build()

            // 앱 내부에서 링크를 자연스럽게 연결시키면서 빠르게 보여줄때 사용
            val intent: CustomTabsIntent = CustomTabsIntent.Builder().build()
            intent.launchUrl(this, authUri)
        }

        api = GithubApiProvider.provideAuthApi()
        authTokenProvider = AuthTokenProvider(this)
        if(authTokenProvider.getToken() != null) {
            launchMainActivity()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        showProgress()

        val uri:Uri = intent.data
        val code:String = uri.getQueryParameter("code")
        getAccessToken(code)
    }

    private fun getAccessToken(code: String) {
        showProgress()


        api.getAccessToken(BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { result ->
                            hideProgress()
                            if(result.access_token != null) authTokenProvider.updateToken(result.access_token)
                            launchMainActivity() },
                        { error -> error.printStackTrace() }
                )
    }

    private fun launchMainActivity() {
        startActivity(
                Intent(this, MainActivity::class.java)
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
