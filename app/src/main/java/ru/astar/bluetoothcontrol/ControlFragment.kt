package ru.astar.bluetoothcontrol

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import ru.astar.bluetoothcontrol.databinding.FragmentControlBinding

/*
В соответствии с паттерном MVVM мы делегируем всю логику ViewModel'и. Активити и фрагменты
отвечают только за отображение данных. Все вычисления и весь код содержащий логику работы
приложения выносится во ViewModel. Нажата кнопка - делегируем во ViewModel, пусть сама разбирается.
Важно: активити и фрагменты не имеют права менять что-либо во ViewModel
 */
class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding: FragmentControlBinding get() = _binding!!

    private val viewModel: ControlViewModel by viewModels {
        ControlViewModelFactory((requireActivity().application as App).adapterProvider)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.angleServo.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar, p1: Int, p2: Boolean) {}
//
//            override fun onStartTrackingTouch(seekBar: SeekBar) {}
//
//            override fun onStopTrackingTouch(seekBar: SeekBar) {
//                viewModel.setAngleServo(seekBar.progress) // ошибка
//            }
//        })
    }

    override fun onStop() {
        super.onStop()
        viewModel.disconnect()
    }

    /* Вызывается, когда фрагмент виден пользователю и активно выполняется. */
    override fun onResume() {
        super.onResume()

        val deviceAddress = requireArguments().getString(KEY_DEVICE_ADDRESS)!!
        viewModel.connect(deviceAddress)
    }

    companion object {
        private const val KEY_DEVICE_ADDRESS = "key_device_address"
        @JvmStatic
        fun newInstance(deviceAddress: String) = ControlFragment().apply {
            arguments = Bundle().apply {
                putString(KEY_DEVICE_ADDRESS, deviceAddress)
            }
        }
    }
}