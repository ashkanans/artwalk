import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.ashkanans.artwalk.R

class LogintabFragment : Fragment() {
    private lateinit var email: View
    private lateinit var pass: View
    private lateinit var forgetPass: View
    private lateinit var login: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.login_tab_fragment, container, false)

        email = root.findViewById(R.id.email)
        pass = root.findViewById(R.id.password)
        forgetPass = root.findViewById(R.id.forget_pass)
        login = root.findViewById(R.id.login)

        val v = 0f

        email.translationX = 800f
        pass.translationX = 800f
        forgetPass.translationX = 800f
        login.translationX = 800f

        email.alpha = v
        pass.alpha = v
        forgetPass.alpha = v
        login.alpha = v

        email.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(300).start()
        pass.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(500).start()
        forgetPass.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(500).start()
        login.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(700).start()

        return root
    }
}
