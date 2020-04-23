// ParkEye Runtime with Messaging (PERM)
// Created by Theodoros Plessas (8160192) for Artifex Electronics

#include <SPI.h>
#include <MFRC522.h>
#include <HCSR04.h>
#include <EEPROM.h>
#include <LiquidCrystal.h>

HCSR04 hc(2, 3);
MFRC522 rfid(10, 9);
MFRC522::MIFARE_Key key;
LiquidCrystal lcd(8, A0, 4, 5, 6, 7);

String uid;
int opmode;
String rfuid;

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

bool occ = false;
int cycles = 0;
int sr04 = 0;
bool infract = false;

void setup() {
  Serial.begin(9600);

  char temp[6];
  temp[5] = 0x00;
  for (int i = 0; i < 5; i++) {
    temp[i] = EEPROM.read(i);
  }
  uid = String(temp);
  Serial.print(uid);

  opmode = EEPROM.read(5);
  Serial.print(opmode);

  char temp2[12];
  temp2[11] = 0x00;
  for (int i = 0; i < 11; i++) {
    temp2[i] = EEPROM.read(i + 6);
  }
  rfuid = String(temp2);
  Serial.print(rfuid);
  Serial.print("*");

  if (opmode != 0) {
    SPI.begin();
    rfid.PCD_Init();
    for (byte i = 0; i < 6; i++) {
      key.keyByte[i] = 0xFF;
    }
  }

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
}

void loop() {

  if (!infract && !occ) {
    lcd.clear();
    lcd.write(byte(0));
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

  int dist = hc.dist();
  check(dist);

  if (cycles == 1000) {
    selfdg();
    cycles = 0;
  }
  delay(500);
}

void check(int dist) {
  if ((dist < 3) && (occ == false)) {
    occupy();
  }
  else if ((dist > 10) && (occ == true)) {
    freed();
  }
  cycles++;
}

void occupy() {
  occ = true;
  Serial.print("OCCD*");

  if (opmode != 0) {
    int present = 0;
    int read = 0;
    bool ok = false;
    byte nuidPICC[4];

    for (int i = 0; i < 2; i++) {
      if (rfid.PICC_IsNewCardPresent()) {
        delay(1000);
        for (int j = 0; j < 3; j++) {
          if (rfid.PICC_ReadCardSerial()) {
            MFRC522::PICC_Type piccType = rfid.PICC_GetType(rfid.uid.sak);
            if (piccType = MFRC522::PICC_TYPE_MIFARE_1K) {
              for (byte i = 0; i < 4; i++) {
                nuidPICC[i] = rfid.uid.uidByte[i];
              }
              if (opmode == 1) {
                if (castString().equals(rfuid)) {
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
              } else {
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
            if (infract || ok)
              break;
            delay(1000);
          } else {
            read++;
            if (read == 3) {
              Serial.print("RFIDFAIL*");
              while (1) {}
            }
          }
        }
        if (infract || ok)
          break;
        delay(1000);
      } else {
        present++;
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

void freed() {
  occ = false;
  Serial.print("FREE*");
  if (infract) {
    infract = false;
  }
}

void selfdg() {
  int dists[10];
  delay(10);

  for (int i = 0; i < 10; i++) {
    dists[i] = hc.dist();
    delay(10);
  }
  for (int i = 1; i < 10; i++) {
    if (abs(dists[i - 1] - dists[i]) > 8) {
      sr04++;
    }
  }
  if (sr04 > 20) {
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
