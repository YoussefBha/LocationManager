# Android app for Location Track

This Android app use Gefences and service to get notify around a location 

The application use service in background that get the user notified once he's around a location added.

* **GoogleApiClient LocationService:**

To access Google APIs, we just need to perform one more step: create an instance of GoogleApiClient. The Google API Client provides a common entry point to all the Google Play services, and manages the network connection between the user’s device and each Google service.

```java
  private void connectWithCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
    googleApiClient = new GoogleApiClient.Builder(context)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(callbacks)
            .addOnConnectionFailedListener(connectionFailedListener)
            .build();
    googleApiClient.connect();
  }
  
