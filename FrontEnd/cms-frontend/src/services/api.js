import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Automatically attach token to every request
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Redirect to login if token expires
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

// Auth endpoints
export const authAPI = {
    register: (data) => api.post('/auth/register', data),
    login: (data) => api.post('/auth/login', data),
    changePassword: (data) => api.post('/auth/change-password', data),
};

// Contacts endpoints
export const contactsAPI = {
    getContacts: (page = 0, size = 10, search = '') =>
        api.get(`/contacts?page=${page}&size=${size}&search=${search}`),
    getContact: (id) => api.get(`/contacts/${id}`),
    createContact: (data) => api.post('/contacts', data),
    updateContact: (id, data) => api.put(`/contacts/${id}`, data),
    deleteContact: (id) => api.delete(`/contacts/${id}`),
};

// User endpoints
export const userAPI = {
    getProfile: () => api.get('/user/profile'),
};

export default api;