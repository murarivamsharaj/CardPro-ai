import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8083', // <-- Add your backend port here!
  headers: {
    'Content-Type': 'application/json',
  },
});

// Optional: Add request interceptor for tokens if needed later
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;