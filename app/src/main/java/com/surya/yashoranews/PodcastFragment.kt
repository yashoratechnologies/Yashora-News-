package com.surya.yashoranews

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.surya.yashoranews.Adapter.PodcastAdapter
import com.surya.yashoranews.DataModels.Podcast
import com.surya.yashoranews.Retrofit.RetrofitInstance
import com.surya.yashoranews.Utils.LocaleHelper
import kotlinx.coroutines.launch

class PodcastFragment : Fragment() {

    private lateinit var loader: ProgressBar

    // 🔥 Categories Lists (Mutable)
    private val newsList = mutableListOf<Podcast>()
    private val mythologyList = mutableListOf<Podcast>()
    private val motivationalList = mutableListOf<Podcast>()
    private val storiesList = mutableListOf<Podcast>()
    private val aajtakList = mutableListOf<Podcast>()
    private val crimeList = mutableListOf<Podcast>()
    private val audiobookList = mutableListOf<Podcast>()
    private val kidsList = mutableListOf<Podcast>()
    private val knowledgeList = mutableListOf<Podcast>()
    private val spiritualityList = mutableListOf<Podcast>()

    // 🔥 Adapters
    private lateinit var adapterNews: PodcastAdapter
    private lateinit var adapterMythology: PodcastAdapter
    private lateinit var adapterMotivational: PodcastAdapter
    private lateinit var adapterStories: PodcastAdapter
    private lateinit var adapterAajtak: PodcastAdapter
    private lateinit var adapterCrime: PodcastAdapter
    private lateinit var adapterAudiobook: PodcastAdapter
    private lateinit var adapterKids: PodcastAdapter
    private lateinit var adapterKnowledge: PodcastAdapter
    private lateinit var adapterSpirituality: PodcastAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_podcast, container, false)

        loader = view.findViewById(R.id.podcastLoader)

        // 🛠️ RecyclerView Setup (Class के अंदर वाले setupRV को call कर रहे हैं)
        adapterNews = setupRV(view.findViewById(R.id.rvNews), newsList)
        adapterMythology = setupRV(view.findViewById(R.id.rvMythology), mythologyList)
        adapterAajtak = setupRV(view.findViewById(R.id.rvAajtak), aajtakList)
        adapterCrime = setupRV(view.findViewById(R.id.rvCrime), crimeList)
        adapterMotivational = setupRV(view.findViewById(R.id.rvMotivational), motivationalList)
        adapterStories = setupRV(view.findViewById(R.id.rvStories), storiesList)
        adapterAudiobook = setupRV(view.findViewById(R.id.rvAudiobook), audiobookList)
        adapterKids = setupRV(view.findViewById(R.id.rvKids), kidsList)
        adapterKnowledge = setupRV(view.findViewById(R.id.rvKnowledge), knowledgeList)
        adapterSpirituality = setupRV(view.findViewById(R.id.rvSpirituality), spiritualityList)

        fetchHomeContent()
        return view
    }

    // ✅ Class के अंदर वाला Single Perfect Function
    private fun setupRV(rv: RecyclerView, list: MutableList<Podcast>): PodcastAdapter {
        // नोट: PodcastAdapter(list) काफी है क्योंकि isSuggestion की default value false है
        val adapter = PodcastAdapter(list)
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rv.setHasFixedSize(true) // Performance boost के लिए
        rv.adapter = adapter
        return adapter
    }

    private fun fetchHomeContent() {
        if (!isAdded) return

        val selectedLang = LocaleHelper.getLanguage(requireContext())
        loader.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getHomePodcasts(selectedLang)

                if (isAdded) {
                    loader.visibility = View.GONE

                    // 📊 Null-Safe update logic
                    updateList(response.news, newsList, adapterNews)
                    updateList(response.mythology, mythologyList, adapterMythology)
                    updateList(response.motivation, motivationalList, adapterMotivational)
                    updateList(response.stories, storiesList, adapterStories)
                    updateList(response.aajtak, aajtakList, adapterAajtak)
                    updateList(response.crime, crimeList, adapterCrime)
                    updateList(response.audiobook, audiobookList, adapterAudiobook)
                    updateList(response.kids, kidsList, adapterKids)
                    updateList(response.knowledge, knowledgeList, adapterKnowledge)
                    updateList(response.spirituality, spiritualityList, adapterSpirituality)
                }

            } catch (e: Exception) {
                if (isAdded) {
                    loader.visibility = View.GONE
                    Log.e("PODCAST_ERROR", "Home Fetch Failed: ${e.message}")
                    Toast.makeText(context, "Check Internet Connection", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ✅ Improved updateList: Null check के साथ
    private fun updateList(newData: List<Podcast>?, targetList: MutableList<Podcast>, adapter: PodcastAdapter) {
        if (!newData.isNullOrEmpty()) {
            targetList.clear()
            targetList.addAll(newData)
            adapter.notifyDataSetChanged()
        }
    }
}