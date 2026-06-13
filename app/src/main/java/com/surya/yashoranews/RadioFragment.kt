package com.surya.yashoranews

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.surya.yashoranews.Adapter.RadioAdapter
import com.surya.yashoranews.Retrofit.RetrofitInstance
import kotlinx.coroutines.launch

class RadioFragment : Fragment() {

    private lateinit var rvRadio: RecyclerView
    private lateinit var loader: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_radio, container, false)

        rvRadio = view.findViewById(R.id.rvRadio)
        loader = view.findViewById(R.id.radioLoader)

        rvRadio.layoutManager = GridLayoutManager(context, 2) // Grid View for radio stations

        loadRadioStations()

        return view
    }

    private fun loadRadioStations() {
        loader.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val stations = RetrofitInstance.api.getRadioStations()
                rvRadio.adapter = RadioAdapter(stations)
                loader.visibility = View.GONE
            } catch (e: Exception) {
                loader.visibility = View.GONE
                Toast.makeText(context, "Radio offline please wait", Toast.LENGTH_SHORT).show()
            }
        }
    }
}