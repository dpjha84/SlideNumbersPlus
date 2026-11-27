package com.dpjha.slidingnumbersplus.services

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory

/** Google decides whether the rating card actually appears. */
fun askForReview(activity: Activity) {
    val manager = ReviewManagerFactory.create(activity)
    manager.requestReviewFlow().addOnCompleteListener { task ->
        if (task.isSuccessful) manager.launchReviewFlow(activity, task.result)
    }
}
