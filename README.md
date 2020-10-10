### Serial Connector

Приложение-сервис для чтения данных по серийному порту.

## Возможности
Подключение:
  - [x] USB
  - [ ] Bluetooth
  - [ ] WebSocket

## Настройки

### USB

* `Скорость соединения` (baud rate) - скорости обмена данными. Для асинхронного режима принят ряд
стандартных скоростей обмена: 4800, 9600, 19200, 38400, 57600 и 115200 бод.

* `Биты данных` (data bits) - Большинство последовательных портов использует от пяти до восьми битов
данных. Двоичные данные обычно передаются как восемь битов.

* `Бит чётности` (parity) - Многие реализации UART имеют возможность автоматически контролировать
целостность данных методом контроля битовой чётности. Когда эта функция включена, последний бит
данных в минимальной посылке («бит чётности») контролируется логикой UART и содержит информацию о
чётности количества единичных бит в этой минимальной посылке.  
`None` - Бит чётности отсутствует.  
`Odd` - Биты данных плюс бит чётности приводят к нечётному числу 1 с.  
`Even` - Биты данных плюс бит чётности приводят к чётному числу 1 с.  
`Mark` - Бит чётности всегда равняется 1.  
`Space` - Бит чётности всегда 0.

* `Стоповый бит` (stop bit) - указывает, когда байт данных был передан.  
`1` - один бит.  
`1.5` - полтора бита (длительность стопового интервала).  
`2` - два бита.

* `Фильтр по названию` - позволяет подключаться только к указанным устройствам. Будет полезна,
если у Вас подключено несколько устройств с одиннаковыми VID и PID и Вы хотите, чтобы Serial Connector
подключался только к определённым устройствам.  
Указанное значение будет интерпретировано как регулярное выражение (RegExp), что позволяет гибко
настроить фильтр.  
`^/dev/bus/usb/` - поключаться к устройствам, путь которых *начинается* с /dev/bus/usb/  
`^/dev/bus/usb/00[0-2]` - поключаться к устройствам, путь которых *начинается* с /dev/bus/usb/000,
/dev/bus/usb/001, /dev/bus/usb/002  
`usb/00[3,5]` - поключаться к устройствам, путь которых *содержит* usb/003 или usb/005

* `Максимальная длина сообщения` - ограничивает максимальную длину сообщения, которое будет принято
от микроконтроллера. Опция добавлена из-за следущего выявленного поведения: при отключении
USB-устройства (Arduino) иногда приходят *мусорные* данные достаточно большого объёма (обычно больше
2048 символов) вследствии чего сервис зависает. Ограничение размера принимаемого сообщения позволяет
избежать зависаний.

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
    * `name`: название устройства, например `/dev/bus/usb/001/002`
    
* При потере соединения (USB):
  * Action: `kg.delletenebre.serialconnector.ACTION_CONNECTION_LOST`
  * Extras:
    * `connectionType`: `usb`
    * `name`: название устройства, например `/dev/bus/usb/001/002`
    
* При получении данных от устройства (USB):
  * Action: `kg.delletenebre.serialconnector.ACTION_DATA_RECEIVED`
  * Extras:
    * `connectionType`: `usb`
    * `name`: название устройства, например `/dev/bus/usb/001/002`
    * `data`: полученные данные


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
