package dev.leonlatsch.photok.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.repositories.PasswordRepository
import dev.leonlatsch.photok.other.PrefManager
import kotlinx.android.synthetic.main.activity_start.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StartActivity : AppCompatActivity() {

    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var passwordRepository: PasswordRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // TODO: Show OnBoarding if first start

        checkVaultState()
    }

    private fun checkVaultState() = lifecycleScope.launch {
        val password = passwordRepository.getPassword()?.password
        if (password == null) {
            startNavHostFragment.findNavController()
                .navigate(R.id.action_unlockFragment_to_setupFragment)
        }
    }
}