package com.marconius.wordbopper

import android.content.Context
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus

class AgeSignalsChecker(context: Context) {
    private val ageSignalsManager = AgeSignalsManagerFactory.create(context.applicationContext)

    fun check(callback: (AgeSignalsCheckResult) -> Unit) {
        ageSignalsManager
            .checkAgeSignals(AgeSignalsRequest.builder().build())
            .addOnSuccessListener { ageSignalsResult ->
                callback(
                    when (ageSignalsResult.userStatus()) {
                        AgeSignalsVerificationStatus.UNKNOWN -> AgeSignalsCheckResult.ResolveInPlayStore
                        AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_PENDING -> AgeSignalsCheckResult.ParentApprovalPending
                        AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED -> AgeSignalsCheckResult.ParentApprovalDenied
                        else -> AgeSignalsCheckResult.Ready
                    }
                )
            }
            .addOnFailureListener {
                callback(AgeSignalsCheckResult.Ready)
            }
    }
}

enum class AgeSignalsCheckResult {
    Ready,
    ResolveInPlayStore,
    ParentApprovalPending,
    ParentApprovalDenied
}
