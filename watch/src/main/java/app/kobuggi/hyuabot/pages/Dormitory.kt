package app.kobuggi.hyuabot.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.MainViewModel

@Composable
fun Dormitory(vm : MainViewModel) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.dormitory_o),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.title3
        )
        Card(
            modifier = Modifier.padding(top = 6.dp),
            onClick = {},
            contentColor = MaterialTheme.colors.onSurface,
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = stringResource(id = R.string.bound_station),
                    style = MaterialTheme.typography.body1
                )
                if (vm.dormitoryArrival[0] < 0) {
                    Text(
                        text = stringResource(id = R.string.out_of_service),
                        style = MaterialTheme.typography.body1
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.remaining_time, vm.dormitoryArrival[0]),
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
        Card(
            modifier = Modifier.padding(top = 6.dp),
            onClick = {},
            contentColor = MaterialTheme.colors.onSurface,
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = stringResource(id = R.string.bound_terminal),
                    style = MaterialTheme.typography.body1
                )
                if (vm.dormitoryArrival[1] < 0) {
                    Text(
                        text = stringResource(id = R.string.out_of_service),
                        style = MaterialTheme.typography.body1
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.remaining_time, vm.dormitoryArrival[1]),
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
        Card(
            modifier = Modifier.padding(top = 6.dp),
            onClick = {},
            contentColor = MaterialTheme.colors.onSurface,
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = stringResource(id = R.string.bound_jungang),
                    style = MaterialTheme.typography.body1
                )
                if (vm.dormitoryArrival[2] < 0) {
                    Text(
                        text = stringResource(id = R.string.out_of_service),
                        style = MaterialTheme.typography.body1
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.remaining_time, vm.dormitoryArrival[2]),
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
        Button(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth(),
            onClick = { vm.getArrivalList() }
        ) {
            Text(text = stringResource(id = R.string.refresh))
        }
    }
}