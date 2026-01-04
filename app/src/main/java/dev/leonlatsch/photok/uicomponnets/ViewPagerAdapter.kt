


package dev.leonlatsch.photok.uicomponnets

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import dev.leonlatsch.photok.other.extensions.empty

/**
 * Adapter for ViewPager with Fragments.
 *
 * @author Leon Latsch
 * @since 1.0.0
 */
class ViewPagerAdapter(
    fragmentManager: FragmentManager
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragmentList = arrayListOf<Fragment>()
    private val fragmentTitleList = arrayListOf<String>()

    override fun getCount(): Int = fragmentList.size

    override fun getItem(position: Int): Fragment = fragmentList.get(position)

    override fun getPageTitle(position: Int): CharSequence? = fragmentTitleList[position]

    /**
     * Add a [Fragment] to the view pager.
     */
    fun addFragment(fragment: Fragment) = addFragment(fragment, String.empty)

    private fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitleList.add(title)
    }
}

package dev.leonlatsch.photok.uicomponnets

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import dev.leonlatsch.photok.other.extensions.empty

/**
 * Adapter for ViewPager with Fragments.
 *
 * @author Leon Latsch
 * @since 1.0.0
 */
class ViewPagerAdapter(
    fragmentManager: FragmentManager
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragmentList = arrayListOf<Fragment>()
    private val fragmentTitleList = arrayListOf<String>()

    override fun getCount(): Int = fragmentList.size

    override fun getItem(position: Int): Fragment = fragmentList.get(position)

    override fun getPageTitle(position: Int): CharSequence? = fragmentTitleList[position]

    /**
     * Add a [Fragment] to the view pager.
     */
    fun addFragment(fragment: Fragment) = addFragment(fragment, String.empty)

    private fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitleList.add(title)
    }
}