VdoTok-Android-Doctor-Application 
========================================= 

Note
==========
* Move to <b>"Code setup for Doctor App"</b> if <b>GIT</b> and <b>Android Studio</b> are already installed.

Git Installation 
============================== 
* Please follow this [link](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git) for GIT installation 
 
IDE Installation & Build Guide 
============================== 
* Android Studio 4.1.2 or later (Stable Version) 
* [Click here](https://developer.android.com/studio?gclid=Cj0KCQjwhr2FBhDbARIsACjwLo2fEHdB3l3eqRlhIvySYNx1-3XjDmuX1eSCbaCI7zU8FKHFkGBcVyMaAtSjEALw_wcB&gclsrc=aw.ds#downloads) to download and install Android Studio 

<b>Watch Connectivity setup:</b> 
* Install WearOS application from playstore for android application. 
* Download it from [here.](https://play.google.com/store/apps/details?id=com.google.android.wearable.app&hl=en&gl=US)
* Follow the instructions to connect your wear with mobile phone and complete the connectivity setup. 
* Make sure that you wear is connected to mobile phone.  
 
<b>Code setup for Doctor App:</b> 
* Shift to <b>"doctorApp"</b> branch from Github interface.

<img width="516" alt="image" src="https://user-images.githubusercontent.com/88875529/159257285-3da9f6d5-3ad2-4de2-89a7-b345119a5b86.png">

* From the <b>"Code"</b> option click on <b>"Download ZIP"</b> to download doctor application only code.

</br><img width="483" alt="image" src="https://user-images.githubusercontent.com/88875529/159257361-63681d4e-50b7-4d09-b2ce-3902b42aa02b.png">

* Unzip the downloaded file to import it in android studio.
* Use <b>"Open Project"</b> if you'r on the Welcome page of android studio.
* If you'r not on Welcome page click on <b>File -> New -> Import Project</b> and select the downloaded project folder.
* Let Android Studio install the components and finish importing the project files.
 
Bravo! You’ve successfully configured the project in Android Studio. 

<b>Download Connect Lib:</b> 
* Go to: https://sdk.vdotok.com/Android-SDKs/ and download “<b>connect.aar</b>” file 
 
<b>Configure Lib:</b> 
* In <b> Android Studio File Explorer</b>, select <b>Project</b> 
<img width="498" alt="123636395-41e08980-d836-11eb-8643-429d6e5510d5" src="https://user-images.githubusercontent.com/86282129/123811571-cb628b00-d90c-11eb-9584-b5a8f12957dc.png"> 
 
* Go to <b>ConnectAppProject -> DoctorApp -> libs</b> 
* Add the downloaded <b>connect.aar</b> file in the libs folder 
<img width="243" alt="Screenshot 2021-12-02 at 4 51 28 PM" src="https://user-images.githubusercontent.com/91589156/159227453-de05505e-37c7-4f16-aed5-6b2da5767630.jpg"> 
<li> After this, click on gradle sync icon from the toolbar 
<img width="21" alt="Screenshot 2021-12-02 at 4 43 51 PM" src="https://user-images.githubusercontent.com/88875529/144415902-78883f01-f5be-4f99-a6e3-d9ea44a71936.png"> to sync project.</li></br> 

<b>Project Signup:</b> 
* Register your account on [VdoTok](https://www.vdotok.com/). After creating an account, [login](https://console.vdotok.com/) to get the <b>Project ID</b> and <b>API Service URL</b>
* From file explorer, double-click on <b>ConnectAppProject -> DoctorApp  -> src -> main -> java -> com -> vdotok -> connectApp -> utils -> ApplicationConstants</b> replace <b>SDK_PROJECT_ID</b> with your own <b>Project Id</b> and <b>BASE_URL</b> with your own <b>API Service URL</b> 
 
<b>Device Setting:</b> 
* To connect a device, enable <b>“developer mode”</b> and <b>“USB debugging”</b> by following the device-specific steps provided [here.](https://developer.android.com/studio/debug/dev-options)
* Some devices need <b>“Install Via USB”</b> option below the <b>“USB debugging”</b> settings to be enabled in order to install the application in the device via USB.
* Click [here](https://support.mobiledit.com/portal/en/kb/articles/how-to-enable-usb-debugging) for additional device specific settings.
 
<b>Build Project:</b> 
* Connect your phone with system in a <b>File-sharing Mode</b> 
* You can find your phone name in running devices list, as described in the below image 
* Select your device and click on <b>Play</b> button<img width="24" alt="Screenshot 2021-09-21 at 1 19 15 PM" src="https://user-images.githubusercontent.com/86282129/134136764-72c0f47e-6ecb-4c62-a562-804b68042fe5.png"> 
* After running some automated commands and building gradle, your app will be installed on your connected device 
  <img width="1012" alt="Screenshot 2021-06-29 at 6 59 36 PM" src="https://user-images.githubusercontent.com/86282129/123811062-5bec9b80-d90c-11eb-96e1-ee50dee125c5.png"> 
