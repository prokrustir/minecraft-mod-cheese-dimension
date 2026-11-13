# Cheese Dimension (Fabric 1.21.1)

Mod dodaje serowy wymiar, portal z bloków sera oraz myszy, które w nim mieszkają.

## Jak zbudować i uruchomić

Wymagania: JDK 21 i dostęp do internetu (Gradle pobierze Minecrafta i biblioteki).

```
./gradlew runClient     # uruchamia grę z modem do testów
./gradlew build         # tworzy plik .jar w build/libs/
```

Na Windows użyj `gradlew.bat`. Gotowy `.jar` (bez `-sources`) wrzuć do folderu `mods` razem z Fabric API dla 1.21.1.

Jeśli Gradle narzeka na wersje, sprawdź aktualne wartości na https://fabricmc.net/develop i wpisz je w `gradle.properties` (`loader_version`, `fabric_version`) oraz w `build.gradle` (wersja pluginu Loom).

## Jak grać

1. Zrób ser: wiadro mleka daje 4 sery (receptura bezkształtna, wiadro wraca).
2. Z 4 serów (2x2) zrób blok sera. Nóż do sera: żelazo, ser, patyk w pionie.
3. Zbuduj ramę jak do portalu Netheru: bloki sera, wnętrze od 2x3 do 21x21.
4. Kliknij nożem do sera blok ramy od środka. Wnętrze zapali się żółtym portalem.
5. Wejdź i poczekaj kilka sekund (w trybie kreatywnym od razu).

Po drugiej stronie powstaje portal powrotny. Myszy pojawiają się na serowym terenie, idą za serem w ręce i rozmnażają się nim. Jajko myszy jest w zakładce „Serowy wymiar".

## Struktura

- `src/main/java/.../` kod: bloki, przedmioty, mysz, portal (`portal/CheesePortalHelper`), klient (`client/`)
- `src/main/resources/data/cheesedimension/` wymiar, typ wymiaru, generacja terenu i biom, receptury
- `src/main/resources/assets/cheesedimension/` modele, tekstury, tłumaczenia (en, pl)

## Wymiar

Teren generuje własny `noise_settings`: blok bazowy to ser, powierzchnia około y=90, w środku dziury jak w serze szwajcarskim. Biom `cheese_plains` ma tylko myszy jako moby.

## Co sprawdzić po pierwszym uruchomieniu

Nie mogłem tego skompilować ani uruchomić (środowisko bez dostępu do Gradle i Mojang), więc projekt jest napisany, ale niesprawdzony w grze. Jeśli kompilator zgłosi błąd, najbardziej prawdopodobne miejsca to:

- `CheesePortalBlock`: sygnatury z interfejsu `Portal` (`getPortalDelay`, `getPortalEffect`, `createTeleportTarget`) i konstruktor `TeleportTarget`
- `ModEntities`: `SpawnRestriction.register` (w razie błędu dostępu dodaj access widener)
- `MouseEntity.canSpawn`: typ `SpawnReason`
- pliki JSON generacji świata: błąd daje komunikat w logu przy wczytywaniu świata

Nazwy w kodzie to Yarn `1.21.1+build.3`.
