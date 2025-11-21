const mongoose = require('mongoose');

mongoose.connect('mongodb://127.0.0.1:27017/kidtracker')
  .then(async () => {
    console.log("MongoDB connected");
    const result = await mongoose.connection.collection('users').deleteMany({});
    console.log("Deleted documents:", result.deletedCount);
    process.exit(0);
  })
  .catch(err => {
    console.error(err);
    process.exit(1);
  });
