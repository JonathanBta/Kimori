import { Injectable } from '@angular/core';
import { FirebaseApp, initializeApp } from 'firebase/app';
import {
  Auth,
  User,
  getAuth,
  GoogleAuthProvider,
  onAuthStateChanged,
  signInWithPopup,
  signOut as firebaseSignOut
} from 'firebase/auth';
import { BehaviorSubject, Observable, firstValueFrom } from 'rxjs';

import { environment } from '../../../environments/environment';

/** FR-022, FR-022a: Google Sign-In and the current authenticated user's ID token. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly app: FirebaseApp = initializeApp(environment.firebase);
  private readonly auth: Auth = getAuth(this.app);
  private readonly userSubject = new BehaviorSubject<User | null>(null);
  private readonly readySubject = new BehaviorSubject<boolean>(false);

  /** Emits the current Firebase user, or null when signed out. */
  readonly currentUser$: Observable<User | null> = this.userSubject.asObservable();
  /** Emits true once the initial auth state has been resolved (avoids a sign-in flash). */
  readonly ready$: Observable<boolean> = this.readySubject.asObservable();

  constructor() {
    onAuthStateChanged(this.auth, (user) => {
      this.userSubject.next(user);
      this.readySubject.next(true);
    });
  }

  get currentUser(): User | null {
    return this.userSubject.value;
  }

  async waitUntilReady(): Promise<void> {
    if (this.readySubject.value) {
      return;
    }
    await firstValueFrom(this.readySubject.asObservable());
  }

  async signInWithGoogle(): Promise<void> {
    await signInWithPopup(this.auth, new GoogleAuthProvider());
  }

  async signOut(): Promise<void> {
    await firebaseSignOut(this.auth);
  }

  /** Bearer token attached to every API request by the auth interceptor. */
  async getIdToken(): Promise<string | null> {
    const user = this.userSubject.value;
    return user ? user.getIdToken() : null;
  }
}
