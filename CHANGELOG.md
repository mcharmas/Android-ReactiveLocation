Change Log
==========

Version 0.10
----------------------------
* Updated dependencies
* Added permission annotations
* Minor improvements and cleanups in sample app

Version 0.8.1
----------------------------
* Updated dependencies
* Added fallback reverse geocode observable that uses web apis to obtain address
* Added API to pass locales for reverse geocoding

Version 0.8
----------------------------
* Updated dependencies to Google Play Services 8.1

Version 0.7
----------------------------

* Removed ```final``` from methods in ```ReactiveLocationProvider``` to enable mockito mocking.
* Updated dependencies.
* Added support to fetch Place by id.

Version 0.6
----------------------------

* Added support for mock locations.
* Corrected geocode to reverse geocode and proper geocode implemented.
* Added support for PendingIntent location updates.
* Simplified and unified observables that were based on status now return status instead of custom responses.
* Updated to newest Play Services version.

Version 0.5
----------------------------

* Fix: now last known location observable when location is disabled emits nothing and completes (instead of null).
* Added Places API support from Google Play Services 7.0.0
* Added Location Settings API from Google Play Services 7.0.0
* Added support for obtaining connection to Google Play Services though observable.
* Added utils to handle ```PendingResponse``` and ```Buffers``` from Google Play Services

Version 0.4
----------------------------

Sorry - no changelog history here.
