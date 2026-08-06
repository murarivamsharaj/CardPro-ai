import type { ApiErrorResponse } from '@/types/api';

/**
 * Extracts a human-readable error message from any error shape.
 * Handles Axios errors, standard errors, and API error responses.
 */
export function getErrorMessage(error: unknown): string {
  if (typeof error === 'string') return error;

  // Axios error with API response
  if (error && typeof error === 'object' && 'response' in error) {
    const axiosError = error as { response: { data: ApiErrorResponse } };
    if (axiosError.response?.data?.error?.message) {
      return axiosError.response.data.error.message;
    }
  }

  // Standard Error instance
  if (error instanceof Error) return error.message;

  return 'An unexpected error occurred. Please try again.';
}

/**
 * Extracts field-level validation errors from an API error response.
 */
export function getFieldErrors(error: unknown): Record<string, string> {
  if (error && typeof error === 'object' && 'response' in error) {
    const axiosError = error as { response: { data: ApiErrorResponse } };
    if (axiosError.response?.data?.error?.details) {
      return axiosError.response.data.error.details as Record<string, string>;
    }
  }
  return {};
}
