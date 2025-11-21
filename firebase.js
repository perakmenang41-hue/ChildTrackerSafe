const admin = require('firebase-admin');
const serviceAccount = require('./firebase-key.json'); // or 'serviceAccountKey.json', use your actual filename

// Initialize Firebase Admin SDK
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// Get Firestore instance
const db = admin.firestore(); // Default Firestore database

// Export Firestore to use in userRoute.js
module.exports = { db };
