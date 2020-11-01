# ENFLogAnalysis

Analysis of the Exposure Notification Framework logs for COVID-19 apps. 

Many people are wondering about the "green" risk contacts in the German COVID-19 tracing app. Unfortunately the app does not display the day when the contact occured. We try to derive the day from two facts:

* Each risk contact is removed automatically after 14 days. So if the number of contacts decreases from one day to the next, then we'll know that a contact had occured 14 days ago.
* If the risk counter increases from one day to another, then we'll make an "educated guess" (with a probability distribution) when a contact could have happened (typically it takes some days until a test result is provided etc.)

Please use this app at your own risk. The results are not reliable due to several privacy restrictions in the ENF and the setup of the COVID-19 tracing apps. Please check out an alternative (better) app
https://play.google.com/store/apps/details?id=org.tosl.warnappcompanion that required a "rooted" phone but delivers better results.

