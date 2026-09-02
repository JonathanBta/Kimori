import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { AuthService } from './auth.service';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  it('redirects to /sign-in when no user is signed in', async () => {
    const fakeAuthService = {
      waitUntilReady: async () => undefined,
      currentUser: null
    };

    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: fakeAuthService }]
    });

    const router = TestBed.inject(Router);
    const result = await TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/' } as never)
    );

    expect(result).toEqual(router.createUrlTree(['/sign-in']));
  });

  it('allows navigation when a user is signed in', async () => {
    const fakeAuthService = {
      waitUntilReady: async () => undefined,
      currentUser: { uid: 'abc' }
    };

    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: fakeAuthService }]
    });

    const result = await TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/' } as never)
    );

    expect(result).toBe(true);
  });
});
