package com.duzi.kotlinsample.api.model

/**
 * Created by KIM on 2018-07-06.
 */

data class GithubRepo (
        var name: String,
        var owner: GithubOwner,
        var description: String?,       // nullable
        var language: String?,          // nullable
        var updated_at: String,
        var stargazers_count: Int
)