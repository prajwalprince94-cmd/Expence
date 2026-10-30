package com.knowspends.app.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.knowspends.app.MainActivity

class QuickAddWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(16.dp)
                        .background(GlanceTheme.colors.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "KnowSpends",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Text(
                        text = "Instant 3-Tap Logging",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(10.dp))

                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val addExpenseIntent = Intent(context, MainActivity::class.java).apply {
                            action = Intent.ACTION_VIEW
                            putExtra("ROUTE", "add_transaction")
                            putExtra("TYPE", "EXPENSE")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }

                        Button(
                            text = "+ Expense",
                            onClick = actionStartActivity(addExpenseIntent),
                            modifier = GlanceModifier.defaultWeight()
                        )

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        val addIncomeIntent = Intent(context, MainActivity::class.java).apply {
                            action = Intent.ACTION_VIEW
                            putExtra("ROUTE", "add_transaction")
                            putExtra("TYPE", "INCOME")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }

                        Button(
                            text = "+ Income",
                            onClick = actionStartActivity(addIncomeIntent),
                            modifier = GlanceModifier.defaultWeight()
                        )
                    }
                }
            }
        }
    }
}

class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget()
}
