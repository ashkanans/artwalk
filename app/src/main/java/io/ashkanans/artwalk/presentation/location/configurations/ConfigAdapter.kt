package io.ashkanans.artwalk.presentation.location.configurations

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager.widget.PagerAdapter
import io.ashkanans.artwalk.R
import io.ashkanans.artwalk.domain.model.PlaceType

class ConfigAdapter(
    private val context: Context,
    private val modelArrayList: ArrayList<ConfigModel>,
    private val placeTypes: List<PlaceType>?,
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
                // Inflate the first layout
                LayoutInflater.from(context)
                    .inflate(R.layout.places_types_checkbox, container, false)
            }

            VIEW_TYPE_TWO -> {
                // Inflate the second layout
                LayoutInflater.from(context).inflate(R.layout.plan_your_time, container, false)
            }

            else -> {
                // Default case (if needed)
                LayoutInflater.from(context)
                    .inflate(R.layout.places_types_checkbox, container, false)
            }
        }

        // Find views and set data based on the layout
        when (viewType) {
            VIEW_TYPE_ONE -> {
                addPlaceTypes(view)
            }

            VIEW_TYPE_TWO -> {
                addPlaningData(view)
            }
            // Handle other view types if needed
        }

        view.setOnClickListener {
            Toast.makeText(context, "Hello!", Toast.LENGTH_SHORT).show()
        }

        container.addView(view, position)
        return view
    }

    private fun addPlaningData(view: View) {
        // Find ExpandableListViews
        val originExpandableListView =
            view.findViewById<ExpandableListView>(R.id.originExpandableListView)
        val destinationExpandableListView =
            view.findViewById<ExpandableListView>(R.id.destinationExpandableListView)

        // Set up data for Origin ExpandableListView
        val originGroups = listOf("Origin")
        val originChildren = listOf(
            listOf("Enter Address or Name of Place")
        )
        val originAdapter = createExpandableListAdapter(originGroups[0]) // Pass single group title
        originExpandableListView.setAdapter(originAdapter)

        // Set up data for Destination ExpandableListView
        val destinationGroups = listOf("Destination")
        val destinationChildren = listOf(
            listOf("Enter Address or Name of Place")
        )
        val destinationAdapter =
            createExpandableListAdapter(destinationGroups[0]) // Pass single group title
        destinationExpandableListView.setAdapter(destinationAdapter)
    }

    private fun createExpandableListAdapter(groupTitle: String): BaseExpandableListAdapter {
        val groups = listOf(groupTitle)
        val children = listOf(listOf("Enter Address or Name of Place"))

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
                val childEditText = view.findViewById<EditText>(R.id.childEditText)
                val childButton = view.findViewById<Button>(R.id.childButton)
                childEditText.hint = getChild(groupPosition, childPosition) as String
                // Add button functionality
                childButton.setOnClickListener {
                    // Implement logic for setting current location
                    Toast.makeText(context, "Set current location clicked", Toast.LENGTH_SHORT)
                        .show()
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
            for (place in placeTypes) {
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