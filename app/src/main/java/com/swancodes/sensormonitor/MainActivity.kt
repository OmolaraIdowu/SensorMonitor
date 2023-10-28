package com.swancodes.sensormonitor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.swancodes.sensormonitor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var proximitySensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpSensor()
        displaySensorList()

        val sensorErrorText: String = resources.getString(R.string.error)

        if (lightSensor == null) {
            binding.lightSensorText.text = sensorErrorText
            binding.lightBar.apply {
                progress = 0f
                labelText = sensorErrorText
            }
        }
        if (proximitySensor == null) {
            binding.proximitySensorText.text = sensorErrorText
            binding.proximityBar.apply {
                progress = 0f
                labelText = sensorErrorText
            }
        }

    }

    private fun setUpSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    }

    private fun displaySensorList() {
        val sensorList: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val sensorText = buildSensorListText(sensorList)
        binding.sensorText.text = sensorText
    }

    private fun buildSensorListText(sensorList: List<Sensor>): String {
        val sensorText = StringBuilder()
        for (currentSensor in sensorList) {
            sensorText.append(currentSensor.name).append(System.lineSeparator())
        }
        return sensorText.toString()
    }

    private fun brightnessLevel(brightness: Float): String {
        return when (brightness.toInt()) {
            0 -> "The room is completely dark, you can't see a thing"
            in 1..10 -> "It's very dim, you'll need some light to see"
            in 11..50 -> "The room is moderately dark, you can navigate with caution"
            in 51..5000 -> "The lighting is comfortable and moderate"
            in 5001..25000 -> "It's quite bright, perfect for most activities"
            else -> "The light is extremely bright, it may be blinding"
        }
    }

    // When new sensor data is available
    override fun onSensorChanged(event: SensorEvent?) {
        val sensorType = event?.sensor?.type
        val currentValue = event?.values?.get(0)

        when (sensorType) {
            Sensor.TYPE_LIGHT -> {
                val brightnessText = brightnessLevel(currentValue!!)
                binding.lightSensorText.text = resources.getString(R.string.light, currentValue)
                binding.lightSensorText2.text = brightnessText
                with(binding.lightBar) {
                    progress = currentValue
                    labelText = currentValue.toString()
                }
            }

            Sensor.TYPE_PROXIMITY -> {
                binding.proximitySensorText.text =
                    resources.getString(R.string.proximity, currentValue!!)
                with(binding.proximityBar) {
                    progress = currentValue
                    labelText = currentValue.toString()
                }
            }
        }
    }

    // If sensor's accuracy changes
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    // To register sensor listeners
    override fun onStart() {
        super.onStart()

        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // Unregistered all the registered listeners
    override fun onStop() {
        super.onStop()

        sensorManager.unregisterListener(this)
    }
}