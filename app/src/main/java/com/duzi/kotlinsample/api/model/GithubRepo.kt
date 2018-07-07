package com.duzi.kotlinsample.api.model

/**
 * Created by KIM on 2018-07-06.
 */

data class GithubRepo (
        var name: String,
        var owner: GithubOwner,
        var description: String,
        var language: String,
        var updated_at: String,
        var stargazers_count: Int
)