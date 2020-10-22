`#include "DHT.h"
#include <SoftwareSerial.h>
#include <Stepper.h>
const int stepsPerRevolution = 2048; //모터바퀴수
int dust_sensor = A0;   // 미세먼지 핀 번호
int rgb_red = 5;    // rgb 핀 빨간색 핀
int rgb_green = 6;  // rgb핀 녹색 핀
int rgb_blue = 7;  // rgb핀 파란색 핀
int buzzerPin = 22;
float dust_value = 0;  // 센서에서 입력 받은 미세먼지 값
float dustDensityug = 0; // ug/m^3 값을 계산
int sensor_led = 12;      // 미세먼지 센서 안에 있는 적외선 led 핀 번호
int sampling = 280;    // 적외선 led를 키고, 센서 값을 읽어 들여 미세먼지를 측정하는 샘플링 시간
int waiting = 40;
int flame = 0;
int ledPin1 = 24;
float stop_time = 9680;   // 센서를 구동하지 않는 시간
Stepper myStepper(stepsPerRevolution, 11, 9, 10, 8);
DHT dht(4, DHT22);

void setup() {
  Serial.begin(9600);
  Serial1.begin(9600);
  myStepper.setSpeed(14);
  pinMode(buzzerPin, OUTPUT);
  pinMode(sensor_led, OUTPUT); // 미세먼지 적외선 led를 출력으로 설정
  pinMode(4, OUTPUT);
  pinMode(rgb_red, OUTPUT);     // 3색 LED 모듈 출력으로 설정, 붉은색
  pinMode(rgb_green, OUTPUT);   // 녹색
  pinMode(rgb_blue, OUTPUT);    // 파란색
  pinMode(ledPin1, OUTPUT);
  dht.begin();
}

void loop() {
  if (Serial1.available())
  {
    char cmd = (char)Serial1.read();
    if (cmd == '0')
    {
      digitalWrite(ledPin1, HIGH);
    }
    else if (cmd == '1')
    {
      digitalWrite(ledPin1, LOW);
    }
  }
  float h = dht.readHumidity();
  float t = dht.readTemperature();
  flame = analogRead(A2);
  digitalWrite(sensor_led, LOW);    // LED 켜기
  delayMicroseconds(sampling);   // 샘플링해주는 시간.
  dust_value = analogRead(dust_sensor); // 센서 값 읽어오기
  delayMicroseconds(waiting);  // 너무 많은 데이터 입력을 피해주기 위해 잠시 멈춰주는 시간.
  digitalWrite(sensor_led, HIGH); // LED 끄기
  delayMicroseconds(stop_time);   // LED 끄고 대기
  dustDensityug = (0.17 * (dust_value * (5.0 / 1024)) - 0.1) * 1000;    // 미세먼지 값 계산

  Serial.print("Dust Density [ug/m3]: ");            // 시리얼 모니터에 미세먼지 값 출력
  Serial.println(dustDensityug);
  Serial.print("Humidity: ");
  Serial.print(h);
  Serial.print(" %\t");
  Serial.print("Temperature: ");
  Serial.print(t);
  Serial.print(" *C ");

  Serial1.print("D,");
  Serial1.print(h);
  Serial1.print(",");
  Serial1.print(t);
  Serial1.print(",");
  Serial1.println(dustDensityug);

  if (flame < 100)
  {
    Serial.print("flame!!! wariming");
    Serial1.print("M,");
    digitalWrite(buzzerPin, HIGH);
    delay(5000);
    digitalWrite(buzzerPin, LOW);
  }

  if (
    dustDensityug <= 30.0) {      // 대기 중 미세먼지가 좋음 일 때 파란색 출력
    analogWrite(rgb_red, 0);
    analogWrite(rgb_green, 0);
    analogWrite(rgb_blue, 255);
    Serial.print("   ");
    Serial.println("blue");
  }
  else if (30.0 < dustDensityug && dustDensityug <= 80.0) {    // 대기 중 미세먼지가 보통 일 때 녹색 출력
    analogWrite(rgb_red, 0);
    analogWrite(rgb_green, 255);
    analogWrite(rgb_blue, 0);
    Serial.print("   ");
    Serial.println("green");
  }
  else if (
    80.0 < dustDensityug && dustDensityug <= 150.0) {   // 대기 중 미세먼지가 나쁨 일 때 노란색 출력
    analogWrite(rgb_red, 255);
    analogWrite(rgb_green, 155);
    analogWrite(rgb_blue, 0);
    Serial.print("   ");
    Serial.println("yellow");
  }
  else
  { // 대기 중 미세먼지가 매우 나쁨 일 때 빨간색 출력
    analogWrite(rgb_red, 255);
    analogWrite(rgb_green, 0);
    analogWrite(rgb_blue, 0);
    Serial.print("   ");
    Serial.println("red");
    Serial.println("Window Open");
    Serial1.println("W,");
    for (int i = 0; i < 10; i++)
    {
      myStepper.step(stepsPerRevolution);
      int h = dht.readHumidity();
      int t = dht.readTemperature();
      Serial.print("humidity:");
      Serial.println(h);
      Serial.print("temperature:");
      Serial.println(t);
    }
    delay(10000); //10초 대기(대기일땐 모든 상태를 멈춤)
    for (int i = 0; i < 10; i++) {
      myStepper.step(-stepsPerRevolution);
      int h = dht.readHumidity();
      int t = dht.readTemperature();
      Serial.print("humidity:");
      Serial.println(h);
      Serial.print("temperature:");
      Serial.println(t);
    }

  }

  delay(1000);

}
