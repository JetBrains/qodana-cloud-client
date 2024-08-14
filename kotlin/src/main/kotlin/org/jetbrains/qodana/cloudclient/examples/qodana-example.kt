package org.jetbrains.qodana.cloudclient.examples

@Suppress("unused")
internal fun licenseApiExample() {
    val qodanaPage = QodanaPage("https://www.jetbrains.com/qodana/")
    val buyPage = qodanaPage.buyUltimatePlus()

    val qodanaFullDiscountCoupon = System.getenv("QODANA_FULL_DISCOUNT_COUPON")
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
