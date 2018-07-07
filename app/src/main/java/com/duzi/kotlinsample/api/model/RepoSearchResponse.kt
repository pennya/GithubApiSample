package com.duzi.kotlinsample.api.model

/**
 * Created by KIM on 2018-07-06.
 */

data class RepoSearchResponse(
        var totalCount: Int,
        var items: List<GithubRepo>
)

