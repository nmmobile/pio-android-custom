![Push IO®](http://push.io/wp-content/uploads/2012/05/pushio_logo.png)

Copyright © 2009 - 2012 Push IO LLC. All Rights Reserved.
Push IO® is a registered trademark in the United States. All other trademarks are the property of their respective owners.

#### Software License Information
The use of any and all Push IO software regardless of release state is governed by the terms of a Software License Agreement and Terms of Use that are not included here but required by reference.

#### Privacy Information
The use of Push IO services is subject to various privacy policies and restrictions on the use of end user information. For complete information please refer to your Master Agreement, our website privacy policies, and/or other related documents.

#### Billing
For information on your account and billing, please contact sales@push.io

#### Contact Info
Push IO
1035 Pearl Street, Suite 400
Boulder, CO 80302 USA
303-335-0903
[support@push.io](mailto:support@push.io)
[www.push.io](http://www.push.io)

## Preface
Push IO is a leading provider of real-time push notification alerts and mobile data delivery.This document provides the necessary information to leverage Push IO for your mobile application. This document corresponds to the latest version of PushIOManager for Android.

## Using the Demo App
Push IO provides this app to demonstrate how to use the Push IO service with an Android device. This app consists of some switches to turn on and off push notifications. Additionally, the app creates custom Notifications in the Notification Manager when it is in the background but displays the Notifications directly when the main Activity of the app is in the foreground.

###App Elements
The Files below show examples of how an app might interact with the Push IO service.

####AndroidManifest.xml
In addition to the setting changes required by the [Integration Guide](http://docs.push.io/PushIOManager_Android/) there are a number of settings specific to this app. The Manifest file configures the ".AboutThisApp" Activity to respond to the user tapping on a Notification from the Notification Center. The main receiver for notifications is the ".NotificationBroadcastReceiver". This receiver lets the app add a custom drawable to the Notification center, when the app is in the background.

####PushSettings.java
This is the main Activity of the app. In the onCreate method the app will register with the Push IO servers if the user has enabled Notifications. In the onItemClickListener method the app will register to receive different types of notifications depending on which row is clicked. In the onResume method, the app will create a BroadcastReceiver to intercept any notifications that are sent to the app while this Activity is running. Notifications that are received while the Activity is running will be displayed at the bottom of the screen.

###NotificationBroadcastReceiver.java
This Activity will receive data from any Notifications that are sent when the main Activity is in the background. It creates a custom Notification item and places it in the Notification Manager.

###AboutThisApp.java
When a user taps on a Notification from the Notification Center this Activity will get launched and will display data from the Notification. This Activity can also get launched from the PushSettings Activity in which case it will not have a Notification.

###Using the App
Before launching the app, follow the basic instructions in the [Integration Guide](http://docs.push.io/PushIOManager_Android/) for setting up an Android application in the Push IO dashboard. The required JAR files are already in the Eclipse project. Replace the empty pushio_config.json file in the assets folder with the one that you downloaded from the Push IO dashboard.

After launching the app *on your device*, click on the "Notifications" checkbox to register the device with the Push IO server. At this time, you can push broadcast notifications to the app. You can either send the notifications from the Dashboard or by constructing RESTful calls and sending them from a command line. Here are some examples that send basic messages to this app. They are patterned from the [Push IO API Integration Guide](http://docs.push.io/PushIO_API/)
```
curl 'https://manage.push.io/api/v1/notify_app/$MY_APP_ID/$MY_Secret_Key' -d 'payload={"message":"Title of my Test","extra":{"details":"Yeah! Here are the details"}}&audience=broadcast'
```

After clicking on the "US News" or "Sports" checkbox the "Categories" will now appear in the Push IO dashboard. At this time, you can push notifications to subscribers of the categories. The example below will only be seen by people who are subscribed to the Sports category
```
curl 'https://manage.push.io/api/v1/notify_app/$MY_APP_ID/$MY_Secret_Key' -d 'payload={"message":"Isotopes Win!","extra":{"details":"Isotopes are on the way to Capital City"}}&tag_querySports'
```

If you need further assistance, please visit our [support site](http://pushio.zendesk.com).

