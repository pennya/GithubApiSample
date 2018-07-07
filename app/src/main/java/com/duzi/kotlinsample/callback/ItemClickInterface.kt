package com.duzi.kotlinsample.callback

import com.duzi.kotlinsample.api.model.GithubRepo

/**
 * Created by kim on 2018. 7. 7..
 */

interface ItemClickInterface {
    fun itemClick(repo: GithubRepo)
}