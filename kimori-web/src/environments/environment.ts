// Firebase Web SDK config for the Kimori project (public client keys — safe to expose in a
// browser bundle; access is protected by Firebase Auth + backend token verification, not secrecy).
export const environment = {
  production: true,
  apiBaseUrl: '/api',
  firebase: {
    apiKey: "AIzaSyB8M3NnWK30tTzfstIEwAR_cYT9gxGIAXk",
    authDomain: "kimori-499114.firebaseapp.com",
    projectId: "kimori-499114",
    storageBucket: "kimori-499114.firebasestorage.app",
    messagingSenderId: "370258405649",
    appId: "1:370258405649:web:1e8841cfe6f08d080c04b0"
  }
};
