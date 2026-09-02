import { Injectable, signal } from '@angular/core';

export interface Notification {
  id: number;
  message: string;
  kind: 'error' | 'success';
}

/** Global toast/error-notification store surfacing the uniform ApiError shape from the backend. */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private nextId = 1;
  readonly notifications = signal<Notification[]>([]);

  error(message: string): void {
    this.push(message, 'error');
  }

  success(message: string): void {
    this.push(message, 'success');
  }

  dismiss(id: number): void {
    this.notifications.update((list) => list.filter((n) => n.id !== id));
  }

  private push(message: string, kind: Notification['kind']): void {
    const id = this.nextId++;
    this.notifications.update((list) => [...list, { id, message, kind }]);
    setTimeout(() => this.dismiss(id), 5000);
  }
}
