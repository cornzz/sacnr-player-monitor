# Sacnr player monitor

Java app that monitors a GTA:SA MP server and notifies the user when a specified target player is online.
With default settings the app monitors [San Andreas Cops And Robbers](https://sacnr.com).


## Usage

[Download](https://github.com/cornzz/sacnr-player-monitor/releases/latest) and run the .jar application and follow the prompts:

```
Monitor different server? [y/n]
```
If `Yes` is chosen, the user can enter the address of a GTA:SA MP server.

If `No` is chosen, the app will default to `server.sacnr.com:7777`.

```
Check for SACNR admins? [y/n]
```
(Prompted only if previous selection was `No`)

If `Yes` is chosen, the app will scrape all current SACNR staff from [sacnr.com](https://sacnr.com) and add them to target list.

```
Add targets (separated by comma)
```
Now the user can add players that are to be monitored like so: "Alpha, Delta, Gamma" (Case sensitive). 
This Step can be skipped, if the user previously decided to add all SACNR staff to the target list.


The app checks online players every 60 seconds and, if a new target player is online, plays a notification sound.
The user is only notified once per target player joining.
