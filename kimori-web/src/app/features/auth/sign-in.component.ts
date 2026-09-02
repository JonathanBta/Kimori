import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../core/auth/auth.service';

/** FR-022: Google Sign-In screen shown to unauthenticated visitors. */
@Component({
  selector: 'app-sign-in',
  standalone: true,
  templateUrl: './sign-in.component.html',
  styleUrl: './sign-in.component.scss'
})
export class SignInComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly signingIn = signal(false);
  readonly errorMessage = signal<string | null>(null);

  async signIn(): Promise<void> {
    this.signingIn.set(true);
    this.errorMessage.set(null);
    try {
      await this.auth.signInWithGoogle();
      await this.router.navigate(['/']);
    } catch {
      this.errorMessage.set('Sign-in failed. Please try again.');
    } finally {
      this.signingIn.set(false);
    }
  }
}
