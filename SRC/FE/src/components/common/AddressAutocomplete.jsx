import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  TextField,
  Typography,
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  InputAdornment,
  IconButton,
  CircularProgress,
  Tooltip,
  Chip,
  Collapse,
  Button,
  ClickAwayListener,
} from '@mui/material';
import {
  LocationOn as LocationIcon,
  MyLocation as GpsIcon,
  Clear as ClearIcon,
  CheckCircleRounded as CheckIcon,
  Tune as TuneIcon,
  Search as SearchIcon,
  InfoOutlined as InfoIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';

export const AddressAutocomplete = ({
  label = 'Địa chỉ nhà hàng',
  placeholder = 'Nhập địa chỉ để tìm kiếm & định vị tự động...',
  value = '',
  latitude = '',
  longitude = '',
  onChangeAddress,
  onSelectLocation,
  onChangeCoordinates,
  required = false,
  disabled = false,
  error = false,
  helperText = '',
}) => {
  const [inputValue, setInputValue] = useState(value || '');
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [searching, setSearching] = useState(false);
  const [locatingGps, setLocatingGps] = useState(false);
  const [showManualCoords, setShowManualCoords] = useState(false);

  const searchTimeoutRef = useRef(null);
  const containerRef = useRef(null);

  // Synchronize internal input value with external value
  useEffect(() => {
    setInputValue(value || '');
  }, [value]);

  // Handle address input change and debounce autocomplete search
  const handleInputChange = (e) => {
    const val = e.target.value;
    setInputValue(val);
    if (onChangeAddress) {
      onChangeAddress(val);
    }

    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }

    if (!val || val.trim().length < 2) {
      setSuggestions([]);
      setShowSuggestions(false);
      setSearching(false);
      return;
    }

    setSearching(true);
    searchTimeoutRef.current = setTimeout(async () => {
      try {
        // 1. Try Photon API (Fast OSM autocomplete)
        const photonUrl = `https://photon.komoot.io/api/?q=${encodeURIComponent(val)}&lang=vi&limit=6`;
        const res = await fetch(photonUrl);
        if (res.ok) {
          const data = await res.json();
          const items = (data.features || []).map((f) => {
            const p = f.properties || {};
            const coords = f.geometry?.coordinates || [0, 0];
            const full = [p.name, p.street, p.district, p.city]
              .filter(Boolean)
              .filter((v, i, a) => a.indexOf(v) === i)
              .join(', ');
            return {
              id: p.osm_id || Math.random(),
              name: p.name || full,
              full: full || p.name || val,
              lat: Number(Number(coords[1]).toFixed(6)),
              lon: Number(Number(coords[0]).toFixed(6)),
            };
          });

          if (items.length > 0) {
            setSuggestions(items);
            setShowSuggestions(true);
            return;
          }
        }

        // 2. Fallback to Nominatim API
        const nomUrl = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
          val
        )}&format=json&countrycodes=vn&addressdetails=1&limit=6`;
        const nomRes = await fetch(nomUrl, { headers: { 'Accept-Language': 'vi' } });
        if (nomRes.ok) {
          const nomData = await nomRes.json();
          const items = (nomData || []).map((d) => ({
            id: d.place_id,
            name: d.name || d.display_name?.split(',')[0],
            full: d.display_name,
            lat: Number(parseFloat(d.lat).toFixed(6)),
            lon: Number(parseFloat(d.lon).toFixed(6)),
          }));
          setSuggestions(items);
          setShowSuggestions(items.length > 0);
        }
      } catch (err) {
        console.warn('Lỗi tìm kiếm gợi ý địa chỉ:', err.message);
      } finally {
        setSearching(false);
      }
    }, 350);
  };

  // Select a suggestion item
  const handleSelectSuggestion = (item) => {
    const selectedAddress = item.full || item.name;
    setInputValue(selectedAddress);
    setSuggestions([]);
    setShowSuggestions(false);

    if (onSelectLocation) {
      onSelectLocation({
        address: selectedAddress,
        latitude: item.lat,
        longitude: item.lon,
      });
    } else {
      if (onChangeAddress) onChangeAddress(selectedAddress);
      if (onChangeCoordinates) {
        onChangeCoordinates({ latitude: item.lat, longitude: item.lon });
      }
    }

    toast.success(`Đã tự động xác định tọa độ: ${item.lat}, ${item.lon}`);
  };

  // Get device GPS location
  const handleGetGpsLocation = () => {
    if (!navigator.geolocation) {
      toast.error('Trình duyệt không hỗ trợ định vị GPS');
      return;
    }

    setLocatingGps(true);
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const lat = Number(pos.coords.latitude.toFixed(6));
        const lng = Number(pos.coords.longitude.toFixed(6));

        try {
          // Reverse geocode to get address text
          const revRes = await fetch(
            `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&addressdetails=1`,
            { headers: { 'Accept-Language': 'vi' } }
          );
          let detectedAddress = '';
          if (revRes.ok) {
            const revData = await revRes.json();
            if (revData?.display_name) {
              detectedAddress = revData.display_name;
              setInputValue(detectedAddress);
            }
          }

          if (onSelectLocation) {
            onSelectLocation({
              address: detectedAddress || inputValue || `Vị trí GPS (${lat}, ${lng})`,
              latitude: lat,
              longitude: lng,
            });
          } else {
            if (detectedAddress && onChangeAddress) onChangeAddress(detectedAddress);
            if (onChangeCoordinates) {
              onChangeCoordinates({ latitude: lat, longitude: lng });
            }
          }

          toast.success(`Đã lấy vị trí GPS hiện tại thành công (${lat}, ${lng})`);
        } catch (err) {
          if (onSelectLocation) {
            onSelectLocation({
              address: inputValue,
              latitude: lat,
              longitude: lng,
            });
          }
          toast.success(`Đã lấy tọa độ GPS: ${lat}, ${lng}`);
        } finally {
          setLocatingGps(false);
        }
      },
      (err) => {
        setLocatingGps(false);
        let msg = 'Không thể lấy vị trí hiện tại';
        if (err.code === 1) msg = 'Vui lòng cấp quyền truy cập vị trí trong trình duyệt';
        toast.error(msg);
      },
      { enableHighAccuracy: true, timeout: 12000, maximumAge: 0 }
    );
  };

  const hasCoordinates =
    latitude !== '' &&
    longitude !== '' &&
    !isNaN(Number(latitude)) &&
    !isNaN(Number(longitude));

  return (
    <ClickAwayListener onClickAway={() => setShowSuggestions(false)}>
      <Box ref={containerRef} sx={{ position: 'relative', width: '100%' }}>
        {label && (
          <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8, color: 'text.primary' }}>
            {label} {required && <span style={{ color: '#ef4444' }}>*</span>}
          </Typography>
        )}

        <TextField
          fullWidth
          required={required}
          disabled={disabled}
          placeholder={placeholder}
          value={inputValue}
          onChange={handleInputChange}
          onFocus={() => {
            if (suggestions.length > 0) setShowSuggestions(true);
          }}
          error={error}
          helperText={helperText}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <LocationIcon sx={{ color: hasCoordinates ? '#10b981' : '#F97316' }} />
              </InputAdornment>
            ),
            endAdornment: (
              <InputAdornment position="end" sx={{ gap: 0.5 }}>
                {searching && <CircularProgress size={18} color="inherit" />}
                {inputValue && !disabled && (
                  <IconButton
                    size="small"
                    onClick={() => {
                      setInputValue('');
                      setSuggestions([]);
                      setShowSuggestions(false);
                      if (onChangeAddress) onChangeAddress('');
                    }}
                  >
                    <ClearIcon fontSize="small" />
                  </IconButton>
                )}
                <Tooltip title="Lấy vị trí GPS hiện tại của thiết bị" arrow>
                  <span>
                    <IconButton
                      size="small"
                      color="primary"
                      disabled={disabled || locatingGps}
                      onClick={handleGetGpsLocation}
                      sx={{
                        bgcolor: 'rgba(249, 115, 22, 0.08)',
                        color: '#F97316',
                        '&:hover': { bgcolor: 'rgba(249, 115, 22, 0.16)' },
                      }}
                    >
                      {locatingGps ? <CircularProgress size={18} color="inherit" /> : <GpsIcon fontSize="small" />}
                    </IconButton>
                  </span>
                </Tooltip>
              </InputAdornment>
            ),
          }}
        />

        {/* FLOATING AUTOCOMPLETE SUGGESTIONS POPUP */}
        {showSuggestions && suggestions.length > 0 && (
          <Paper
            elevation={8}
            sx={{
              position: 'absolute',
              top: 'calc(100% + 4px)',
              left: 0,
              right: 0,
              zIndex: 9999,
              maxHeight: 280,
              overflowY: 'auto',
              borderRadius: 2.5,
              border: '1px solid',
              borderColor: 'divider',
              boxShadow: '0 12px 32px rgba(0,0,0,0.14)',
            }}
          >
            <Box sx={{ px: 2, py: 1, bgcolor: '#F8FAFC', borderBottom: '1px solid #E2E8F0', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <Typography variant="caption" sx={{ fontWeight: 700, color: '#64748B' }}>
                Gợi ý địa điểm ({suggestions.length}) - Chọn để tự động gán tọa độ
              </Typography>
            </Box>
            <List disablePadding>
              {suggestions.map((item, idx) => (
                <ListItem
                  key={item.id || idx}
                  button
                  onClick={() => handleSelectSuggestion(item)}
                  sx={{
                    py: 1.2,
                    px: 2,
                    borderBottom: idx !== suggestions.length - 1 ? '1px solid #F1F5F9' : 'none',
                    '&:hover': {
                      bgcolor: '#FFF7ED',
                    },
                  }}
                >
                  <ListItemIcon sx={{ minWidth: 36, color: '#F97316' }}>
                    <SearchIcon fontSize="small" />
                  </ListItemIcon>
                  <ListItemText
                    primary={
                      <Typography variant="body2" sx={{ fontWeight: 700, color: '#0F172A' }}>
                        {item.name}
                      </Typography>
                    }
                    secondary={
                      <Box sx={{ mt: 0.3 }}>
                        <Typography variant="caption" sx={{ color: '#64748B', display: 'block', lineHeight: 1.4 }}>
                          {item.full}
                        </Typography>
                        <Typography variant="caption" sx={{ color: '#10B981', fontWeight: 600, display: 'inline-block', mt: 0.2 }}>
                          📍 Lat: {item.lat} • Lng: {item.lon}
                        </Typography>
                      </Box>
                    }
                  />
                </ListItem>
              ))}
            </List>
          </Paper>
        )}

        {/* COORDINATES STATUS BAR */}
        <Box
          sx={{
            mt: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: 1,
          }}
        >
          {hasCoordinates ? (
            <Chip
              size="small"
              icon={<CheckIcon sx={{ fontSize: '16px !important', color: '#059669 !important' }} />}
              label={`Tọa độ tự động: Vĩ độ ${latitude}, Kinh độ ${longitude}`}
              sx={{
                bgcolor: 'rgba(16, 185, 129, 0.1)',
                color: '#059669',
                fontWeight: 600,
                fontSize: '0.78rem',
                border: '1px solid rgba(16, 185, 129, 0.25)',
              }}
            />
          ) : (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <InfoIcon sx={{ fontSize: 16, color: '#F59E0B' }} />
              <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                Chọn một địa chỉ gợi ý hoặc nhấn nút <b>GPS</b> để tự động lấy tọa độ
              </Typography>
            </Box>
          )}

          <Button
            size="small"
            startIcon={<TuneIcon sx={{ fontSize: 14 }} />}
            onClick={() => setShowManualCoords(!showManualCoords)}
            sx={{
              textTransform: 'none',
              fontSize: '0.75rem',
              fontWeight: 600,
              color: 'text.secondary',
              py: 0.2,
              px: 0.8,
            }}
          >
            {showManualCoords ? 'Ẩn tọa độ chi tiết' : 'Tùy chỉnh tọa độ'}
          </Button>
        </Box>

        {/* OPTIONAL EXPANDABLE MANUAL COORDINATE INPUTS */}
        <Collapse in={showManualCoords}>
          <Box
            sx={{
              mt: 1.5,
              p: 1.5,
              borderRadius: 2,
              bgcolor: 'background.default',
              border: '1px dashed',
              borderColor: 'divider',
              display: 'grid',
              gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' },
              gap: 1.5,
            }}
          >
            <TextField
              size="small"
              label="Tọa độ Vĩ độ (Latitude)"
              value={latitude}
              onChange={(e) => {
                const val = e.target.value;
                if (onChangeCoordinates) {
                  onChangeCoordinates({ latitude: val, longitude });
                } else if (onSelectLocation) {
                  onSelectLocation({ address: inputValue, latitude: val, longitude });
                }
              }}
              placeholder="VD: 10.7769"
              helperText="Được tự động gán từ địa chỉ đã chọn"
            />
            <TextField
              size="small"
              label="Tọa độ Kinh độ (Longitude)"
              value={longitude}
              onChange={(e) => {
                const val = e.target.value;
                if (onChangeCoordinates) {
                  onChangeCoordinates({ latitude, longitude: val });
                } else if (onSelectLocation) {
                  onSelectLocation({ address: inputValue, latitude, longitude: val });
                }
              }}
              placeholder="VD: 106.7009"
              helperText="Được tự động gán từ địa chỉ đã chọn"
            />
          </Box>
        </Collapse>
      </Box>
    </ClickAwayListener>
  );
};

export default AddressAutocomplete;
