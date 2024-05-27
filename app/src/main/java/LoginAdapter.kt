import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class LoginAdapter(
    fm: FragmentManager,
    private val context: Context,
    private val totalTabs: Int
) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return totalTabs
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> LogintabFragment()
            1 -> SignuptabFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
