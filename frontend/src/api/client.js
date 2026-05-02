const configuredBaseUrl = import.meta.env.VITE_API_URL?.trim();
const baseUrl = configuredBaseUrl || (import.meta.env.DEV ? "http://localhost:8080" : "");

export function apiClient(token) {
  async function request(path, options = {}) {
    if (!baseUrl) {
      throw new Error("Backend URL is not configured");
    }

    const response = await fetch(`${baseUrl}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers
      }
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ error: response.statusText }));
      throw new Error(error.error || "Request failed");
    }
    if (response.status === 204) return null;
    return response.json();
  }

  return {
    get: (path) => request(path),
    post: (path, body) => request(path, { method: "POST", body: JSON.stringify(body) }),
    delete: (path) => request(path, { method: "DELETE" })
  };
}
