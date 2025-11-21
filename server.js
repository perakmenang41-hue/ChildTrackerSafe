require('dotenv').config();
const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');

const app = express();

// Middleware
app.use(cors());
app.use(express.json()); // parse JSON body

// Routes
const usersRoute = require('./routes/usersRoute'); 
app.use('/api/users', usersRoute);  // All user routes start with /api/users

// MongoDB Connection
mongoose.connect('mongodb://127.0.0.1:27017/kidtracker')
  .then(() => console.log("✅ MongoDB connected"))
  .catch(err => console.log("❌ MongoDB Error:", err));

// Start Server
const PORT = process.env.PORT || 5000;
// Use 0.0.0.0 so Android emulator or devices can connect
app.listen(PORT, "0.0.0.0", () => {
    console.log(`🚀 Server running at: http://localhost:${PORT}`);
});
