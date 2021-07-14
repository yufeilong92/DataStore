package com.example.datastore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.widget.Toast
import com.example.datastore.User.UserPreferences
import com.example.datastore.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private val mHandle = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val it = msg.obj as UserPreferences
            val age = it.age
            val name = it.name
            val phone = it.phone
            viewBinding.tvContent.text = "$age$name$phone"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.btnOne.setOnClickListener {
            val str = viewBinding.etInput.text.toString()
            if (TextUtils.isEmpty(str)) {
                Toast.makeText(this@MainActivity, "数据是空的", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            GlobalScope.launch {
                UserHelp.write(this@MainActivity, 11, "小米", str)
            }
        }
        viewBinding.btnTwo.setOnClickListener {
            GlobalScope.launch {
                val read = UserHelp.read(this@MainActivity)
                read.collect {
                    val obtainMessage = mHandle.obtainMessage()
                    obtainMessage.obj = it
                    mHandle.sendMessage(obtainMessage)
                }
            }
        }
    }
}