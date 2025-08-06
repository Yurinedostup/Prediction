package com.example.prediction.screens.game

import android.os.*
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.prediction.databinding.GameFragmentBinding
import android.content.Context
import android.os.Vibrator
import android.os.VibratorManager // Для API 31+
import android.os.Build

class game_fragment : Fragment() {

    private lateinit var binding: GameFragmentBinding
    private lateinit var viewModel: GameViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GameFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)

        binding.apply {
            gameViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
        viewModel.eventGameFinish.observe(viewLifecycleOwner) { isFinished ->
            if (isFinished) {
                findNavController().navigate(
                    game_fragmentDirections.actionGameFragmentToScoreFragment(viewModel.score.value ?: 0)
                )
                viewModel.onGameFinishComplete()
            }
        }

        viewModel.eventBuzz.observe(viewLifecycleOwner) { buzzType ->
            if (buzzType != GameViewModel.BuzzType.NO_BUZZ) {
                buzz(buzzType.pattern)
                viewModel.onBuzzComplete()
            }
        }
    }

    private fun buzz(pattern: LongArray) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Для Android 12+ (API 31+)
            val vibratorManager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            // Для версий ниже Android 12
            @Suppress("DEPRECATION")
            requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
}