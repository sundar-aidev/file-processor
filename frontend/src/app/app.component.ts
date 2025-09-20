import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule, HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs/operators';

export const API_URL = 'http://localhost:8080/api/v1/files/upload';

export interface FileProcessResponse {
  id: string;
  filename: string;
  fileType: string;
  lines: number;
  words: number;
  timestamp: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent {
  private readonly http = inject(HttpClient);

  readonly selectedFile = signal<File | null>(null);
  readonly isUploading = signal(false);
  readonly response = signal<FileProcessResponse | null>(null);
  readonly errorMsg = signal<string | null>(null);

  readonly canUpload = computed(() => !!this.selectedFile() && !this.isUploading());
  readonly apiUrl = API_URL;

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement | null;
    const file = input?.files?.item(0) ?? null;

    this.resetOutcome();
    this.selectedFile.set(file);
  }

  upload(): void {
    const file = this.selectedFile();
    if (!file || this.isUploading()) return; // No-op: nothing to send or already busy.

    const body = new FormData();
    body.append('file', file, file.name);

    this.isUploading.set(true);
    this.resetOutcome();

    this.http
      .post<FileProcessResponse>(API_URL, body)
      .pipe(finalize(() => this.isUploading.set(false)))
      .subscribe({
        next: (res) => this.response.set(res),
        error: (err: HttpErrorResponse) => this.errorMsg.set(this.humanizeError(err)),
      });
  }

  //Helpers
  private resetOutcome(): void {
    this.response.set(null);
    this.errorMsg.set(null);
  }

  private humanizeError(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'Cannot reach the backend. Is the server running at http://localhost:8080?';
    }
    
    const backendMessage =
      typeof err.error === 'string'
        ? err.error
        : (err.error?.message || err.error?.error) as string | undefined;

    if (err.status === 400 && backendMessage) return backendMessage;
    if (err.status === 415) return 'Unsupported media type. The client must send multipart/form-data.';
    if (err.status === 413) return 'File is too large. Please upload a smaller file.';
    if (err.status === 500) return 'The server encountered an error. Please try again later.';

    return backendMessage || `Request failed (${err.status}). Please try again.`;
  }
}
