import React, { useEffect, useRef, useState, useCallback } from 'react';
import { useOutletContext, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Card,
  Button,
  CircularProgress,
  TextField,
  InputBase,
  InputAdornment,
  IconButton,
  Paper,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Chip,
  Avatar,
  Divider,
  Stack,
  Alert,
  Tooltip,
  ClickAwayListener,
  Popper,
} from '@mui/material';
import {
  Search as SearchIcon,
  MyLocation as MyLocationIcon,
  Save as SaveIcon,
  Settings as SettingsIcon,
  Store as StoreIcon,
  Place as PlaceIcon,
  Clear as ClearIcon,
  Visibility as ViewIcon,
  Schedule as TimeIcon,
  Phone as PhoneIcon,
  LocationOn as LocationIcon,
  GpsFixed as GpsIcon,
  Assignment as LicenseIcon,
  Close as CloseIcon,
} from '@mui/icons-material';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import foodService from '../../services/foodService';
import toast from 'react-hot-toast';

export const MerchantMap = () => {
  const { restaurant, refreshRestaurant } = useOutletContext();
  const navigate = useNavigate();

  const mapContainerRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const markerRef = useRef(null);
  const searchInputRef = useRef(null);

  // Coordinates and Address state
  const [address, setAddress] = useState('');
  const [latitude, setLatitude] = useState(21.033333);
  const [longitude, setLongitude] = useState(105.789123);

  // Search & Autocomplete state
  const [searchQuery, setSearchQuery] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [searching, setSearching] = useState(false);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const searchTimeoutRef = useRef(null);

  // UI state
  const [locating, setLocating] = useState(false);
  const [saving, setSaving] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [licensePreviewOpen, setLicensePreviewOpen] = useState(false);

  // Initialize data from restaurant context
  useEffect(() => {
    if (restaurant) {
      if (restaurant.address) setAddress(restaurant.address);
      if (restaurant.latitude) setLatitude(Number(restaurant.latitude));
      if (restaurant.longitude) setLongitude(Number(restaurant.longitude));
    }
  }, [restaurant]);

  // Custom Leaflet DivIcon for Restaurant Marker
  const createMarkerIcon = () => {
    return L.divIcon({
      className: 'custom-res-pin',
      html: `
        <div style="position: relative; width: 44px; height: 50px; transform: translate(-50%, -100%);">
          <div style="
            width: 44px;
            height: 44px;
            background: linear-gradient(135deg, #F97316 0%, #EA580C 100%);
            border-radius: 50% 50% 50% 0;
            transform: rotate(-45deg);
            box-shadow: 0 4px 14px rgba(234, 88, 12, 0.45);
            display: flex;
            align-items: center;
            justify-content: center;
            border: 3px solid #FFFFFF;
          ">
            <span style="transform: rotate(45deg); font-size: 20px;">🏪</span>
          </div>
        </div>
      `,
      iconSize: [0, 0],
      iconAnchor: [0, 0],
      popupAnchor: [0, -48],
    });
  };

  // Reverse Geocoding with OpenStreetMap Nominatim
  const reverseGeocode = async (lat, lng) => {
    try {
      const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&addressdetails=1`;
      const res = await fetch(url, { headers: { 'Accept-Language': 'vi' } });
      if (!res.ok) return;
      const data = await res.json();
      if (data && data.display_name) {
        setAddress(data.display_name);
      }
    } catch (err) {
      console.warn('Lỗi reverse geocode:', err.message);
    }
  };

  // Handle Marker Position Change
  const updateMarkerPosition = useCallback((lat, lng, fetchAddress = true) => {
    const latFixed = Number(lat.toFixed(6));
    const lngFixed = Number(lng.toFixed(6));
    setLatitude(latFixed);
    setLongitude(lngFixed);

    if (markerRef.current) {
      markerRef.current.setLatLng([lat, lng]);
      markerRef.current.setPopupContent(`
        <div style="font-family: inherit; font-size: 13px; line-height: 1.4;">
          <b style="color: #F97316; font-size: 14px;">${restaurant?.name || 'Vị trí Quán ăn'}</b><br/>
          <span>Tọa độ: ${latFixed}, ${lngFixed}</span>
        </div>
      `);
    }

    if (fetchAddress) {
      reverseGeocode(lat, lng);
    }
  }, [restaurant?.name]);

  // Initialize Map
  useEffect(() => {
    if (!mapContainerRef.current) return;

    const initialLat = restaurant?.latitude ? Number(restaurant.latitude) : latitude;
    const initialLng = restaurant?.longitude ? Number(restaurant.longitude) : longitude;

    if (!mapInstanceRef.current) {
      const map = L.map(mapContainerRef.current).setView([initialLat, initialLng], 15);

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
      }).addTo(map);

      // Add Draggable Marker
      const marker = L.marker([initialLat, initialLng], {
        icon: createMarkerIcon(),
        draggable: true,
      }).addTo(map);

      marker.bindPopup(`
        <div style="font-family: inherit; font-size: 13px; line-height: 1.4;">
          <b style="color: #F97316; font-size: 14px;">${restaurant?.name || 'Vị trí Quán ăn'}</b><br/>
          <span>${restaurant?.address || 'Kéo ghim hoặc nhấp vào bản đồ để chọn vị trí'}</span>
        </div>
      `);

      marker.on('dragend', () => {
        const pos = marker.getLatLng();
        updateMarkerPosition(pos.lat, pos.lng, true);
      });

      // Click on Map to Move Marker
      map.on('click', (e) => {
        updateMarkerPosition(e.latlng.lat, e.latlng.lng, true);
        if (mapInstanceRef.current) {
          mapInstanceRef.current.panTo([e.latlng.lat, e.latlng.lng]);
        }
      });

      markerRef.current = marker;
      mapInstanceRef.current = map;
    } else {
      mapInstanceRef.current.setView([initialLat, initialLng], 15);
      if (markerRef.current) {
        markerRef.current.setLatLng([initialLat, initialLng]);
      }
      mapInstanceRef.current.invalidateSize();
    }
  }, [restaurant?.id]);

  // Autocomplete Search Debounce using Photon API (OpenStreetMap) with fallback
  const handleSearchChange = (e) => {
    const val = e.target.value;
    setSearchQuery(val);

    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }

    if (!val || val.trim().length < 2) {
      setSuggestions([]);
      setShowSuggestions(false);
      return;
    }

    setSearching(true);
    searchTimeoutRef.current = setTimeout(async () => {
      try {
        // 1. First try Photon API (Fast OpenStreetMap Autocomplete)
        const photonUrl = `https://photon.komoot.io/api/?q=${encodeURIComponent(val)}&lang=vi&limit=6`;
        const res = await fetch(photonUrl);
        if (res.ok) {
          const data = await res.json();
          const items = (data.features || []).map((f) => {
            const p = f.properties || {};
            const coords = f.geometry?.coordinates || [0, 0];
            const full = [p.name, p.street, p.district, p.city].filter(Boolean).filter((v, i, a) => a.indexOf(v) === i).join(', ');
            return {
              id: p.osm_id || Math.random(),
              name: p.name || full,
              full: full || p.name || val,
              lat: coords[1],
              lon: coords[0],
            };
          });
          if (items.length > 0) {
            setSuggestions(items);
            setShowSuggestions(true);
            return;
          }
        }

        // 2. Fallback to Nominatim API
        const nomUrl = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(val)}&format=json&countrycodes=vn&addressdetails=1&limit=6`;
        const nomRes = await fetch(nomUrl, { headers: { 'Accept-Language': 'vi' } });
        if (nomRes.ok) {
          const nomData = await nomRes.json();
          const items = (nomData || []).map((d) => ({
            id: d.place_id,
            name: d.name || d.display_name?.split(',')[0],
            full: d.display_name,
            lat: parseFloat(d.lat),
            lon: parseFloat(d.lon),
          }));
          setSuggestions(items);
          setShowSuggestions(items.length > 0);
        }
      } catch (err) {
        console.warn('Lỗi tìm kiếm gợi ý:', err.message);
      } finally {
        setSearching(false);
      }
    }, 350);
  };

  // Select a suggestion
  const handleSelectSuggestion = (item) => {
    const lat = Number(item.lat);
    const lon = Number(item.lon);
    setAddress(item.full || item.name);
    setSearchQuery(item.name || '');
    setSuggestions([]);
    setShowSuggestions(false);

    updateMarkerPosition(lat, lon, false);

    if (mapInstanceRef.current) {
      mapInstanceRef.current.flyTo([lat, lon], 16, { duration: 1.2 });
    }
  };

  // Geocode address string (locate by restaurant address)
  const handleGeocodeAddress = async (addrToSearch = address) => {
    const query = addrToSearch || address || restaurant?.address;
    if (!query || query.trim().length < 2) {
      toast.error('Vui lòng nhập địa chỉ cần định vị');
      return;
    }

    try {
      setLocating(true);
      // 1. Try Photon first
      const photonUrl = `https://photon.komoot.io/api/?q=${encodeURIComponent(query)}&lang=vi&limit=1`;
      const res = await fetch(photonUrl);
      if (res.ok) {
        const data = await res.json();
        if (data.features && data.features.length > 0) {
          const f = data.features[0];
          const coords = f.geometry?.coordinates || [0, 0];
          const lat = coords[1];
          const lon = coords[0];
          const p = f.properties || {};
          const full = [p.name, p.street, p.district, p.city].filter(Boolean).join(', ');

          updateMarkerPosition(lat, lon, false);
          if (mapInstanceRef.current) {
            mapInstanceRef.current.flyTo([lat, lon], 16, { duration: 1.2 });
          }
          toast.success(`Đã định vị thành công: ${full || query}`);
          return;
        }
      }

      // 2. Fallback to Nominatim
      const nomUrl = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&countrycodes=vn&limit=1`;
      const nomRes = await fetch(nomUrl, { headers: { 'Accept-Language': 'vi' } });
      if (nomRes.ok) {
        const nomData = await nomRes.json();
        if (nomData && nomData.length > 0) {
          const lat = parseFloat(nomData[0].lat);
          const lon = parseFloat(nomData[0].lon);
          updateMarkerPosition(lat, lon, false);
          if (mapInstanceRef.current) {
            mapInstanceRef.current.flyTo([lat, lon], 16, { duration: 1.2 });
          }
          toast.success(`Đã định vị thành công: ${nomData[0].display_name}`);
          return;
        }
      }
      toast.error('Không tìm thấy tọa độ cho địa chỉ này. Bạn có thể kéo ghim 🏪 trên bản đồ để chọn.');
    } catch (err) {
      toast.error('Lỗi khi định vị: ' + err.message);
    } finally {
      setLocating(false);
    }
  };

  // GPS Geolocation: Get Current Position (Fixing cache & precision handling)
  const handleGetCurrentLocation = () => {
    if (!navigator.geolocation) {
      toast.error('Trình duyệt của bạn không hỗ trợ định vị GPS');
      return;
    }

    setLocating(true);
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;
        const accuracy = Math.round(position.coords.accuracy || 0);

        updateMarkerPosition(lat, lng, true);

        if (mapInstanceRef.current) {
          mapInstanceRef.current.flyTo([lat, lng], 16, { duration: 1.2 });
        }

        // On desktop/PC with LAN or weak Wi-Fi, geolocation may default to ISP center
        if (accuracy > 500) {
          toast(
            `Định vị thiết bị (độ lệch ~${accuracy}m do định vị mạng/IP máy tính). Bạn có thể kéo ghim 🏪 hoặc gõ địa chỉ vào ô tìm kiếm để định vị chính xác nhất.`,
            { icon: '📍', duration: 7000 }
          );
        } else {
          toast.success(`Đã lấy vị trí GPS hiện tại (độ chính xác ±${accuracy}m)!`);
        }
        setLocating(false);
      },
      (error) => {
        setLocating(false);
        let msg = 'Không thể lấy vị trí hiện tại';
        if (error.code === error.PERMISSION_DENIED) {
          msg = 'Vui lòng cho phép quyền truy cập vị trí trong trình duyệt';
        } else if (error.code === error.TIMEOUT) {
          msg = 'Quá thời gian lấy vị trí GPS';
        }
        toast.error(msg);
      },
      { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }
    );
  };

  // Save Restaurant Location to Database
  const handleSaveLocation = async () => {
    if (!restaurant?.id) {
      toast.error('Chưa có thông tin quán ăn để cập nhật');
      return;
    }

    if (!address || !address.trim()) {
      toast.error('Vui lòng nhập hoặc chọn địa chỉ của quán');
      return;
    }

    const payload = {
      name: restaurant.name,
      phone: restaurant.phone,
      address: address.trim(),
      latitude: parseFloat(latitude),
      longitude: parseFloat(longitude),
      openTime: restaurant.openTime || '08:00',
      closeTime: restaurant.closeTime || '22:00',
      imageUrl: restaurant.imageUrl || 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=500',
    };

    try {
      setSaving(true);
      await foodService.updateRestaurant(restaurant.id, payload);
      toast.success('Đã cập nhật vị trí và địa chỉ quán thành công!');
      if (refreshRestaurant) {
        await refreshRestaurant();
      }
    } catch (err) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi lưu vị trí quán');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
      {/* Top Header */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1.5 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800, color: '#0F172A', mb: 0.3 }}>
            Bản đồ vị trí quán ăn
          </Typography>
          <Typography variant="body2" sx={{ color: '#64748B' }}>
            Xác định tọa độ GPS và địa chỉ kinh doanh chính xác của quán trên bản đồ số
          </Typography>
        </Box>

        <Stack direction="row" spacing={1.5}>
          {/* Nút Xem Chi Tiết Quán (Chế độ xem - Read-only) */}
          <Button
            variant="outlined"
            size="small"
            startIcon={<ViewIcon />}
            onClick={() => setDetailModalOpen(true)}
            sx={{
              borderRadius: 2.5,
              textTransform: 'none',
              fontWeight: 700,
              color: '#334155',
              borderColor: '#CBD5E1',
              '&:hover': { borderColor: '#94A3B8', bgcolor: '#F8FAFC' },
            }}
          >
            Chi tiết quán
          </Button>

          {/* Nút Đến Cài Đặt Quán */}
          <Button
            variant="outlined"
            size="small"
            startIcon={<SettingsIcon />}
            onClick={() => navigate('/merchant/settings')}
            sx={{
              borderRadius: 2.5,
              textTransform: 'none',
              fontWeight: 700,
              color: '#F97316',
              borderColor: '#FED7AA',
              '&:hover': { borderColor: '#F97316', bgcolor: '#FFF7ED' },
            }}
          >
            Cài đặt quán
          </Button>
        </Stack>
      </Box>

      <Card
        sx={{
          p: { xs: 2, sm: 2.5 },
          borderRadius: 3.5,
          border: '1px solid #E2E8F0',
          boxShadow: '0 2px 10px rgba(0,0,0,0.03)',
          bgcolor: '#FFFFFF',
          position: 'relative',
          overflow: 'visible !important',
          zIndex: 100,
        }}
      >
        <Stack spacing={2}>
          {/* Row 1: Address, Coordinates & Save Button */}
          <Stack
            direction={{ xs: 'column', md: 'row' }}
            spacing={2}
            alignItems={{ xs: 'stretch', md: 'center' }}
            justifyContent="space-between"
          >
            {/* Address Field with Locate Button */}
            <TextField
              label="Địa chỉ chi nhánh quán ăn"
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              size="small"
              fullWidth
              multiline
              maxRows={2}
              helperText="Có thể chỉnh sửa chi tiết số nhà/ngõ, hoặc tìm kiếm / kéo ghim trên bản đồ để đổi vị trí"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <LocationIcon sx={{ color: '#F97316' }} />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <Tooltip title="Định vị bản đồ theo địa chỉ này">
                      <span>
                        <IconButton
                          size="small"
                          onClick={() => handleGeocodeAddress(address)}
                          disabled={locating || !address}
                          sx={{ color: '#0284C7' }}
                        >
                          <SearchIcon fontSize="small" />
                        </IconButton>
                      </span>
                    </Tooltip>
                  </InputAdornment>
                ),
              }}
              sx={{ flex: 2 }}
            />

            {/* Coordinates Fields */}
            <Stack direction="row" spacing={1.5} sx={{ flex: 1 }}>
              <TextField
                label="Vĩ độ (Lat)"
                value={latitude}
                size="small"
                fullWidth
                InputProps={{ readOnly: true }}
                sx={{ bgcolor: '#F8FAFC' }}
              />
              <TextField
                label="Kinh độ (Lng)"
                value={longitude}
                size="small"
                fullWidth
                InputProps={{ readOnly: true }}
                sx={{ bgcolor: '#F8FAFC' }}
              />
            </Stack>

            {/* Save Button */}
            <Button
              variant="contained"
              onClick={handleSaveLocation}
              disabled={saving}
              startIcon={saving ? <CircularProgress size={18} color="inherit" /> : <SaveIcon />}
              sx={{
                minWidth: { xs: '100%', md: 170 },
                height: 40,
                borderRadius: 2.5,
                bgcolor: '#F97316',
                fontWeight: 700,
                textTransform: 'none',
                boxShadow: 'none',
                '&:hover': { bgcolor: '#EA580C' },
              }}
            >
              {saving ? 'Đang lưu...' : 'Lưu vị trí quán'}
            </Button>
          </Stack>

          <Divider sx={{ my: 0.5 }} />

          {/* Row 2: Ô tìm kiếm gợi ý Autocomplete & Nút Vị trí hiện tại (Nằm ở TRÊN HẲN BẢN ĐỒ, DƯỚI phần địa chỉ) */}
          <ClickAwayListener onClickAway={() => setShowSuggestions(false)}>
            <Box sx={{ position: 'relative' }}>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} alignItems="center">
                <Box ref={searchInputRef} sx={{ flex: 1, width: '100%' }}>
                  <TextField
                    fullWidth
                    size="small"
                    placeholder="Nhập tên địa điểm, trường học, đường phố để gợi ý tự động (VD: Phenikaa, Cầu Giấy)..."
                    value={searchQuery}
                    onChange={handleSearchChange}
                    onFocus={() => suggestions.length > 0 && setShowSuggestions(true)}
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          {searching ? <CircularProgress size={18} color="primary" /> : <SearchIcon color="action" />}
                        </InputAdornment>
                      ),
                      endAdornment: searchQuery ? (
                        <InputAdornment position="end">
                          <IconButton
                            size="small"
                            onClick={() => {
                              setSearchQuery('');
                              setSuggestions([]);
                              setShowSuggestions(false);
                            }}
                          >
                            <ClearIcon fontSize="small" />
                          </IconButton>
                        </InputAdornment>
                      ) : null,
                    }}
                    sx={{
                      '& .MuiOutlinedInput-root': {
                        borderRadius: 2.5,
                        bgcolor: '#F8FAFC',
                      },
                    }}
                  />
                </Box>

                <Tooltip title="Lấy vị trí GPS hiện tại của thiết bị">
                  <Button
                    variant="outlined"
                    onClick={handleGetCurrentLocation}
                    disabled={locating}
                    startIcon={locating ? <CircularProgress size={16} color="inherit" /> : <MyLocationIcon />}
                    sx={{
                      minWidth: { xs: '100%', sm: 175 },
                      height: 40,
                      borderRadius: 2.5,
                      textTransform: 'none',
                      fontWeight: 700,
                      whiteSpace: 'nowrap',
                      borderColor: '#38BDF8',
                      color: '#0284C7',
                      bgcolor: '#F0F9FF',
                      '&:hover': { bgcolor: '#E0F2FE', borderColor: '#0284C7' },
                    }}
                  >
                    {locating ? 'Đang định vị...' : 'Vị trí hiện tại'}
                  </Button>
                </Tooltip>
              </Stack>

              {/* Autocomplete Suggestions Dropdown List via Portal Popper (Never hidden under Leaflet map) */}
              <Popper
                open={Boolean(showSuggestions && suggestions.length > 0)}
                anchorEl={searchInputRef.current}
                placement="bottom-start"
                style={{
                  zIndex: 9999,
                  width: searchInputRef.current ? searchInputRef.current.clientWidth : 'auto',
                }}
              >
                <Paper
                  elevation={8}
                  sx={{
                    mt: 0.8,
                    borderRadius: 2.5,
                    overflow: 'hidden',
                    border: '1px solid #CBD5E1',
                    maxHeight: 320,
                    overflowY: 'auto',
                    bgcolor: '#FFFFFF',
                    boxShadow: '0 16px 36px rgba(0,0,0,0.22)',
                  }}
                >
                  <List disablePadding>
                    {suggestions.map((item, idx) => (
                      <ListItem key={item.id || idx} disablePadding>
                        <ListItemButton
                          onClick={() => handleSelectSuggestion(item)}
                          sx={{
                            py: 1.2,
                            px: 2,
                            borderBottom: '1px solid #F1F5F9',
                            '&:hover': { bgcolor: '#FFF7ED' },
                          }}
                        >
                          <ListItemIcon sx={{ minWidth: 34, color: '#F97316' }}>
                            <PlaceIcon fontSize="small" />
                          </ListItemIcon>
                          <ListItemText
                            primary={item.name}
                            secondary={item.full !== item.name ? item.full : null}
                            primaryTypographyProps={{
                              fontSize: '0.86rem',
                              fontWeight: 700,
                              color: '#0F172A',
                            }}
                            secondaryTypographyProps={{
                              fontSize: '0.78rem',
                              color: '#64748B',
                              noWrap: true,
                            }}
                          />
                        </ListItemButton>
                      </ListItem>
                    ))}
                  </List>
                </Paper>
              </Popper>
            </Box>
          </ClickAwayListener>
        </Stack>
      </Card>

      {/* 2. Bản đồ Container (Nằm riêng biệt hoàn toàn ở dưới, KHÔNG bị che khuất) */}
      <Card
        sx={{
          borderRadius: 3.5,
          border: '1px solid #E2E8F0',
          overflow: 'hidden',
          boxShadow: '0 2px 10px rgba(0,0,0,0.03)',
          position: 'relative',
        }}
      >
        {/* Mẹo góc trên bên phải trên bản đồ */}
        <Box
          sx={{
            position: 'absolute',
            top: 14,
            right: 14,
            zIndex: 1000,
            bgcolor: 'rgba(255, 255, 255, 0.95)',
            backdropFilter: 'blur(4px)',
            border: '1px solid #E2E8F0',
            borderRadius: 2.5,
            px: 2,
            py: 0.8,
            boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
            display: { xs: 'none', md: 'flex' },
            alignItems: 'center',
            gap: 1,
          }}
        >
          <Typography variant="caption" sx={{ fontWeight: 700, color: '#334155' }}>
            💡 Mẹo: Nhấp vào điểm bất kỳ hoặc kéo ghim 🏪 để đổi vị trí
          </Typography>
        </Box>

        {/* Map Canvas */}
        <Box
          ref={mapContainerRef}
          sx={{
            width: '100%',
            height: '640px',
            minHeight: '480px',
            bgcolor: '#E2E8F0',
          }}
        />
      </Card>

      {/* Modal Xem Chi Tiết Thông Tin Quán (Chế độ CHỈ XEM - Read Only) */}
      <Dialog
        open={detailModalOpen}
        onClose={() => setDetailModalOpen(false)}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: 3.5,
            p: 1,
          },
        }}
      >
        <DialogTitle sx={{ pb: 1 }}>
          <Stack direction="row" justifyContent="space-between" alignItems="center">
            <Typography variant="h6" fontWeight={800} color="#0F172A">
              Chi tiết thông tin quán ăn
            </Typography>
            <Chip label="Chế độ xem (Read-only)" size="small" color="default" sx={{ fontWeight: 700, fontSize: '0.75rem' }} />
          </Stack>
        </DialogTitle>

        <DialogContent dividers sx={{ py: 2.5 }}>
          <Stack spacing={2.5}>
            {/* Header with Avatar & Basic Info */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Avatar
                src={restaurant?.imageUrl || 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=200'}
                alt={restaurant?.name || 'Restaurant'}
                sx={{ width: 72, height: 72, borderRadius: 3, border: '2px solid #E2E8F0' }}
              />
              <Box>
                <Typography variant="subtitle1" fontWeight={800} color="#0F172A">
                  {restaurant?.name || 'Chưa đăng ký quán ăn'}
                </Typography>
                <Typography variant="caption" color="text.secondary" display="block">
                  Mã gian hàng: #{restaurant?.id || '---'}
                </Typography>
                <Chip
                  size="small"
                  label={restaurant?.status === 'OPEN' ? 'Đang mở cửa' : restaurant?.status === 'BUSY' ? 'Quán đang bận' : 'Tạm đóng cửa'}
                  sx={{
                    mt: 0.5,
                    height: 22,
                    fontSize: '0.72rem',
                    fontWeight: 700,
                    bgcolor: restaurant?.status === 'OPEN' ? '#ECFDF5' : '#FEF2F2',
                    color: restaurant?.status === 'OPEN' ? '#047857' : '#B91C1C',
                  }}
                />
              </Box>
            </Box>

            <Divider />

            {/* Detailed Properties List */}
            <Stack spacing={1.8}>
              <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5 }}>
                <PhoneIcon sx={{ color: '#64748B', fontSize: 20, mt: 0.2 }} />
                <Box>
                  <Typography variant="caption" color="text.secondary" fontWeight={600}>
                    Số điện thoại hotline
                  </Typography>
                  <Typography variant="body2" fontWeight={700} color="#0F172A">
                    {restaurant?.phone || 'Chưa thiết lập'}
                  </Typography>
                </Box>
              </Box>

              <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5 }}>
                <LocationIcon sx={{ color: '#F97316', fontSize: 20, mt: 0.2 }} />
                <Box>
                  <Typography variant="caption" color="text.secondary" fontWeight={600}>
                    Địa chỉ kinh doanh
                  </Typography>
                  <Typography variant="body2" fontWeight={700} color="#0F172A">
                    {restaurant?.address || 'Chưa thiết lập'}
                  </Typography>
                </Box>
              </Box>

              <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5 }}>
                <PlaceIcon sx={{ color: '#0284C7', fontSize: 20, mt: 0.2 }} />
                <Box>
                  <Typography variant="caption" color="text.secondary" fontWeight={600}>
                    Tọa độ GPS chi nhánh
                  </Typography>
                  <Typography variant="body2" fontWeight={700} color="#0F172A">
                    Vĩ độ: {restaurant?.latitude || latitude} | Kinh độ: {restaurant?.longitude || longitude}
                  </Typography>
                </Box>
              </Box>

              <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5 }}>
                <TimeIcon sx={{ color: '#64748B', fontSize: 20, mt: 0.2 }} />
                <Box>
                  <Typography variant="caption" color="text.secondary" fontWeight={600}>
                    Thời gian hoạt động hàng ngày
                  </Typography>
                  <Typography variant="body2" fontWeight={700} color="#0F172A">
                    {restaurant?.openTime || '08:00'} - {restaurant?.closeTime || '22:00'}
                  </Typography>
                </Box>
              </Box>

              {/* Giấy phép kinh doanh */}
              <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5 }}>
                <LicenseIcon sx={{ color: '#8B5CF6', fontSize: 20, mt: 0.2 }} />
                <Box sx={{ flex: 1 }}>
                  <Typography variant="caption" color="text.secondary" fontWeight={600}>
                    Giấy phép kinh doanh
                  </Typography>
                  {restaurant?.licenseImageUrl ? (
                    <Box sx={{ mt: 0.8, display: 'flex', alignItems: 'center', gap: 1.5 }}>
                      <Box
                        component="img"
                        src={restaurant.licenseImageUrl}
                        alt="Giấy phép kinh doanh"
                        onClick={() => setLicensePreviewOpen(true)}
                        sx={{
                          width: 80,
                          height: 55,
                          objectFit: 'cover',
                          borderRadius: 2,
                          border: '1px solid #DDD6FE',
                          cursor: 'pointer',
                          boxShadow: '0 2px 6px rgba(0,0,0,0.06)',
                          transition: 'all 0.2s',
                          '&:hover': { transform: 'scale(1.05)', borderColor: '#8B5CF6' },
                        }}
                      />
                      <Button
                        size="small"
                        variant="outlined"
                        onClick={() => setLicensePreviewOpen(true)}
                        sx={{
                          textTransform: 'none',
                          fontSize: '0.75rem',
                          fontWeight: 700,
                          borderRadius: 1.5,
                          color: '#7C3AED',
                          borderColor: '#DDD6FE',
                          '&:hover': { borderColor: '#7C3AED', bgcolor: '#F5F3FF' },
                        }}
                      >
                        🔍 Xem bản lớn
                      </Button>
                    </Box>
                  ) : (
                    <Typography variant="body2" sx={{ color: '#94A3B8', fontStyle: 'italic', mt: 0.3 }}>
                      Chưa cập nhật ảnh giấy phép kinh doanh
                    </Typography>
                  )}
                </Box>
              </Box>
            </Stack>

            {/* Notice Alert */}
            <Alert severity="info" sx={{ borderRadius: 2, fontSize: '0.82rem' }}>
              Đây là chế độ xem nhanh thông tin quán. Để chỉnh sửa tên quán, số điện thoại, giờ hoạt động hoặc ảnh quán, vui lòng vào trang <b>Cài đặt quán</b>.
            </Alert>
          </Stack>
        </DialogContent>

        <DialogActions sx={{ p: 2, justifyContent: 'space-between' }}>
          <Button
            variant="outlined"
            onClick={() => setDetailModalOpen(false)}
            sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600, color: '#64748B', borderColor: '#CBD5E1' }}
          >
            Đóng
          </Button>

          <Button
            variant="contained"
            startIcon={<SettingsIcon />}
            onClick={() => {
              setDetailModalOpen(false);
              navigate('/merchant/settings');
            }}
            sx={{
              borderRadius: 2,
              textTransform: 'none',
              fontWeight: 700,
              bgcolor: '#F97316',
              boxShadow: 'none',
              '&:hover': { bgcolor: '#EA580C' },
            }}
          >
            Đi đến Cài đặt quán để chỉnh sửa
          </Button>
        </DialogActions>
      </Dialog>

      {/* Lightbox xem ảnh Giấy Phép Kinh Doanh */}
      <Dialog open={licensePreviewOpen} onClose={() => setLicensePreviewOpen(false)} maxWidth="md">
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 1 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <LicenseIcon sx={{ color: '#7C3AED' }} />
            <Typography variant="h6" sx={{ fontWeight: 800 }}>
              Giấy phép kinh doanh - {restaurant?.name}
            </Typography>
          </Box>
          <IconButton onClick={() => setLicensePreviewOpen(false)} size="small">
            <CloseIcon fontSize="small" />
          </IconButton>
        </DialogTitle>
        <DialogContent sx={{ p: 2, textAlign: 'center', bgcolor: '#F8FAFC' }}>
          <Box
            component="img"
            src={restaurant?.licenseImageUrl}
            alt="Giấy phép kinh doanh"
            sx={{
              maxWidth: '100%',
              maxHeight: '75vh',
              objectFit: 'contain',
              borderRadius: 2,
              boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
            }}
          />
        </DialogContent>
      </Dialog>
    </Box>
  );
};

export default MerchantMap;
