import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';
import { getErrorMessage } from '../../utils/api';

export interface Lead {
  id: string;
  profileId: string;
  visitorName: string;
  visitorEmail?: string;
  visitorPhone: string;
  message?: string;
  aiFollowup?: string;
  capturedAt?: string;
}

interface FetchLeadsParams {
  page?: number;
  size?: number;
  search?: string;
}

export const fetchLeads = createAsyncThunk(
  'lead/fetchLeads',
  async ({ page = 0, size = 20, search = '' }: FetchLeadsParams, { rejectWithValue }) => {
    try {
      const params = new URLSearchParams({ page: String(page), size: String(size) });
      if (search.trim()) params.set('search', search.trim());
      const response = await api.get(`/api/v1/leads?${params.toString()}`);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(getErrorMessage(error));
    }
  }
);

interface LeadsState {
  leads: Lead[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
  loading: boolean;
  error: string | null;
}

const leadsSlice = createSlice({
  name: 'leads',
  initialState: {
    leads: [] as Lead[],
    totalPages: 0,
    totalElements: 0,
    currentPage: 0,
    loading: false,
    error: null as string | null,
  } as LeadsState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchLeads.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchLeads.fulfilled, (state, action) => {
        state.loading = false;
        state.leads = action.payload?.content ?? [];
        state.totalPages = action.payload?.totalPages ?? 0;
        state.totalElements = action.payload?.totalElements ?? state.leads.length;
        state.currentPage = action.payload?.number ?? 0;
      })
      .addCase(fetchLeads.rejected, (state, action) => {
        state.loading = false;
        state.error = (action.payload as string) || 'Failed to fetch leads';
      });
  },
});

export default leadsSlice.reducer;
