import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

export const fetchUserCards = createAsyncThunk(
  'card/fetchUserCards',
  async (_, { rejectWithValue }) => {
    try {
      const response = await api.get('/api/v1/cards');
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch cards');
    }
  }
);

export const createCardProfile = createAsyncThunk(
  'card/createCardProfile',
  async (cardData: any, { rejectWithValue }) => {
    try {
      // ✅ ADDED /api/v1 prefix here
      const response = await api.post('/api/v1/cards', cardData);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create card');
    }
  }
);

export const updateCardProfile = createAsyncThunk(
  'card/updateCardProfile',
  async ({ cardId, cardData }: { cardId: string | number; cardData: any }, { rejectWithValue }) => {
    try {
      // ✅ ADDED /api/v1 prefix here
      const response = await api.put(`/api/v1/cards/${cardId}`, cardData);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update card');
    }
  }
);

const cardSlice = createSlice({
  name: 'card',
  initialState: {
    cards: [] as any[],
    selectedCard: null as any,
    loading: false,
    error: null as string | null,
  },
  reducers: {
    setSelectedCard: (state, action) => {
      state.selectedCard = action.payload;
    },
    clearCardError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchUserCards.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUserCards.fulfilled, (state, action) => {
        state.loading = false;
        state.cards = action.payload;
      })
      .addCase(fetchUserCards.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      .addCase(createCardProfile.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createCardProfile.fulfilled, (state, action) => {
        state.loading = false;
        state.cards.push(action.payload);
      })
      .addCase(createCardProfile.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      .addCase(updateCardProfile.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.cards.findIndex((c) => c.id === action.payload.id);
        if (index !== -1) {
          state.cards[index] = action.payload;
        }
      });
  },
});

export const { setSelectedCard, clearCardError } = cardSlice.actions;
export default cardSlice.reducer;