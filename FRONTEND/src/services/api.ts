import axios from 'axios';

// Allow individual requests (e.g. the public card viewer) to opt out of the
// global 401 → /login redirect. Public endpoints must not kick users to login.
declare module 'axios' {
  export interface AxiosRequestConfig {
    /** When true, a 401 response is NOT redirected to /login. Public endpoints only. */
    skipAuthRedirect?: boolean;
  }
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://cardpro-ai.onrender.com',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Dynamically attach token on every single request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    // Only attach if token exists and is not the string "undefined" or "null"
    if (token && token !== 'undefined' && token !== 'null' && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Log why it failed before redirecting
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Public endpoints (skipAuthRedirect) handle 401 themselves — never force login
    if (error.response && error.response.status === 401 && !error.config?.skipAuthRedirect) {
      console.error('401 Unauthorized caught by interceptor. Redirecting to login...', error.config?.url);
      localStorage.removeItem('token');
      // Use replace to prevent back-button loops
      window.location.replace('/login');
    }
    return Promise.reject(error);
  }
);

export default api;