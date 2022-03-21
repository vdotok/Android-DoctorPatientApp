
VdoTok-Android-Patient-Application 
========================================= 

Note
==========
* Move to <b>"Code setup for Patient App"</b> if <b>GIT</b> and <b>Android Studio</b> are already installed.

Git Installation 
============================== 
* Please follow this [link](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git) for GIT installation 
 
IDE Installation & Build Guide 
============================== 
* Android Studio 4.1.2 or later (Stable Version) 
* [Click here](https://developer.android.com/studio?gclid=Cj0KCQjwhr2FBhDbARIsACjwLo2fEHdB3l3eqRlhIvySYNx1-3XjDmuX1eSCbaCI7zU8FKHFkGBcVyMaAtSjEALw_wcB&gclsrc=aw.ds#downloads) to download and install Android Studio 
* Android SDKs 21(Lollipop) or later 
  * For downloading Android SDKs, open <b>Android Studio</b> 
  * While on the Welcome page, open <b>SDK Manager</b> 
  * On Android Studio 4.1 or onward, click on configure <img width="100" alt="Screenshot 2021-09-21 at 12 40 27 PM" src="https://user-images.githubusercontent.com/86282129/134131257-af72db7c-912d-47f3-9758-4de0479db9ca.png"> from the right bottom and select <b>SDK Manager</b> 
  * For Android Studio 4.2 or onward, click on three dots <img width="19" alt="Screenshot 2021-09-21 at 12 35 14 PM" src="https://user-images.githubusercontent.com/86282129/134130491-4f77bf35-a845-4a07-b577-bb4f7df1195a.png"> from the top right corner and select <b>SDK Manager</b> 
  * In case the Welcome page doesn't open, go to file menu <b>Tools -> SDK Manager</b> 
  * Click all checkboxes from <b>API Level 21 and onward</b> 
  * Click <b>Apply</b> and download relevant packages 

<b>Watch Connectivity setup:</b> 
* Install WearOS application from playstore for android application. 
* Download it from [here.](https://play.google.com/store/apps/details?id=com.google.android.wearable.app&hl=en&gl=US)
* Follow the instructions to connect your wear with mobile phone and complete the connectivity setup. 
* Make sure that you wear is connected to mobile phone.  
 
<b>Code setup for Patient App:</b> 
* Open <b>Android Studio</b> 
* Click on <b>Get from Version Control</b> or in case the welcome page doesn't open, go to <b>File Menu->New</b> and select <b>Project From Version Control</b> 
* Select <b>Repository URL</b> from left menu 
* Select <b>Git</b> from <b>Version control</b> dropdown menu 
* Paste this URL https://github.com/vdotok/Android-DoctorPatientApp.git in URL section 
* Click on <b>Clone</b> button and wait for build.gradle file. You can see the progress at the bottom of <b>Android Studio</b> 
* Let the <b>Android Studio</b> install the components 
 
Bravo! You’ve successfully configured the project in Android Studio. 
 
<b>Download Connect Lib:</b> 
* Go to: https://sdk.vdotok.com/Android-SDKs/ and download “<b>connect.aar</b>”  and “<b>wearSDK.aar</b>” files 
 
<b>Configure Lib:</b> 
* In <b> Android Studio File Explorer</b>, select <b>Project</b> 
<img width="498" alt="123636395-41e08980-d836-11eb-8643-429d6e5510d5" src="https://user-images.githubusercontent.com/86282129/123811571-cb628b00-d90c-11eb-9584-b5a8f12957dc.png"> 
 
* For <b>connect.aar</b> go to <b>DoctorPatientApp -> connectApp -> libs</b>,add the downloaded <b>connect.aar</b> file in the libs folder 
<img width="281" alt="Screenshot 2022-03-21 at 1 14 34 PM" src="https://user-images.githubusercontent.com/91589156/159226560-017f8dd6-ee5e-4ac0-a500-813c5d112693.png">
* For <b>wearSDK.aar</b> go to <b>DoctorPatientApp -> wearApp -> libs</b>,add the downloaded <b>wearSDK.aar</b> file in the libs folder 
<img width="281" alt="Screenshot 2022-03-21 at 1 14 34 PM" src="https://user-images.githubusercontent.com/91589156/159226814-bdacc4f3-4079-4a99-b2f2-a7b280d8979f.jpg">
<li> After this, click on gradle sync icon from the toolbar 
<img width="21" alt="Screenshot 2021-12-02 at 4 43 51 PM" src="https://user-images.githubusercontent.com/88875529/144415902-78883f01-f5be-4f99-a6e3-d9ea44a71936.png"> to sync project.</li></br> 

<b>Project Signup:</b> 
* Register your account on [VdoTok](https://www.vdotok.com/). After creating an account, [login](https://console.vdotok.com/) to get the <b>Project ID</b> and <b>API Service URL</b>
* From file explorer, double-click on <b>vdotok-chat -> app -> src -> main -> java -> com -> vdotok -> chat -> utils -> ApplicationConstants</b> replace <b>SDK_PROJECT_ID</b> with your own <b>Project Id</b> and <b>BASE_URL</b> with your own <b>API Service URL</b> 
 
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
