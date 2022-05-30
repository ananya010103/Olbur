# Olbur
Project

README:
=======

Problem Statement:
------------------
The chosen problem topic is "solving crimes through face detection".
The specific problem statement is that quite often the cab drivers who were allocated by aggregator apps and the actual drivers who show up are quite different. But for the end-user or consumer of the mobile app does not have any convenient way to ascertain the above uncertainty. To solve this, I have created a separate mobile app (which can also integrated as APIs with existing ones) to verify if the driver who has arrived and the allocated driver are one and the same by using Azure Face APIs from Cognitive Services. 

Solution:
---------
1. Drivers photographs are captured in a central database of the organization
2. The picture is presented to the users first, in addition to the name
3. If they suspect that the driver's face looks different, they can optionally choose to click a picture of the driver from the app itself 
4. This picture is uploaded to a public bucket and verified against the already registered picture using Azure Face APIs
5. On successful verification, the user can continue with the ride without hassles
6. If the app reports failure, user can report this as a potential issue and the cab aggregator organization can investigate further

Supported Platform:
-------------------
The mobile app is currently supported only on Android 8+


Technology stack & tools used:
-----------------
- Android Studio v 3.1.26
- Java(JRE v1.8)
- Face API v1.0
- JSON
- Firebase Realtime Database (some issue occurred due to which this was not used later)
- Postman

Installation:
-------------
- The APK is unsigned. Hence, Play Protected settings need to be turned OFF


Creation of master data:
------------------------
1. Create person groups, persons and add face for that person using Postman (as per Azure Face API 1.0 docs)
2. Upload the corresponding master photo for every person also using Postman
3. Modify the database.json file under Assets folder to add the ID, Name and vehicle number 

Features of the application:
----------------------------
1. Check the name of the alloted driver
2. Take a picture of the driver 
3. Verify if the picture taken and registered picture match
4. As the next step, users can also report if pictures do not match 

Debugging any potential errors:
-------------------------------
1. Ensure that location is turned ON and date/time is auto-synchronised
2. The app will request for camera permissions and that is required to capture picture, deny it will disable the app features
3. The picture taken is uploaded to Firebase dynamically, ensure that Internet is turned on during the usage of the app for the same

