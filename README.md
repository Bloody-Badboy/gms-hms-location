<h1 align="center">GMS-HMS Location Engine</h1>

<p align="center">
A library to request location update for both GMS and HMS with less boilerplate code.
</p>

---

# Usages

### Gradle
Add below codes to your **root** `build.gradle` file (not your module build.gradle file).
```gradle
allprojects {
    repositories {
        mavenCentral()
    }
}
```
And add a dependency code to your **module**'s `build.gradle` file.
```gradle
dependencies {
    implementation 'dev.arpan:location-engine:1.0.1'
}
```

Create `LocationRequest` object
```kotlin
private val locationRequest = LocationRequest().apply {
    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    interval = 20000L // default is 10000
    fastestInterval = 10000L // default is (10000 / 6)
}
```

Initialize `LocationFetcher` in your activity or fragment
```kotlin
 private val fetcher: LocationFetcher by lazy {
        LocationFetcher(
            context = this,
            request = locationRequest, // location request
            receiver = object : LocationReceiver {
                override fun onReceived(location: Location) {
                   // TODO: do something with location
                }
            }
        )
    }
```

Start `LocationSettingsActivity` to check location permission and device GPS settings to request location update
```kotlin
startActivityForResult(
                LocationSettingsActivity.buildIntent(
                    this,
                    locationRequest
                ), 100)
```

check the result in `onActivityResult` and start location update
```kotlin
 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100){
            when (resultCode) {
                RESULT_OK -> {
                    // start requesing locatoin updates
                    fetcher.startLocationUpdates()
                }
                LocationSettingsActivity.RESULT_PERMISSION_DENIED -> {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
                LocationSettingsActivity.RESULT_PERMANENTLY_PERMISSION_DENIED -> {
                    Toast.makeText(this, "Location permission permanently denied", Toast.LENGTH_SHORT)
                        .show()
                }
                LocationSettingsActivity.RESULT_GPS_NOT_ENABLED -> {
                    Toast.makeText(this, "GPS not enabled", Toast.LENGTH_SHORT).show()
                }
            }
       }
    }
```

## Found this useful? :heart:
Support it by joining __[stargazers](https://github.com/Bloody-Badboy/gms-hms-location/stargazers)__ for this repository. :star: <br>

# License
```xml
Copyright 2021 Arpan Sarkar

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```