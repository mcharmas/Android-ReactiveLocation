ReactiveLocation library for Android
====================================

Small library that wraps Google Play Services API in brilliant [RxJava](https://github.com/ReactiveX/RxJava)
```Observables``` reducing boilerplate to minimum.

Current stable version - 2.1
---------------

**This version works with Google Play Services 11+ and RxJava 2.+**

Artifact name: ```android-reactive-location2```

RxJava1 stable version - 1.0
--------------
**RxJava1 version:**

Artifact name: ```android-reactive-location```

What can you do with that?
--------------------------

* easily connect to Play Services API
* obtain last known location
* subscribe for location updates
* use location settings API
* manage geofences
* geocode location to list of addresses
* activity recognition
* use current place API
* fetch place autocomplete suggestions

How does the API look like?
----------------------------

Simple. All you need is to create ```ReactiveLocationProvider``` using your context.
All observables are already there. Examples are worth more than 1000 words:


### Getting last known location

```java
ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);
locationProvider.getLastKnownLocation()
    .subscribe(new Consumer<Location>() {
        @Override
        public void call(Location location) {
            doSthImportantWithObtainedLocation(location);
        }
    });
```

Yep, Java 8 is not there yet (and on Android it will take a while) but there is
absolutely no Google Play Services LocationClient callbacks hell and there is no
clean-up you have to do.

### Subscribing for location updates

```java
LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
                                  .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                  .setNumUpdates(5)
                                  .setInterval(100);

ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);
Subscription subscription = locationProvider.getUpdatedLocation(request)
    .filter(...)    // you can filter location updates
    .map(...)       // you can map location to sth different
    .flatMap(...)   // or event flat map
    ...             // and do everything else that is provided by RxJava
    .subscribe(new Consumer<Location>() {
        @Override
        public void call(Location location) {
            doSthImportantWithObtainedLocation(location);
        }
    });
```

When you are done (for example in ```onStop()```) remember to unsubscribe.

```java
subscription.unsubscribe();
```

### Subscribing for Activity Recognition

Getting activity recognition is just as simple

```java

ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);
Subscription subscription = locationProvider.getDetectedActivity(0) // detectionIntervalMillis
    .filter(...)    // you can filter location updates
    .map(...)       // you can map location to sth different
    .flatMap(...)   // or event flat map
    ...             // and do everything else that is provided by RxJava
    .subscribe(new Consumer<ActivityRecognitionResult>() {
        @Override
        public void call(ActivityRecognitionResult detectedActivity) {
            doSthImportantWithObtainedActivity(detectedActivity);
        }
    });
```

### Reverse geocode location

Do you need address for location?

```java
Observable<List<Address>> reverseGeocodeObservable = locationProvider
    .getReverseGeocodeObservable(location.getLatitude(), location.getLongitude(), MAX_ADDRESSES);

reverseGeocodeObservable
    .subscribeOn(Schedulers.io())               // use I/O thread to query for addresses
    .observeOn(AndroidSchedulers.mainThread())  // return result in main android thread to manipulate UI
    .subscribe(...);
```

### Geocode location

Do you need address for a text search query?

```java
Observable<List<Address>> geocodeObservable = locationProvider
    .getGeocodeObservable(String userQuery, MAX_ADDRESSES);

geocodeObservable
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(...);
```

### Managing geofences

For geofence management use `addGeofences` and `removeGeofences` methods.

### Checking location settings though location settings API

To get ```LocationSettingsResponse``` for your ```LocationRequest``` check
out ```ReactiveLocationProvider.checkLocationSettings()``` method. Sample
usage can be found in **sample** project in ```MainActivity``` class.

### Connecting to Google Play Services API

If you just need managed connection to Play Services API
use ```ReactiveLocationProvider.getGoogleApiClientObservable()```.
On subscription it will connect to the API.
Unsubscription will close the connection.

### Creating observable from PendingResult

If you are manually using Google Play Services and you are dealing with
```PendingResult``` you can easily transform them to observables with
```ReactiveLocationProvider.fromPendingResult()``` method.

### Transforming buffers to observable

To transform any buffer to observable and autorelease it on unsubscription
use ```DataBufferObservable.from()``` method. It will let you easily flatMap
such data as ```PlaceLikelihoodBuffer``` or ```AutocompletePredictionBuffer```
from Places API. For usage example see ```PlacesActivity``` sample.

### Places API

You can fetch current place or place suggestions using:

* ```ReactiveLocationProvider.getCurrentPlace()```
* ```ReactiveLocationProvider.getPlaceAutocompletePredictions()```
* ```ReactiveLocationProvider.getPlaceById()```

For more info see sample project and ```PlacesActivity```.

### Cooler examples

Do you need location with certain accuracy but don't want to wait for it more than 4 sec? No problem.

```java
LocationRequest req = LocationRequest.create()
                         .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                         .setExpirationDuration(TimeUnit.SECONDS.toMillis(LOCATION_TIMEOUT_IN_SECONDS))
                         .setInterval(LOCATION_UPDATE_INTERVAL);

Observable<Location> goodEnoughQuicklyOrNothingObservable = locationProvider.getUpdatedLocation(req)
            .filter(new Func1<Location, Boolean>() {
                @Override
                public Boolean call(Location location) {
                    return location.getAccuracy() < SUFFICIENT_ACCURACY;
                }
            })
            .timeout(LOCATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS, Observable.just((Location) null), AndroidSchedulers.mainThread())
            .first()
            .observeOn(AndroidSchedulers.mainThread());

goodEnoughQuicklyOrNothingObservable.subscribe(...);
```


How to use it?
--------------

Library is available in maven central.

### Gradle

Just use it as dependency in your *build.gradle* file
along with Google Play Services and RxJava.

```groovy
dependencies {
    ...
    compile 'pl.charmas.android:android-reactive-location2:2.1@aar'
    compile 'com.google.android.gms:play-services-location:11.0.4' //you can use newer GMS version if you need
    compile 'com.google.android.gms:play-services-places:11.0.4'
    compile 'io.reactivex:rxjava:2.0.5' //you can override RxJava version if you need
}
```

### Maven

Ensure you have android-maven-plugin version that support **aar** archives and add
following dependency:

```xml
<dependency>
    <groupId>pl.charmas.android</groupId>
    <artifactId>android-reactive-location2</artifactId>
    <version>2.1</version>
    <type>aar</type>
</dependency>
```

It may be necessary to add google play services and rxanroid dependency as well.

Sample
------

Sample usage is available in *sample* directory.

Places API requires API Key. Before running samples you need to create project on API console
and obtain API Key using this [guide](https://developers.google.com/places/android/signup).
Obtained key should be exported as gradle property named: ```REACTIVE_LOCATION_GMS_API_KEY``` for
example in ```~/.gradle/gradle.properties```.


References
------

If you need Google Fit library rxified please take a look at [RxFit](https://github.com/patloew/RxFit).

License
=======

    Copyright (C) 2015 Michał Charmas (http://blog.charmas.pl)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	     http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
