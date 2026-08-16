import api from './api';
import { API_ENDPOINTS } from '../utils/constants';

export interface CardDetailsSuggestion {
  suggestedBio: string;
  suggestedTagline: string;
  suggestedJobTitle: string;
  model: string;
  fallback: boolean;
}

/**
 * Magic Autofill: asks the ai-service to suggest a bio, tagline, and job
 * title from a few keywords / a rough summary. The caller decides whether to
 * keep the suggestions or edit them.
 */
export const generateCardDetails = async (prompt: string, tone?: string): Promise<CardDetailsSuggestion> => {
  const response = await api.post(API_ENDPOINTS.AI.GENERATE_CARD_DETAILS, {
    prompt,
    ...(tone ? { tone } : {}),
  });
  return response.data;
};

export default generateCardDetails;
