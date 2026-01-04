


package dev.leonlatsch.photok.news.newfeatures.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.leonlatsch.photok.databinding.ItemNewsBinding
import dev.leonlatsch.photok.news.newfeatures.ui.model.NewFeatureViewData

/**
 * Adapter for news entries in the [NewFeaturesDialog].
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
class NewFeaturesAdapter(
    private val featureViewData: List<NewFeatureViewData>,
) : RecyclerView.Adapter<NewFeaturesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewFeaturesViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context))
        return NewFeaturesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewFeaturesViewHolder, position: Int) {
        holder.bindTo(featureViewData[position])
    }

    override fun getItemCount(): Int = featureViewData.size
}

package dev.leonlatsch.photok.news.newfeatures.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.leonlatsch.photok.databinding.ItemNewsBinding
import dev.leonlatsch.photok.news.newfeatures.ui.model.NewFeatureViewData

/**
 * Adapter for news entries in the [NewFeaturesDialog].
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
class NewFeaturesAdapter(
    private val featureViewData: List<NewFeatureViewData>,
) : RecyclerView.Adapter<NewFeaturesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewFeaturesViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context))
        return NewFeaturesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewFeaturesViewHolder, position: Int) {
        holder.bindTo(featureViewData[position])
    }

    override fun getItemCount(): Int = featureViewData.size
}