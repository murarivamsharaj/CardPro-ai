import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import cardReducer from './slices/cardSlice'; // 1. Import cardReducer

export const store = configureStore({
  reducer: {
    auth: authReducer,
    card: cardReducer, // 2. Add cardReducer here
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;