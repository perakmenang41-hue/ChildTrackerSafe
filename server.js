// server.js
require('dotenv').config();
const express = require('express');
const cors = require('cors');

const { db } = require('./firebase'); // Firestore setup
const usersRoute = require('./routes/userRoute'); // Firestore-based user routes

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/users', usersRoute);

// Port
const PORT = process.env.PORT || 5000;
app.listen(PORT, "0.0.0.0", () => {
    console.log(`🚀 Server running at: http://localhost:${PORT}`);
});
