package com.duzi.kotlinsample.api

import com.duzi.kotlinsample.api.model.GithubRepo
import com.duzi.kotlinsample.api.model.RepoSearchResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by KIM on 2018-07-06.
 */

interface GithubApi {
    @GET("search/repositories")
    fun searchRepository(@Query("q") query: String): Observable<RepoSearchResponse>

    @GET("repos/{owner}/{name}")
    fun getRepository(@Path("owner") ownerLogin: String,
                      @Path("name") repoName: String): Observable<GithubRepo>
}