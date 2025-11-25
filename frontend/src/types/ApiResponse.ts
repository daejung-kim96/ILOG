export interface ApiResponse<T> {
    body: T;
    success: boolean;
    error?: string;
}