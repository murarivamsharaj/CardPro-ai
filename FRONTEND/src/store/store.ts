import { configureStore } from '@reduxjs/toolkit';
import cardReducer from './slices/cardSlice';

export const store = configureStore({
  reducer: {
    card: cardReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;