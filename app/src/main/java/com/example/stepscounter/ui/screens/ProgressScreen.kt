package  com.example.stepscounter.ui.screens
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import java.util.Calendar

@Composable
fun ProgressScreen(goalSteps: Int, navController: NavController) {
    val context = LocalContext.current
    var stepsCount by remember { mutableStateOf(0) }
    var lastMilestone by remember { mutableStateOf(0) }
    var lastUpdatedDay by remember { mutableStateOf(Calendar.getInstance()) }  // Using Calendar instead of LocalDate
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val today = Calendar.getInstance()  // Get today's date with Calendar
                if (today.get(Calendar.DAY_OF_YEAR) != lastUpdatedDay.get(Calendar.DAY_OF_YEAR)) {
                    stepsCount = 0  // Reset steps count at the start of a new day
                    lastMilestone = 0  // Reset milestones tracking
                    lastUpdatedDay = today
                }
                val newSteps = event.values[0].toInt()
                stepsCount = newSteps.coerceAtMost(goalSteps)
                checkAndNotifyMilestones(newSteps, goalSteps, context, lastMilestone) { achievedMilestone ->
                    lastMilestone = achievedMilestone
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(key1 = sensorManager) {
        sensorManager.registerListener(sensorEventListener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
            CircularProgressIndicator(
                progress = stepsCount.toFloat() / goalSteps,
                color = Color.Blue,
                strokeWidth = 8.dp,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                "Steps: $stepsCount / $goalSteps",
                fontSize = 20.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Button to manually increment steps for testing
        Button(onClick = {
            val increment = 1000
            val potentialNewSteps = stepsCount + increment
            stepsCount = potentialNewSteps.coerceAtMost(goalSteps)
            checkAndNotifyMilestones(stepsCount, goalSteps, context, lastMilestone) { achievedMilestone ->
                lastMilestone = achievedMilestone
                Log.d("ProgressScreen", "Increment button pressed: Steps = $stepsCount, Last Milestone = $lastMilestone")
            }
        }) {
            Text("Increment Steps for Testing")
        }
    }
}


fun checkAndNotifyMilestones(steps: Int, goal: Int, context: Context, lastMilestone: Int, updateLastMilestone: (Int) -> Unit) {
    val milestones = listOf(50, 75, 100) // Milestones in percentage
    Log.d("MilestoneCheck", "Checking milestones for steps: $steps")
    milestones.forEach {
        val milestoneStep = goal * it / 100
        Log.d("MilestoneCheck", "Milestone $it% = $milestoneStep steps, last milestone = $lastMilestone")
        if (steps >= milestoneStep && it > lastMilestone) {
            Log.d("MilestoneCheck", "Reached milestone $it% at $steps steps")
            showNotification("Milestone reached", "You have reached $it% of your goal!", context)
            updateLastMilestone(it)
            return@forEach  // Break after notifying to prevent multiple notifications
        }
    }
}

fun showNotification(title: String, text: String, context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Log the lack of permissions
            Log.d("Notification", "POST_NOTIFICATIONS permission is not granted.")
            // Request the permission here

            return
        }
    }

    val notificationManager = NotificationManagerCompat.from(context)
    val notificationId = System.currentTimeMillis().toInt()  // Unique ID for each notification

    val builder = NotificationCompat.Builder(context, "your_channel_id")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    notificationManager.notify(notificationId, builder.build())
    Log.d("Notification", "Notification posted: $title - $text")
}
