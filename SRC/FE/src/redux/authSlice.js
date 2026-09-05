import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import authService from '../services/authService';

const getInitialToken = () => {
  const token = localStorage.getItem('omni_token');
  return token && token !== 'undefined' && token !== 'null' ? token : null;
};

const getInitialUser = () => {
  try {
    const raw = localStorage.getItem('omni_user');
    return raw && raw !== 'undefined' ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
};

const token = getInitialToken();
const user = getInitialUser();

// Async Thunk for Login with Phone Number & Password
export const loginUser = createAsyncThunk(
  'auth/loginUser',
  async ({ phoneNumber, password }, { rejectWithValue }) => {
    try {
      const data = await authService.login({ phoneNumber, password });
      return data;
    } catch (error) {
      return rejectWithValue(error.message || 'Đăng nhập thất bại');
    }
  }
);

// Async Thunk for Logout
export const logoutUser = createAsyncThunk('auth/logoutUser', async () => {
  await authService.logout();
});

const initialState = {
  user: user,
  token: token,
  isAuthenticated: Boolean(token && user),
  loading: false,
  error: null,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    clearAuthError: (state) => {
      state.error = null;
    },
    logout: (state) => {
      state.user = null;
      state.token = null;
      state.isAuthenticated = false;
      state.loading = false;
      state.error = null;
      localStorage.removeItem('omni_token');
      localStorage.removeItem('omni_user');
    },
  },
  extraReducers: (builder) => {
    builder
      // Login User
      .addCase(loginUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.loading = false;
        state.isAuthenticated = true;
        state.user = action.payload.user;
        state.token = action.payload.token;
        state.error = null;
        localStorage.setItem('omni_token', action.payload.token);
        localStorage.setItem('omni_user', JSON.stringify(action.payload.user));
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.loading = false;
        state.isAuthenticated = false;
        state.token = null;
        state.user = null;
        state.error = action.payload || 'Đăng nhập thất bại';
        localStorage.removeItem('omni_token');
        localStorage.removeItem('omni_user');
      })
      // Logout User
      .addCase(logoutUser.fulfilled, (state) => {
        state.user = null;
        state.token = null;
        state.isAuthenticated = false;
        state.loading = false;
        state.error = null;
        localStorage.removeItem('omni_token');
        localStorage.removeItem('omni_user');
      });
  },
});

export const { clearAuthError, logout } = authSlice.actions;
export default authSlice.reducer;
