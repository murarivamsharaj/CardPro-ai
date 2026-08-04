import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api'; 

// 1. Define arguments for fetching cards
interface FetchCardsParams {
  search?: string;
  page?: number;
  size?: number;
}

// 2. Define arguments for creating a card
interface CreateCardParams {
  slug: string;
  templateId: string;
  profileData: Record<string, any>;
}

// Thunk to fetch user cards with pagination and search
export const fetchUserCards = createAsyncThunk(
  'card/fetchUserCards',
  async ({ search = '', page = 0, size = 10 }: FetchCardsParams, { rejectWithValue }) => {
    try {
      const response = await api.get(`/api/v1/cards?search=${search}&page=${page}&size=${size}`);
      return response.data; 
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch cards');
    }
  }
);

// Thunk to create a new card
export const createCard = createAsyncThunk(
  'card/createCard',
  async (cardData: CreateCardParams, { rejectWithValue }) => {
    try {
      const response = await api.post('/api/v1/cards', cardData);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create card');
    }
  }
);

const cardSlice = createSlice({
  name: 'card',
  initialState: {
    cards: [] as any[],
    totalPages: 0,
    currentPage: 0,
    selectedCard: null as any,
    loading: false,
    error: null as string | null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      // Fetch User Cards Cases
      .addCase(fetchUserCards.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUserCards.fulfilled, (state, action) => {
        state.loading = false;
        state.cards = action.payload.content || [];
        state.totalPages = action.payload.totalPages || 0;
        state.currentPage = action.payload.number || 0;
      })
      .addCase(fetchUserCards.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      
      // Create Card Cases
      .addCase(createCard.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createCard.fulfilled, (state, action) => {
        state.loading = false;
        // Optionally prepend the newly created card to the existing list
        state.cards.unshift(action.payload);
      })
      .addCase(createCard.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });
  }
});

export default cardSlice.reducer;