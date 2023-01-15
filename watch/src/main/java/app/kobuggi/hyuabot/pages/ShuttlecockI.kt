package app.kobuggi.hyuabot.pages

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import app.kobuggi.hyuabot.MainViewModel
import app.kobuggi.hyuabot.R


@Composable
fun ShuttlecockI(vm : MainViewModel) {
    Column{
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.shuttlecock_i),
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
                    text = stringResource(id = R.string.bound_dormitory),
                    style = MaterialTheme.typography.body1
                )
                if (vm.shuttlecockInArrival[0] < 0) {
                    Text(
                        text = stringResource(id = R.string.out_of_service),
                        style = MaterialTheme.typography.body1
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.remaining_time, vm.shuttlecockInArrival[0]),
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