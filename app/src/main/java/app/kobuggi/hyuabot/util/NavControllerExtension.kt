package app.kobuggi.hyuabot.util

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections

object NavControllerExtension {
    fun NavController.safeNavigate(directions: NavDirections) {
        currentDestination?.getAction(directions.actionId)?.run { navigate(directions) }
    }

    fun NavController.safeNavigate(
        @IdRes currentDestinationID: Int,
        @IdRes id: Int,
        args: Bundle? = null
    ) {
        if (currentDestination?.id == currentDestinationID) {
            navigate(id, args)
        }
    }
}
