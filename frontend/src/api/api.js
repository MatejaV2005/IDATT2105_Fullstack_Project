import axios from 'axios';
import { clearAuthToken, getAuthToken, setAuthToken } from '@/utils/auth';

const api = axios.create({
    baseURL: '/api',
    withCredentials: true
});

function getLoginRoute() {
    if (typeof window === 'undefined') {
        return '/auth';
    }

    return new URL('/auth', window.location.origin).toString();
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
        const requestUrl = typeof error.config?.url === 'string' ? error.config.url : '';
        const isAuthRequest =
            requestUrl.includes('/auth/login') ||
            requestUrl.includes('/auth/register') ||
            requestUrl.includes('/auth/logout') ||
            requestUrl.includes('/auth/refresh');

        if (error.response?.status === 401 && !error.config?._retry && !isAuthRequest) {
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
