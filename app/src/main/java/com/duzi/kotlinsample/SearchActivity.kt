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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity(), ItemClickInterface {

    private lateinit var menuSearch: MenuItem
    private lateinit var searchView: SearchView
    private val adapter by lazy { SearchAdapter(this) }
    private val api by lazy { provideGithubApi(this) }

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
        searchView = (menuSearch.actionView as SearchView).apply {
            setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    updateTitle(query)
                    hideSoftKeyboard()
                    collapseSearchView()
                    searchRepository(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })
        }

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


    private fun updateTitle(query: String) = supportActionBar?.run { subtitle = query }


    private fun collapseSearchView() = menuSearch.collapseActionView()

    private fun searchRepository(query: String) {
        clearResults()
        hideError()
        showProgress()

        api.searchRepository(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {r ->
                            hideProgress()
                            with(adapter) {
                                setItems(r.items)
                                notifyDataSetChanged()
                            }
                            if(r.items.isEmpty()) {
                                println("totalCount = " + r.totalCount)
                                showError("no data")
                            }},
                        {e -> showError("No Successful: " + e.message)}
                )

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
