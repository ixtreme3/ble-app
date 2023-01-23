package ru.astar.bluetoothcontrol

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.bluetooth.BluetoothDevice
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.astar.bluetoothcontrol.databinding.FragmentDevicesBinding

/* DevicesFragment отвечает за отрисовку списка устройств

Важно: В соответствии с паттерном MVVM мы делегируем всю логику ViewModel'и. Активити и фрагменты
отвечают только за отображение данных. Все вычисления и весь код содержащий логику работы
приложения выносится во ViewModel. Нажата кнопка - делегируем во ViewModel, пусть сама разбирается.
Важно: активити и фрагменты не имеют права менять что-либо во ViewModel
 */
class DevicesFragment : Fragment(), DevicesAdapter.Callback {
    private var _binding: FragmentDevicesBinding? = null
    private val binding: FragmentDevicesBinding get() = _binding!!

    /* Создаем devicesAdapter для работы с RecyclerView */
    private val devicesAdapter = DevicesAdapter()

    /*
    Особый способ создать viewModel, чтобы она не пересоздавалась, например, при перевороте экрана
    - Во viewModel будет лежать список найденных устройств, а также храниться вся логика поиска
    bluetooth-устройств
    */
    private val viewModel: DevicesViewModel by viewModels {
        DeviceViewModelFactory((requireActivity().application as App).adapterProvider)
    }

    /* Вызывается для создания компонентов внутри фрагмента */
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

    /* Вызывается сразу после onCreateView() */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Привязываем devicesAdapter к RecyclerView
        - LinearLayoutManager указывает на то, что найденные устройства будут отображаться в виде
        простого списка
        - addItemDecoration добавляет разделительную черту
        */
        binding.devicesRecycler.apply {
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
            layoutManager = LinearLayoutManager(requireContext())
            adapter = devicesAdapter
        }

        /* Передаем devicesAdapter ссылку на текущий DevicesFragment, чтобы потом в devicesAdapter
        обратиться по этой ссылке к методу onItemClick
        */
        devicesAdapter.addCallback(this)

        /* К кнопке поиска привязываем проверку на то, выданы ли разрешения для поиска устройств.
        - Если уже выданы - во viewModel запускается процесс поиска новых устройств.
        - Если еще не выданы - показываем всплывающее окно с просьбой выдачи разрешений и запускаем
        процесс поиска новых устройств.
        */
        binding.fabStartScan.setOnClickListener {
            checkLocation.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /* Вызывается, когда фрагмент становится видимым после запуска такого же метода в родительской
    активности
    */
    override fun onStart() {
        super.onStart()
        subscribeToViewModel()
    }

    /* Вызывается когда фрагмент больше не является видимым и вместе с представлением переходит
    в состояние CREATED
    */
    override fun onStop() {
        super.onStop()
        viewModel.stopScan()
    }

    private fun subscribeToViewModel() {
        /* Подписка на изменение данных - код сработает в тот момент, когда devices viewModel
        поменяется
        Важно: от LiveData не нужно отписываться самостоятельно
        */
        viewModel.devices.observe(viewLifecycleOwner) { devices ->
            devicesAdapter.update(devices)
        }
    }

    /* Обработчик нажатия на элемент списка. При нажатии на какое-то устройство из списка текущий
    фрагмент (т.е. фрагмент отвечающий за список устройств) меняется на фрагмент, содержащий главный
    экран приложения.
    Важно: Соединение с выбранным устройством (device.address) обрабатывается во viewModel'и
    главного экрана.
    */
    override fun onItemClick(device: BluetoothDevice) {
        parentFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.containerFragment, MainFragment.newInstance(device.address))
            .commit()
    }

    private val checkLocation = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startScan()
        }
    }

    /* Мини-фабрикуа для этого фрагмента */
    companion object {
        fun newInstance() = DevicesFragment()
    }
}