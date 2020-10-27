/*
 *   Copyright 2020 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.leonlatsch.photok.ui.start.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentOnboardingBinding
import dev.leonlatsch.photok.settings.Config
import dev.leonlatsch.photok.ui.components.BindableFragment
import kotlinx.android.synthetic.main.fragment_onboarding.*
import javax.inject.Inject

/**
 * On boarding fragment.
 * Used as a "tutorial".
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class OnBoardingFragment :
    BindableFragment<FragmentOnboardingBinding>(R.layout.fragment_onboarding) {

    @Inject
    lateinit var config: Config

    private var isLastPage = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(Fragment(R.layout.fragment_onboarding_slide_1))
        viewPagerAdapter.addFragment(Fragment(R.layout.fragment_onboarding_slide_2))
        viewPagerAdapter.addFragment(Fragment(R.layout.fragment_onboarding_slide_3))
        onBoardingViewPager.adapter = viewPagerAdapter
        onBoardingViewPager.addOnPageChangeListener(onPageChangeListener)
    }

    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            isLastPage = position == 2
            onBoardingButton.text = if (isLastPage) {
                getString(R.string.onboarding_finish)
            } else {
                getString(R.string.onboarding_next)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    fun buttonClicked() {
        if (isLastPage) {
            finish()
        } else {
            onBoardingViewPager.setCurrentItem(onBoardingViewPager.currentItem + 1, true)
        }
    }

    fun finish() {
        findNavController().navigate(R.id.action_onBoardingFragment_to_setupFragment)
        config.putBoolean(Config.SYSTEM_FIRST_START, false)
    }

    override fun bind(binding: FragmentOnboardingBinding) {
        super.bind(binding)
        binding.context = this
    }
}