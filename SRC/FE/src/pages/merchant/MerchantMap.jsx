import React, { useEffect, useRef, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  Box,
  Typography,
  Card,
  Chip,
  Button,
  CircularProgress,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  Store as StoreIcon,
  LocationOn as LocationIcon,
} from '@mui/icons-material';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import foodService from '../../services/foodService';

export const MerchantMap = () => {
  const { restaurant } = useOutletContext();
  const mapContainerRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const markersGroupRef = useRef(null);

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchOrdersForMap = async () => {
    if (!restaurant?.id) return;
    try {
      setLoading(true);
      const res = await foodService.getRestaurantOrders(restaurant.id);
      const list = Array.isArray(res) ? res : res?.data || [];
      setOrders(list);
    } catch (err) {
      console.warn('Lỗi lấy đơn hàng cho bản đồ:', err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrdersForMap();
  }, [restaurant?.id]);

  // Init Leaflet Map
  useEffect(() => {
    if (!mapContainerRef.current) return;

    const lat = restaurant?.latitude || 21.033333;
    const lng = restaurant?.longitude || 105.789123;

    if (!mapInstanceRef.current) {
      const map = L.map(mapContainerRef.current).setView([lat, lng], 13);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
      }).addTo(map);

      markersGroupRef.current = L.layerGroup().addTo(map);
      mapInstanceRef.current = map;
    } else {
      mapInstanceRef.current.setView([lat, lng], 13);
      mapInstanceRef.current.invalidateSize();
    }

    return () => {
      // Keep map alive or clean up on unmount
    };
  }, [restaurant?.latitude, restaurant?.longitude]);

  // Update Markers
  useEffect(() => {
    if (!mapInstanceRef.current || !markersGroupRef.current) return;

    markersGroupRef.current.clearLayers();

    // 1. Restaurant Marker
    if (restaurant?.latitude && restaurant?.longitude) {
      const resIcon = L.divIcon({
        className: 'custom-res-icon',
        html: `<div style="font-size: 28px; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3));">🏪</div>`,
        iconSize: [32, 32],
        iconAnchor: [16, 16],
      });

      L.marker([restaurant.latitude, restaurant.longitude], { icon: resIcon })
        .addTo(markersGroupRef.current)
        .bindPopup(`<b>${restaurant.name}</b><br>${restaurant.address || ''}`)
        .openPopup();
    }

    // 2. Orders Markers
    const activeOrders = orders.filter((o) =>
      ['PENDING', 'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'DELIVERING'].includes(o.status)
    );

    activeOrders.forEach((o) => {
      if (o.deliveryLatitude && o.deliveryLongitude) {
        const orderIcon = L.divIcon({
          className: 'custom-order-icon',
          html: `<div style="font-size: 24px; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3));">📍</div>`,
          iconSize: [24, 24],
          iconAnchor: [12, 24],
        });

        const custInfo = o.customerName ? `Khách: <b>${o.customerName}</b><br>` : '';
        L.marker([o.deliveryLatitude, o.deliveryLongitude], { icon: orderIcon })
          .addTo(markersGroupRef.current)
          .bindPopup(`
            <b>Đơn hàng #OF${o.id}</b><br>
            ${custInfo}
            📍 ${o.deliveryAddress || 'Địa chỉ giao'}<br>
            Trạng thái: <b>${o.status}</b>
          `);
      }
    });
  }, [orders, restaurant]);

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1.5 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800, color: '#0F172A', mb: 0.3 }}>
            Bản đồ đơn giao
          </Typography>
          <Typography variant="body2" sx={{ color: '#64748B' }}>
            Theo dõi trực quan vị trí nhà hàng và các điểm giao hàng của đơn đang kích hoạt
          </Typography>
        </Box>

        <Button
          variant="outlined"
          size="small"
          startIcon={loading ? <CircularProgress size={16} /> : <RefreshIcon />}
          onClick={fetchOrdersForMap}
          sx={{ borderRadius: 2.5, textTransform: 'none', fontWeight: 700 }}
        >
          Làm mới bản đồ
        </Button>
      </Box>

      {/* Map Container */}
      <Card
        sx={{
          borderRadius: 3.5,
          border: '1px solid #E2E8F0',
          overflow: 'hidden',
          boxShadow: '0 2px 10px rgba(0,0,0,0.03)',
        }}
      >
        <Box
          ref={mapContainerRef}
          sx={{
            width: '100%',
            height: '680px',
            minHeight: '500px',
            bgcolor: '#E2E8F0',
          }}
        />
      </Card>
    </Box>
  );
};

export default MerchantMap;
