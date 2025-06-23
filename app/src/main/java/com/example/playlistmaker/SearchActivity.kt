package com.example.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import retrofit2.http.Query

class SearchActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView

    companion object {
        const val SEARCH_STRING = "SEARCH_STRING"
        const val SEARCH = ""
    }

    private var searchString: String = SEARCH

    private val itunesBaseUrl = "https://itunes.apple.com"
    private val retrofit = Retrofit.Builder()
        .baseUrl(itunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val itunesAPI = retrofit.create(ItunesApi::class.java)

    private val tracks = ArrayList<Track>()
    private val adapter = TrackAdapter(tracks)
    private lateinit var inputEditText: TextView
    private lateinit var imageError: ImageView
    private lateinit var placeholderMessage: TextView
    private lateinit var refresh: Button

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_STRING, searchString)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchString = savedInstanceState.getString(SEARCH_STRING, SEARCH)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val rootView = findViewById<LinearLayout>(R.id.search_root_view)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        inputEditText = findViewById<EditText>(R.id.inputEditText)
        val clearButton = findViewById<ImageView>(R.id.clearIcon)
        imageError = findViewById(R.id.iv_Error)
        placeholderMessage = findViewById(R.id.tv_Error)
        refresh = findViewById(R.id.btn_refresh)
        recycler = findViewById(R.id.rvTrack)

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
            inputEditText.setText(searchString)
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        clearButton.setOnClickListener {
            inputEditText.setText("")
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
            tracks.clear()
            adapter.notifyDataSetChanged()
            refresh.visibility = View.GONE
            placeholderMessage.visibility = View.GONE
            imageError.visibility = View.GONE
        }
        refresh.setOnClickListener {
            search(sanitizeText(inputEditText.text.toString()))
        }
        btnBack.setOnClickListener {
            finish()
        }
        inputEditText.doOnTextChanged { text, _, _, _ ->
            if (text.toString().isNotEmpty()) {
                clearButton.isVisible = true
                recycler.visibility = View.VISIBLE
            } else {
                clearButton.isVisible = false
                recycler.visibility = View.GONE
            }
            searchString = text?.toString().orEmpty()
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                search(sanitizeText(inputEditText.text.toString()))
                true
            }
            false
        }
    }

    private fun search(query: String) {
        if (query.isNotEmpty()) {
            itunesAPI.search(query).enqueue(object :
                Callback<TrackResponse> {
                override fun onResponse(
                    call: Call<TrackResponse>, response: Response<TrackResponse>
                ) {
                    if (response.code() == 200) {
                        tracks.clear()
                        if (response.body()?.results?.isNotEmpty() == true) {
                            tracks.addAll(response.body()?.results!!)
                            adapter.notifyDataSetChanged()
                            imageError.visibility = View.GONE
                            refresh.visibility = View.GONE
                            inputEditText.setText(query)
                        } else {
                            tracks.clear()
                            adapter.notifyDataSetChanged()
                            placeholderMessage.text = getString(R.string.nothing_found)
                            placeholderMessage.visibility = View.VISIBLE
                            imageError.setImageResource(R.drawable.image_nothing_found)
                            imageError.visibility = View.VISIBLE
                            refresh.visibility = View.GONE
                        }
                    } else {
                        placeholderMessage.text = getString(R.string.no_internet)
                        imageError.setImageResource(R.drawable.image_no_internet)
                        refresh.isVisible = true
                    }

                }

                override fun onFailure(call: Call<TrackResponse>, t: Throwable) {

                    placeholderMessage.text = getString(R.string.no_internet)
                    imageError.setImageResource(R.drawable.image_no_internet)
                    placeholderMessage.visibility = View.VISIBLE
                    imageError.visibility = View.VISIBLE
                    refresh.isVisible = true

                }

            })
        }
    }

    private fun sanitizeText(text: String): String {
        return text
            .replace(Regex("[^\\p{L}\\p{N}.&\\-'/ ]+"), "")
            .replace(Regex("\\s+"), " ")
            .trimStart()
            .trim()
    }
}