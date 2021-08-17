package app.kobuggi.hyuabot.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.function.DatabaseHelper

class ContactActivity : AppCompatActivity() {
    lateinit var databaseHelper : DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        databaseHelper = DatabaseHelper(this)
    }
}