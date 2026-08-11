import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';
import { getErrorMessage } from '../../utils/api';

interface FetchCardsParams {
  search?: string;
  page?: number;
  size?: number;
  sort?: string; 
}

interface CreateCardParams {
  slug: string;
  templateId: string;
  profileData: Record<string, any>;
}

export const fetchUserCards = createAsyncThunk(
  'card/fetchUserCards',
  async ({ search = '', page = 0, size = 10, sort = 'id,desc' }: FetchCardsParams, { rejectWithValue }) => {
    try {
      const response = await api.get(`/api/v1/cards?search=${search}&page=${page}&size=${size}&sort=${sort}`);
      return response.data; 
    } catch (error: any) {
      return rejectWithValue(getErrorMessage(error));
    }
  }
);

export const createCard = createAsyncThunk(
  'card/createCard',
  async (cardData: CreateCardParams, { rejectWithValue }) => {
    try {
      const response = await api.post('/api/v1/cards', cardData);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(getErrorMessage(error));
    }
  }
);

export const fetchAnalytics = createAsyncThunk(
  'card/fetchAnalytics',
  async (_, { rejectWithValue }) => {
    try {
      const response = await api.get('/api/v1/analytics/summary');
      return response.data;
    } catch (error: any) {
      return rejectWithValue(getErrorMessage(error));
    }
  }
);

const cardSlice = createSlice({
  name: 'card',
  initialState: {
    cards: [] as any[],
    analytics: {
      totalViews: 0,
      uniqueVisitors: 0,
      totalLeads: 0,
      clickThroughRate: 0,
      viewsByDate: {} as Record<string, number>,
      clicksByLink: {} as Record<string, number>,
    },
    totalPages: 0,
    selectedCard: null as any,
    loading: false,
    error: null as string | null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      // fetchUserCards cases
      .addCase(fetchUserCards.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUserCards.fulfilled, (state, action) => {
        state.loading = false;
        state.cards = action.payload.content || [];
        state.totalPages = action.payload.totalPages || 0;
      })
      .addCase(fetchUserCards.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      // createCard cases
      .addCase(createCard.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createCard.fulfilled, (state, action) => {
        state.loading = false;
        // Prevent duplicate entries
        if (!state.cards.some((c) => c.id === action.payload.id)) {
          state.cards.unshift(action.payload);
        }
      })
      .addCase(createCard.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      // fetchAnalytics cases
      .addCase(fetchAnalytics.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAnalytics.fulfilled, (state, action) => {
        state.loading = false;
        state.analytics = action.payload;
      })
      .addCase(fetchAnalytics.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
        state.analytics = {
          totalViews: 0,
          uniqueVisitors: 0,
          totalLeads: 0,
          clickThroughRate: 0,
          viewsByDate: {},
          clicksByLink: {},
        };
      });
  }
});

export default cardSlice.reducer;