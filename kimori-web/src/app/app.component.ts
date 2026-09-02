import { Component, inject } from '@angular/core';
import { NgClass } from '@angular/common';
import { RouterOutlet } from '@angular/router';

import { NotificationService } from './core/notifications/notification.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NgClass],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  private readonly notificationService = inject(NotificationService);
  readonly notifications = this.notificationService.notifications;

  dismiss(id: number): void {
    this.notificationService.dismiss(id);
  }
}
