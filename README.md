### Serial Connector

Приложение-сервис для чтения данных по серийному порту.

## Возможности
- [ ] Подключение:
    - [x] USB
    - [ ] Bluetooth
    - [ ] WebSocket

## Настройки

### USB

`Фильтр по названию` - позволяет подключаться только к указанным устройствам. Указанное значение
будет интерпретировано как регулярное выражение (RegExp), что позволяет гибко настроить фильтр.
/dev/bus/usb/00[0-2] - поключаться к устройствам, путь которых содержит /dev/bus/usb/000,
/dev/bus/usb/001, /dev/bus/usb/002
/dev/bus/usb/00[3,5] - поключаться к устройствам, путь которых содержит /dev/bus/usb/003,
/dev/bus/usb/005

## Arduino → Serial Connector
Формат отправляемой команды: `<key:value>`

Пример простого скетча для ардуино:
```cpp
void setup() {
  Serial.begin(115200);
}

void loop() {
  int value = random(-15, 40);
  Serial.println("<temperature:" + String(value) + ">");
  delay(2000);
}
```

## Serial Connector → Android
Broadcast Intent'ы:
* При открытии соединения (USB):
  * Action: `kg.delletenebre.serialconnector.ACTION_CONNECTION_ESTABLISHED`
  * Extras:
    * `connectionType`: `usb`
    * `portName`: 


## Форматирование строк
Можно использовать следующие значения:
* `%key` - будет заменено на ключ текущей команды;
* `%value` - будет заменено на значение текущее команды;
* `hex2dec(x)`, `dec2hex(x)`, `bin2dec(x)`, `dec2bin(x)` - конвертирование `x` из одной системы
счисления в другую;
* `%{...}` - вместо ` ... ` нужно подставить математическое выражение (формулу). [Описание
доступных операторов и выражений](https://github.com/uklimaschewski/EvalEx#supported-operators).

Например, от контроллера приходят данные о температуре в Фаренгейтах, а нам нужно перевести в
градусы Цельсия. Для этого нужно написать:

`%{round((%value - 32) * (5 / 9), 1)} ºC`

По формуле выше, мы перевели градусы из Ф в С и округлили полученное значение до десятых (до одного
знака после запятой). Т.е. если от контроллера пришла цифра 89, то мы на выходе получим `31.7 ºC`.
При этом текст до и после `%{...}` остаётся без изменений.

Форматирование строк применяется:
* при действии команды `Отправить данные`;
* в уведомлении при распознавании команды;
* в создаваемом Intent'е при распознавании команды.


## Библиотеки
* [UsbSerial](https://github.com/felHR85/UsbSerial)
* [EvalEx](https://github.com/uklimaschewski/EvalEx)


## Альтернативы
* [Serial Manager](https://github.com/delletenebre/SerialManager)
* [Serial Manager 2](https://github.com/delletenebre/SerialManager2)
* [Remote Inputs Manager / Remote steering wheel control](http://forum.xda-developers.com/showthread.php?t=2635159)
