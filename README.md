# DeluxePanel - A panel used for mods and admins
The purpose of this application is to help administrators on the community servers handle player reports. What that means is: a random player on the server have the opportunity to write a special command (i.e: !report <\suspect> <\reason>), to report a suspect. The community have loads of players connected daily in a ratio of administrators. Therefore, I thought this would be a suiting project to tackle. Whenever a suspect is reported, he/she is inserted into a database. I have an ajax script running on a server I bought for $1 somewhere in USA, which quries for new entries in the database. If there is a new entry, the administrators that have the application will be notified with an notification. 

The soul purpose of the application is to display and manage reported players. This is done by querying the database for new entries (with PHP, and print the output in a json format, so the application can parse the information), and display a "report-card" (see below). An administrator then have the option to claim the report and either go onto the server and handle the report for there or execute a command with the application.

<br>

&nbsp; &nbsp; &nbsp; &nbsp;![alt tag](https://riat.io/img/portfolio/dp-landing.png) &nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp; &nbsp;
![alt tag](https://riat.io/img/portfolio/dp-claim.png)
