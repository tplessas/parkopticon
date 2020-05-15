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

const int proximity = 0;

//declare sensors
MFRC522 rfid(4, 5);
MFRC522::MIFARE_Key key;

//config variables
String uid; //ParkEye UID
int opmode;
String rfuid; //RFID tag UID, always loaded no matter the opmode

//status variables
bool occ = false;
bool infract = false; //infraction status, used in rfid subroutine

void setup() {
  //init serial at 74880
  Serial.begin(74880);
  EEPROM.begin(512);
  pinMode(proximity, INPUT);

  //reading and printing uid
  char temp[6];
  temp[5] = 0x00; //null char, end of byte array
  for (int i = 0; i < 5; i++) {
    temp[i] = EEPROM.read(i);
  }
  uid = String(temp);
  Serial.print(uid);

  //reading and printing opmode
  opmode = EEPROM.read(5);
  Serial.print(opmode);

  //reading and printing rfuid
  //TODO find out what it does when all bytes==255
  char temp2[12];
  temp2[11] = 0x00; //null char, end of byte array
  for (int i = 0; i < 11; i++) {
    temp2[i] = EEPROM.read(i + 6);
  }
  rfuid = String(temp2);
  Serial.print(rfuid);
  Serial.print("*");

  //opmode 1, 2: init SPI bus for MFRC522 and setting key to default
  //TODO store key in config?
  if (opmode != 0) {
    SPI.begin();
    rfid.PCD_Init();
    for (byte i = 0; i < 6; i++) {
      key.keyByte[i] = 0xFF;
    }
  }

  EEPROM.end();
}

void loop() {
  check();

  //delay before next check
  delay(500);
}

//check proximity sensor status
void check() {
  if ((digitalRead(proximity) == LOW) && (occ == false)) {
    occupy();
  }
  else if ((digitalRead(proximity) == HIGH) && (occ == true)) {
    freed();
  }
}

//occupy parking spot
void occupy() {
  occ = true;
  Serial.print("OCCD*");
  delay(500);

  //opmode 1,2: rfid subroutine
  //TODO add delays between occupy and check, reread attempts to ensure successful reading
  if (opmode != 0) {
    int present = 0; //times tag was NOT present
    int read = 0; //read attempts
    bool ok = false; //used to break from ReadCardSerial loop if there's no infraction

    for (int i = 0; i < 2; i++) {
      if (rfid.PICC_IsNewCardPresent()) { //check if tag is present
        for (int j = 0; j < 3; j++) {
          if (rfid.PICC_ReadCardSerial()) { //check if tag is read successfully
            MFRC522::PICC_Type piccType = rfid.PICC_GetType(rfid.uid.sak);
            if (piccType == MFRC522::PICC_TYPE_MIFARE_1K) { //check if tag is legal (MIFARE 1K)
              if (opmode == 1) {
                if (castString().equals(rfuid)) { //compare with stored
                  Serial.print("ALLGOOD*");
                  ok = true;
                } else {
                  Serial.print("UNAUTHPK*");
                  infract = true;
                }
              } else { //castString and print to serial
                Serial.print(castString());
                Serial.print("*");
                ok = true;
              }
            } else {
              Serial.print("ILLGLTAG*");
              infract = true;
            }
            if (infract || ok) //infraction or ALLGOOD/sent to serial
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
        if (infract || ok)
          break;
        delay(1000); //delay before next present check
      } else {
        present++;
        //NOCARD if tag not found this number of times
        if (present == 2) {
          Serial.print("NOCARD*");
          infract = true;
        }
      }
    }
  }
}

//free parking spot
void freed() {
  occ = false;
  Serial.print("FREE*");
  if (infract) {
    infract = false;
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
