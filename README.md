# Sacnr player monitor

Java app that monitors a GTA:SA MP server and notifies the user when a specified user is online.
With default settings the app monitors [sacnr.com](https://sacnr.com).


## Usage

Run the application and follow the prompts:

```
Monitor different server? [y/n]
```
If `y` is chosen, the user can enter the address of a GTA:SA MP server.

If `n` is chosen, the app will default to `server.sacnr.com:7777`.

```
Check for admins? [y/n]
```
(Prompted only if previous selection was `n`)

If `y` is chosen, the app will scrape all current SACNR staff from [sacnr.com](https://sacnr.com) and add them to target list.

```
Add targets (separated by comma), or press enter
```
Now the user can specify, which users to monitor like so: "Alpha, Delta, Gamma". This Step can be skipped by simply pressing enter, 
if the user previously decided to add all SACNR staff to the target list.


The app checks online players every 30 seconds and, if a target player is online, plays a notification sound.