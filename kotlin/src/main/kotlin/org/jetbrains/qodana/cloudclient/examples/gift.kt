@file:Suppress("UNUSED_PARAMETER", "unused", "UnusedReceiverParameter")

package org.jetbrains.qodana.cloudclient.examples

internal class QodanaPage(val url: String)

internal fun QodanaPage.buyUltimatePlus(): BuyPage {
    return BuyPage()
}

internal class BuyPage

internal fun BuyPage.applyAnnualDiscountForThreeContributors(discountCode: String) {
}

internal fun BuyPage.fillPaymentDetails() {}

internal fun BuyPage.orderAndPay(): String {
    return ""
}

internal fun getQodanaCloudProjectToken(license: String): String {
    return "token"
}

internal fun analyzeRepository(url: String, qodanaLicense: String, block: AnalyzeRepository.() -> Unit): AnalyzeRepository {
    return AnalyzeRepository(url, qodanaLicense)
}

internal class AnalyzeRepository(val url: String, qodanaLicense: String) {
    fun checkLicensesCompliance() {
    }

    fun checkVulnerabilities() {
    }

    fun checkCodeStyle() {
    }

    fun checkBugs() {

    }

    fun checkCoverage() {
    }

    fun setQualityGate() {
    }
}
