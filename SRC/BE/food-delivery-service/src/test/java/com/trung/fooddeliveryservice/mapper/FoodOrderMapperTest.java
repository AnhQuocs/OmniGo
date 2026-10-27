package com.trung.fooddeliveryservice.mapper;

import com.trung.fooddeliveryservice.entity.FoodOrder;
import com.trung.fooddeliveryservice.entity.Restaurant;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FoodOrderMapperTest {
    private final FoodOrderMapper mapper = new FoodOrderMapper();

    @Test
    void mapsRestaurantCoordinatesSeparatelyFromDeliveryCoordinates() throws Exception {
        var restaurant = Restaurant.builder().id(12L).latitude(10.75).longitude(106.65).build();
        var order = FoodOrder.builder().id(74L).restaurant(restaurant)
                .deliveryLatitude(21.0).deliveryLongitude(105.0).build();
        var response = mapper.toResponse(order);
        var json = new ObjectMapper().valueToTree(response);
        assertEquals(10.75, json.get("restaurantLatitude").asDouble());
        assertEquals(106.65, json.get("restaurantLongitude").asDouble());
        assertEquals(21.0, response.getDeliveryLatitude());
        assertEquals(105.0, response.getDeliveryLongitude());
        assertEquals(10.75, mapper.toResponseList(List.of(order)).get(0).getRestaurantLatitude());
    }

    @Test
    void preservesNullCoordinatesWithoutInventingZeroLocation() {
        var order = FoodOrder.builder().restaurant(Restaurant.builder().build()).build();
        assertNull(mapper.toResponse(order).getRestaurantLatitude());
        assertNull(mapper.toResponse(order).getRestaurantLongitude());
        order.setRestaurant(null);
        assertNull(mapper.toResponse(order).getRestaurantLatitude());
        assertNull(mapper.toResponse(order).getRestaurantLongitude());
    }
}
