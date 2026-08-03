import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

export const loginUser = createAsyncThunk(
  'auth/loginUser',
  async (credentials: { email?: string; password?: string }, { rejectWithValue }) => {
    try {
      // ✅ FIXED: Added /api/v1 prefix to match API Gateway routing
      const response = await api.post('/api/v1/auth/login', credentials);
      
      // Safety check in case your backend sends 'accessToken' instead of 'token'
      const token = response.data.token || response.data.accessToken;
      
      if (token) {
        localStorage.setItem('token', token);
      }
      
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Login failed');
    }
  }
);

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: null as any,
    token: localStorage.getItem('token') || null,
    loading: false,
    error: null as string | null,
  },
  reducers: {
    logout: (state) => {
      localStorage.removeItem('token');
      state.user = null;
      state.token = null;
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload.user;
        // Map token dynamically based on what the backend returned
        state.token = action.payload.token || action.payload.accessToken; 
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });
  },
});

export const { logout } = authSlice.actions;
export default authSlice.reducer;