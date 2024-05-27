import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class LoginAdapter(
    fragmentActivity: FragmentActivity,
    private val context: Context,
    private val totalTabs: Int
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return totalTabs
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LogintabFragment()
            1 -> SignuptabFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
