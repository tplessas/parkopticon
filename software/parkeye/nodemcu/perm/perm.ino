// ParkEye Runtime with Messaging (PERM)
// Created by Theodoros Plessas (8160192) for Artifex Electronics
//
// MIT License
//
// Copyright (c) 2020 Theodoros Plessas
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

//libraries
#include <SPI.h>
#include <MFRC522.h>
#include <EEPROM.h>
#include <RTCVars.h>

//declare mfrc pins
MFRC522 rfid(4, 5);
MFRC522::MIFARE_Key key;

RTCVars state; // stores variables in RTCmem, persistence after deep sleep-reset cycle

//config variables
char uid[6]; //ParkEye UID
int opmode;
char rfuid[12]; //RFID tag UID, always loaded no matter the opmode

//status variables as integers for RTCmem storage
int occ;
int infract; //infraction status, used in rfid subroutine

void setup() {
  //init serial at 74880 as debug port, NOT COMPATIBLE WITH edgeWare Serial
  Serial.begin(74880);

  //pass variable pointers to RTC state object
  for (int i = 0; i < 6; i++) {
    state.registerVar(&(uid[i]));
  }
  state.registerVar(&opmode);
  for (int i = 0; i < 12; i++) {
    state.registerVar(&(rfuid[i]));
  }
  state.registerVar(&occ);
  state.registerVar(&infract);

  if (!state.loadFromRTC()) { //cold boot, load config from EEPROM
    EEPROM.begin(512);
    delay(5000);
    Serial.print("HARD*"); //as in hard reset

    //reading and printing uid
    uid[5] = 0x00; //null char, end of byte array
    for (int i = 0; i < 5; i++) {
      uid[i] = EEPROM.read(i);
    }
    Serial.print(uid);

    //reading and printing opmode
    opmode = EEPROM.read(5);
    Serial.print(opmode);

    //reading and printing rfuid
    rfuid[11] = 0x00; //null char, end of byte array
    for (int i = 0; i < 11; i++) {
      rfuid[i] = EEPROM.read(i + 6);
    }
    Serial.print(rfuid);
    Serial.print("*");

    free(); //called to init status vars to false, !!!NO CAR IN PLACE!!!
    state.saveToRTC();
    EEPROM.end();
    ESP.deepSleep(0); //sleep until rst LOW
  } else { //soft reset, variables in memory
    Serial.print("SOFT*"); //as in soft reset
    flip();
    ESP.deepSleep(0); //sleep until rst LOW
  }
}

//flip occupied status on wakeup
void flip() {
  if (occ == 0) {
    occupy();
  } else {
    free();
  }
  state.saveToRTC();
}

//opmode 1, 2: init SPI bus for MFRC522 and setting key to default
//TODO store key in config?
void mfrc522init() {
  SPI.begin();
  rfid.PCD_Init();
  for (byte i = 0; i < 6; i++) {
    key.keyByte[i] = 0xFF;
  }
}

//occupy parking spot
void occupy() {
  occ = 1;
  Serial.print("OCCD*");

  //opmode 1,2: rfid subroutine
  //TODO add reread attempts to ensure successful reading
  if (opmode != 0) {
    int present = 0; //times tag was NOT present
    int read = 0; //read attempts
    bool ok = false; //used to break from ReadCardSerial loop if there's no infraction
    mfrc522init();

    for (int i = 0; i < 2; i++) {
      rfid.PCD_SoftPowerUp(); //wake mfrc522
      delay(1000); //delay to ensure mfrc522 wakeup and tag present
      if (rfid.PICC_IsNewCardPresent()) { //check if tag is present
        for (int j = 0; j < 3; j++) {
          if (rfid.PICC_ReadCardSerial()) { //check if tag is read successfully
            MFRC522::PICC_Type piccType = rfid.PICC_GetType(rfid.uid.sak);
            if (piccType == MFRC522::PICC_TYPE_MIFARE_1K) { //check if tag is legal (MIFARE 1K)
              if (opmode == 1) {
                if (castString().equals((String)rfuid)) { //compare with stored
                  Serial.print("ALLGOOD*");
                  ok = true;
                } else {
                  Serial.print("UNAUTHPK*");
                  infract = 1;
                }
              } else { //castString and print to serial
                Serial.print(castString());
                Serial.print("*");
                ok = true;
              }
            } else {
              Serial.print("ILLGLTAG*");
              infract = 1;
            }
            if (infract == 1 || ok) //infraction or ALLGOOD/sent to serial
              break;
            delay(1000); //delay before next read attempt
          } else {
            read++;
            //RFIDFAIL if tag not read this number of times
            if (read == 3) {
              Serial.print("RFIDFAIL*");
              ESP.deepSleep(0);
            }
          }
        }
        if (infract == 1 || ok)
          break;
      } else {
        present++;
        //NOCARD if tag not found this number of times
        if (present == 2) {
          Serial.print("NOCARD*");
          infract = 1;
        }
      }
    }
    rfid.PCD_SoftPowerDown(); //put mfrc522 to sleep
  }
}

//free parking spot
void free() {
  occ = 0;
  Serial.print("FREE*");
  if (infract == 1) {
    infract = 0;
  }
}

//turns RFID byte array into spaced hex string
//TODO make all rfid operations use bytes, not ascii byte-to-string nonsense
//this is dumb
String castString() {
  String str;

  for (byte i = 0; i < 4; i++) {
    String temp = String(rfid.uid.uidByte[i], HEX);
    temp.toUpperCase();
    str = str + String(temp);
    if (i < 3) {
      str = str + " ";
    }
  }
  return str;
}

//never reached, only here to placate the compiler
void loop() {
}
