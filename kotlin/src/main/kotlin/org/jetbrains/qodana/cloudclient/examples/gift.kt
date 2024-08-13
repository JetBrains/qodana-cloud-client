@file:Suppress("UNUSED_PARAMETER", "unused", "UnusedReceiverParameter")

package org.jetbrains.qodana.cloudclient.examples

class QodanaPage(val url: String)

fun QodanaPage.buyUltimatePlus(): BuyPage {
    return BuyPage()
}

class BuyPage

fun BuyPage.applyAnnualDiscountForThreeContributors(discountCode: String) {
}

fun BuyPage.fillPaymentDetails() {}

fun BuyPage.orderAndPay(): String {
    return ""
}

fun getQodanaCloudProjectToken(license: String): String {
    return "token"
}

fun analyzeRepository(url: String, qodanaLicense: String, block: AnalyzeRepository.() -> Unit): AnalyzeRepository {
    return AnalyzeRepository(url, qodanaLicense)
}

class AnalyzeRepository(val url: String, qodanaLicense: String) {
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

