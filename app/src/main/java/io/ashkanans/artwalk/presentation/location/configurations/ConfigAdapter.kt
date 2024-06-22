package io.ashkanans.artwalk.presentation.location.configurations

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.BaseExpandableListAdapter
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.ashkanans.artwalk.R
import io.ashkanans.artwalk.domain.model.DataModel
import io.ashkanans.artwalk.domain.model.PlaceType
import io.ashkanans.artwalk.presentation.location.MapsFragment

class ConfigAdapter(
    private val context: Context,
    private val modelArrayList: ArrayList<ConfigModel>,
    private val placeTypes: List<PlaceType>?,
    private val fragment: MapsFragment // Add this line
) : PagerAdapter() {

    override fun getCount(): Int {
        return modelArrayList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    private fun getItemViewType(position: Int): Int {
        return modelArrayList[position].viewType
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val viewType = getItemViewType(position)
        val view: View = when (viewType) {
            VIEW_TYPE_ONE -> {
                LayoutInflater.from(context)
                    .inflate(R.layout.places_types_checkbox, container, false)
            }

            VIEW_TYPE_TWO -> {
                LayoutInflater.from(context).inflate(R.layout.plan_your_time, container, false)
            }

            else -> {
                LayoutInflater.from(context)
                    .inflate(R.layout.places_types_checkbox, container, false)
            }
        }

        // Find views and set data based on the layout
        when (viewType) {
            VIEW_TYPE_ONE -> {
                addPlaceTypes(view)
                val saveAndCloseButton = view.findViewById<Button>(R.id.PTCsaveAndCloseButton)
                saveAndCloseButton.setOnClickListener {
                    updatePlaceTypes(view)
                    Toast.makeText(context, "Preferences saved!", Toast.LENGTH_SHORT).show()
                    fragment.toggleCardVisibility()
                    fragment.mapHandler.drawPlacesMarkers()
                }

            }

            VIEW_TYPE_TWO -> {
                addPlaningData(view)
                setupKeyboardVisibilityListener(view)
            }
        }

        view.setOnClickListener {
            Toast.makeText(context, "Hello!", Toast.LENGTH_SHORT).show()
        }

        container.addView(view, position)
        return view
    }

    private fun updatePlaceTypes(view: View) {
        val parentLayout = view.findViewById<LinearLayout>(R.id.parentLinearLayout)

        // Iterate through each child in the parent layout
        for (i in 0 until parentLayout.childCount) {
            val linearLayout = parentLayout.getChildAt(i) as LinearLayout
            val checkBox =
                linearLayout.findViewWithTag<androidx.appcompat.widget.AppCompatCheckBox>("checkbox_$i")

            // Update the isChecked value in placeTypes
            placeTypes?.get(i)?.isChecked = checkBox.isChecked
        }

        // Optionally, update the placeTypesModel in DataModel
        DataModel.getPlaceTypesModel { model ->
            model?.dataModel = placeTypes ?: listOf()
            DataModel.setPlaceTypesModel(model!!)
        }

    }

    private fun setupKeyboardVisibilityListener(view: View) {
        val rootView = view.rootView
        val saveAndCloseButton = view.findViewById<Button>(R.id.PYTsaveAndCloseButton)
        val headerCardView = view.findViewById<CardView>(R.id.headerCardView)

        rootView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            private var initialHeight = -1
            private var wasKeyboardOpened = false

            override fun onGlobalLayout() {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val visibleHeight = rect.bottom - rect.top

                if (initialHeight == -1) {
                    initialHeight = visibleHeight
                }

                // Check if the height difference indicates the keyboard is opened
                val heightDifference = initialHeight - visibleHeight
                val isKeyboardOpened = heightDifference > initialHeight * 0.15

                if (isKeyboardOpened != wasKeyboardOpened) {
                    wasKeyboardOpened = isKeyboardOpened

                    if (isKeyboardOpened) {
                        // Keyboard is opened
                        animateViewOut(saveAndCloseButton)
                        animateViewOut(headerCardView)
                    } else {
                        // Keyboard is closed
                        animateViewIn(saveAndCloseButton)
                        animateViewIn(headerCardView)
                    }
                }
            }

            private fun animateViewOut(view: View) {
                val translationY =
                    ObjectAnimator.ofFloat(view, "translationY", 0f, view.height.toFloat())
                translationY.duration = 300
                translationY.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                    }
                })
                translationY.start()
            }

            private fun animateViewIn(view: View) {
                view.visibility = View.VISIBLE
                val translationY =
                    ObjectAnimator.ofFloat(view, "translationY", view.height.toFloat(), 0f)
                translationY.duration = 300
                translationY.start()
            }
        })
    }

    private fun addPlaningData(view: View) {
        val expandableListView = view.findViewById<ExpandableListView>(R.id.ExpandableListView)
        val groups = listOf("Origin", "Destination")
        val children = listOf(
            listOf("Address or Name"),
            listOf("Address or Name")
        )
        val adapter = createExpandableListAdapter(groups, children)
        expandableListView.setAdapter(adapter)
    }

    private fun createExpandableListAdapter(
        groups: List<String>,
        children: List<List<String>>
    ): BaseExpandableListAdapter {
        return object : BaseExpandableListAdapter() {
            override fun getGroupCount(): Int {
                return groups.size
            }

            override fun getChildrenCount(groupPosition: Int): Int {
                return children[groupPosition].size
            }

            override fun getGroup(groupPosition: Int): Any {
                return groups[groupPosition]
            }

            override fun getChild(groupPosition: Int, childPosition: Int): Any {
                return children[groupPosition][childPosition]
            }

            override fun getGroupId(groupPosition: Int): Long {
                return groupPosition.toLong()
            }

            override fun getChildId(groupPosition: Int, childPosition: Int): Long {
                return childPosition.toLong()
            }

            override fun hasStableIds(): Boolean {
                return true
            }

            override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
                return true
            }

            override fun getGroupView(
                groupPosition: Int,
                isExpanded: Boolean,
                convertView: View?,
                parent: ViewGroup?
            ): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.expandable_list_group, parent, false)
                val groupTextView = view.findViewById<TextView>(R.id.groupTitleTextView)
                groupTextView.text = getGroup(groupPosition) as String
                return view
            }

            override fun getChildView(
                groupPosition: Int,
                childPosition: Int,
                isLastChild: Boolean,
                convertView: View?,
                parent: ViewGroup?
            ): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.expandable_list_item, parent, false)

                val textInputLayout = view.findViewById<TextInputLayout>(R.id.textInputLayout)
                val editText = view.findViewById<TextInputEditText>(R.id.editText)

                if (getChild(groupPosition, childPosition) == "Set current location") {
                    textInputLayout.visibility = View.GONE

                } else {
                    textInputLayout.visibility = View.VISIBLE
                    editText.hint = getChild(groupPosition, childPosition) as String
                }

                return view
            }
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    companion object {
        const val VIEW_TYPE_ONE = 1
        const val VIEW_TYPE_TWO = 2
        // Add more view types if needed
    }

    private fun addPlaceTypes(view: View) {
        val parentLayout = view.findViewById<LinearLayout>(R.id.parentLinearLayout)

        // Iterate over the place types and create views dynamically
        if (placeTypes != null) {
            for ((index, place) in placeTypes.withIndex()) {
                // Create the main LinearLayout
                val linearLayout = LinearLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 0, 0, 8.dpToPx()) // Convert 8dp to pixels
                    gravity = Gravity.CENTER_VERTICAL
                }

                // Create the ImageView
                val imageView = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setImageResource(R.drawable.baseline_location_on_24)
                    setColorFilter(Color.parseColor(place.color))
                }

                // Create the TextView
                val textView = TextView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginStart = 8.dpToPx() // Convert 8dp to pixels
                    }
                    text = place.type
                    setTextAppearance(com.google.maps.android.R.style.amu_Bubble_TextAppearance_Dark)
                }

                // Create the RelativeLayout for the CheckBox
                val relativeLayout = RelativeLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        weight = 1f
                    }
                }

                // Create the CheckBox
                val checkBox = androidx.appcompat.widget.AppCompatCheckBox(context).apply {
                    isChecked = place.isChecked
                    layoutParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    }
                    tag = "checkbox_$index" // Set a unique tag for each checkbox
                }

                // Add CheckBox to RelativeLayout
                relativeLayout.addView(checkBox)

                // Add ImageView, TextView, and RelativeLayout to main LinearLayout
                linearLayout.addView(imageView)
                linearLayout.addView(textView)
                linearLayout.addView(relativeLayout)

                // Add the main LinearLayout to the parent layout
                parentLayout.addView(linearLayout)
            }
        }
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}