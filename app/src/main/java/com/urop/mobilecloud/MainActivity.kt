package com.urop.mobilecloud

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.measureTimeMillis


class MainActivity : AppCompatActivity() {

    private var logString: String = ""
    private val webSocket = WebSocketClient(this)
    private val worker = Worker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logAppend("Welcome to Mobile Cloud!")
        logAppend("Tap Connect to start ... ")

        netSwitcher.setOnCheckedChangeListener { _, isChecked -> switchChecked(isChecked) }
    }


    private fun switchChecked(isChecked: Boolean) {
        if (isChecked) {
            webSocket.connect("ws://jackys-windows:9544")
            webSocket.sendMessage(Task("Message", "Hi, server!"))
        } else {
            webSocket.close(4321, "bye")
        }
    }

    fun logAppend(str: String) {
        val curTime = SimpleDateFormat("[HH:mm:ss:SSS] ", Locale.getDefault()).format(Date())
        logString = curTime + str + "\n" + logString
        runOnUiThread {
            logText.text = logString
        }
    }


    fun msgParser(msg: String) {
//        logAppend("receive msg: $msg")
        val task = msg.json2task()
        if (task.cmd == "Message") {
            logAppend("Msg: ${task.data}")
        } else {
            worker.addTask(task)
            var res = Task()
            val duration = measureTimeMillis { res = worker.work() }
            logAppend("${res.cmd} ${res.meta} done: " + duration + "ms")
//            val res = worker.result

//            val sendMsg = res.task2json()
//            logAppend("sending msg: $sendMsg")
            webSocket.sendMessage(res)
        }

    }

}

class Worker {
//    var data: IntArray

    private var taskBuffer: MutableList<Task> = mutableListOf()
//    var result: String? = null
//    @Volatile var result: Task? = null


    fun addTask(task: Task) {
//        val t: Task =
        taskBuffer.add(task)
    }


    fun work(): Task {
        val task = taskBuffer.removeAt(0)

//        val (cmd, data) = task
//        var resCmd = cmd

        val resData: String = when (task.cmd) {
            "QSRT" -> qsort(task.data)
            "MSRT" -> msort(task.data)
            else -> "CMD not understood!"
        }
        val tt = task
        tt.data = resData
        return tt
    }

    private fun qsort(data: String): String {
//        data.sorted()
        return data.json2arr().sortedArray().arr2json()
    }

    private fun msort(data: String): String {
//        data.sorted()
        val a = data.json2arr()
        val size = a.size
        val mid = size / 2
//        val high = size

        val temp = IntArray(size)
        var i = 0
        var j = mid
        var k = 0
        while (i < mid && j < size) {
            temp[k++] = if (a[i] < a[j]) a[i++] else a[j++]
        }
        while (i < mid) temp[k++] = a[i++]
        while (j < size) temp[k++] = a[j++]

        return temp.arr2json()
    }

}

data class Task(
    val cmd: String = "", var data: String = "",
    val meta: String = "", var waitCount: Int = 0
)

val gson = Gson()

fun String.json2task(): Task {
    return gson.fromJson(this, Task::class.java)
}

fun String.json2arr(): IntArray {
    return gson.fromJson(this, IntArray::class.java)
}

fun IntArray.arr2json(): String {
    return gson.toJson(this)
}

fun Task.task2json(): String {
    return gson.toJson(this)
}