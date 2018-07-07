package com.duzi.kotlinsample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.duzi.kotlinsample.api.GithubApi
import com.duzi.kotlinsample.api.GithubApiProvider
import com.duzi.kotlinsample.api.model.GithubRepo
import com.duzi.kotlinsample.callback.ItemClickInterface
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    private lateinit var adapter: SearchAdapter
    private lateinit var api: GithubApi
    private lateinit var menuSearch: MenuItem
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        adapter = SearchAdapter(object: ItemClickInterface {
            override fun itemClick(repo: GithubRepo) {
                moveActivity(repo)
            }

        })
        rvActivitySearchList.adapter = adapter
        rvActivitySearchList.layoutManager = LinearLayoutManager(this)

        api = GithubApiProvider.provideGithubApi(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_search, menu)
        menuSearch = menu.findItem(R.id.menuAcctivitySearchQuery)
        searchView = menuSearch.actionView as SearchView
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                updateTitle(query)
                collapseSearchView()
                searchRepository(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        menuSearch.expandActionView()
        return true
    }

    private fun updateTitle(query: String) {
        supportActionBar.let {
            it?.subtitle = query
        }
    }

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
                            adapter.setItems(r.items)
                            adapter.notifyDataSetChanged()
                            if(r.items.isEmpty()) {
                                println("totalCount = " + r.totalCount)
                                showError("no data")
                            }},
                        {e -> showError("No Successful: " + e.message)}
                )

    }

    private fun clearResults() {
        adapter.clearItems()
        adapter.notifyDataSetChanged()
    }

    private fun hideError() {
        tvActivitySearchMessage.text = ""
        tvActivitySearchMessage.visibility = View.GONE
    }

    private fun showProgress() {
        pbActivitySearch.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        pbActivitySearch.visibility = View.GONE
    }

    private fun showError(message: String) {
        tvActivitySearchMessage.text = message
        tvActivitySearchMessage.visibility = View.VISIBLE
    }

    private fun moveActivity(repo: GithubRepo) {
        startActivity(
                Intent(this, RepositoryActivity::class.java)
                        .putExtra(RepositoryActivity.KEY_USER_LOGIN, repo.owner.login)
                        .putExtra(RepositoryActivity.KEY_REPO_NAME, repo.name)
        )
    }
}
