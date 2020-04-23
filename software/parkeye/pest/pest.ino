// ParkEye Setup Tool (PEST)
// Created by Theodoros Plessas (8160192) for Artifex Electronics

#include <EEPROM.h>
#include <LiquidCrystal.h>

LiquidCrystal lcd(8, A0, 4, 5, 6, 7);

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
  Serial.begin(9600);
  Serial.println("          ParkEye Setup Tool (PEST)");
  Serial.println("(c)2020 Artifex Electronics | All rights reserved");
  Serial.println();

  lcd.begin(16, 2);
  lcd.createChar(0, logo);
  lcd.createChar(1, maint);

  lcd.setCursor(1, 0);
  lcd.write(byte(1));
  lcd.print(" Parkopticon");
  lcd.setCursor(0, 1);
  lcd.print("Maintenance Mode");
}

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
    if (uid.length() == 5) {
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
  Serial.println("|1 Basic|");
  Serial.println("|2 RFID Stored|");
  Serial.println("|3 RFID Serial|");

  while (flag) {
    while (flag2) {
      if (Serial.available() > 0) {
        opmode = Serial.parseInt();
        flag2 = false;
      }
    }

    if (opmode == 1) {
      Serial.println("Wrote: Basic");
      EEPROM.write(5, 0);
      flag = false;
    }
    else if (opmode == 2) {
      Serial.println("Wrote: RFID Stored");
      EEPROM.write(5, 1);
      flag = false;
    }
    else if (opmode == 3) {
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
  if (opmode == 2) {
    Serial.println();
    Serial.println("Set RFID tag UID (XX XX XX XX):");
    while (flag) {
      while (flag2) {
        if (Serial.available() > 0) {
          uid = Serial.readString();
          flag2 = false;
        }
      }

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

void dump() {
  int counter = 0;
  Serial.println("EEPROM Dump");
  Serial.print(" *   ");
  for (int i = 0; i < 16; i++) {
    if (i < 9) {
    Serial.print(String(i + 1) + "   ");
    }
    else {
      Serial.print(String(i + 1) + "  ");
    }
  }
  Serial.println();
  for (int i = 0; i < 64; i++) {
    if (i < 9) {
      Serial.print(" ");
    }
    Serial.print(String(i + 1) + "   ");
    for (int j = 0; j < 16; j++) {
      if (EEPROM.read(counter) > 99) {
        Serial.print(String(EEPROM.read(counter)) + " ");
      } else if (EEPROM.read(counter) < 99 && EEPROM.read(counter) > 9) {
        Serial.print(String(EEPROM.read(counter)) + "  ");
      } else {
        Serial.print(String(EEPROM.read(counter)) + "   ");
      }
      counter++;
    }
    Serial.println();
  }
  Serial.println();
}

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
