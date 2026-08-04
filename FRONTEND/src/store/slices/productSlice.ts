import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

interface FetchProductsParams {
  search?: string;
  page?: number;
  size?: number;
}

export const fetchProducts = createAsyncThunk(
  'product/fetchProducts',
  async ({ search = '', page = 0, size = 10 }: FetchProductsParams, { rejectWithValue }) => {
    try {
      const response = await api.get(`/api/v1/products?search=${search}&page=${page}&size=${size}`);
      return response.data; 
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch products');
    }
  }
);

const productSlice = createSlice({
  name: 'product',
  initialState: {
    products: [] as any[],
    totalPages: 0,
    currentPage: 0,
    selectedProduct: null as any,
    loading: false,
    error: null as string | null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchProducts.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchProducts.fulfilled, (state, action) => {
        state.loading = false;
        state.products = action.payload.content || [];
        state.totalPages = action.payload.totalPages || 0;
        state.currentPage = action.payload.number || 0;
      })
      .addCase(fetchProducts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });
  }
});

export default productSlice.reducer;