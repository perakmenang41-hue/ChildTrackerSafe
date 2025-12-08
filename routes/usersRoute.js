// backend/routes/userRoute.js
const express = require('express');
const router = express.Router();
const sendRegistrationEmail = require('../emailService'); // Email service
const { db } = require('../firebase'); // Firestore setup

// ===================== REGISTER =====================
router.post('/register', async (req, res) => {
  try {
    const { name, email, age, country, password } = req.body;

    if (!name || !email || !age || !country || !password) {
      return res.status(400).json({ success: false, message: "All fields are required" });
    }

    // Check if email already exists
    const existingSnapshot = await db.collection('registered_users')
      .where('email', '==', email)
      .get();

    if (!existingSnapshot.empty) {
      return res.status(400).json({ success: false, message: "Email already registered" });
    }

    // Generate uniqueId
    const uniqueId = Math.random().toString(36).substring(2, 10);

    // Save user in Firestore
    await db.collection('registered_users').doc(uniqueId).set({
      name,
      email,
      age,
      country,
      password, // ⚠️ Hash in production
      uniqueId,
      registeredAt: new Date().toISOString()
    });

    // Send UID email
    await sendRegistrationEmail(email, uniqueId);

    res.status(200).json({
      success: true,
      message: 'Registration successful, email sent.',
      uniqueId
    });

  } catch (err) {
    console.error("Registration failed:", err.message);
    res.status(500).json({ success: false, message: 'Registration failed' });
  }
});

// ===================== LOGIN =====================
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ success: false, message: "Email and password required" });
    }

    // Query user
    const snapshot = await db.collection('registered_users')
      .where('email', '==', email)
      .where('password', '==', password)
      .get();

    if (snapshot.empty) {
      return res.status(400).json({ success: false, message: "Invalid email or password" });
    }

    const userData = snapshot.docs[0].data();

    res.status(200).json({
      success: true,
      message: "Login successful",
      user: {
        name: userData.name,
        email: userData.email,
        age: userData.age,
        country: userData.country,
        uniqueId: userData.uniqueId,
        registeredAt: userData.registeredAt
      }
    });

  } catch (err) {
    console.error("Login failed:", err.message);
    res.status(500).json({ success: false, message: "Server Error" });
  }
});

module.exports = router;
