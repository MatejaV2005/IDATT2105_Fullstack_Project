import axios from 'axios';
import { clearAuthToken, getAuthToken, setAuthToken } from '@/utils/auth';

const api = axios.create({
    baseURL: '/api',
    withCredentials: true
});

function getLoginRoute() {
    if (typeof window === 'undefined') {
        return '/mobile/login';
    }

    return window.location.pathname.startsWith('/desktop') ? '/desktop/sign-in' : '/mobile/login';
}

api.interceptors.request.use((config) => {
    const token = getAuthToken();

    if (token?.trim()) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${token.trim()}`;
    }

    return config;
});

api.interceptors.response.use(
    response => response,
    async error => {
        if (error.response?.status === 401 && !error.config._retry) {
            error.config._retry = true;
            try {
                const refreshResponse = await api.post('/auth/refresh');
                const nextToken = typeof refreshResponse.data === 'string' ? refreshResponse.data.trim() : '';

                if (nextToken) {
                    setAuthToken(nextToken);
                    error.config.headers = error.config.headers ?? {};
                    error.config.headers.Authorization = `Bearer ${nextToken}`;
                }

                return api(error.config);
            } catch (refreshError) {
                clearAuthToken();
                window.location.href = getLoginRoute();
                return Promise.reject(refreshError);
            }
        }
        return Promise.reject(error);
    }
);

export default api;
