ReactiveLocation library for Android
====================================

Small library that wraps Google Play Services API in brilliant [RxJava](https://github.com/Netflix/RxJava)
```Observables``` reducing boilerplate to minimum.

**This library is still in its early beta.**

What can you do with that?
--------------------------

* obtain last known location
* subscribe for location updates
* manage geofences
* geocode location to list of addresses

How does the API look like?
----------------------------

Simple. All you need is to create ```ReactiveLocationProvider``` using your context.
All observables are already there. Examples are worth more than 1000 words:


### Getting last known location

    ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);
    locationProvider.getLastKnownLocation()
        .subscribe(new Action1<Location>() {
            @Override
            public void call(Location location) {
                doSthImportantWithObtainedLocation(location);
            }
        });

Yep, Java 8 is not there yet (and on Android it will take a while) but there is
absolutely no Google Play Services LocationClient callbacks hell and there is no
clean-up you have to do.

### Subscribing for location updates


    LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
                                      .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                      .setNumUpdates(5)
                                      .setInterval(100);

    ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);
    Subscription subscription = locationUpdatesObservable = locationProvider.getUpdatedLocation(request)
        .filter(...)    // you can filter location updates
        .map(...)       // you can map location to sth different
        .flatMap(...)   // or event flat map
        ...             // and do everything else that is provided by RxJava
        .subscribe(new Action1<Location>() {
            @Override
            public void call(Location location) {
                doSthImportantWithObtainedLocation(location);
            }
        });

When you are done (for example in ```onStop()```) remember to unsubscribe.

    subscription.unsubscribe();

### Geocode location

Do you need address for location?

    Observable<List<Address> geocodeObservable = locationProvider
        .getGeocodeObservable(location.getLatitude(), location.getLongitude(), MAX_ADDRESSES);

    geocodeObservable
        .subscribeOn(Schedulers.io())               // use I/O thread to query for addresses
        .observeOn(AndroidSchedulers.mainThread())  // return result in main android thread to manipulate UI
        .subscribe(...);


### Managing geofences

For geofence management use ```addGeofences``` and ```removeGeofences``` methods.

### Cooler examples

Do you need location with certain accuracy but don't want to wait for it more than 4 sec? No problem.

    LocationRequest req = LocationRequest.create()
                             .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                             .setExpirationDuration(TimeUnit.SECONDS.toMillis(LOCATION_TIMEOUT_IN_SECONDS)
                             .setInterval(LOCATION_UPDATE_INTERVAL);

    Observable<Location> goodEnoughQuicklyOrNothingObservable = locationProvider.getUpdatedLocation(req)
                .filter(new Func1<Location, Boolean>() {
                    @Override
                    public Boolean call(Location location) {
                        return location.getAccuracy() < SUFFICIENT_ACCURACY;
                    }
                })
                .timeout(LOCATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS, Observable.from((Location) null), AndroidSchedulers.mainThread())
                .first()
                .observeOn(AndroidSchedulers.mainThread());

    goodEnoughQuicklyOrNothingObservable.subscribe(...);


How to use it?
--------------

For now it is not available in Maven Central so you have to clone it, install it to
you local maven repo by ```gradlew build install``` and use it as dependency in your project
along with Google Play Services and RxJava.

    compile 'pl.charmas.android:android-reactive-location:0.1@aar'
    compile 'com.google.android.gms:play-services:4.1.32'
    compile 'com.netflix.rxjava:rxjava-android:0.16.1'



Sample
------

Sample usage is available in *sample* directory.

License
=======

    Copyright (C) 2014 Michał Charmas (http://blog.charmas.pl)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	     http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
