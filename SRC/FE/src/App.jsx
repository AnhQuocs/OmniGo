import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Provider } from 'react-redux';
import { Toaster } from 'react-hot-toast';
import { store } from './redux/store';
import { ColorModeProvider } from './context/ThemeContext';

import Layout from './components/Layout';
import PrivateRoute from './components/PrivateRoute';

import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Users from './pages/Users';
import Drivers from './pages/Drivers';
import Bookings from './pages/Bookings';
import Payments from './pages/Payments';
import Pricing from './pages/Pricing';
import Restaurants from './pages/Restaurants';
import FoodOrders from './pages/FoodOrders';
import NotFound from './pages/NotFound';

export function App() {
  return (
    <Provider store={store}>
      <ColorModeProvider>
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 4000,
            style: {
              background: '#172334',
              color: '#FFFFFF',
              border: '1px solid rgba(255, 255, 255, 0.1)',
              fontSize: '0.88rem',
              fontWeight: 600,
            },
          }}
        />
        <BrowserRouter>
          <Routes>
            {/* Public Route */}
            <Route path="/login" element={<Login />} />

            {/* Protected Admin Routes */}
            <Route element={<PrivateRoute />}>
              <Route element={<Layout />}>
                <Route index element={<Dashboard />} />
                <Route path="dashboard" element={<Navigate to="/" replace />} />
                <Route path="users" element={<Users />} />
                <Route path="drivers" element={<Drivers />} />
                <Route path="bookings" element={<Bookings />} />
                <Route path="restaurants" element={<Restaurants />} />
                <Route path="food-orders" element={<FoodOrders />} />
                <Route path="payments" element={<Payments />} />
                <Route path="pricing" element={<Pricing />} />
              </Route>
            </Route>

            {/* 404 Catch-all */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </ColorModeProvider>
    </Provider>
  );
}

export default App;
