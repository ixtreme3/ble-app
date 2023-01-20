package ru.astar.bluetoothcontrol

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.astar.bluetoothcontrol.databinding.FragmentDevicesBinding

/*
В соответствии с паттерном MVVM мы делегируем всю логику ViewModel'и. Активити и фрагменты
отвечают только за отображение данных. Все вычисления и весь код содержащий логику работы
приложения выносится во ViewModel. Нажата кнопка - делегируем во ViewModel, пусть сама разбирается.
Важно: активити и фрагменты не имеют права менять что-либо во ViewModel
 */
class DevicesFragment : Fragment(), DevicesAdapter.Callback {

    private var _binding: FragmentDevicesBinding? = null
    private val binding: FragmentDevicesBinding get() = _binding!!

    private val devicesAdapter = DevicesAdapter()

    /*
    Особый способ создать viewModel, чтобы она не пересоздавалась, например, при перевороте экрана
    */
    private val viewModel: DevicesViewModel by viewModels {
        DeviceViewModelFactory((requireActivity().application as App).adapterProvider)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.devicesRecycler.apply {
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
            layoutManager = LinearLayoutManager(requireContext())
            adapter = devicesAdapter
        }

        devicesAdapter.addCallback(this)

        binding.fabStartScan.setOnClickListener {
            checkLocation.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onStart() {
        super.onStart()
        subscribeOnViewModel()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopScan()
    }

    private fun subscribeOnViewModel() {
        /* Подписка на изменение данных - код сработает в тот момент, когда devices viewModel
        поменяется
        Важно: от LiveData не нужно отписываться самостоятельно
        */
        viewModel.devices.observe(viewLifecycleOwner) { devices ->
            devicesAdapter.update(devices)
        }
    }

    override fun onItemClick(device: BluetoothDevice) {
        parentFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.containerFragment, ControlFragment.newInstance(device.address))
            .commit()
    }

    private val checkLocation = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startScan()
        }
    }

    companion object {
        fun newInstance() = DevicesFragment()
    }
}