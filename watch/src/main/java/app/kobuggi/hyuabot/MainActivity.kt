package app.kobuggi.hyuabot

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.kobuggi.hyuabot.pages.*
import app.kobuggi.hyuabot.ui.PagerScreen
import com.google.accompanist.pager.ExperimentalPagerApi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val vm = MainViewModel()
        super.onCreate(savedInstanceState)
        setContent {
            Main(this, vm)
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Main(context: Context, vm: MainViewModel) {
    LaunchedEffect(Unit, block = {
        vm.getArrivalList()
    })
    if (vm.errorMessage.isNotEmpty()) {
        Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show()
    }
    PagerScreen(
        count = 6,
    ) { page -> Box(
        modifier = Modifier
            .padding(top = 16.dp, bottom = 16.dp)
            .fillMaxSize()
            .padding(16.dp),
    ) {
        when (page) {
            0 -> Dormitory(vm)
            1 -> ShuttlecockO(vm)
            2 -> Station(vm)
            3 -> Terminal(vm)
            4 -> Jungang(vm)
            5 -> ShuttlecockI(vm)
        }
    }}
}