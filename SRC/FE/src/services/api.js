import axios from 'axios';

// Vite environment based baseURL
const getBaseURL = () => {
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL;
  }
  return import.meta.env.MODE === 'production'
    ? 'https://api.ridehailingsystem.online'
    : 'http://localhost:8080';
};

export const API_BASE_URL = getBaseURL();

// Create configured Axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
  withCredentials: true,
});

// Request Interceptor: Attach JWT Token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('omni_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Error handling & token expiration
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Auto logout on token expiration if not on login page
      if (!window.location.pathname.includes('/login')) {
        localStorage.removeItem('omni_token');
        localStorage.removeItem('omni_user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Standardized HTTP Helper Methods for Scalability
export const get = async (url, params = {}, config = {}) => {
  const response = await apiClient.get(url, { params, ...config });
  return response.data;
};

export const post = async (url, data = {}, config = {}) => {
  const response = await apiClient.post(url, data, config);
  return response.data;
};

export const put = async (url, data = {}, config = {}) => {
  const response = await apiClient.put(url, data, config);
  return response.data;
};

export const patch = async (url, data = {}, config = {}) => {
  const response = await apiClient.patch(url, data, config);
  return response.data;
};

export const del = async (url, config = {}) => {
  const response = await apiClient.delete(url, config);
  return response.data;
};

export default apiClient;
