package com.example.styleap.util

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.example.styleap.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*

@Singleton
class BillingClientWrapper @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : PurchasesUpdatedListener, BillingClientStateListener {

    private lateinit var billingClient: BillingClient
    private lateinit var applicationContext: Context // Store context safely

    // Use StateFlow for observing purchase results and connection status
    private val _purchaseState = MutableStateFlow<Resource<Purchase>?>(null) // null initially
    val purchaseState = _purchaseState.asStateFlow()

    private val _billingConnectionState = MutableStateFlow<Resource<Unit>>(Resource.Loading())
    val billingConnectionState = _billingConnectionState.asStateFlow()

    private val _premiumProductDetails = MutableStateFlow<ProductDetails?>(null)
    val premiumProductDetails = _premiumProductDetails.asStateFlow()

    private val _premiumStatus = MutableStateFlow(false)
    val premiumStatus = _premiumStatus.asStateFlow()

    // Keep track of queried products to avoid re-querying unnecessarily
    private var productDetailsMap = mutableMapOf<String, ProductDetails>()

    // Coroutine scope for background tasks
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        // Define your premium product ID (must match Google Play Console)
        const val PREMIUM_PRODUCT_ID = "premium_subscription_monthly" // Replace with your actual ID
        // Consider adding more product IDs if needed (e.g., yearly)
    }

    fun initialize(context: Context) {
        Timber.d("Initializing BillingClientWrapper")
        applicationContext = context.applicationContext // Use application context
        billingClient = BillingClient.newBuilder(applicationContext)
            .setListener(this)
            .enablePendingPurchases() // Required for pending transactions
            .build()

        if (!billingClient.isReady) {
            Timber.d("BillingClient not ready, starting connection...")
            _billingConnectionState.value = Resource.Loading()
            billingClient.startConnection(this)
        } else {
             Timber.d("BillingClient already ready.")
             _billingConnectionState.value = Resource.Success(Unit)
             queryPremiumProductDetails() // Query details if already connected
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Timber.d("Billing setup finished successfully.")
            _billingConnectionState.value = Resource.Success(Unit)
            // Query products and purchases after connection is established
            queryPremiumProductDetails()
            queryPurchasesAsync() // Check for existing active purchases
        } else {
            Timber.e("Billing setup failed: ${billingResult.debugMessage}")
            _billingConnectionState.value = Resource.Error("Billing setup failed: ${billingResult.debugMessage}")
        }
    }

    override fun onBillingServiceDisconnected() {
        Timber.w("Billing service disconnected. Trying to reconnect...")
        _billingConnectionState.value = Resource.Error("Billing service disconnected.")
        // Implement retry logic if desired, e.g., exponential backoff
        // For simplicity, we might just try connecting again after a delay
        coroutineScope.launch {
             delay(5000) // Wait 5 seconds before retrying
             if (::billingClient.isInitialized && !billingClient.isReady) {
                 billingClient.startConnection(this@BillingClientWrapper)
             }
        }
    }

    private fun queryPremiumProductDetails() {
        if (!billingClient.isReady) {
            Timber.w("queryPremiumProductDetails called but BillingClient is not ready.")
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS) // Or INAPP if it's not a subscription
                .build()
            // Add other product types if needed
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
                Timber.d("Product details query successful: ${productDetailsList.size} items found.")
                productDetailsList.forEach { productDetails ->
                     productDetailsMap[productDetails.productId] = productDetails
                     if(productDetails.productId == PREMIUM_PRODUCT_ID) {
                         _premiumProductDetails.value = productDetails // Update the specific premium product details flow
                     }
                }
            } else {
                Timber.e("Failed to query product details: ${billingResult.debugMessage}")
                // Optionally update state to reflect error
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productId: String = PREMIUM_PRODUCT_ID) {
         if (!billingClient.isReady) {
             Timber.e("Billing client not ready to launch purchase flow.")
             _purchaseState.value = Resource.Error("Billing client not connected.")
             return
         }

        val productDetails = productDetailsMap[productId]
        if (productDetails == null) {
            Timber.e("Product details not found for $productId. Cannot launch purchase flow.")
            _purchaseState.value = Resource.Error("Product details not available.")
            // Consider querying again here if needed
            return
        }

        // Correctly retrieve the offer token for subscriptions
        // Assuming the first (or only) subscription offer is the one we want
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken

        if (productDetails.productType == BillingClient.ProductType.SUBS && offerToken == null) {
            Timber.e("Offer token is required for subscription purchase but not found for $productId.")
             _purchaseState.value = Resource.Error("Subscription offer details missing.")
            return
        }


        val productDetailsParamsList = mutableListOf<BillingFlowParams.ProductDetailsParams>()

        val builder = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)

        // Set offer token only for subscriptions
        if (productDetails.productType == BillingClient.ProductType.SUBS && offerToken != null) {
            builder.setOfferToken(offerToken)
        }

        productDetailsParamsList.add(builder.build())


        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Timber.e("Failed to launch billing flow: ${billingResult.debugMessage}")
            _purchaseState.value = Resource.Error("Failed to launch billing flow: ${billingResult.debugMessage}")
        } else {
            Timber.d("Billing flow launched successfully.")
            // State will be updated via onPurchasesUpdated
        }
    }


    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        Timber.d("onPurchasesUpdated called with result: ${billingResult.responseCode}")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            Timber.d("Purchase successful or pending. Count: ${purchases.size}")
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Timber.i("User cancelled the purchase flow.")
            _purchaseState.value = Resource.Error("Purchase cancelled by user.")
        } else {
            Timber.e("Purchase failed with error: ${billingResult.debugMessage}")
            _purchaseState.value = Resource.Error("Purchase failed: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        Timber.d("Handling purchase: ${purchase.orderId}, state: ${purchase.purchaseState}")
        // Verify purchase signature (highly recommended on backend)
        // For client-side verification (less secure):
        // if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
        //     Timber.e("Got a purchase: $purchase; but signature is bad. Skipping...")
        //     return; // Or handle appropriately
        // }

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
             _purchaseState.value = Resource.Success(purchase) // Emit success state

            // Acknowledge the purchase if it's not a subscription or if it's consumable
             if (!purchase.isAcknowledged) {
                 val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                     .setPurchaseToken(purchase.purchaseToken)
                     .build()
                 coroutineScope.launch {
                     val ackResult = billingClient.acknowledgePurchase(acknowledgePurchaseParams)
                     if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                          Timber.i("Purchase acknowledged successfully: ${purchase.orderId}")
                         // Grant entitlement (e.g., activate premium via API call)
                         activatePremiumOnBackend(purchase) // Call backend here
                     } else {
                         Timber.e("Failed to acknowledge purchase ${purchase.orderId}: ${ackResult.debugMessage}")
                         // Consider retry logic or informing the user
                     }
                 }
             } else {
                 Timber.d("Purchase ${purchase.orderId} is already acknowledged.")
                 // Potentially re-grant entitlement if needed or just confirm state
                 activatePremiumOnBackend(purchase) // Ensure backend knows
             }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Timber.i("Purchase is pending: ${purchase.orderId}. Handle pending state in UI.")
            // Update UI to inform the user about the pending purchase
             _purchaseState.value = Resource.Loading() // Indicate pending state

        } else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
             Timber.w("Purchase state unspecified: ${purchase.orderId}")
              _purchaseState.value = Resource.Error("Purchase state unclear.")
        }
    }

     // Placeholder for backend call to activate premium
    private fun activatePremiumOnBackend(purchase: Purchase) {
        coroutineScope.launch {
            Timber.d("Attempting to activate premium on backend for order: ${purchase.orderId}")
            
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Timber.e("Cannot activate premium on backend: User not logged in.")
                return@launch
            }
            
            try {
                // Call the API endpoint to activate premium
                val result = userRepository.activatePremium(userId, purchase.purchaseToken)
                
                when (result) {
                    is Resource.Success -> {
                        Timber.i("Premium activated successfully on backend for order ${purchase.orderId}")
                        // Update local premium status
                        _premiumStatus.value = true
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to activate premium on backend: ${result.message}")
                        // You could retry or show an error to the user
                    }
                    is Resource.Loading -> {
                        // This should not happen in this case
                        Timber.d("Premium activation in progress")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error calling backend to activate premium for order ${purchase.orderId}")
                // Handle network or other errors
            }
        }
    }


    // Query existing purchases (e.g., on app start or billing setup)
    fun queryPurchasesAsync() {
        if (!billingClient.isReady) {
            Timber.w("queryPurchasesAsync called but BillingClient is not ready.")
            return
        }
        Timber.d("Querying existing purchases...")

        // Query for active subscriptions and non-consumed one-time purchases
        querySubscriptionPurchases()
        // queryInAppPurchases() // Add if you have one-time products
    }

    private fun querySubscriptionPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, purchaseList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Timber.d("Subscription purchases query successful: ${purchaseList.size} found.")
                // Check if we have any active premium subscriptions
                var hasPremiumSubscription = false
                
                purchaseList.forEach { purchase ->
                    if (purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        Timber.i("Found active and acknowledged subscription: ${purchase.products.firstOrNull()}")
                        
                        // Check if this is our premium product
                        if (purchase.products.contains(PREMIUM_PRODUCT_ID)) {
                            hasPremiumSubscription = true
                            // Update backend if needed
                            activatePremiumOnBackend(purchase)
                        }
                    } else {
                        // Handle unacknowledged or pending subscriptions if necessary
                        handlePurchase(purchase)
                    }
                }
                
                // Update premium status based on active subscriptions
                _premiumStatus.value = hasPremiumSubscription
            } else {
                Timber.e("Failed to query subscription purchases: ${billingResult.debugMessage}")
            }
        }
    }

     // Optional: Add query for INAPP products if needed
    // private fun queryInAppPurchases() { ... }


    // Call this when your app/activity is destroyed
    fun destroy() {
        if (::billingClient.isInitialized && billingClient.isReady) {
            Timber.d("Closing BillingClient connection.")
            billingClient.endConnection()
        }
        coroutineScope.cancel() // Cancel ongoing coroutines
    }
}

// Helper function for signature verification (needs implementation or use backend)
// private fun verifyValidSignature(signedData: String, signature: String): Boolean {
//    // IMPORTANT: It's strongly recommended to perform signature verification
//    // on your backend server to prevent tampering.
//    // If you must do it on the client, use your Base64 encoded public key
//    // from the Google Play Console.
//    // return Security.verifyPurchase(BASE_64_ENCODED_PUBLIC_KEY, signedData, signature)
//    Timber.w("Signature verification is NOT implemented. Skipping check.")
//    return true // Placeholder - DO NOT use in production without proper verification
// } 