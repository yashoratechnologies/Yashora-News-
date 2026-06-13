package com.surya.yashoranews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.surya.yashoranews.Adapter.NewsAdapter
import com.surya.yashoranews.DataModels.Article
import com.surya.yashoranews.DataModels.NewsMainResponse
import com.surya.yashoranews.Retrofit.RetrofitInstance
import com.surya.yashoranews.Utils.LocaleHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var progressBar: ProgressBar
    private lateinit var newsAdapter: NewsAdapter
    private val fullNewsList = mutableListOf<Article>()

    private var lastDateCursor: String? = null
    private var isLoading = false
    private var currentCategory = "general"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news, container, false)

        // Views Initialize
        viewPager = view.findViewById(R.id.viewPagerNewsFragment)
        progressBar = view.findViewById(R.id.progressBarFragment)

        // Adapter Setup
        newsAdapter = NewsAdapter(fullNewsList)
        viewPager.adapter = newsAdapter
        viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

        // Infinite Scroll Logic
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position >= fullNewsList.size - 2 && !isLoading && fullNewsList.isNotEmpty()) {
                    fetchNews(lastDateCursor)
                }
            }
        })

        fetchNews(null) // Pahli baar news load karo
        return view
    }

    // MainActivity se category update karne ke liye
    fun updateCategory(category: String) {
        currentCategory = category
        refreshFeed()
    }

    private fun refreshFeed() {
        lastDateCursor = null
        fullNewsList.clear()
        newsAdapter.notifyDataSetChanged()
        fetchNews(null)
    }

    private fun fetchNews(cursor: String?) {
        // 🔥 Safety Check: Agar fragment attached nahi hai toh aage mat badho
        if (!isAdded || context == null) return

        if (isLoading) return
        isLoading = true
        progressBar.visibility = View.VISIBLE

        val currentLang = LocaleHelper.getLanguage(requireContext())

        RetrofitInstance.api.getNews(currentLang, currentCategory, 20, cursor)
            .enqueue(object : Callback<NewsMainResponse> {
                override fun onResponse(call: Call<NewsMainResponse>, response: Response<NewsMainResponse>) {
                    // Fragment check again before updating UI
                    if (!isAdded) return

                    isLoading = false
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        val res = response.body()!!
                        lastDateCursor = res.lastDate

                        if (res.articles.isNotEmpty()) {
                            val startPos = fullNewsList.size
                            fullNewsList.addAll(res.articles)
                            newsAdapter.notifyItemRangeInserted(startPos, res.articles.size)
                        }
                    }
                }

                override fun onFailure(call: Call<NewsMainResponse>, t: Throwable) {
                    if (!isAdded) return
                    isLoading = false
                    progressBar.visibility = View.GONE
                }
            })
    }
}