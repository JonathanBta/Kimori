import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

/** FR-022: redirects unauthenticated visitors to sign-in before any tree data is shown. */
export const authGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  await auth.waitUntilReady();

  if (auth.currentUser) {
    return true;
  }
  return router.createUrlTree(['/sign-in']);
};
