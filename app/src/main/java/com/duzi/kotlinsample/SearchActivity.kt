package com.duzi.kotlinsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.duzi.kotlinsample.api.model.GithubRepo
import com.duzi.kotlinsample.api.provideGithubApi
import com.duzi.kotlinsample.callback.ItemClickInterface
import com.jakewharton.rxbinding2.support.v7.widget.queryTextChangeEvents
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity(), ItemClickInterface {

    private lateinit var menuSearch: MenuItem
    private lateinit var searchView: SearchView
    private val adapter by lazy { SearchAdapter(this) }
    private val api by lazy { provideGithubApi(this) }
    private val compositeDisposable = CompositeDisposable()
    private val viewDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        with(rvActivitySearchList) {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = this@SearchActivity.adapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_search, menu)
        menuSearch = menu.findItem(R.id.menuAcctivitySearchQuery)
        searchView = menuSearch.actionView as SearchView

        viewDisposable += searchView.queryTextChangeEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .filter { it.isSubmitted }
                .map { it.queryText() }
                .filter { it.isNotEmpty() }
                .map { it.toString() }
                .subscribe({ query ->
                    updateTitle(query)
                    hideSoftKeyboard()
                    collapseSearchView()
                    searchRepository(query)
                })

        with(menuSearch) {
            setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(p0: MenuItem?): Boolean = true
                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                    if("" == searchView.query) finish()
                    return true
                }
            })

            expandActionView()
        }

        return true
    }

    override fun itemClick(repo: GithubRepo) = moveActivity(repo)

    override fun onDestroy() {
        compositeDisposable.clear()

        // 액티비티가 완전히 종료되고 있는 경우에만 해제
        // 화면이 꺼지거나 다른 액티비티가 호출하여 액티비티가 화면에서 사라지는 경우에는 해제 X
        if(isFinishing) viewDisposable.clear()
        super.onDestroy()
    }

    private fun updateTitle(query: String) = supportActionBar?.run { subtitle = query }

    private fun collapseSearchView() = menuSearch.collapseActionView()

    private fun searchRepository(query: String) {
        api.searchRepository(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    clearResults()
                    hideError()
                    showProgress()
                }
                .doOnTerminate { hideProgress() }
                .flatMap {
                    if (it.items.isEmpty()) {
                        Observable.error(IllegalStateException(""))
                    } else {
                        Observable.just(it.items)
                    }
                }
                .subscribe({ items ->
                    with(adapter) {
                        setItems(items)
                        notifyDataSetChanged()
                    }
                })
                {e -> showError("No Successful: " + e.message)}
                .let { compositeDisposable += it }
    }

    private fun clearResults() {
        with(adapter) {
            clearItems()
            notifyDataSetChanged()
        }
    }

    private fun hideSoftKeyboard() =
        ( getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager ).run {
            hideSoftInputFromWindow(searchView.windowToken, 0) }

    private fun hideError() {
        with(tvActivitySearchMessage) {
            text = ""
            visibility = View.GONE
        }
    }

    private fun showProgress() {
        pbActivitySearch.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        pbActivitySearch.visibility = View.GONE
    }

    private fun showError(message: String?) {
        with(tvActivitySearchMessage) {
            text = message ?: "Unexpected error"
            visibility = View.VISIBLE
        }
    }

    private fun moveActivity(repo: GithubRepo) =
        startActivity(
                Intent(this@SearchActivity, RepositoryActivity::class.java)
                        .putExtra(RepositoryActivity.KEY_USER_LOGIN, repo.owner.login)
                        .putExtra(RepositoryActivity.KEY_REPO_NAME, repo.name)
        )
}
