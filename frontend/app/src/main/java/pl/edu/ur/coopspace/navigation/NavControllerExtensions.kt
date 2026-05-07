package pl.edu.ur.coopspace.navigation

import android.app.Activity
import android.content.Context
import androidx.navigation.NavController

/**
 * Safely pop the navigation back stack. If there is no destination to pop to,
 * finish the Activity (if available) to avoid leaving an empty UI state.
 */
fun NavController.safePopBackOrFinish(context: Context) {
    val handled = this.popBackStack()
    if (!handled) {
        (context as? Activity)?.finish()
    }
}
