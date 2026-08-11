import { configureStore } from '@reduxjs/toolkit';
import cardReducer from './slices/cardSlice';
import leadsReducer from './slices/leadsSlice';

export const store = configureStore({
  reducer: {
    card: cardReducer,
    leads: leadsReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;