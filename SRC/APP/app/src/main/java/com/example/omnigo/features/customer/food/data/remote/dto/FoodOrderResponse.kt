package com.example.omnigo.features.customer.food.data.remote.dto



import com.google.gson.annotations.SerializedName



data class FoodOrderItemResponse(

    @SerializedName("id")

    val id: Long? = null,

    @SerializedName("menuItemId")

    val menuItemId: Long,

    @SerializedName("itemName")

    val itemName: String? = null,

    @SerializedName(value = "price", alternate = ["itemPrice"])
    val itemPrice: Double? = null,

    @SerializedName("quantity")

    val quantity: Int = 1,

    @SerializedName("subtotal")

    val subtotal: Double? = null,

    @SerializedName("note")

    val note: String? = null

)



data class FoodOrderResponse(

    @SerializedName(value = "id", alternate = ["orderId"])
    val id: Long? = null,

    @SerializedName("customerId")
    val customerId: Long? = null,

    @SerializedName("driverId")

    val driverId: Long? = null,

    @SerializedName("driverName")

    val driverName: String? = null,

    @SerializedName("driverPhone")

    val driverPhone: String? = null,

    @SerializedName(value = "vehiclePlate", alternate = ["driverVehiclePlate"])
    val driverVehiclePlate: String? = null,

    @SerializedName("avatarUrl")
    val driverAvatarUrl: String? = null,

    @SerializedName("restaurantId")

    val restaurantId: Long? = null,

    @SerializedName("restaurantName")

    val restaurantName: String? = null,

    @SerializedName("restaurantAddress")

    val restaurantAddress: String? = null,

    @SerializedName("status")

    val status: String? = null,

    @SerializedName("paymentMethod")

    val paymentMethod: String? = null,

    @SerializedName("isPaid")

    val isPaid: Boolean? = null,

    @SerializedName("itemsPrice")

    val itemsPrice: Double? = null,

    @SerializedName("deliveryFee")

    val deliveryFee: Double? = null,

    @SerializedName("totalPrice")

    val totalPrice: Double? = null,

    @SerializedName(value = "deliveryAddress", alternate = ["dropOffAddress"])
    val dropOffAddress: String? = null,

    @SerializedName(value = "deliveryLatitude", alternate = ["dropOffLatitude"])
    val dropOffLatitude: Double? = null,

    @SerializedName(value = "deliveryLongitude", alternate = ["dropOffLongitude"])
    val dropOffLongitude: Double? = null,

    @SerializedName("note")

    val note: String? = null,

    @SerializedName(value = "orderItems", alternate = ["items"])
    val items: List<FoodOrderItemResponse>? = null,

    @SerializedName("estimatedDeliveryMinutes")

    val estimatedDeliveryMinutes: Int? = null,

    @SerializedName("createdAt")

    val createdAt: String? = null

)
