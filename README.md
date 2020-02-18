# Tracker App

A Zero-conf Location Tracker.

An Android Studio project.

## Design philosophy

The idea behing Android Tracker is to have minimal configuration while building the app and Zero configuration while deploying the devices in the field. The only configuration option needed in the app is the Tracker web-server locatation update POST endpoint and salt (to prevent rogue device updates).

The app and server were originally developed for a Dutch company and have never been properly translated.

## Configuration

Open ```app/src/main/res/values/strings.xml``` and update ```post_url``` to contain the URL of the post.php script in the Tracker webserver, e.g. ```https://tracker-web.service.tld/post.php```. Update the ```post_salt``` as well.

## Building

The App has been succesfully built using Android Studio 3.5.3. Target API is chosen as low as possible, so that cheap Android phones can be used to track vehicels.

## Companion Webserver

The App was designed to work together with the Companion webserver https://github.com/mrvanes/tracker-web

## Starting

The App is designed to be Zero-conf, which means no action is required after starting the app. It needs (hi accuracy) Location privileges, which are to be assigned manually in the Android Configuration, App Settings for the tracker app.

Once the app is started, it will try to find it's location and set up a network connection with the tracker webserver location update endpoint. As soon as this has succeeded the status icon will turn green. The status icon can be clicked to open a status windown showing debug information about the connection, status and device-id. The device-id is the unique identifier that will be used to upload status/location messages to the tracker webserver. The tracker webserver then needs to assign a vehicle to this device-id.

