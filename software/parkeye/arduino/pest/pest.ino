// ParkEye Setup Tool (PEST)
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
#include <EEPROM.h>
#include <LiquidCrystal.h>

//init display
LiquidCrystal lcd(8, A0, 4, 5, 6, 7);

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

void setup() {
  //init serial at 9600, print message
  Serial.begin(9600);
  Serial.println("          ParkEye Setup Tool (PEST)");
  Serial.println("(c)2020 Artifex Electronics | All rights reserved");
  Serial.println();

  //init LCD, creating symbols and displaying maintenance mode message
  lcd.begin(16, 2);
  lcd.createChar(0, logo);
  lcd.createChar(1, maint);
  lcd.setCursor(1, 0);
  lcd.write(byte(1));
  lcd.print(" Parkopticon");
  lcd.setCursor(0, 1);
  lcd.print("Maintenance Mode");
}

//write config
void write() {
  bool flag = true;
  bool flag2 = true;
  String uid;
  Serial.println("Set device UID (XXXXX):");
  while (flag) {
    while (flag2) {
      if (Serial.available() > 0) {
        uid = Serial.readString();
        flag2 = false;
      }
    }
    if (uid.length() == 5) { //form check
      for (int i = 0; i < 5; i++) {
        EEPROM.write(i, uid[i]);
      }
      Serial.print("Wrote: ");
      char data;
      for (int i = 0; i < 5; i++) {
        data = EEPROM.read(i);
        Serial.print(String(data));
      }
      Serial.println();
      Serial.println();
      flag = false;
    }
    else {
      Serial.println("Incorrect UID length, enter new value:");
      flag2 = true;
    }
  }

  int opmode;
  flag = true;
  flag2 = true;
  Serial.println("Set operation mode:");
  Serial.println("|0 Basic|");
  Serial.println("|1 RFID Stored|");
  Serial.println("|2 RFID Serial|");

  while (flag) {
    while (flag2) {
      if (Serial.available() > 0) {
        opmode = Serial.parseInt();
        flag2 = false;
      }
    }

    if (opmode == 0) {
      Serial.println("Wrote: Basic");
      EEPROM.write(5, 0);
      flag = false;
    }
    else if (opmode == 1) {
      Serial.println("Wrote: RFID Stored");
      EEPROM.write(5, 1);
      flag = false;
    }
    else if (opmode == 2) {
      Serial.println("Wrote: RFID Remote");
      EEPROM.write(5, 2);
      flag = false;
    }
    else {
      Serial.println("Enter value 1-3:");
      flag2 = true;
    }
  }

  flag = true;
  flag2 = true;
  if (opmode == 1) {
    Serial.println();
    Serial.println("Set RFID tag UID (XX XX XX XX):");
    while (flag) {
      while (flag2) {
        if (Serial.available() > 0) {
          uid = Serial.readString();
          flag2 = false;
        }
      }

      //form check and write
      if (uid.length() == 11) {
        if (isSpace(uid[2]) && isSpace(uid[5]) && isSpace(uid[8])) {
          for (int i = 0; i < 11; i++) {
            EEPROM.write(i + 6, uid[i]);
          }
          Serial.print("Wrote: ");
          char data;
          for (int i = 0; i < 11; i++) {
            data = EEPROM.read(i + 6);
            Serial.print(String(data));
            flag = false;
          }
          Serial.println();
        }
      }
      else {
        Serial.println("Invald form, enter new value:");
        flag2 = true;
      }
    }
  }

  Serial.println("\nSetup Complete");
}

//read stored config
void read() {
  Serial.print("Device UID: ");
  char data;
  for (int i = 0; i < 5; i++) {
    data = EEPROM.read(i);
    Serial.print(String(data));
  }
  Serial.println();
  Serial.print("Operation mode: ");
  if (EEPROM.read(5) == 0) {
    Serial.print("Basic");
  } else if (EEPROM.read(5) == 1) {
    Serial.print("RFID Stored");
  } else {
    Serial.print("RFID Remote");
  }
  Serial.println();
  Serial.print("RFID UID: ");
  for (int i = 0; i < 11; i++) {
    data = EEPROM.read(i + 6);
    Serial.print(String(data));
  }

}

//EEPROM dump routine
void dump() {
  Serial.println("EEPROM Dump");

  int counter = 0; //byte to be read, up to 1023
  //print column headers
  Serial.print(" *   ");
  for (int i = 0; i < 16; i++) {
    if (i < 9) {
    Serial.print(String(i + 1) + "   "); //one digit, three spaces
    }
    else {
      Serial.print(String(i + 1) + "  "); //two digits, two spaces
    }
  }
  Serial.println();
  for (int i = 0; i < 64; i++) {
    if (i < 9) {
      Serial.print(" "); //extra space for one digit
    }
    Serial.print(String(i + 1) + "   "); //line header and three spaces

    for (int j = 0; j < 16; j++) {
      if (EEPROM.read(counter) > 99) { //three digits, one space
        Serial.print(String(EEPROM.read(counter)) + " ");
      } else if (EEPROM.read(counter) < 99 && EEPROM.read(counter) > 9) { //two digits, two spaces
        Serial.print(String(EEPROM.read(counter)) + "  ");
      } else {
        Serial.print(String(EEPROM.read(counter)) + "   "); //one digit, three spaces
      }
      counter++;
    }
    Serial.println();
  }
  Serial.println();
}

//main menu
void loop() {
  int opmode;
  bool flag = true;
  Serial.println("Select utility:");
  Serial.println("|1 Write config|");
  Serial.println("|2 Read config |");
  Serial.println("|3 Dump EEPROM |");

  while (flag) {
    if (Serial.available() > 0) {
      opmode = Serial.parseInt();
      flag = false;
    }
  }

  if (opmode == 1) {
    write();
  }
  else if (opmode == 2) {
    read();
  }
  else if (opmode == 3) {
    dump();
  }
  else {
    Serial.println("Enter value 1-3:");
    flag = true;
  }
  Serial.println();
}
