### Serial Connector

Приложение-сервис для чтения данных по серийному порту.


## Возможности
Подключение:
  - [x] USB
  - [x] Bluetooth
  - [ ] WebSocket

## Настройки


### Общие

* **`Автозапуск при загрузке системы`** - сервис будет запущен при загрузке системы.

* **`Задержка запуска после загрузки системы`** - задержка в секундах, после которого будет запущен
сервис. Будет полезна, если нужно дать время для инициализации иных Ваших скриптов и/или устройств.

* **`Перезапуск при включении экрана`** - останавливает и запускает сервис при включении экрана.

* **`Задержка перезапуска после включения экрана`** - задержка в секундах, перед перезапуском сервиса.

* **`Завершить при отключении экрана`** - нужно ли завершать работу сервиса, при блокировке экрана.

### Сообщения

* **`Завершающий символ`** - символ в конце сообщения, который будет означать конец сообщения. Иногда
(в основном при соединении по Bluetooth 2.1) сообщения приходят частями. Например, мы отправляем строку
`Привет, мир!`, но данная строка может быть получена двумя (или больше) сообщениями: сначала `Привет,`
затем ` мир!`. В данном случае сервис получит два сообщения и создаст два события. Указав
**`Завершающий символ`**, сервис будет создавать событие о получении сообщения только если этот символ
присутствует в сообщении. При этом, остальные части сообщения накапливаются в буфере и не будут
утеряны.  
*Указанный символ будет удалён из сообщения.*  
Данная опция интерпретируется как регулярное выражение (RegExp), т.е. `\n` означает перенос строки,
`\n\r` - перенос строки и возврат каретки.  
Пустое поле отключает проверку завершаюшего символа.  
Например, если Arduino отправляет такое сообщение:  `inside:23|outside:30` и `Завершающий символ`
указан как прямой слэш `|`, то сервисом будет создано два события: первое `inside:23`, второе
`outside:30`.

* **`Максимальная длина сообщения`** - ограничивает максимальную длину сообщения, которое будет принято
от микроконтроллера. Опция добавлена из-за следущего выявленного поведения: при отключении
USB-устройства (Arduino) иногда приходят *мусорные* данные достаточно большого объёма (обычно больше
2048 символов) вследствии чего сервис зависает. Ограничение размера принимаемого сообщения позволяет
избежать зависаний.


### USB соединение

* **`Скорость соединения`** (baud rate) - скорости обмена данными. Для асинхронного режима принят ряд
стандартных скоростей обмена: 4800, 9600, 19200, 38400, 57600 и 115200 бод.

* **`Биты данных`** (data bits) - Большинство последовательных портов использует от пяти до восьми битов
данных. Двоичные данные обычно передаются как восемь битов.

* **`Бит чётности`** (parity) - Многие реализации UART имеют возможность автоматически контролировать
целостность данных методом контроля битовой чётности. Когда эта функция включена, последний бит
данных в минимальной посылке («бит чётности») контролируется логикой UART и содержит информацию о
чётности количества единичных бит в этой минимальной посылке.  
`None` - Бит чётности отсутствует.  
`Odd` - Биты данных плюс бит чётности приводят к нечётному числу 1 с.  
`Even` - Биты данных плюс бит чётности приводят к чётному числу 1 с.  
`Mark` - Бит чётности всегда равняется 1.  
`Space` - Бит чётности всегда 0.

* **`Стоповый бит`** (stop bit) - указывает, когда байт данных был передан.  
`1` - один бит.  
`1.5` - полтора бита (длительность стопового интервала).  
`2` - два бита.

* **`Фильтр по названию`** - позволяет подключаться только к указанным устройствам. Будет полезна,
если у Вас подключено несколько устройств с одиннаковыми VID и PID и Вы хотите, чтобы Serial Connector
подключался только к определённым устройствам.  
Указанное значение будет интерпретировано как регулярное выражение (RegExp), что позволяет гибко
настроить фильтр.  
`^/dev/bus/usb/` - поключаться к устройствам, путь которых *начинается* с /dev/bus/usb/  
`^/dev/bus/usb/00[0-2]` - поключаться к устройствам, путь которых *начинается* с /dev/bus/usb/000,
/dev/bus/usb/001, /dev/bus/usb/002  
`usb/00[3,5]` - поключаться к устройствам, путь которых *содержит* usb/003 или usb/005  


### Bluetooth соединение

* **`Bluetooth устройство`** - список спаренных устройств.

* **`Протокол Low Energy`** - использовать протокол Low Energy при подключении к указанному
устройству.  
Если используете модуль HC-05 или HC-06 то данная опция должна быть **отключена**.  
Если используете модули Bluetooth версии 4.0 и выше (маркируются по разному: AT-09, SH-HC-08, HC-10,
все c маркировкой BLE), то данную опцию необходимо **включить**. Обязательно проверьте
**`UUID сервиса`** и **`UUID характеристики`**.

* **`UUID сервиса`** и **`UUID характеристики`** - узнать эти UUID можно с помощью 
[Bluetooth LE Scanner](https://play.google.com/store/apps/details?id=uk.co.alt236.btlescan) или
иными способами. У разных производителей скорее всего разные UUID.  
<img src="https://github.com/delletenebre/serial_connector/raw/master/images/bluetooth_le_uuids.png" width="240">

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


## Благодарности
* Спасибо Константину ([kkostyann](https://www.drive2.ru/users/kkostyann)) за помощь и выявление
багов.
* Спасибо всему сообществу [PCCar.ru](http://pccar.ru/)
* Спасибо сообществу [DRIVE2.RU](https://www.drive2.ru/)
* Благодарю всех за поддержку которую вы мне оказываете, несмотря на то, что некоторые проблемы
решаются долгое время.


## Библиотеки
* [felHR85/UsbSerial](https://github.com/felHR85/UsbSerial)
* [douglasjunior/AndroidBluetoothLibrary](https://github.com/douglasjunior/AndroidBluetoothLibrary)


## Альтернативы
* [Serial Manager](https://github.com/delletenebre/SerialManager)
* [Serial Manager 2](https://github.com/delletenebre/SerialManager2)
* [Remote Inputs Manager / Remote steering wheel control](http://forum.xda-developers.com/showthread.php?t=2635159)
