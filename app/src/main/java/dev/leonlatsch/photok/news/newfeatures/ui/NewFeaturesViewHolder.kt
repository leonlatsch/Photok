package dev.leonlatsch.photok.news.newfeatures.ui

import androidx.recyclerview.widget.RecyclerView
import dev.leonlatsch.photok.databinding.ItemNewsBinding
import dev.leonlatsch.photok.news.newfeatures.ui.model.NewFeatureViewData

/**
 * ViewHolder for displaying news. Used by [NewFeaturesAdapter].
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
class NewFeaturesViewHolder(
    private val binding: ItemNewsBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bindTo(viewData: NewFeatureViewData) {
        binding.itemNewsTitle.text = viewData.title
        binding.itemNewsSummary.text = viewData.summary
    }
}