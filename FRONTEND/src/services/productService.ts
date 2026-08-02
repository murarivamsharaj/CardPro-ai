import api from './api';
import { Product, ProductRequest } from '../types/product';

const PRODUCT_API_URL = '/api/products'; // Routed via API Gateway

export const productService = {
  getAllProducts: async () => {
    const response = await api.get<Product[]>(PRODUCT_API_URL);
    return response.data;
  },

  getProductById: async (id: number) => {
    const response = await api.get<Product>(`${PRODUCT_API_URL}/${id}`);
    return response.data;
  },

  createProduct: async (data: ProductRequest) => {
    const response = await api.post<Product>(PRODUCT_API_URL, data);
    return response.data;
  },

  updateProduct: async (id: number, data: ProductRequest) => {
    const response = await api.put<Product>(`${PRODUCT_API_URL}/${id}`, data);
    return response.data;
  },

  deleteProduct: async (id: number) => {
    await api.delete(`${PRODUCT_API_URL}/${id}`);
  },
};