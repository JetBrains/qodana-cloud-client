package org.jetbrains.qodana.cloudclient.examples

fun main() {
    val qodanaPage = QodanaPage("https://www.jetbrains.com/qodana/")
    val buyPage = qodanaPage.buyUltimatePlus()

    val qodanaFullDiscountCoupon = "xoxp-12345678901-987654321012-987654321012-1234123412341234567890abcdef12345"
    buyPage.applyAnnualDiscountForThreeContributors(qodanaFullDiscountCoupon)
    buyPage.fillPaymentDetails()
    val qodanaLicense = buyPage.orderAndPay()
    enjoy(qodanaLicense)
}

private fun enjoy(qodanaLicense: String) {
    val token = getQodanaCloudProjectToken(qodanaLicense)
    analyzeRepository("https://github.com/my-project", token) {
        checkLicensesCompliance()
        checkVulnerabilities()
        checkCodeStyle()
        checkCoverage()
        checkBugs()

        setQualityGate()
    }
}
