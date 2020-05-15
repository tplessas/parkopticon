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
#include <HCSR04.h>
#include <EEPROM.h>
#include <LiquidCrystal.h>

//init sensors
HCSR04 hc(2, 3);
MFRC522 rfid(10, 9);
MFRC522::MIFARE_Key key;
LiquidCrystal lcd(8, A0, 4, 5, 6, 7);

//config variables
String uid; //ParkEye UID
int opmode;
String rfuid; //RFID tag UID, always loaded no matter the opmode

//status variables
bool occ = false;
int cycles = 0; //cycles ran up to selfdg
int sr04 = 0; //inaccurate reading count
bool infract = false; //infraction status, used in rfid subroutine

//display graphics as custom characters
byte logo[8] =
{
  0b01110,
  0b10001,
  0b10101,
  0b10001,
  0b10111,
  0b10111,
  0b01110,
  0b00100
};

byte idsym[8] =
{
  0b10000,
  0b10000,
  0b10000,
  0b10110,
  0b00101,
  0b00101,
  0b00101,
  0b00110
};

byte maint[8] =
{
  0b01110,
  0b10101,
  0b10001,
  0b11011,
  0b10001,
  0b10101,
  0b01110,
  0b00100
};

byte good[8] =
{
  0b01110,
  0b11111,
  0b10101,
  0b11111,
  0b10101,
  0b11011,
  0b01110,
  0b00100
};

byte bad[8] =
{
  0b01110,
  0b11111,
  0b10101,
  0b11011,
  0b10101,
  0b11111,
  0b01110,
  0b00100
};

void setup() {
  //init serial at 9600
  Serial.begin(9600);

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

  //init LCD, creating symbols and displaying boot message for 1500ms
  lcd.begin(16, 2);
  lcd.createChar(0, logo);
  lcd.createChar(1, idsym);
  lcd.createChar(2, maint);
  lcd.createChar(3, good);
  lcd.createChar(4, bad);

  lcd.setCursor(1, 0);
  lcd.write(byte(0));
  lcd.print(" Parkopticon");
  lcd.setCursor(0, 1);
  lcd.print("Artifex/Tronics");

  delay(1500);

  //write uid and parking rights to display
  //TODO communicate with ParkBrain to print limited when opmode==0
  lcd.clear();
  lcd.setCursor(10, 0);
  lcd.write(byte(1));
  lcd.print(uid);
  if (opmode == 0) {
    lcd.setCursor(0, 1);
    lcd.print("Free Pkng");
  } else {
    lcd.setCursor(0, 1);
    lcd.print("Limited Pkng");
  }
}

void loop() {

  //update display at ParkEye state change
  if (!infract && !occ) {
    lcd.setCursor(0, 0);
    lcd.write(byte(0));
    lcd.print("         "); //previous message whitespaced
  }

  //distance check from vehicle
  check((int)hc.dist());

  if (cycles == 1000) {
    selfdg();
    cycles = 0;
  }

  //delay before next check
  delay(500);
}

//compare vehicle dist to cutoffs
void check(int dist) {
  if ((dist < 3) && (occ == false)) { //TODO store cutoff in config?
    occupy();
  }
  else if ((dist > 10) && (occ == true)) { //10 as cutoff to avoid state change due to possible imprecise measurement
    freed();
  }
  cycles++;
}

//occupy parking spot
void occupy() {
  occ = true;
  Serial.print("OCCD*");
  delay(500);

  //opmode 1,2: rfid subroutine
  //TODO add reread attempts to ensure successful reading
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
                  lcd.setCursor(0, 0);
                  lcd.write(byte(3));
                  lcd.print("PARKED");
                } else {
                  Serial.print("UNAUTHPK*");
                  infract = true;
                  lcd.setCursor(0, 0);
                  lcd.write(byte(4));
                  lcd.print("UNAUTHPK");
                }
              } else { //castString and print to serial
                Serial.print(castString());
                Serial.print("*");
                ok = true;
              }
            } else {
              Serial.print("ILLGLTAG*");
              infract = true;
              lcd.setCursor(0, 0);
              lcd.write(byte(4));
              lcd.print("ILLGLTAG");
            }
            if (infract || ok) //infraction or ALLGOOD/sent to serial
              break;
            delay(1000); //delay before next read attempt
          } else {
            read++;
            //RFIDFAIL if tag not read this number of times
            if (read == 3) {
              Serial.print("RFIDFAIL*");
              while (1) {}
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
          lcd.setCursor(0, 0);
          lcd.write(byte(4));
          lcd.print("NOCARD");
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

//HC-SRO4 self-diagnostic, takes 10 readings and checks for significant delta from previous one
//TODO make more reliable, adjust sensitivity
void selfdg() {
  int dists[10];
  delay(10); //minimum delay to ensure accurate readings

  for (int i = 0; i < 10; i++) {
    dists[i] = hc.dist();
    delay(10);
  }
  for (int i = 1; i < 10; i++) {
    if (abs(dists[i - 1] - dists[i]) > 8) {
      sr04++;
    }
  }

  //fail if it happens over 5 times in device uptime
  if (sr04 > 10) {
    Serial.print("SR04FAIL*");
    lcd.clear();
    lcd.setCursor(1, 0);
    lcd.write(byte(2));
    lcd.print(" Parkopticon");
    lcd.setCursor(0, 1);
    lcd.print("  Out of Order");
    while (1) {}
  } else {
    Serial.print("SR04PASS*");
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
