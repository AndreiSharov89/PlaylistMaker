package com.example.playlistmaker

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    companion object {
        const val SEARCH_STRING = "SEARCH_STRING"
        const val SEARCH = ""
    }

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var history: SearchHistory

    private lateinit var trackRecycler: RecyclerView
    private lateinit var historyRecycler: RecyclerView

    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var imageError: ImageView
    private lateinit var placeholderMessage: TextView
    private lateinit var refresh: Button
    private lateinit var btnBack: ImageView
    private lateinit var historySection: LinearLayout
    private lateinit var clearHistoryButton: Button

    private val tracks = ArrayList<Track>()
    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    private var searchString: String = SEARCH

    private val itunesBaseUrl = "https://itunes.apple.com"
    private val retrofit = Retrofit.Builder()
        .baseUrl(itunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val itunesAPI = retrofit.create(ItunesApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        sharedPrefs = getSharedPreferences("search_history", MODE_PRIVATE)
        history = SearchHistory(sharedPrefs)

        val rootView = findViewById<LinearLayout>(R.id.search_root_view)
        btnBack = findViewById(R.id.btn_back)
        inputEditText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.clearIcon)
        imageError = findViewById(R.id.iv_Error)
        placeholderMessage = findViewById(R.id.tv_Error)
        refresh = findViewById(R.id.btn_refresh)
        trackRecycler = findViewById(R.id.rvTrack)
        historyRecycler = findViewById(R.id.rv_searchHistory)
        historySection = findViewById(R.id.searchHistorySection)
        clearHistoryButton = findViewById(R.id.btn_clear_history)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        if (savedInstanceState != null) {
            searchString = savedInstanceState.getString(SEARCH_STRING, SEARCH)
            inputEditText.setText(searchString)
        }

        adapter = TrackAdapter(tracks) { track ->
            history.addTrack(track)
            historyAdapter.notifyDataSetChanged()
        }

        historyAdapter = TrackAdapter(history.getHistory()) { track ->
            history.addTrack(track)
            historyAdapter.notifyDataSetChanged()
        }

        trackRecycler.layoutManager = LinearLayoutManager(this)
        trackRecycler.adapter = adapter

        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyRecycler.adapter = historyAdapter

        clearButton.setOnClickListener {
            inputEditText.setText("")
            hideKeyboard()
            tracks.clear()
            adapter.notifyDataSetChanged()
            refresh.visibility = View.GONE
            placeholderMessage.visibility = View.GONE
            imageError.visibility = View.GONE
            val historyTracks = history.getHistory()
            if (historyTracks.isNotEmpty()) {
                historyAdapter.notifyDataSetChanged()
                historyAdapter.setTracks(historyTracks)
                historySection.visibility = View.VISIBLE
            } else {
                historySection.visibility = View.GONE
            }
        }

        refresh.setOnClickListener {
            search(sanitizeText(inputEditText.text.toString()))
        }

        btnBack.setOnClickListener {
            finish()
        }

        inputEditText.post {
            inputEditText.requestFocus()
        }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && inputEditText.text.isEmpty()) {
                val historyTracks = history.getHistory()
                if (historyTracks.isNotEmpty()) {
                    historyAdapter.setTracks(historyTracks)
                    historyAdapter.notifyDataSetChanged()
                    historySection.visibility = View.VISIBLE
                } else {
                    historySection.visibility = View.GONE
                }
            } else {
                historySection.visibility = View.GONE
            }
        }

        clearHistoryButton.setOnClickListener {
            history.clear()
            historyAdapter.notifyDataSetChanged()
            historySection.visibility = View.GONE
        }

        inputEditText.doOnTextChanged { text, _, _, _ ->
            clearButton.isVisible = !text.isNullOrEmpty()
            searchString = text?.toString().orEmpty()
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                search(sanitizeText(inputEditText.text.toString()))
                true
            } else {
                false
            }
        }
    }

    private fun search(query: String) {
        if (query.isNotEmpty()) {
            itunesAPI.search(query).enqueue(object : Callback<TrackResponse> {
                override fun onResponse(
                    call: Call<TrackResponse>, response: Response<TrackResponse>
                ) {
                    if (response.isSuccessful) {
                        tracks.clear()
                        val results = response.body()?.results.orEmpty()
                        if (results.isNotEmpty()) {
                            tracks.addAll(results)
                            placeholderMessage.visibility = View.GONE
                            imageError.visibility = View.GONE
                            refresh.visibility = View.GONE
                            historySection.visibility = View.GONE
                            adapter.setTracks(tracks)
                            adapter.notifyDataSetChanged()
                            trackRecycler.visibility = View.VISIBLE
                        } else {
                            tracks.clear()
                            adapter.notifyDataSetChanged()
                            placeholderMessage.text = getString(R.string.nothing_found)
                            placeholderMessage.visibility = View.VISIBLE
                            imageError.setImageResource(R.drawable.image_nothing_found)
                            imageError.visibility = View.VISIBLE
                            refresh.visibility = View.GONE
                            historySection.visibility = View.GONE
                        }
                    } else {
                        showErrorState()
                    }
                }

                override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                    showErrorState()
                }
            })
        }
    }

    private fun showErrorState() {
        placeholderMessage.text = getString(R.string.no_internet)
        imageError.setImageResource(R.drawable.image_no_internet)
        placeholderMessage.visibility = View.VISIBLE
        imageError.visibility = View.VISIBLE
        refresh.visibility = View.VISIBLE
        historySection.visibility = View.GONE
    }

    private fun sanitizeText(text: String): String {
        return text.replace(Regex("[^\\p{L}\\p{N}.&\\-'/ ]+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_STRING, searchString)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchString = savedInstanceState.getString(SEARCH_STRING, SEARCH)
    }
}
