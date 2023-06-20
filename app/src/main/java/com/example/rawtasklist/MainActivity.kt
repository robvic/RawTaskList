package com.example.rawtasklist

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.*
import kotlinx.serialization.json.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addTaskButton: Button = findViewById(R.id.add_task_button)
        val sharedPreferences = getSharedPreferences("taskList", Context.MODE_PRIVATE)
        val taskListJson = sharedPreferences.getString("taskList", "")
        val tasks = if (!taskListJson.isNullOrEmpty()) {
            jsonToTaskList(taskListJson).tasks.toMutableList()
        } else {
            mutableListOf()
        }
        val adapter = TaskAdapter(tasks, this)
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        addTaskButton.setOnClickListener {
            // Inflate the dialog layout
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_task, null)
            // Create and show the dialog
            AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("New Task")
                .setPositiveButton("Add") { _, _ ->
                    // Get the task name from the EditText
                    val taskName = dialogView.findViewById<EditText>(R.id.edittext_task_name).text.toString()

                    // Check if the task name is not empty
                    if (taskName.isNotEmpty()) {
                        // Create a new task
                        val task = Task(taskName, false)

                        // Add the task to the adapter
                        adapter.addTask(task)

                        // Scroll to the position of the new item
                        recyclerView.smoothScrollToPosition(tasks.size - 1)
                    } else {
                        // Show an error if the task name is empty
                        Toast.makeText(this, "Task name cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        }
    }
}
@Serializable
data class Task(val title: String, var isCompleted: Boolean)

// Convert Task to JSON
fun taskToJson(task: Task): String {
    return Json.encodeToString(task)
}
// Convert JSON to Task
fun jsonToTask(json: String): Task {
    return Json.decodeFromString(json)
}

@Serializable
data class TaskList(val tasks: List<Task>)

// Convert TaskList to JSON
fun taskListToJson(taskList: TaskList): String {
    return Json.encodeToString(taskList)
}

// Convert JSON to TaskList
fun jsonToTaskList(json: String): TaskList {
    return Json.decodeFromString(json)
}

class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val checkBox: CheckBox = itemView.findViewById(R.id.task_checkbox)
    val textView: TextView = itemView.findViewById(R.id.task_text)

    fun bind(task: Task) {
        checkBox.setOnCheckedChangeListener(null) // Remove previous listener
        checkBox.text = task.title
        checkBox.isChecked = task.isCompleted
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            task.isCompleted = isChecked
        }
    }
}

class TaskAdapter(private val tasks: MutableList<Task>, private val context: Context) : RecyclerView.Adapter<TaskViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                holder.checkBox.background = ContextCompat.getDrawable(context, R.drawable.checkbox_tick_marked)
                removeTask(position)
            } else {
                holder.checkBox.background = ContextCompat.getDrawable(context, R.drawable.checkbox_tick)
            }
        }
        holder.textView.text = tasks[position].title
    }

    override fun getItemCount() = tasks.size

    private fun saveTasks() {
        val sharedPreferences = context.getSharedPreferences("taskList", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val taskListJson = taskListToJson(TaskList(tasks))
        editor.putString("taskList", taskListJson)
        editor.apply()
    }

    fun addTask(task: Task) {
        tasks.add(task)
        notifyItemInserted(tasks.size - 1)
        saveTasks()
    }

    private fun removeTask(position: Int) {
        tasks.removeAt(position)
        notifyItemRemoved(position)
        saveTasks()
    }
}
