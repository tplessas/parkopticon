#include <EEPROM.h>

void setup() {
  EEPROM.begin(512);

  for(int i = 0; i < 512; i++) {
    EEPROM.write(i, 255);
  }

  EEPROM.end();
  Serial.print("END");

}

void loop() {
  // put your main code here, to run repeatedly:

}
