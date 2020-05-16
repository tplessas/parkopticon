const int proximity = 2;
const int rst = 3;
bool curr;
bool prev;

void setup() {
  Serial.begin(9600);
  pinMode(proximity, INPUT);
  pinMode(rst, OUTPUT);
  digitalWrite(rst, HIGH);
  prev = (digitalRead(proximity) == HIGH ? true : false);
  Serial.print(prev);
}

void loop() {
  curr = (digitalRead(proximity) == HIGH ? true : false);
  if(curr != prev){
    digitalWrite(rst, LOW);
    delay(100);
    digitalWrite(rst, HIGH);
    Serial.print("pulsed");
  }
  prev = curr;
}
