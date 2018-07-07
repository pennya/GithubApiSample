package com.duzi.kotlinsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.duzi.kotlinsample.api.model.GithubRepo
import com.duzi.kotlinsample.api.provideGithubApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_repository.*
import java.text.SimpleDateFormat
import java.util.*

class RepositoryActivity : AppCompatActivity() {

    private val api by lazy { provideGithubApi(this) }
    private val dateFormatInResponse = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
    private val dateFormatToShow = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    companion object {
        val KEY_USER_LOGIN = "KEY_USER_LOGIN"
        val KEY_REPO_NAME = "KEY_REPO_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository)

        val login: String? = intent.getStringExtra(KEY_USER_LOGIN)
        val repo: String? = intent.getStringExtra(KEY_REPO_NAME)

        if(login != null && repo != null) showRepositoryInfo(login, repo)
    }

    private fun showRepositoryInfo(login: String, repo: String) {
        showProgress()
        api.getRepository(login, repo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { r ->
                            hideProgress(true)
                            showRepository(r)
                        },
                        { e ->
                            e.printStackTrace()
                            showError("Not successful: " + e.message)
                        }
                )
    }

    private fun showProgress() {
        llActivityRepositoryContent.visibility = View.GONE
        pbActivityRepository.visibility = View.VISIBLE
    }

    private fun hideProgress(isSucceed: Boolean) {
        llActivityRepositoryContent.visibility = if(isSucceed) View.VISIBLE else View.GONE
        pbActivityRepository.visibility = View.GONE
    }

    private fun showError(message: String) {
        with(tvActivityRepositoryMessage) {
            text = message
            visibility = View.VISIBLE
        }
    }

    private fun showRepository(repo: GithubRepo) {
        GlideApp.with(this)
                .load(repo.owner.avatar_url)
                .into(ivActivityRepositoryProfile)

        tvActivityRepositoryName.text = repo.name
        tvActivityRepositoryStars.text = repo.stargazers_count.toString()
        tvActivityRepositoryDescription.text = repo.description
        tvActivityRepositoryLanguage.text = repo.language
        tvActivityRepositoryLastUpdate.text =
                dateFormatToShow.format(dateFormatInResponse.parse(repo.updated_at))
    }
}
