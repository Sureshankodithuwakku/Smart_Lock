🔐 Smart IoT Lock

A Smart IoT Lock System designed and developed with a keypad, RFID authentication, and Firebase real-time database integration. It enables secure remote key sharing, user authentication, and real-time access control through a mobile application.

🚀 Features

    🔑 Multi-factor Authentication: Unlock using a keypad or RFID card.
    
    ☁️ Firebase Real-time Database: Synchronizes access logs and credentials instantly.
    
    📱 Mobile App Integration: Manage and share digital keys remotely.
    
    🛡️ Secure Remote Access: Control who can unlock the door anytime, anywhere.
    
    📊 Real-time Monitoring: View entry logs and authentication attempts live.
    
    📝 Lock Registration:
        Admin-only feature to register a new lock.
        Assign a custom name for each lock.
        Add new members/users to access the lock.
        

🛠️ Tech Stack

    Hardware:
      ESP32 Microcontroller
      RFID Reader
      Keypad
      Solenoid Lock
      
    Cloud:
      Firebase Realtime Database
      
    Mobile App:
      Built with Android Studio 
      
    Programming:
      C++ for ESP32 firmware

⚙️ Installation & Setup

    1️⃣ Hardware Setup
      1. Connect the RFID reader, keypad, and solenoid lock to the ESP32.
      2. Flash the firmware from hardware/ using Arduino IDE or PlatformIO.
      3. Configure WiFi credentials and Firebase API keys in the firmware.
    
    2️⃣ Firebase Setup
      1. Create a Firebase project.
      2. Set up Realtime Database with appropriate security rules.
      3. Copy API credentials into the ESP32 firmware and mobile app.
    
    3️⃣ Mobile App Setup
      1. Open the mobile-app/ project in Android Studio.
      2. Add your Firebase configuration (google-services.json).
      3. Build and run the app on your device.



