const admin = require('firebase-admin');

// Parse Firebase JSON from environment variable
const serviceAccount = JSON.parse(process.env.FIREBASE_KEY_JSON);

// Initialize Firebase Admin SDK
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// Get Firestore instance
const db = admin.firestore(); // Default Firestore database

module.exports = { db };
