import axios from 'axios';

export const TOKEN_KEY = 'tf_token';
export const USER_KEY = 'tf_user';

// Same-origin by default; proxied to the gateway (see vite.config.js / nginx.conf).
const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
});

// Attach the JWT to every request if we have one.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// On 401 (expired/invalid token) drop the session and bounce to login —
// except for the auth calls themselves (wrong password shouldn't redirect).
client.interceptors.response.use(
  (res) => res,
  (error) => {
    const url = error.config?.url || '';
    if (error.response?.status === 401 && !url.includes('/api/auth/')) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      if (window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
    }
    return Promise.reject(error);
  },
);

// Extracts a human-readable message from our ApiErrorResponse shape.
export function errorMessage(error, fallback = 'Something went wrong') {
  const data = error?.response?.data;
  if (data?.validationErrors?.length) {
    return data.validationErrors.map((v) => `${v.field}: ${v.message}`).join(', ');
  }
  return data?.message || error?.message || fallback;
}

export default client;
