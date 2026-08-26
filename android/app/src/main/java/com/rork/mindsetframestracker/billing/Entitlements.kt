package com.rork.mindsetframestracker.billing

enum class SubscriptionTier {
    NONE,
    FOUNDING,
    REGULAR,
}

enum class Feature {
    UNLIMITED_HABITS,
    ALL_LANGUAGES,
    PDF_EXPORTS,
    HUAWEI_HEALTH_KIT,
    HEALTH_CONNECT,
    STRAVA,
}

object Entitlements {

    fun tierForProductId(productId: String): SubscriptionTier = when (productId) {
        "mindset_premium_founding_monthly",
        "mindset_premium_founding_yearly" -> SubscriptionTier.FOUNDING
        "mindset_premium_monthly",
        "mindset_premium_yearly" -> SubscriptionTier.REGULAR
        else -> SubscriptionTier.NONE
    }

    fun hasAccess(tier: SubscriptionTier, feature: Feature): Boolean = when (feature) {
        Feature.UNLIMITED_HABITS,
        Feature.ALL_LANGUAGES,
        Feature.PDF_EXPORTS,
        Feature.HUAWEI_HEALTH_KIT,
        Feature.HEALTH_CONNECT -> tier != SubscriptionTier.NONE
        // Strava gated to Regular only, per your call above — remove this
        // special case if you decide Founding should get it too.
        Feature.STRAVA -> tier == SubscriptionTier.REGULAR
    }
}
