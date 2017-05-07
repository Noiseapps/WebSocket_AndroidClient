package com.noiseapps.websockets

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_17
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

class MainActivity : AppCompatActivity() {

    private lateinit var configurationView: View
    private lateinit var serverAddress: EditText
    private lateinit var nickname: EditText
    private lateinit var statusMessage: TextView
    private lateinit var connectButton: Button

    private lateinit var listView : RecyclerView
    private lateinit var messageInput : EditText
    private lateinit var sendButton : Button
    private lateinit var disconnectButton : Button

    private var socket : WebSocketClient? = null
    private val listAdapter = ListAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        connectButton.setOnClickListener(this::connect)
        disconnectButton.setOnClickListener(this::disconnect)
        sendButton.setOnClickListener(this::sendMessage)

        listView.adapter = listAdapter
        listView.layoutManager = LinearLayoutManager(this)
    }

    override fun onPause() {
        super.onPause()
        disconnect(disconnectButton)
    }

    fun bindViews() {
        configurationView = findViewById(R.id.configurationView)
        serverAddress = findViewById(R.id.serverAddress) as EditText
        nickname = findViewById(R.id.nicknameInput) as EditText
        statusMessage = findViewById(R.id.statusMessage) as TextView
        connectButton = findViewById(R.id.connect) as Button

        listView = findViewById(R.id.messagesList) as RecyclerView
        messageInput = findViewById(R.id.messageInput) as EditText
        sendButton = findViewById(R.id.sendMessage) as Button
        disconnectButton = findViewById(R.id.disconnect) as Button
    }

    fun connect(view : View) {
        socket?.close()

        var address = serverAddress.text.toString()
        if(address.isEmpty()) {
            address = "192.168.1.162:8080"
        }
        val uri = URI("ws://$address/order")
        socket = SocketClient(uri)
        socket!!.connect()
    }

    fun disconnect(view : View) {
        socket?.close()
        socket = null
    }

    fun sendMessage(view : View) {
        val msg = Message(messageInput.text.toString())
        socket?.send(Gson().toJson(msg))
        messageInput.setText("")
    }

    fun showConfiguration(show: Boolean) {
        configurationView.visibility = if (show) View.VISIBLE else View.GONE
        if(show) {
            socket?.close()
            socket = null
            nickname.setText("")
        }
    }

    fun addItemToList(message: Message) {
        listAdapter.addItem(message)
        listView.scrollToPosition(listAdapter.itemCount -1)
    }

    inner class SocketClient(uri: URI) : WebSocketClient(uri, Draft_17()) {
        override fun onOpen(handshakedata: ServerHandshake?) {
            runOnUiThread {
                addItemToList(Message("Status", "Connection opened"))
                val msg = Message("handshake", nickname.text.toString())
                this.send(Gson().toJson(msg))
                showConfiguration(false)
            }
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            runOnUiThread {
                val msg = "Connection terminated with code $code because of $reason"
                statusMessage.text = msg
                addItemToList(Message("Status", msg))
                showConfiguration(true)
            }
        }

        override fun onMessage(message: String?) {
            runOnUiThread {
                val msg = Gson().fromJson(message, Message::class.java)
                addItemToList(Message(msg.title, msg.message))
            }
        }

        override fun onError(ex: Exception?) {
            runOnUiThread {
                val msg = "Connection quit unexpectedly beacause of ${ex?.localizedMessage}"
                addItemToList(Message("Status", msg))
                statusMessage.text = msg
                showConfiguration(true)
            }
        }

    }
}
