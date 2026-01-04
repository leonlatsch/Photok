


package dev.leonlatsch.photok.recoverymenu

import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.ActivityRecoveryMenuBinding
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.bindings.BindableActivity
import javax.inject.Inject

@AndroidEntryPoint
class RecoveryMenuActivity :
    BindableActivity<ActivityRecoveryMenuBinding>(R.layout.activity_recovery_menu) {

    @Inject
    override lateinit var config: Config

    @Inject
    lateinit var navigator: RecoveryMenuNavigator

    private val viewModel: RecoveryMenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigationEvent.observe(this) {
            navigator.navigate(it, this)
        }
    }

    override fun bind(binding: ActivityRecoveryMenuBinding) {
        super.bind(binding)
        binding.viewModel = viewModel
    }
}

package dev.leonlatsch.photok.recoverymenu

import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.ActivityRecoveryMenuBinding
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.bindings.BindableActivity
import javax.inject.Inject

@AndroidEntryPoint
class RecoveryMenuActivity :
    BindableActivity<ActivityRecoveryMenuBinding>(R.layout.activity_recovery_menu) {

    @Inject
    override lateinit var config: Config

    @Inject
    lateinit var navigator: RecoveryMenuNavigator

    private val viewModel: RecoveryMenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigationEvent.observe(this) {
            navigator.navigate(it, this)
        }
    }

    override fun bind(binding: ActivityRecoveryMenuBinding) {
        super.bind(binding)
        binding.viewModel = viewModel
    }
}