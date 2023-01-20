package ru.astar.bluetoothcontrol

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.bluetooth.BluetoothDevice
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.astar.bluetoothcontrol.databinding.ItemDeviceBinding

// RecyclerView вызывает методы данного класса для связывания представлений с их данными
// "Связывает ваши данные с представлениями ViewHolder"
// "Adapter создает объекты ViewHolder по мере необходимости, а также устанавливает данные для этих представлений."
class DevicesAdapter : RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder>() {

    private val items = mutableListOf<BluetoothDevice>()
    private var callback: Callback? = null

    @SuppressLint("NotifyDataSetChanged")
    fun update(items: List<BluetoothDevice>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun addCallback(callback: Callback) {
        this.callback = callback
    }

    /* Ключевой метод
    "RecyclerView вызывает этот метод всякий раз, когда ему нужно создать новый ViewHolder.
    Метод создает и инициализирует ViewHolder и связанное с ним представление, но не заполняет
    содержимое представления — ViewHolder еще не привязан к конкретным данным."
    "Процесс связывания представлений с их данными называется binding."
    */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    /* Ключевой метод
    RecyclerView вызывает этот метод, чтобы связать ViewHolder с данными. Метод извлекает
    соответствующие данные и использует их для заполнения макета держателя представления.
    */
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    /* Ключевой метод
    RecyclerView вызывает этот метод, чтобы получить размер набора данных. Например, в приложении
    адресной книги это может быть общее количество адресов. RecyclerView использует это, чтобы
    определить, когда больше нет элементов, которые можно отобразить.
    */
    override fun getItemCount() = items.size

    /* Определяет каждый элемент списка. Изначально не содержит никаких данных.
    "Предоставляет все функциональные возможности для ваших элементов списка"
     */
    inner class DeviceViewHolder(private val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("MissingPermission")
        fun bind(item: BluetoothDevice) {
            binding.container.setOnClickListener { callback?.onItemClick(item) }

            binding.apply {
                textName.text = item.name ?: textName.context.getString(R.string.unnamed_device)
                textAddress.text = item.address
            }
        }
    }

    interface Callback {
        fun onItemClick(device: BluetoothDevice)
    }
}
