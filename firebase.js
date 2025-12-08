// firebase.js
const admin = require("firebase-admin");

// Ensure the environment variable exists
if (!process.env.FIREBASE_KEY_JSON) {
  throw new Error("Missing FIREBASE_KEY_JSON environment variable");
}

// Parse the JSON from the environment variable
const serviceAccount = JSON.parse(process.env.FIREBASE_KEY_JSON);

// Initialize Firebase Admin SDK
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// Firestore instance
const db = admin.firestore();

module.exports = { admin, db };
