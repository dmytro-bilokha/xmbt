# XMBT (XMPP Bot)

The main goal of the application is to provide set of useful bots usable
via XMPP aka Jabber.

The application consists of:
 * `xmbt-main` - the main module containing logic for bots initialization and managing XMPP connection.

 * `xmbt-app` - the module which represents whole application with all the modules builtin. 

 * `xmbt-bot-echo` - simple echo bot. Just echoes every request.

 * `xmbt-bot-null` - null bot. Never responds to the message, like /dev/null.

 * `xmbt-bot-sysinfo` - a bot provides basic system information.
 
 * `xmbt-bot-wg` - web gateway bot. Allows to send and get messages via web page.

 * `xmbt-bot-ns` - a bot which plans train trips with the [Netherlands railroad](https://ns.nl).

 * `xmbt-bot-weather` - a bot provides weather report for a given city in the Netherlands.
 
 * `xmbt` - a script to use XMBT as a FreeBSD daemon.

## TODO: more info on commands, each bot, deployment, environment configuration, etc.

## TODO: describe properties file

## Requirements

 * JDK 11.

 * MySQL 8.0 or later. Using other SQL database is possible, but require changes.

## Attributions

The application uses following 3rd party content and services:

 * [Buitenradar API](https://www.buienradar.nl/) to fetch rain forecast.

 * [World Cities DB](https://simplemaps.com/data/world-cities) to find GPS coordinates of the Dutch cities.
 
 * [Weerlive KNMI API](http://weerlive.nl/delen.php) to fetch weather report and forecast
