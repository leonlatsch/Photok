package dev.leonlatsch.photok.settings.ui.credits

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentCreditsBinding
import dev.leonlatsch.photok.other.systemBarsPadding
import dev.leonlatsch.photok.uicomponnets.bindings.BindableFragment

/**
 * Fragment for displaying credits for icons and contributors.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
class CreditsFragment : BindableFragment<FragmentCreditsBinding>(R.layout.fragment_credits) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.systemBarsPadding()
        super.onViewCreated(view, savedInstanceState)

        binding.creditsToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        requireActivity().assets.open(CONTRIBUTORS_FILE).let {
            val json = String(it.readBytes())
            val listType = object : TypeToken<ArrayList<CreditEntry?>?>() {}.type
            val entries: ArrayList<CreditEntry> = Gson().fromJson(json, listType)
            entries.add(0, CreditEntry.createHeader())
            entries.add(CreditEntry.createFooter())

            val layoutManager = LinearLayoutManager(requireContext())
            binding.creditsRecycler.layoutManager = layoutManager
            binding.creditsRecycler.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    layoutManager.orientation
                )
            )
            binding.creditsRecycler.adapter = CreditsAdapter(entries, openWebsite)
        }
    }

    private val openWebsite: (url: String?) -> Unit = {
        if (it != null) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(it)
            startActivity(intent)
        }
    }

    companion object {
        const val CONTRIBUTORS_FILE = "contributors.json"
    }
}