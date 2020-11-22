# ENFLogAnalysis

Analysis of the Exposure Notification Framework logs for COVID-19 apps. 

## Introduction

Many people are wondering about the "green" risk contacts in the German COVID-19 tracing app. Unfortunately the app does not display the day when the contact occured. We try to derive the day from two facts:

* Each risk contact is removed automatically after 14 days. So if the number of contacts decreases from one day to the next, then we'll know that a contact had occured 14 days ago.
* If the risk counter increases from one day to another, then we'll make an "educated guess" (with a probability distribution) when a contact could have happened (typically it takes some days until a test result is provided etc.)

Please use this app at your own risk. The results are not reliable due to several privacy restrictions in the ENF and the setup of the COVID-19 tracing apps. Please check out an alternative (better) app
https://play.google.com/store/apps/details?id=org.tosl.warnappcompanion that required a "rooted" phone but delivers better results.

Check out the discussions:
* https://github.com/corona-warn-app/cwa-backlog/issues/23
* https://felixlen.github.io/ena_log/

**This app has been denied by the Google Play policy. So, please install it manually to your smartphone.**

## How to use

Here a short tutorial how to use the app.

1. Every morning(!) open your Corona Warn App and ensure that it's updated with the latest exposure keys:

    ![CoronaWarnApp](/app/src/main/res/drawable/stepbystep1.png)

2. Then open this app, select "ENF Logfile öffnen" from the menu to open the Google Play settings for the Exposure Notification Framework:

    ![OpenSettings](/app/src/main/res/drawable/stepbystep2.png)

3. Select the "Überprüfungen auf mögliche Begegnungen" which opens the ENF logfile:

    ![OpenLogfile](/app/src/main/res/drawable/stepbystep3.png)

4. And then select from the menu "Überprüfungen exportieren" and choose the ENFLogAnalysis app.

    ![ExportLogfile](/app/src/main/res/drawable/stepbystep4.png)

5. Now all data will be imported and displayed. 