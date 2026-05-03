/**
 * Base URL for the Spring Boot API (no trailing slash).
 * With `ng serve`, requests go to the same origin and `proxy.conf.json` forwards `/api` → http://127.0.0.1:8080 (avoids CORS).
 */
export const API_BASE_URL = '/api';
