ФИО: Еремеев Александр Николаевич
Группа: Б9123-09.03.01 цд
Приложение PokeDex построено на базе PokeAPI (https://pokeapi.co/).
PokeAPI - это бесплатный RESTful API, который предоставляет полную информацию о покемонах из всех поколений игр Pokemon. API возвращает данные о:
1. Списке всех покемонов (имя, ID, изображение)
2. Детальной информации о каждом покемоне (характеристики, способности, типы, рост, вес)
3. Спрайтах и официальных артворках покемонов
4. Эволюциях, движениях и других игровых данных

Инструкция по запуску:

1. Клонировать репозиторий
git clone <repository-url>
  cd pokedex
   
2. Открыть проект в Android Studio
File → Open → выбрать папку проекта

3.Синхронизировать Gradle
Android Studio автоматически предложит синхронизировать
Или вручную: File → Sync Project with Gradle Files

4.Запустить приложение
Нажать кнопку "Run" (зеленый треугольник)
Выбрать эмулятор или подключенное устройство

API ключ не требуется - PokeAPI является полностью открытым и бесплатным сервисом без необходимости регистрации или получения ключей доступа.

<img width="359" height="624" alt="image" src="https://github.com/user-attachments/assets/5469b614-625d-4741-b0ed-1a656f08f543" /> (Скриншот Loading)

<img width="354" height="627" alt="image" src="https://github.com/user-attachments/assets/b24f6b97-6484-43c4-b332-3fe7a0fa24cd" /> (Скриншот Listscreen)

<img width="358" height="599" alt="image" src="https://github.com/user-attachments/assets/0792d778-9b26-4b39-8dd3-ce5a9dbc9f56" /> (Скриншот Detail)

<img width="351" height="600" alt="image" src="https://github.com/user-attachments/assets/57afc98d-f534-46b8-a0ed-516d8f145016" /> (Скриншот Favourite)

<img width="352" height="625" alt="image" src="https://github.com/user-attachments/assets/18d00956-2e38-4732-9342-5cf3df83e42a" /> (Скриншот Error, с кнопкой Retry)


Чеклист
Обязательное:
 3 экрана: List, Detail (с ID в route), Favourites
 UiState (Loading, Success, Error, Empty)
 ViewModel + viewModelScope
 Stateless UI (state + onEvent)
 Repository между ViewModel и Retrofit
 Suspend функции для сетевых запросов
 Все UI состояния реализованы
 Избранное (add/remove)
 Избранное сохраняется при повороте экрана
 Compose + Material3
 Navigation Compose
 Retrofit + kotlinx.serialization
 Debounce поиска (Job + delay 300ms)

Бонусы:
 Экран Favourites как отдельный route
 Кнопка Refresh
 Кэш результата в памяти (Repository)
 Логирование запросов (OkHttp logging interceptor)

Примечания:
Для работы приложения требуется интернет-соединение
При первом запуске приложение загрузит список первых 151 покемона (первое поколение)
Все запросы логируются в Logcat (тег: OkHttp)
