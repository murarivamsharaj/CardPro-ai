import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';
import { analyticsService } from '../../services/analyticsService';
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
  address?: string;
  gender?: string;
  socialLinks?: Record<string, string>;
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

/**
 * Fetch the current user's own digital card(s) from GET /api/v1/cards/me.
 * The backend models one card per user and returns a single object; it is
 * normalized into an array so the grid UI stays uniform. A 404 means the
 * user has no card yet — that is an empty library, not an error.
 */
export const fetchMyCards = createAsyncThunk(
  'card/fetchMyCards',
  async (_, { rejectWithValue }) => {
    try {
      const response = await api.get('/api/v1/cards/me');
      const data = response.data;
      const list = Array.isArray(data) ? data : [data];
      return { content: list.filter(Boolean), totalPages: 1 };
    } catch (error: any) {
      if (error?.response?.status === 404) {
        return { content: [], totalPages: 0 };
      }
      return rejectWithValue(getErrorMessage(error));
    }
  }
);

interface UpdateMyCardParams {
  slug?: string;
  templateId?: string;
  profileData?: Record<string, any>;
  address?: string;
  gender?: string;
  socialLinks?: Record<string, string>;
  isActive?: boolean;
}

export const updateMyCard = createAsyncThunk(
  'card/updateMyCard',
  async (cardData: UpdateMyCardParams, { rejectWithValue }) => {
    try {
      const response = await api.put('/api/v1/cards/me', cardData);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(getErrorMessage(error));
    }
  }
);

export const deleteMyCard = createAsyncThunk(
  'card/deleteMyCard',
  async (_, { rejectWithValue }) => {
    try {
      await api.delete('/api/v1/cards/me');
      return true;
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
      // Single source of truth for analytics payloads lives in analyticsService
      return await analyticsService.getUserAnalytics(30);
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
      // fetchMyCards cases
      .addCase(fetchMyCards.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchMyCards.fulfilled, (state, action) => {
        state.loading = false;
        state.cards = action.payload.content;
        state.totalPages = action.payload.totalPages;
      })
      .addCase(fetchMyCards.rejected, (state, action) => {
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