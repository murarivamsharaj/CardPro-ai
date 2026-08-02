import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { Product, ProductRequest } from '../types/product';
import { productService } from '../services/productService';

interface ProductState {
  products: Product[];
  selectedProduct: Product | null;
  loading: boolean;
  error: string | null;
}

const initialState: ProductState = {
  products: [],
  selectedProduct: null,
  loading: false,
  error: null,
};

// Async Thunks
export const fetchProducts = createAsyncThunk('products/fetchAll', async (_, thunkAPI) => {
  try {
    return await productService.getAllProducts();
  } catch (error: any) {
    return thunkAPI.rejectWithValue(error.response?.data?.message || error.message || 'Failed to fetch products');
  }
});

export const addNewProduct = createAsyncThunk('products/add', async (data: ProductRequest, thunkAPI) => {
  try {
    return await productService.createProduct(data);
  } catch (error: any) {
    return thunkAPI.rejectWithValue(error.response?.data?.message || error.message || 'Failed to create product');
  }
});

export const editProduct = createAsyncThunk(
  'products/edit',
  async ({ id, data }: { id: number; data: ProductRequest }, thunkAPI) => {
    try {
      return await productService.updateProduct(id, data);
    } catch (error: any) {
      return thunkAPI.rejectWithValue(error.response?.data?.message || error.message || 'Failed to update product');
    }
  }
);

export const removeProduct = createAsyncThunk('products/remove', async (id: number, thunkAPI) => {
  try {
    await productService.deleteProduct(id);
    return id;
  } catch (error: any) {
    return thunkAPI.rejectWithValue(error.response?.data?.message || error.message || 'Failed to delete product');
  }
});

const productSlice = createSlice({
  name: 'products',
  initialState,
  reducers: {
    clearSelectedProduct: (state) => {
      state.selectedProduct = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch
      .addCase(fetchProducts.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchProducts.fulfilled, (state, action: PayloadAction<Product[]>) => {
        state.loading = false;
        state.products = action.payload;
      })
      .addCase(fetchProducts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      // Add
      .addCase(addNewProduct.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(addNewProduct.fulfilled, (state, action: PayloadAction<Product>) => {
        state.loading = false;
        state.products.push(action.payload);
      })
      .addCase(addNewProduct.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      // Edit
      .addCase(editProduct.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(editProduct.fulfilled, (state, action: PayloadAction<Product>) => {
        state.loading = false;
        const index = state.products.findIndex((p) => p.id === action.payload.id);
        if (index !== -1) {
          state.products[index] = action.payload;
        }
      })
      .addCase(editProduct.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      // Remove
      .addCase(removeProduct.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(removeProduct.fulfilled, (state, action: PayloadAction<number>) => {
        state.loading = false;
        state.products = state.products.filter((p) => p.id !== action.payload);
      })
      .addCase(removeProduct.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });
  },
});

export const { clearSelectedProduct } = productSlice.actions;
export default productSlice.reducer;