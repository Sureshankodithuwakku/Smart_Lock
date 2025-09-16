#include <Wire.h>
#include <Adafruit_SSD1306.h>
#include <MFRC522.h>
#include <Keypad.h>
#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <EEPROM.h>
#include "addons/TokenHelper.h"
// EEPROM configuration
#define EEPROM_SIZE 64
#define OLED_RESET -1
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);
// RFID pins
#define SS_PIN 5
#define RST_PIN 4
MFRC522 mfrc522(SS_PIN, RST_PIN);
// Keypad configuration
const byte ROW_NUM = 4;
const byte COL_NUM = 3;
char keys[ROW_NUM][COL_NUM] = {
{'1', '2', '3'},
{'4', '5', '6'},
{'7', '8', '9'},
{'*', '0', '#'}
};
byte pin_rows[ROW_NUM] = {13, 12, 14, 27};
byte pin_column[COL_NUM] = {26, 25, 33};
Keypad keypad = Keypad(makeKeymap(keys), pin_rows, pin_column, ROW_NUM, COL_NUM);
// Push button and solenoid lock pins
#define BUTTON_1 34
#define BUTTON_2 32
#define SOLENOID_LOCK 15
// WiFi and Firebase configuration
#define WIFI_SSID "Hotspot"
#define WIFI_PASSWORD "aqaq1234"
#define API_KEY "AIzaSyCnWupU8-plHguciD17dulphw3rQTxcRSk"
#define DATABASE_URL "https://doorlock-e0b24-default-rtdb.firebaseio.com/"
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;
String storedLockUUID; // Variable to store the lockUUID read from EEPROM
void setup() {
Serial.begin(115200);
EEPROM.begin(EEPROM_SIZE);
storedLockUUID = readLockUUIDFromEEPROM(); // Read the stored lockUUID from EEPROM
// Initialize OLED display
if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
Serial.println(F("OLED display not found")); while (1); 
}
display.clearDisplay();
display.display();
delay(2000);
// Connect to WiFi
connectToWiFi();
// Initialize Firebase
config.api_key = API_KEY;
config.database_url = DATABASE_URL;
Firebase.reconnectWiFi(true);
config.token_status_callback = tokenStatusCallback;
Firebase.begin(&config, &auth);
if (Firebase.signUp(&config, &auth, "", "")) {
Serial.println("Anonymous authentication succeeded."); 
} else {
Serial.printf("Authentication failed: %s\n", config.signer.signupError.message.c_str()); 
}
// Initialize RFID reader
SPI.begin();
mfrc522.PCD_Init();
// Initialize pins
pinMode(BUTTON_1, INPUT_PULLUP);
pinMode(BUTTON_2, INPUT);
pinMode(SOLENOID_LOCK, OUTPUT);
digitalWrite(SOLENOID_LOCK, LOW);
displayMessage("Scan RFID tag");
}
void loop() {
// Check WiFi connection
if (WiFi.status() != WL_CONNECTED) {
connectToWiFi(); 
}
// Handle button 2 (toggle solenoid lock)
if (digitalRead(BUTTON_2) == LOW) {
toggleSolenoidLock(); delay(500); 
} else {
digitalWrite(SOLENOID_LOCK, LOW); 
}
delay(500);
// Handle button 1 (register lock)
if (digitalRead(BUTTON_1) == LOW) {
handleButton1(); delay(500); 
}
// Unlocked the door by checking the door state
String keyState = checkKeyStateFirebase(storedLockUUID);
if (keyState == "true"){
toggleSolenoidLock(); delay(10000); 
} else {
digitalWrite(SOLENOID_LOCK, LOW); 
}
// Handle RFID card scan
if (mfrc522.PICC_IsNewCardPresent() && mfrc522.PICC_ReadCardSerial()) {
String rfidUID = getRFIDUID(); Serial.println("Scanned RFID UID: " + rfidUID); processRFID(rfidUID); mfrc522.PICC_HaltA(); delay(2000); displayMessage("Scan RFID tag"); 
}
}
// Function to connect to WiFi
void connectToWiFi() {
if (WiFi.status() != WL_CONNECTED) {
Serial.print("Connecting to WiFi"); WiFi.begin(WIFI_SSID, WIFI_PASSWORD); while (WiFi.status() != WL_CONNECTED) { delay(500); Serial.print("."); } Serial.println("\nConnected to WiFi"); 
}
}
// Function to get RFID UID
String getRFIDUID() {
String uid = "";
for (byte i = 0; i < mfrc522.uid.size; i++) {
uid += String(mfrc522.uid.uidByte[i], HEX); 
}
return uid;
}
// Function to process RFID UID
void processRFID(String uid) {
if (isRFIDRegistered(uid)) {
String userNIC = checkUserInFirebase(uid); String userName = getUserNameFromFirebase(userNIC); String userKeys = getUserKeysFromFirebase(userNIC); displayUserInfo(userName, userKeys); 
} else {
displayMessage("New RFID. Registering..."); String userName = getUserName(); saveUserToFirebase(uid, userName); displayMessage("RFID SAVED: " + userName); 
}
}
// Function to check if RFID is registered
bool isRFIDRegistered(String uid) {
String path = "/RFID_tags/" + uid + "/NIC";
return Firebase.RTDB.getString(&fbdo, path) && fbdo.stringData() != "";
}
// Function to check user in Firebase
String checkUserInFirebase(String uid) {
String path = "/RFID_tags/" + uid + "/NIC";
return Firebase.RTDB.getString(&fbdo, path) ? fbdo.stringData() : "Unknown";
}
// Function to get user name from Firebase
String getUserNameFromFirebase(String NIC) {
String path = "/Users/" + NIC + "/name";
return Firebase.RTDB.getString(&fbdo, path) ? fbdo.stringData() : "Unknown";
}
// Function to get user keys from Firebase
String getUserKeysFromFirebase(String NIC) {
String path = "/Users/" + NIC + "/keys";
return Firebase.RTDB.getString(&fbdo, path) ? fbdo.stringData() : "Not Found";
}
// Function to check key state in Firebase
String checkKeyStateFirebase(String keyUUID) {
String path = "/Doors/" + keyUUID + "/state";
return Firebase.RTDB.getString(&fbdo, path) ? fbdo.stringData() : "Unknown";
}
// Function to save user to Firebase
void saveUserToFirebase(String uid, String name) {
String path = "/RFID_tags/" + uid + "/NIC";
Firebase.RTDB.setString(&fbdo, path, name);
}
// Function to get user name from keypad
String getUserName() {
String name = "";
char key;
displayMessage("Enter Name:");
while ((key = keypad.getKey()) != '#') {
if (key) { name += key; displayMessage("Name: " + name); } 
}
return name;
}
// Function to handle button 1 (register lock)
void handleButton1() {
String lockName = "";
char key;
displayMessage("Enter LOCK name:");
while ((key = keypad.getKey()) != '#') {
if (key) { lockName += key; displayMessage("LOCK Name: " + lockName + "saved"); } 
}
saveLockToFirebase(lockName);
}
// Function to save lock to Firebase
void saveLockToFirebase(String lockName) {
String lockUUID = generateUUID();
storeLockUUIDToEEPROM(lockUUID); // Save the lockUUID to EEPROM
FirebaseJson json;
json.set("door_id", lockName);
json.set("state", "false");
json.set("UUID", lockUUID);
Firebase.RTDB.setJSON(&fbdo, "/Doors/" + lockUUID, &json);
displayMessage("LOCK SAVED: " + lockName);
}
// Function to toggle solenoid lock
void toggleSolenoidLock() {
digitalWrite(SOLENOID_LOCK, HIGH);
Serial.println("Solenoid Unlocked!");
}
// Function to generate UUID
String generateUUID() {
String uuid;
for (int i = 0; i < 8; i++) uuid += String(random(0, 16), HEX);
return uuid;
}
// Function to store lock UUID to EEPROM
void storeLockUUIDToEEPROM(String uuid) {
for (int i = 0; i < uuid.length(); i++) {
EEPROM.write(i, uuid[i]); 
}
EEPROM.write(uuid.length(), '\0'); // Null-terminate the string
EEPROM.commit();
}
// Function to read lock UUID from EEPROM
String readLockUUIDFromEEPROM() {
String uuid = "";
for (int i = 0; i < EEPROM_SIZE; i++) {
char c = EEPROM.read(i); if (c == '\0') break; uuid += c; 
}
return uuid;
}
// Function to display a message on the OLED
void displayMessage(String msg) {
display.clearDisplay();
display.setTextSize(1);
display.setTextColor(SSD1306_WHITE);
display.setCursor(0, 0);
display.println(msg);
display.display();
}
// Function to display user info on the OLED
void displayUserInfo(String userName, String userKeys) {
display.clearDisplay();
display.setTextSize(1);
display.setTextColor(SSD1306_WHITE);
display.setCursor(0, 0);
display.println("User: " + userName);
if (userKeys != "Not Found") {
FirebaseJson json; json.setJsonData(userKeys); size_t count = json.iteratorBegin(); for (size_t i = 0; i < count; i++) { FirebaseJson::IteratorValue value = json.valueAt(i); String keyUUID = value.key; // Filter out unnecessary keys if (keyUUID != "UUID" && keyUUID != "door_id" && keyUUID != "door_name") { if (keyUUID == storedLockUUID) { display.println("Welcome, " + userName); toggleSolenoidLock(); delay(500); } } } json.iteratorEnd(); 
} else {
display.println("No Keys Found"); 
}
display.display();
}