
package com.surya.trex.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

import com.surya.trex.data.model.InstalledApp

class InstalledAppsRepository(
    private val context: Context
) {

    fun getInstalledApps(): List<InstalledApp> {

        val packageManager = context.packageManager

        val applications =
            packageManager.getInstalledApplications(
                PackageManager.GET_META_DATA
            )

        return applications
            .filter { applicationInfo ->

                // Show only applications that can actually be launched.
                packageManager.getLaunchIntentForPackage(
                    applicationInfo.packageName
                ) != null
            }
            .map { applicationInfo ->

                InstalledApp(
                    appName =
                        packageManager.getApplicationLabel(
                            applicationInfo
                        ).toString(),

                    packageName =
                        applicationInfo.packageName
                )
            }
            .sortedBy {
                it.appName.lowercase()
            }
    }
}

