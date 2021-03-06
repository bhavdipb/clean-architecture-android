package com.duyp.architecture.clean.android.powergit.data.di

import android.content.Context
import com.duyp.architecture.clean.android.powergit.data.BuildConfig
import com.duyp.architecture.clean.android.powergit.data.api.ApiCache
import com.duyp.architecture.clean.android.powergit.data.api.ApiConstants.TIME_OUT_API
import com.duyp.architecture.clean.android.powergit.data.api.IssueService
import com.duyp.architecture.clean.android.powergit.data.api.SearchService
import com.duyp.architecture.clean.android.powergit.data.api.UserService
import com.duyp.architecture.clean.android.powergit.data.api.annotations.AnnotationWrapCallAdapterFactory
import com.duyp.architecture.clean.android.powergit.data.api.annotations.OwnerType
import com.duyp.architecture.clean.android.powergit.data.api.annotations.RequestAnnotations
import com.duyp.architecture.clean.android.powergit.data.api.converters.GithubResponseConverter
import com.duyp.architecture.clean.android.powergit.data.api.interceptors.AuthorizationInterceptor
import com.duyp.architecture.clean.android.powergit.data.api.interceptors.CacheInterceptor
import com.duyp.architecture.clean.android.powergit.data.api.interceptors.ContentTypeInterceptor
import com.duyp.architecture.clean.android.powergit.data.api.interceptors.PaginationInterceptor
import com.duyp.architecture.clean.android.powergit.domain.usecases.GetAuthentication
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    internal fun provideApiCache(@ApplicationContext context: Context): ApiCache {
        return try {
            val httpCacheDirectory = File(context.cacheDir, "httpCache")
            ApiCache(cache = Cache(httpCacheDirectory, (10 * 1024 * 1024).toLong())) // 10 MB
        } catch (e: Exception) {
            ApiCache(null)
        }
    }

    @Provides
    @Singleton
    internal fun provideRequestAnnotations() = RequestAnnotations()

    @Provides
    @Singleton
    internal fun provideOkHttpClient(cache: ApiCache, requestAnnotations: RequestAnnotations,
                                     getAuthentication: GetAuthentication): OkHttpClient {
        // okHttp client
        val clientBuilder = OkHttpClient.Builder()
                .connectTimeout(TIME_OUT_API.toLong(), TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT_API.toLong(), TimeUnit.SECONDS)
                .readTimeout(TIME_OUT_API.toLong(), TimeUnit.SECONDS)
                .addInterceptor(ContentTypeInterceptor())
                .addInterceptor(PaginationInterceptor())
                .addInterceptor(AuthorizationInterceptor(requestAnnotations) { ownerType ->
                    var token: String? = null
                    when (ownerType) {
                        OwnerType.USER_NORMAL -> token = getAuthentication.getCurrentUserAuthentication()
                                .subscribeOn(Schedulers.io())
                                .blockingGet()
                    }
                    token
                })
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)

        // add logging interceptor
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            // only log network calls (no log for force cache which isn't through network)
            clientBuilder.addNetworkInterceptor(logging)
        }

        // cache
        cache.get()?.let {
            clientBuilder
                    .cache(it)
                    .addNetworkInterceptor(CacheInterceptor(requestAnnotations))
        }

        return clientBuilder.build()
    }

    @Provides
    @Singleton
    internal fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson, requestAnnotations: RequestAnnotations) = Retrofit.Builder()
            .baseUrl(BuildConfig.REST_URL)
            .client(okHttpClient)
            .addConverterFactory(GithubResponseConverter(gson))
            .addCallAdapterFactory(
                    AnnotationWrapCallAdapterFactory.create(
                            RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()), requestAnnotations
                    )
            )
            .build()!!

    @Provides
    @Singleton
    internal fun provideUserService(retrofit: Retrofit) = retrofit.create(UserService::class.java)!!

    @Provides
    @Singleton
    internal fun provideSearchService(retrofit: Retrofit) = retrofit.create(SearchService::class.java)!!

    @Provides
    @Singleton
    internal fun provideIssueService(retrofit: Retrofit) = retrofit.create(IssueService::class.java)!!
}