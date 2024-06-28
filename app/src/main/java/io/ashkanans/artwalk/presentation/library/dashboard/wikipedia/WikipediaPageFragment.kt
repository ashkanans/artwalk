package io.ashkanans.artwalk.presentation.library.dashboard.wikipedia

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.ashkanans.artwalk.R
import io.ashkanans.artwalk.domain.repository.wikipedia.WikipediaRepositoryUsage
import java.util.Locale


class WikipediaPageFragment : Fragment(), TextToSpeech.OnInitListener {

    private lateinit var pageTitleTextView: TextView
    private lateinit var sectionsRecyclerView: RecyclerView
    private lateinit var readButton: Button
    private lateinit var tts: TextToSpeech
    private var isReading = false
    private var sections: List<String> = emptyList()
    private var currentSectionIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wikipedia_page, container, false)
        pageTitleTextView = view.findViewById(R.id.pageTitle)
        sectionsRecyclerView = view.findViewById(R.id.sectionsRecyclerView)
        readButton = view.findViewById(R.id.readButton)
        tts = TextToSpeech(context, this)

        sectionsRecyclerView.layoutManager = LinearLayoutManager(context)

        var landmarkName = arguments?.getString("landmarkName")
        val pageTitle = arguments?.getString("pageTitle") ?: landmarkName
        if (pageTitle != null) {
            fetchWikipediaPage(pageTitle)
        }

        readButton.setOnClickListener {
            if (isReading) {
                stopReading()
            } else {
                startReading()
            }
        }

        return view
    }

    private fun fetchWikipediaPage(title: String) {
        val wikipediaRepositoryUsage = WikipediaRepositoryUsage()
        wikipediaRepositoryUsage.fetchWikipediaPage(title) { wikipediaPage ->
            if (wikipediaPage != null) {
                pageTitleTextView.text = wikipediaPage.title
                sections = wikipediaPage.sections.values.toList()
                sectionsRecyclerView.adapter = SectionsAdapter(sections)
            }
        }
    }

    private fun startReading() {
        if (sections.isNotEmpty()) {
            isReading = true
            readButton.text = "Stop"
            readNextSection()
        }
    }

    private fun stopReading() {
        if (tts.isSpeaking) {
            tts.stop()
        }
        isReading = false
        readButton.text = "Read"
        currentSectionIndex = 0
    }

    private fun readNextSection() {
        if (currentSectionIndex < sections.size) {
            tts.speak(sections[currentSectionIndex], TextToSpeech.QUEUE_FLUSH, null, null)
            currentSectionIndex++
        } else {
            stopReading()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            tts.setOnUtteranceProgressListener(object :
                android.speech.tts.UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    if (isReading) {
                        readNextSection()
                    }
                }

                override fun onError(utteranceId: String?) {}
            })
        }
    }

    override fun onDestroy() {
        if (tts.isSpeaking) {
            tts.stop()
        }
        tts.shutdown()
        super.onDestroy()
    }

    class SectionsAdapter(private val sections: List<String>) :
        RecyclerView.Adapter<SectionsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val sectionTextView: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.sectionTextView.text = sections[position]
        }

        override fun getItemCount(): Int {
            return sections.size
        }
    }
}
