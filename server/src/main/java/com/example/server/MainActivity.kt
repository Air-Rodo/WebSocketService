package com.example.server

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.server.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "TAG"
        val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0 -> {
                        Log.d(TAG, "handleMessage: 0 = ${msg.obj}")
                    }
                    1 -> {
                        val stringBuffer = StringBuffer()
                        if (WebSocketService.mClientIpList.size < 1) {
                            stringBuffer.append("当前无设备连接\n")
                        } else {
                            for (ip in WebSocketService.mClientIpList) {
                                stringBuffer.append("$ip:已连接\n")
                            }
                        }
                        Log.d(TAG, "handleMessage: 1 = $stringBuffer")
                    }
                }
            }
        }
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initListener()
    }

    private fun initListener() {
        binding.btnStartServer.setOnClickListener {
            startService(Intent(this@MainActivity, WebSocketService::class.java))
        }

        binding.btnStopServer.setOnClickListener {
            stopService(Intent(this@MainActivity, WebSocketService::class.java))
        }

        binding.btnSendMsg.setOnClickListener {
            val message = Message()
            message.what = 1
            message.obj = binding.etText.text.toString()
            WebSocketService.mService.sendMessage(message)
            binding.etText.setText("")
        }
    }
}