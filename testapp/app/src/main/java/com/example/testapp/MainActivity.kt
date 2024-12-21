package com.example.testapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.setPadding
import java.io.IOException
import java.io.InputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var bluetoothSocket: BluetoothSocket
    private val REQUEST_ENABLE_BT = 1
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var buttons: List<Button>
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // LinearLayout을 생성하여 레이아웃을 설정
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(16)
        }

        // TextView를 생성하여 데이터를 표시할 수 있게 설정
        textView = TextView(this).apply {
            text = "Waiting for data..."
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // ScrollView를 추가하여 데이터를 스크롤할 수 있게 설정
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            addView(textView)
        }

        // 버튼을 생성하여 레이아웃에 추가
        buttons = (1..6).map { index ->
            Button(this).apply {
                text = index.toString()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    // 버튼 클릭 시 이벤트 처리
                    Toast.makeText(this@MainActivity, "Button $index clicked", Toast.LENGTH_SHORT).show()
                }
                layout.addView(this)
            }
        }

        // Layout에 ScrollView 추가
        layout.addView(scrollView)
        setContentView(layout)

        // BluetoothAdapter 초기화
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // 블루투스 지원 여부 확인
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "블루투스가 지원되지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 블루투스 활성화 확인 및 요청
        if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            setupBluetoothConnection()
        }
    }

    private fun setupBluetoothConnection() {
        // 필요한 권한 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_ENABLE_BT)
            return
        }

        // HC-06 디바이스 검색
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        val hc06Device: BluetoothDevice? = pairedDevices?.find { it.name == "HC-06" }

        if (hc06Device == null) {
            Toast.makeText(this, "HC-06 디바이스를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // UUID 설정 (HC-06은 시리얼 포트 프로파일 사용)
        val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        try {
            bluetoothSocket = hc06Device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket.connect()
            Toast.makeText(this, "HC-06에 연결되었습니다.", Toast.LENGTH_SHORT).show()

            // 데이터 수신 시작
            receiveData()
        } catch (e: Exception) {
            Toast.makeText(this, "블루투스 연결 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun receiveData() {
        val inputStream: InputStream = bluetoothSocket.inputStream
        val buffer = ByteArray(32)

        Thread {
            while (true) {
                try {
                    val bytes = inputStream.read(buffer)

                    if (bytes != -1) {
                        val readMessage = String(buffer, 0, bytes).trim()
                        runOnUiThread {
                            textView.append("$readMessage\n")
                            handleBluetoothData(readMessage)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }.start()
    }

    private fun handleBluetoothData(data: String) {
        // 입력된 데이터에 따라 해당 버튼 클릭 호출
        when (data.trim()) {
            "1" -> simulateKeyEvent(KeyEvent.KEYCODE_1)
            "2" -> simulateKeyEvent(KeyEvent.KEYCODE_2)
            "3" -> simulateKeyEvent(KeyEvent.KEYCODE_3)
            "4" -> simulateKeyEvent(KeyEvent.KEYCODE_4)
            "5" -> simulateKeyEvent(KeyEvent.KEYCODE_5)
            "6" -> simulateKeyEvent(KeyEvent.KEYCODE_6)
            "7" -> {
                currentIndex = (currentIndex + 1) % buttons.size
                simulateKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT)
            }
            "8" -> {
                currentIndex = (currentIndex + 1) % buttons.size
                simulateKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT)
            }
            "9" -> {
                currentIndex = if (currentIndex - 1 < 0) buttons.size - 1 else currentIndex - 1
                simulateKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT)
            }
            "10" -> {
                currentIndex = if (currentIndex - 1 < 0) buttons.size - 1 else currentIndex - 1
                simulateKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT)
            }
        }
    }

    private fun simulateKeyEvent(keyCode: Int) {
        val keyEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        onKeyDown(keyCode, keyEvent)
        onKeyUp(keyCode, keyEvent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            bluetoothSocket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_1 -> buttons[0].performClick()
            KeyEvent.KEYCODE_2 -> buttons[1].performClick()
            KeyEvent.KEYCODE_3 -> buttons[2].performClick()
            KeyEvent.KEYCODE_4 -> buttons[3].performClick()
            KeyEvent.KEYCODE_5 -> buttons[4].performClick()
            KeyEvent.KEYCODE_6 -> buttons[5].performClick()
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                currentIndex = if (currentIndex - 1 < 0) buttons.size - 1 else currentIndex - 1
                buttons[currentIndex].performClick()
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                currentIndex = (currentIndex + 1) % buttons.size
                buttons[currentIndex].performClick()
            }
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
            return super.onKeyUp(keyCode, event)
    }
}
