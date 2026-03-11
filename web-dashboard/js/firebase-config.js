// Firebase Configuration
const firebaseConfig = {
    apiKey: "AIzaSyDaQ4b0DZMryXEsyco-IXAIy0hFX_W9UgA",
    authDomain: "hmooude-37c70.firebaseapp.com",
    databaseURL: "https://hmooude-37c70-default-rtdb.firebaseio.com",
    projectId: "hmooude-37c70",
    storageBucket: "hmooude-37c70.firebasestorage.app",
    messagingSenderId: "407210691250",
    appId: "1:407210691250:web:29eca46834d02fc29897ab",
    measurementId: "G-FW0030K1Z6"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);

// Get references
const database = firebase.database();
const storage = firebase.storage();

// Export for use in other files
window.db = database;
window.storage = storage;
