package com.app.samplewearapp.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.samplewearapp.util.CustomViewType.Companion.TYPE_DEFAULT
import com.app.samplewearapp.util.CustomViewType.Companion.TYPE_OPTIONS
import com.app.samplewearapp.R
import java.util.*

class CustomRecyclerAdapter(private val mDataSet: ArrayList<CustomViewType.ViewTypeData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var viewHolder: RecyclerView.ViewHolder? = null

        when (viewType) {
            TYPE_DEFAULT -> {
                viewHolder = DefaultViewHolder(LayoutInflater.from(viewGroup.context).inflate(
                            R.layout.recycler_item_default,
                            viewGroup,
                            false
                        )
                )
            }
            TYPE_OPTIONS -> {
                viewHolder = OptionsViewHolder(LayoutInflater.from(viewGroup.context).inflate(
                            R.layout.recycler_item_options,
                            viewGroup,
                            false
                        )
                )
            }
        }
        return viewHolder!!
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder.itemViewType) {
            TYPE_DEFAULT -> { }
            TYPE_OPTIONS -> {}
        }
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun getItemViewType(position: Int): Int {
        val dataLayerScreenData = mDataSet[position]
        return dataLayerScreenData.type
    }


    class DefaultViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class OptionsViewHolder(view: View) : RecyclerView.ViewHolder(view)

}