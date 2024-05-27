import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.ashkanans.artwalk.R

class SignuptabFragment : Fragment() {
    private lateinit var email: View
    private lateinit var pass: View
    private lateinit var confirmPass: View
    private lateinit var number: View
    private lateinit var signUp: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.signup_tab_fragment, container, false)

        email = root.findViewById(R.id.email)
        pass = root.findViewById(R.id.password)
        confirmPass = root.findViewById(R.id.confirm_password)
        number = root.findViewById(R.id.number)
        signUp = root.findViewById(R.id.signup)

        val v = 0f

        email.translationX = 800f
        pass.translationX = 800f
        confirmPass.translationX = 800f
        number.translationX = 800f
        signUp.translationX = 800f

        email.alpha = v
        pass.alpha = v
        confirmPass.alpha = v
        number.alpha = v
        signUp.alpha = v

        email.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(300).start()
        pass.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(500).start()
        confirmPass.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(500).start()
        number.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(700).start()
        signUp.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(700).start()

        return root
    }
}
